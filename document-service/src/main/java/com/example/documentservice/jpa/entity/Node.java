package com.example.documentservice.jpa.entity;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "node")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Node {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(length = 36)
    private String id;

    /**
     * CHUONG / MUC / DIEU
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 10)
    private NodeType type;

    @Column(name = "chi_muc", length = 50)
    private String chiMuc;

    /**
     * Tên chương / mục / điều
     */
    @Column(name = "ten", columnDefinition = "TEXT", nullable = false)
    private String ten;

    /**
     * Mã pháp căn (mapc)
     */
    @Column(name = "mapc", length = 255)
    private String mapc;

    /**
     * Node thuộc chủ đề nào
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chu_de_id")
    private ChuDe chuDe;

    /**
     * Node thuộc đề mục nào (có thể null với CHUONG)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "de_muc_id")
    private DeMuc deMuc;

    public enum NodeType{
        CHUONG,
        MUC,
        DIEU
    }
}
