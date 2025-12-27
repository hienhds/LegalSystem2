package com.example.documentservice.jpa.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.util.List;

@Entity
@Table(name = "chu_de")
@Getter@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChuDe {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(length = 36)
    private String id; // UUID string

    @Column(name = "ten", nullable = false)
    private String ten;

    @Column(name = "stt", nullable = false)
    private Integer stt;

    @OneToMany(mappedBy = "chuDe", fetch = FetchType.LAZY)
    private List<DeMuc> deMucs;

    @OneToMany(mappedBy = "chuDe", fetch = FetchType.LAZY)
    private List<Node> nodes;
}
