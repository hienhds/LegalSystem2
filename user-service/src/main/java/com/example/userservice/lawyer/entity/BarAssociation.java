package com.example.userservice.lawyer.entity;


import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "bar_associations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BarAssociation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bar_association_id")
    private Long barAssociationId;

    @Column(nullable = false, unique = true, name = "association_name")
    private String associationName;

    @Column(name = "address")
    private String address;

    @Column(name = "contact_email", unique = true, nullable = false)
    private String contactEmail;

    @Column(name = "phone_number", unique = true, nullable = false)
    private String phoneNumber;

    @OneToMany(mappedBy = "barAssociation", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Lawyer> lawyers = new ArrayList<>();
}
