package shop.wannab.userservice.auth.domain;

import java.util.Collection;
import java.util.List;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import shop.wannab.userservice.user.domain.entity.State;
import shop.wannab.userservice.user.domain.entity.User;

public class CustomUserDetails implements UserDetails {

    @Getter
    private final Long id;
    private final String username;
    private final String password;
    private final List<GrantedAuthority> authorities;
    private final State state;

    public CustomUserDetails(User user) {
        this.id = user.getUserId();
        this.username = user.getUsername();
        this.password = user.getPassword();
        this.state = user.getState();
        this.authorities = List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole()));
    }

    @Override
    public boolean isEnabled() {
        return this.state == State.ACTIVATE;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }
}
