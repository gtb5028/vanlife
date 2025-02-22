package org.beerbower.vanlife.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
@Entity
@Table(name = "usr")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "usr_id_seq")
    @SequenceGenerator(name = "usr_id_seq", sequenceName = "public.usr_id_seq", allocationSize = 1)
    private Long id;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private String email;
    private String picture;
    @Column(nullable = false)
    @JsonIgnore
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

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id) && Objects.equals(name, user.name) && Objects.equals(email, user.email) && Objects.equals(picture, user.picture) && Objects.equals(password, user.password) && Objects.equals(roles, user.roles) && Objects.equals(active, user.active);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, email, picture, password, roles, active);
    }
}
