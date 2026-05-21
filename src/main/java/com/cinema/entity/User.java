package com.cinema.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, length = 200)
    private String password;

    @Column(length = 20)
    private String phone;

    @Column(length = 100)
    private String email;

    @Column(length = 200)
    private String avatar;

    @Column(length = 10)
    private String gender;

    @Column(length = 20)
    private String birthday;

    @Column(name = "real_name", length = 50)
    private String realName;

    @Column(name = "id_card", length = 20)
    private String idCard;

    @Column(name = "member_level")
    private Integer memberLevel = 0;

    private Integer points = 0;

    @Column(name = "total_spent")
    private Double totalSpent = 0.0;

    @Column(length = 20)
    private String role = "user";

    @Column(length = 50)
    private String createdAt;
}
