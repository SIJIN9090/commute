package com.example.commute.entity;

import com.example.commute.enums.RoleType;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "MEMBER")
public class Member implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MEMBER_ID")
    private Long id;

    @Column(name = "MEMBER_NAME", length = 10, nullable = false)
    private String username;

    @Column(name = "MEMBER_EMAIL", length = 50, nullable = false, unique = true)
    private String email;

    @Column(name = "MEMBER_PASSWORD", length = 20, nullable = false)
    private String password;

    @Column(name = "MEMBER_PHONE", length = 20, nullable = false, unique = true)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(name = "MEMBER_ROLE", nullable = false)
    private RoleType role;

    @Column(name = "MEMBER_TEAM", length = 50)
    private String team;

    @Column(name = "MEMBER_JOIN_DATE", columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime joinDate;

    @Column(name = "MEMBER_DELETE_DATE")
    private LocalDateTime deleteDate;

    @Column(name = "MEMBER_VACATION", columnDefinition = "INT DEFAULT 12")
    private Integer vacation = 12;

    @Column(name = "MEMBER_WORK_STATUS", length = 100)
    private String workStatus;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public boolean isAdmin() {
        return RoleType.ADMIN.equals(this.role);
    }
}
