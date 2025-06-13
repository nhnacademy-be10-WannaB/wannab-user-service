package shop.wannab.userservice.user.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;
    @Column(name = "user_password")
    private String password;
    @Column(name = "user_username")
    private String username;
    @Column(name = "user_name")
    private String name;
    @Column(name = "user_email")
    private String email;
    @Column(name = "nickname")
    private String nickname;
    @Column(name = "user_phone")
    private String phone;
    @Column(name = "user_birth")
    private LocalDate birth;
    @Column(name = "user_create_at")
    private LocalDate creationAt;
    @Column(name = "user_last_login_at")
    private LocalDate lastLoginAt;
    @Column(name = "points")
    private int points;
    @Column(name = "user_role")
    private Role role;
    @Column(name = "user_state")
    private State state;
    @Column(name = "provider_id")
    private String providerId;
    @Column(name = "provider_name")
    private String providerName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grade_id")
    private UserGrade userGrade;

    public User(String password,
                String username,
                String name,
                String email,
                String nickname,
                String phone,
                LocalDate birth,
                LocalDate creationAt,
                int points,
                Role role,
                State state,
                String providerId,
                String providerName){
        this.password = password;
        this.username = username;
        this.name = name;
        this.email = email;
        this.nickname = nickname;
        this.phone = phone;
        this.birth = birth;
        this.creationAt = creationAt;
        this.points = points;
        this.role = role;
        this.state = state;
        this.providerId = providerId;
        this.providerName = providerName;
    }
}
