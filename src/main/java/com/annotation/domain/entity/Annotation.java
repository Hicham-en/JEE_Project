package com.annotation.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "annotation")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Annotation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "chosen_class", nullable = false, length = 100)
    private String chosenClass;

    @Column(name = "annotation_time", nullable = false)
    private LocalDateTime annotationTime;

    @Column(nullable = false, length = 50)
    private String source;

    @Column(name = "duration_seconds")
    private Long durationSeconds;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "text_pair_id", nullable = false)
    private TextPair textPair;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "annotator_id", nullable = false)
    private Annotator annotator;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Annotation annot)) return false;
        return id != null && id.equals(annot.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
