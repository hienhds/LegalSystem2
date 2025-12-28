package com.example.documentservice.jpa.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import java.util.List;
@Entity
@Table(name = "de_muc")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeMuc {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(length = 36)
    private String id; // UUID

    @Column(name = "ten", nullable = false, length = 500)
    private String ten;

    @Column(name = "stt", nullable = false)
    private Integer stt;

    /**
     * Quan hệ nhiều đề mục thuộc 1 chủ đề
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chu_de_id", nullable = false)
    private ChuDe chuDe;

    @OneToMany(mappedBy = "deMuc", fetch = FetchType.LAZY)
    private List<Node> nodes;

}