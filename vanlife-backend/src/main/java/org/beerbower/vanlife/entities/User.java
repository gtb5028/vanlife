package org.beerbower.vanlife.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "usr")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private String email;
    private String picture;
    @Column(nullable = false)
    private String password;
    @Column(nullable = false)
    private String roles;
    @Column(nullable = false)
    private Boolean active;

    public User() {
    }

    public User(Long id, String name, String email, String picture, String password, String roles, Boolean active) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.picture = picture;
        this.password = password;
        this.roles = roles;
        this.active = active;
    }
}
