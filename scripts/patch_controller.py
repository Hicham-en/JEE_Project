import re

with open("src/main/java/com/annotation/presentation/controller/annotator/AnnotatorWorkspaceController.java", "r") as f:
    content = f.read()

replacement = """        List<TextPair> textPairs = task.getTextPairs();
        model.addAttribute("task", task);
        model.addAttribute("dataset", dataset);
        model.addAttribute("textPairs", textPairs);

        List<com.annotation.domain.entity.Annotation> existingAnnotations = annotationRepository.findByTaskDatasetId(dataset.getId());
        java.util.Map<Long, String> userAnnotations = existingAnnotations.stream()
                .filter(a -> a.getAnnotator().getId().equals(annotatorId))
                .collect(java.util.stream.Collectors.toMap(
                        a -> a.getTextPair().getId(), 
                        com.annotation.domain.entity.Annotation::getChosenClass,
                        (existing, replacementVal) -> existing
                ));
        model.addAttribute("userAnnotations", userAnnotations);
"""

content = re.sub(r"""        List<TextPair> textPairs = task.getTextPairs\(\);.*?model\.addAttribute\("existingAnnotations", existingAnnotations\);""", replacement, content, flags=re.DOTALL)

with open("src/main/java/com/annotation/presentation/controller/annotator/AnnotatorWorkspaceController.java", "w") as f:
    f.write(content)

