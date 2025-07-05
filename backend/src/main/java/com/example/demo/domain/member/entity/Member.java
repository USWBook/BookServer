package com.example.demo.domain.member.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Table(name ="members")
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name ="member_id")
    private UUID id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String studentId;


    /**
     * 전공과 학교는 어떤 방식으로 저장할지 다시 고민해야할듯함
     * 추후 확장한다면 학교는 정수 색인이랑 매칭시켜서 따로 테이블로 관리하고 해당 인덱스값을
     * 외래 키로 받아서 전공들 테이블 만들면 되려나 싶음
     */
    @Column(nullable = false)
    private String school;

    @Column(nullable = false)
    private String major;
}
