package com.annotation.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "nlp_run")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NLPRun {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 50)
    private NLPRunType type;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 50)
    private NLPRunStatus status;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "duration_seconds")
    private Long durationSeconds;

    @Column(name = "script_path", length = 255)
    private String scriptPath;

    @Column(columnDefinition = "JSON")
    private String params;

    @Column(columnDefinition = "TEXT")
    private String logs;

    private Double accuracy;

    @Column(name = "f1_score")
    private Double f1Score;

    @Column(name = "confusion_matrix", columnDefinition = "JSON")
    private String confusionMatrix;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dataset_id", nullable = false)
    private Dataset dataset;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NLPRun process)) return false;
        return id != null && id.equals(process.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
