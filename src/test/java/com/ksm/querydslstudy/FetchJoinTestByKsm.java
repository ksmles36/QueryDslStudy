package com.ksm.querydslstudy;

import com.ksm.querydslstudy.entity.Member;
import com.ksm.querydslstudy.entity.Team;
import com.ksm.querydslstudy.jpaRepository.MemberJpaRepository;
import com.ksm.querydslstudy.jpaRepository.TeamJpaRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static com.ksm.querydslstudy.entity.QMember.member;
import static com.ksm.querydslstudy.entity.QTeam.team;

@SpringBootTest
@Transactional
public class FetchJoinTestByKsm {
    //직접 fetchJoin 테스트 하며 확인해보자!

    @Autowired
    EntityManager em;

    JPAQueryFactory queryFactory;

    @Autowired
    MemberJpaRepository memberJpaRepository;
    @Autowired
    TeamJpaRepository teamJpaRepository;

    @BeforeEach
    public void before() {
        queryFactory = new JPAQueryFactory(em);

        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        Team teamC = new Team("teamC");
        Team teamD = new Team("teamD");
        Team teamE = new Team("teamE");

        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamA);
        Member member3 = new Member("member3", 30, teamA);
        Member member4 = new Member("member4", 40, teamB);
        Member member5 = new Member("member5", 40, teamB);
        Member member6 = new Member("member6", 40, teamB);
        Member member7 = new Member("member7", 40, teamB);
        Member member8 = new Member("member8", 40, teamB);
        Member member9 = new Member("member9", 40, teamC);
        Member member10 = new Member("member10", 40, teamD);
        Member member11 = new Member("member11", 40, teamE);

        em.persist(teamA);
        em.persist(teamB);
        em.persist(teamC);
        em.persist(teamD);
        em.persist(teamE);
        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);
        em.persist(member5);
        em.persist(member6);
        em.persist(member7);
        em.persist(member8);
        em.persist(member9);
        em.persist(member10);
        em.persist(member11);

        em.flush();
        em.clear();
    }


    //직접 fetchJoin 테스트 하며 확인해보자!
    @Test
    public void fetchJoinByKsm() {

//        List<Team> teamList = teamJpaRepository.findAllByKsm();

//        List<Team> teamList = queryFactory
//                .selectFrom(team)
//                .fetch();

        List<Team> teamList = queryFactory
                .selectFrom(team)
                .join(team.members, member).fetchJoin()
//                .join(member).on(member.team.id.eq(team.id)).fetchJoin()
                .fetch();

        for (Team team1 : teamList) {
            team1.getMembers().forEach((e) -> System.out.println("여기! = " + e.getUsername()));
        }
    }


}
