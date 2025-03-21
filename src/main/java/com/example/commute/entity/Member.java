package com.example.commute.entity;

import com.example.commute.enums.RoleType;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "MEMBER")
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MEMBER_ID")
    private Long id;

    @Column(name = "MEMBER_NAME", length = 10, nullable = false)
    private String username;

    @Column(name = "MEMBER_PASSWORD", length = 225, nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "MEMBER_ROLE", nullable = false)
    private RoleType role;


    public boolean isAdmin() {
        return RoleType.ADMIN.equals(this.role); // RoleType.ADMIN을 확인
    }

    // getExpenses 메서드 추가
    // 여러 Expense를 가질 수 있는 관계 설정
    @Getter
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<Expense> expenses;

}
