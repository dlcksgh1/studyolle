package com.studyolle.modules.account;


import com.studyolle.modules.study.Study;
import com.studyolle.modules.tag.Tag;
import com.studyolle.modules.zone.Zone;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Getter @Setter @EqualsAndHashCode(of = "id")
@Builder @AllArgsConstructor @NoArgsConstructor
public class Account {

    @Id @GeneratedValue
    private Long id;

    @Column(unique = true)
    private String email;

    @Column(unique = true)
    private String nickname;

    private String password;
    private boolean emailVerified;
    private String emailCheckToken;
    private LocalDateTime joinedAt;
    private String bio;
    private String url;
    private String occupation;
    private String location;

    @Lob @Basic(fetch = FetchType.EAGER)
    private String profileImage;

    private boolean studyCreatedByEmail;
    @Builder.Default
    private boolean studyCreatedByWeb = true;
    private boolean studyEnrollmentResultByEmail;
    @Builder.Default
    private boolean studyEnrollmentResultByWeb = true;
    private boolean studyUpdatedByEmail;
    @Builder.Default
    private boolean studyUpdatedByWeb  = true;

    private LocalDateTime emailCheckTokenGeneratedAt;

    @Builder.Default
    @ManyToMany
    private Set<Tag> tags = new HashSet<>();

    @Builder.Default
    @ManyToMany
    private Set<Zone> zones = new HashSet<>();

    public void generateToken() {
        this.emailCheckToken = UUID.randomUUID().toString();
        this.emailCheckTokenGeneratedAt = LocalDateTime.now();
    }

    public void completeSignup() {
        this.emailVerified = true;
        this.joinedAt = LocalDateTime.now();
    }

    public boolean isValidToken(String token) {
        return this.emailCheckToken.equals(token);
    }

    public boolean enableToSendEmail() {
        return this.emailCheckTokenGeneratedAt.isBefore(LocalDateTime.now().minusMinutes(5));
    }

}
