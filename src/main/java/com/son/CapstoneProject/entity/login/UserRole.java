package com.son.CapstoneProject.entity.login;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "User_Role",
        uniqueConstraints = {
                @UniqueConstraint(name = "USER_ROLE_UK", columnNames = {"User_Id", "Role_Id"})
        }
)
@Getter
@Setter
public class UserRole {

    @Id
    @GeneratedValue
    @Column(name = "Id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "User_Id", nullable = false)
    private AppUser appUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "Role_Id", nullable = false)
    private AppRole appRole;

}