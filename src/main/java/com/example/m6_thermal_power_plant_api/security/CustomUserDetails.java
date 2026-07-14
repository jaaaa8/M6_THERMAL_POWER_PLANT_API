package com.example.m6_thermal_power_plant_api.security;

import com.example.m6_thermal_power_plant_api.entity.Account;
import com.example.m6_thermal_power_plant_api.entity.enums.AccountStatus;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class CustomUserDetails implements UserDetails {
    private final Integer accountId;
    private final String username;
    private final String password;
    private final boolean enabled;
    private final Collection<? extends GrantedAuthority> authorities;

    public CustomUserDetails(Integer accountId, String username, String password,
                             boolean enabled, Collection<? extends GrantedAuthority> authorities) {
        this.accountId = accountId;
        this.username = username;
        this.password = password;
        this.enabled = enabled;
        this.authorities = authorities;
    }

    // Build từ Account entity (UserDetailsService dùng — có password để Spring verify)
    public static CustomUserDetails from(Account account) {
        List<GrantedAuthority> authorities = account.getRoles().stream()
                .map(r -> (GrantedAuthority) new SimpleGrantedAuthority("ROLE_" + r.getName()))
                .collect(Collectors.toList());
        return new CustomUserDetails(
                account.getId(),
                account.getUsername(),
                account.getPasswordHash(),
                account.getStatus() == AccountStatus.ACTIVE,
                authorities
        );
    }

    // Build từ JWT claims (filter dùng — không cần password, không cần query DB).
    // authorities gồm 2 loại: "ROLE_xxx" và permission code thô.
    public static CustomUserDetails fromClaims(Integer accountId, String username,
                                                List<String> roles, List<String> permissions) {
        List<GrantedAuthority> authorities = new java.util.ArrayList<>();
        roles.forEach(r -> authorities.add(new SimpleGrantedAuthority("ROLE_" + r)));
        permissions.forEach(p -> authorities.add(new SimpleGrantedAuthority(p)));
        return new CustomUserDetails(accountId, username, null, true, authorities);
    }

    @Override public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }
    @Override public String getPassword() { return password; }
    @Override public String getUsername() { return username; }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return enabled; }
}
