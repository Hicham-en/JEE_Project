package com.annotation.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "text_pair")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TextPair {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String text1;

    @Column(columnDefinition = "TEXT")
    private String text2;

    @Column(columnDefinition = "JSON")
    private String metadata;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dataset_id", nullable = false)
    private Dataset dataset;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TextPair pair)) return false;
        return id != null && id.equals(pair.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
