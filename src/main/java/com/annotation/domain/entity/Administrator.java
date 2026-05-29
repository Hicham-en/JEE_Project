package com.annotation.domain.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "administrators")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class Administrator extends User {
}
