package com.ksm.querydslstudy;

import com.ksm.querydslstudy.dto.MemberDto;
import com.ksm.querydslstudy.dto.UserDto;
import com.ksm.querydslstudy.entity.Member;
import com.ksm.querydslstudy.entity.QMember;
import com.ksm.querydslstudy.entity.Team;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static com.ksm.querydslstudy.entity.QMember.member;

@SpringBootTest
@Transactional
public class QueryDslMiddleTest {

    @Autowired
    EntityManager em;

    JPAQueryFactory queryFactory;

    @BeforeEach
    public void before() {
        queryFactory = new JPAQueryFactory(em);

        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        em.persist(teamA);
        em.persist(teamB);
        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamA);
        Member member3 = new Member("member3", 30, teamB);
        Member member4 = new Member("member4", 40, teamB);
        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);
    }

    //jpql 로 하면 불편한 점 설명 위해
    @Test
    public void findDtoByJPQL() {
        List<MemberDto> result = em.createQuery(
                        "select new com.ksm.querydslstudy.dto.MemberDto(m.username, m.age) " +
                                "from Member m", MemberDto.class)
                .getResultList();

        for (MemberDto memberDto : result) {
            System.out.println("memberDto = " + memberDto);
        }
    }

    //프로퍼티 접근 - Setter
    @Test
    public void findDtoBySetter() {
        List<MemberDto> result = queryFactory
                .select(Projections.bean(MemberDto.class, member.username, member.age))
                .from(member)
                .fetch();

        for (MemberDto memberDto : result) {
            System.out.println("memberDto = " + memberDto);
        }
    }

    //필드 직접 접근
    @Test
    public void findDtoByFields() {
        List<MemberDto> result = queryFactory
                .select(Projections.fields(MemberDto.class, member.username, member.age))
                .from(member)
                .fetch();

        for (MemberDto memberDto : result) {
            System.out.println("memberDto = " + memberDto);
        }
    }

    //필드 직접 접근2 - 별칭을 사용하는 경우 - dto 와의 이름이 맞지 않을 때 별칭 as 사용하기
    //서브쿼리에 별칭 사용하는 경우
    @Test
    public void findUserDtoByFields() {
        List<UserDto> result = queryFactory
                .select(Projections.fields(UserDto.class,
                        member.username, member.age))
                .from(member)
                .fetch();

        for (UserDto userDto : result) {
            System.out.println("userDto1 = " + userDto);
        }


        //dto 와의 이름이 맞지 않을 때 별칭 as 사용하기
        List<UserDto> result2 = queryFactory
                .select(Projections.fields(UserDto.class,
                        member.username.as("name"), member.age))
                .from(member)
                .fetch();

        for (UserDto userDto : result2) {
            System.out.println("userDto2 = " + userDto);
        }


        //서브쿼리에 별칭 사용하는 경우
        //서브쿼리의 경우에는 ExpressionUtils.as() 로 감싸줘야 실행 가능하다.
        QMember memberSub = new QMember("memberSub");
        List<UserDto> result3 = queryFactory
                .select(Projections.fields(UserDto.class,
                        member.username.as("name"),

                        ExpressionUtils.as(
                                JPAExpressions
                                        .select(memberSub.age.max())
                                        .from(memberSub)
                                , "age")))

                .from(member)
                .fetch();

        for (UserDto userDto : result3) {
            System.out.println("userDto3 = " + userDto);
        }
    }

    //생성자 사용
    @Test
    public void findDtoByConstructor() {
        List<MemberDto> result = queryFactory
                .select(Projections.constructor(MemberDto.class, member.username, member.age))
                .from(member)
                .fetch();

        for (MemberDto memberDto : result) {
            System.out.println("memberDto = " + memberDto);
        }


        //필드 사용과 다르게, 생성자로 프로젝션 하는 경우에는 조회문의 이름 지정이 달라도
        // 타입과 순서에 맞으면 알아서 데이터 들어간다. 별칭 안해줘도
        List<UserDto> result2 = queryFactory
                .select(Projections.constructor(UserDto.class,
                        member.username,
                        member.age))
                .from(member)
                .fetch();

        for (UserDto userDto : result2) {
            System.out.println("userDto = " + userDto);
        }
    }










}