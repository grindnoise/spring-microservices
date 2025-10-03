package com.evilcorp.person_service.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

@Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
@Entity
@SuperBuilder
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Table(name = "addresses", schema = "person")
public class Address extends BaseEntity {

    @ManyToOne(targetEntity = Country.class,
            fetch = FetchType.LAZY,
            optional = false)
    @JoinColumn(name = "country_id", nullable = false)
    private Country country;

    @Size(max = 128)
    @Column(name = "address", nullable = false, length = 128)
    private String address;

    @Size(max = 32)
    @Column(name = "zip_code", nullable = false, length = 32)
    private String zipCode;

    @Size(max = 64)
    @Column(name = "city", nullable = false, length = 64)
    private String city;
}
