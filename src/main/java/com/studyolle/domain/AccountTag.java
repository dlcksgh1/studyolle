package com.studyolle.domain;

import javax.persistence.*;

import static javax.persistence.FetchType.LAZY;

@Entity
public class AccountTag {

    @Id @GeneratedValue
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "ACCOUNT_ID")
    private Account account;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "TAG_ID")
    private Tag tag;
}
