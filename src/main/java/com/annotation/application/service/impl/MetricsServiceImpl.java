package com.annotation.application.service.impl;

import com.annotation.application.dto.MetricsDTO;
import com.annotation.application.dto.SpammerDTO;
import com.annotation.application.service.IMetricsService;
import com.annotation.domain.entity.Annotation;
import com.annotation.domain.entity.Annotator;
import com.annotation.domain.repository.AnnotationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class MetricsServiceImpl implements IMetricsService {

    private final AnnotationRepository annotationRepository;

    public MetricsServiceImpl(AnnotationRepository annotationRepository) {
        this.annotationRepository = annotationRepository;
    }

    @Override
    public double computeCohenKappa(Long datasetId, Long annotatorId1, Long annotatorId2) {
        List<Annotation> allAnnotations = annotationRepository.findByTaskDatasetId(datasetId);

        List<Annotation> annotations1 = allAnnotations.stream()
                .filter(a -> a.getAnnotator() != null && annotatorId1.equals(a.getAnnotator().getId()))
                .toList();
        List<Annotation> annotations2 = allAnnotations.stream()
                .filter(a -> a.getAnnotator() != null && annotatorId2.equals(a.getAnnotator().getId()))
                .toList();

        if (annotations1.isEmpty() || annotations2.isEmpty()) return 0.0;

        Map<Long, String> map1 = annotations1.stream()
                .filter(a -> a.getTextPair() != null)
                .collect(Collectors.toMap(a -> a.getTextPair().getId(), Annotation::getChosenClass, (v1, v2) -> v1));
        Map<Long, String> map2 = annotations2.stream()
                .filter(a -> a.getTextPair() != null)
                .collect(Collectors.toMap(a -> a.getTextPair().getId(), Annotation::getChosenClass, (v1, v2) -> v1));

        Set<Long> commonItems = new HashSet<>(map1.keySet());
        commonItems.retainAll(map2.keySet());

        if (commonItems.isEmpty()) return 0.0;

        int total = commonItems.size();
        int observedAgreement = 0;
        Map<String, Integer> count1 = new HashMap<>();
        Map<String, Integer> count2 = new HashMap<>();

        for (Long itemId : commonItems) {
            String c1 = map1.get(itemId);
            String c2 = map2.get(itemId);
            if (c1.equals(c2)) observedAgreement++;
            count1.put(c1, count1.getOrDefault(c1, 0) + 1);
            count2.put(c2, count2.getOrDefault(c2, 0) + 1);
        }

        double po = (double) observedAgreement / total;
        double pe = 0.0;
        Set<String> allClasses = new HashSet<>(count1.keySet());
        allClasses.addAll(count2.keySet());

        for (String c : allClasses) {
            double p1 = (double) count1.getOrDefault(c, 0) / total;
            double p2 = (double) count2.getOrDefault(c, 0) / total;
            pe += p1 * p2;
        }

        if (Math.abs(pe - 1.0) < 1e-10) return 1.0;
        return (po - pe) / (1.0 - pe);
    }

    @Override
    public MetricsDTO computeFleissKappa(Long datasetId) {
        List<Annotation> annotations = annotationRepository.findByTaskDatasetId(datasetId);

        if (annotations.isEmpty()) {
            return new MetricsDTO(datasetId, 0.0, 0, 0, "Aucune annotation");
        }

        Map<Long, Map<String, Integer>> nij = new HashMap<>();
        Set<String> allClasses = new HashSet<>();
        Set<Long> uniqueAnnotators = new HashSet<>();

        for (Annotation ann : annotations) {
            if (ann.getTextPair() == null || ann.getAnnotator() == null) continue;
            Long itemId = ann.getTextPair().getId();
            String chosenClass = ann.getChosenClass();
            allClasses.add(chosenClass);
            uniqueAnnotators.add(ann.getAnnotator().getId());

            nij.putIfAbsent(itemId, new HashMap<>());
            Map<String, Integer> classCounts = nij.get(itemId);
            classCounts.put(chosenClass, classCounts.getOrDefault(chosenClass, 0) + 1);
        }

        int N = nij.size();
        if (N == 0) {
            return new MetricsDTO(datasetId, 0.0, uniqueAnnotators.size(), 0, "Pas d'items annotés");
        }

        long totalAssignments = 0;
        double sumPi = 0.0;
        int eligibleItems = 0;
        Map<String, Integer> sumClassAssignments = new HashMap<>();

        for (String c : allClasses) {
            sumClassAssignments.put(c, 0);
        }

        for (Map.Entry<Long, Map<String, Integer>> entry : nij.entrySet()) {
            Map<String, Integer> itemClasses = entry.getValue();
            int nForItem = itemClasses.values().stream().mapToInt(Integer::intValue).sum();

            // Fleiss Kappa measures inter-annotator agreement and requires at least 2 ratings per item.
            if (nForItem < 2) {
                continue;
            }

            eligibleItems++;
            totalAssignments += nForItem;

            for (Map.Entry<String, Integer> classEntry : itemClasses.entrySet()) {
                sumClassAssignments.put(classEntry.getKey(), sumClassAssignments.get(classEntry.getKey()) + classEntry.getValue());
            }

            double sumOfSquares = itemClasses.values().stream().mapToDouble(val -> val * val).sum();
            double pi = (sumOfSquares - nForItem) / ((double) nForItem * (nForItem - 1));
            sumPi += pi;
        }

        if (eligibleItems == 0 || totalAssignments == 0) {
            return new MetricsDTO(datasetId, 0.0, uniqueAnnotators.size(), N,
                    "Données insuffisantes (au moins 2 annotations par texte requises)");
        }

        double pBar = sumPi / eligibleItems;
        double pe = 0.0;

        for (String c : allClasses) {
            double pj = (double) sumClassAssignments.get(c) / totalAssignments;
            pe += pj * pj;
        }

        double kappa;
        if (Math.abs(pe - 1.0) < 1e-10) {
            kappa = 1.0;
        } else {
            kappa = (pBar - pe) / (1.0 - pe);
        }

        String interpretation;
        if (kappa < 0) interpretation = "Désaccord";
        else if (kappa <= 0.20) interpretation = "Accord léger";
        else if (kappa <= 0.40) interpretation = "Accord passable";
        else if (kappa <= 0.60) interpretation = "Accord modéré";
        else if (kappa <= 0.80) interpretation = "Accord fort";
        else interpretation = "Accord presque parfait";

        return new MetricsDTO(datasetId, kappa, uniqueAnnotators.size(), N, interpretation);
    }

    /**
     * Détecte les annotateurs suspects (spammers).
     * <br>
     * Pondérations ajustables du suspicionScore :
     * - Entropie (35%) : Score croissant quand l'entropie de Shannon diminue (score = 1 - H).
     * - Temps moyen (20%) : Score maximal si le temps de réponse est très court (score = max(0, 1 - avgTime/5.0s)).
     * - Cohen Kappa (45%) : Score croissant quand l'accord inter-annotateur (Cohen's Kappa moyen) est bas (score = (1 - kappa) / 2).
     */
    @Override
    @Transactional(readOnly = true)
    public List<SpammerDTO> detectSpammers(Long datasetId) {
        List<Annotation> annotations = annotationRepository.findByTaskDatasetId(datasetId);
        if (annotations.isEmpty()) return Collections.emptyList();

        Set<String> allClasses = annotations.stream().map(Annotation::getChosenClass).collect(Collectors.toSet());
        double q = Math.max(2, allClasses.size()); // log2(q) where q >= 2
        double log2q = Math.log(q) / Math.log(2); // log2(q)

        Map<Annotator, List<Annotation>> byAnnotator = annotations.stream().collect(Collectors.groupingBy(Annotation::getAnnotator));
        List<SpammerDTO> spammers = new ArrayList<>();

        for (Map.Entry<Annotator, List<Annotation>> entry : byAnnotator.entrySet()) {
            Annotator annotator = entry.getKey();
            List<Annotation> userAnnotations = entry.getValue();

            // 1. Calculate Entropy
            Map<String, Long> classCounts = userAnnotations.stream().collect(Collectors.groupingBy(Annotation::getChosenClass, Collectors.counting()));
            double h = 0.0;
            long total = userAnnotations.size();
            for (Long count : classCounts.values()) {
                double p = (double) count / total;
                h -= p * (Math.log(p) / Math.log(2)); // p * log2(p)
            }
            double normalizedH = h / log2q;

            // 2. Average Time
            double avgTime = userAnnotations.stream().mapToLong(a -> a.getDurationSeconds() != null ? a.getDurationSeconds() : 5L).average().orElse(5.0);

            // 3. Average Cohen's Kappa against others
            double totalKappa = 0.0;
            int comparisons = 0;
            for (Annotator other : byAnnotator.keySet()) {
                if (!annotator.getId().equals(other.getId())) {
                    totalKappa += computeCohenKappa(datasetId, annotator.getId(), other.getId());
                    comparisons++;
                }
            }
            double meanKappa = comparisons > 0 ? (totalKappa / comparisons) : 1.0;

            // 4. Class dominance ratio (helps detect "always same class" even when entropy threshold is not crossed)
            long maxClassCount = classCounts.values().stream().mapToLong(Long::longValue).max().orElse(0L);
            double dominantClassRatio = total > 0 ? (double) maxClassCount / total : 0.0;

            // Computing individual dimension suspicion scores [0, 1]
            double scoreEntropy = Math.max(0.0, 1.0 - normalizedH);
            double scoreTime = Math.max(0.0, 1.0 - (avgTime / 5.0)); // Highly suspicious if < 5s average
            double scoreKappa = Math.max(0.0, (1.0 - meanKappa) / 2.0); // -1 -> 1.0, 1 -> 0.0

            double suspicionScore = (0.35 * scoreEntropy) + (0.20 * scoreTime) + (0.45 * scoreKappa);

            List<String> reasons = new ArrayList<>();
            if (normalizedH < 0.15) reasons.add(String.format("Entropie très faible (%.2f < 0.15) : tendance à toujours choisir la même classe.", normalizedH));
            if (avgTime < 3.0) reasons.add(String.format("Temps d'annotation très rapide (%.1fs/élément en moyenne).", avgTime));
            if (meanKappa < 0.2) reasons.add(String.format("Accord très faible avec les autres annotateurs (Cohen Kappa moyen = %.2f).", meanKappa));
            if (dominantClassRatio >= 0.90) reasons.add(String.format("Classe dominante excessive (%.0f%% des réponses sur une seule classe).", dominantClassRatio * 100));

            boolean severeLowAgreement = total >= 8 && meanKappa <= 0.0;
            boolean extremeDominance = total >= 8 && dominantClassRatio >= 0.90;

            if (normalizedH < 0.15 || severeLowAgreement || extremeDominance || suspicionScore >= 0.50) {
                spammers.add(new SpammerDTO(
                        annotator.getId(),
                        annotator.getNom(),
                        annotator.getPrenom(),
                        suspicionScore * 100.0,
                        reasons
                ));
            }
        }
        
        spammers.sort((s1, s2) -> Double.compare(s2.suspicionScore(), s1.suspicionScore()));
        return spammers;
    }
}
