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
@Table(name = "individuals", schema = "person")
public class Individual extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false, cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Size(max = 64)
    @Column(name = "passport_number", nullable = false, unique = true, length = 64)
    private String passportNumber;

    @Size(max = 64)
    @Column(name = "phone_number", nullable = false, unique = true, length = 64)
    private String phoneNumber;
}
