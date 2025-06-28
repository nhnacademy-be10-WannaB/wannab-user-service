package shop.wannab.userservice.user.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Builder
@AllArgsConstructor
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "user_password")
    private String password;

    @Column(name = "user_username", unique = true)
    private String username;

    @Column(name = "user_name")
    private String name;

    @Column(name = "user_email")
    private String email;

    @Builder.Default
    @Column(name = "nickname")
    private String nickname = "unknown";

    @Column(name = "user_phone")
    private String phone;

    @Column(name = "user_birth")
    private LocalDate birth;

    @Builder.Default
    @Column(name = "user_create_at")
    private LocalDate creationAt = LocalDate.now();

    @Builder.Default
    @Column(name = "user_last_login_at")
    private LocalDate lastLoginAt = null;

    @Builder.Default
    @Column(name = "points")
    private int points = 0;

    @Builder.Default
    @Column(name = "user_role")
    @Enumerated(EnumType.STRING)
    private Role role = Role.USER;

    @Builder.Default
    @Column(name = "user_state")
    @Enumerated(EnumType.STRING)
    private State state = State.ACTIVATE;

    @Column(name = "provider_id")
    private String providerId;

    @Column(name = "provider_name")
    private String providerName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grade_id")
    private UserGrade userGrade;
}
