package com.example.sbp.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "privileges")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "name")
public class Privilege {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String name;

    public Privilege(String name) {
        this.name = name;
    }
}
