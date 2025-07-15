package shop.wannab.userservice.user.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
import jakarta.validation.constraints.NotNull;
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
    private String userLoginId;

    @Column(name = "user_name")
    private String name;

    @Column(name = "user_email")
    private String email;

    @NotNull
    @Column(name = "nickname")
    private String nickname;

    @Column(name = "user_phone")
    private String phone;

    @Column(name = "user_birth")
    private LocalDate birth;

    @NotNull
    @Column(name = "user_create_at")
    private LocalDate creationAt;

    @Column(name = "user_last_login_at")
    private LocalDate lastLoginAt;

    @NotNull
    @Column(name = "points")
    private Integer points;

    @NotNull
    @Column(name = "user_role")
    @Enumerated(EnumType.STRING)
    private Role role;

    @NotNull
    @Column(name = "user_state")
    @Enumerated(EnumType.STRING)
    private State state;

    @Column(name = "provider_id")
    private String providerId;

    @Column(name = "provider_name")
    private String providerName;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    @JoinColumn(name = "grade_id")
    private UserGrade userGrade;


    // 회원가입
    public User(String password, String userLoginId, String name, String email, String phone, LocalDate birth,
                String providerName, String providerId, UserGrade userGrade) {
        // 공용
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.birth = birth;

        // 일반 전용
        this.password = password;
        this.userLoginId = userLoginId;

        // 소셜 전용
        this.providerName = providerName;
        this.providerId = providerId;

        // 공용 설정
        this.nickname = "unknown";
        this.creationAt = LocalDate.now();
        this.lastLoginAt = null;
        this.points = 0;
        this.role = Role.USER;
        this.state = State.ACTIVATE;
        this.userGrade = userGrade;
    }

    // 일반 회원가입
    @Builder(builderMethodName = "standard")
    public User(String password, String userLoginId, String name, String email, String phone,
                LocalDate birth, UserGrade userGrade) {
        this(password, userLoginId, name, email, phone, birth, null, null, userGrade);
    }

    // 소셜 회원가입
    @Builder(builderMethodName = "social")
    public User(String providerId, String providerName, String name, String email, LocalDate birth, String phone,
                UserGrade userGrade) {
        this(null, null, name, email, phone, birth, providerName, providerId, userGrade);
    }
}
