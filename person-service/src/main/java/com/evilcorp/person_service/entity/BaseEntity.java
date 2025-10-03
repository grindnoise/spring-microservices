package com.evilcorp.person_service.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;
import java.util.UUID;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@MappedSuperclass
public class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @NotNull
    @ColumnDefault("true")
    @Column(name = "active", nullable = false)
    private Boolean active;

    @NotNull
    @ColumnDefault("(now) AT TIME ZONE 'utc'::text")
    @Column(name = "created", nullable = false)
    private Instant created;

    @NotNull
    @ColumnDefault("(now) AT TIME ZONE 'utc'::text")
    @Column(name = "updated", nullable = false)
    private Instant updated;
}
