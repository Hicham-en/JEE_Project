package com.annotation.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "possible_class")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PossibleClass {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String libelle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dataset_id", nullable = false)
    private Dataset dataset;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PossibleClass pc)) return false;
        return id != null && id.equals(pc.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
