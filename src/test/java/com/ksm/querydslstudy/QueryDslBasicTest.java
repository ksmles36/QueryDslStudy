package com.ksm.querydslstudy;

import com.ksm.querydslstudy.entity.Member;
import com.ksm.querydslstudy.entity.QMember;
import com.ksm.querydslstudy.entity.Team;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceUnit;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static com.ksm.querydslstudy.entity.QMember.member;
import static com.ksm.querydslstudy.entity.QTeam.team;
import static com.querydsl.jpa.JPAExpressions.select;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Transactional
public class QueryDslBasicTest {

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

    @Test
    public void startJPQL() {
        String qlString =
                "select m from Member m " +
                "where m.username = :username";

        Member findMember = em.createQuery(qlString, Member.class)
                .setParameter("username", "member1")
                .getSingleResult();

//        assertEquals(findMember.getUsername(), "member1");

        //내가 잘 몰라서 junit jupiter 로 테스트코드 검증 짰는데 실습 때
        //근데 찾아보니 AssertJ 를 사용하는게 더 나은 것 같다.
        //import 시에 jupiter 말고 assertJ 를 선택해서 import 받고 static import 처리 해주면 된다!
        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    public void startQuerydsl() {
        Member findMember = queryFactory
                .select(member)
                .from(member)
                .where(member.username.eq("member1"))
                .fetchOne();

        assertEquals(findMember.getUsername(), "member1");
    }

    @Test
    public void search() {
        Member fetchOne = queryFactory
                .selectFrom(member)
                .where(member.username.eq("member1")
                        .and(member.age.eq(10)))
                .fetchOne();

        assertEquals(fetchOne.getUsername(), "member1");
        assertEquals(fetchOne.getAge(), 10);
    }

    @Test
    public void searchAndParam() {
        Member fetchOne = queryFactory
                .selectFrom(member)
                .where(member.username.eq("member1"), member.age.eq(10))
                .fetchOne();

        assertEquals(fetchOne.getUsername(), "member1");
        assertEquals(fetchOne.getAge(), 10);
    }

    @Test
    public void resultFetch() {
        List<Member> fetch = queryFactory
                .selectFrom(member)
                .fetch();

        Member member1 = queryFactory
                .selectFrom(member)
                .orderBy(member.id.asc())
                .fetchFirst();

        //부트 3.x 쿼리dsl 5.0 이상 부터는 페이징정보 포함 메소드가 비권장으로 바뀜.
//        List<Member> fetch1 = queryFactory
//                .selectFrom(member)
//                .offset(pageable.getOffset())
//                .limit(pageable.getPageSize())
//                .fetch();

        Long l = queryFactory
                .select(member.count())
                .from(member)
                .fetchOne();

        System.out.println("fetch = " + fetch);
        System.out.println("member1 = " + member1);
//        System.out.println("fetch1 = " + fetch1);
        System.out.println("l = " + l);
    }

    /**
     * 회원 정렬 순서
     * 1. 회원 나이 내림차순(desc)
     * 2. 회원 이름 올림차순(asc)
     * 단 2에서 회원 이름이 없으면 마지막에 출력(nulls last)
     */
    @Test
    public void sort() {
        em.persist(new Member(null, 100));
        em.persist(new Member("member5", 100));
        em.persist(new Member("member6", 100));

        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.eq(100))
                .orderBy(member.age.desc(), member.username.asc().nullsLast())
                .fetch();

        Member member5 = result.get(0);
        Member member6 = result.get(1);
        Member memberNull = result.get(2);

        assertEquals(member5.getUsername(), "member5");
        assertEquals(member6.getUsername(), "member6");
        assertEquals(memberNull.getUsername(), null);
    }

    @Test
    public void paging1() {
        //fetchResults() 는 쿼리dsl 5.0 부터 비권장(Deprecated) 됨
        List<Member> result = queryFactory
                .selectFrom(member)
                .orderBy(member.username.desc())
                .offset(1) //0부터 시작(zero index)
                .limit(2) //최대 2건 조회
                .fetch();
        assertEquals(result.size(), 2);
    }

    @Test
    public void aggregation() {
        List<Tuple> result = queryFactory
                .select(member.count(),
                        member.age.sum(),
                        member.age.avg(),
                        member.age.max(),
                        member.age.min())
                .from(member)
                .fetch();

        Tuple tuple = result.get(0);

        assertEquals(tuple.get(member.count()), 4);
        assertEquals(tuple.get(member.age.sum()), 100);
        assertEquals(tuple.get(member.age.avg()), 25);
        assertEquals(tuple.get(member.age.max()), 40);
        assertEquals(tuple.get(member.age.min()), 10);
    }

    //팀의 이름과 각 팀의 평균 연령을 구하라.
    @Test
    public void group() {
        List<Tuple> result = queryFactory
                .select(team.name, member.age.avg())
                .from(member)
                .join(member.team, team)
                .groupBy(team.name)
                .fetch();

        Tuple teamA = result.get(0);
        Tuple teamB = result.get(1);

        assertEquals(teamA.get(team.name), "teamA");
        assertEquals(teamA.get(member.age.avg()), 15);

        assertEquals(teamB.get(team.name), "teamB");
        assertEquals(teamB.get(member.age.avg()), 35);
    }

    //팀 A에 소속된 모든 회원
    @Test
    public void join() {
//        QMember member = QMember.member;
//        QTeam team = team;

        List<Member> result = queryFactory
                .selectFrom(member)
                .join(member.team, team)
                .where(team.name.eq("teamA"))
                .fetch();

        result.forEach(System.out::println);

    }

    //회원과 팀을 조인하면서, 팀 이름이 teamA인 팀만 조회, 회원은 모두 조회
    @Test
    public void join_on_filtering() {
        List<Tuple> teamA = queryFactory
                .select(member, team)
                .from(member)
                .leftJoin(member.team, team).on(team.name.eq("teamA"))
                .fetch();

        for (Tuple tuple : teamA) {
            System.out.println("tuple = " + tuple);
        }
    }

    //연관관계가 없는 엔티티 외부 조인
    //회원의 이름과 팀의 이름이 같은 대상 외부 조인
    @Test
    public void join_on_no_relation() throws Exception {
        em.persist(new Member("teamA"));
        em.persist(new Member("teamB"));

        List<Tuple> result = queryFactory
                .select(member, team)
                .from(member)
                .leftJoin(team).on(member.username.eq(team.name))
                .fetch();
        
        for (Tuple tuple : result) {
            System.out.println("tuple = " + tuple);
        }
    }

    @PersistenceUnit
    EntityManagerFactory emf;

    @Test
    public void fetchJoinNo() {
        em.flush();
        em.clear();

        Member findMember = queryFactory
                .selectFrom(member)
                .where(member.username.eq("member1"))
                .fetchOne();

        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());
        assertEquals(loaded, false);

        System.out.println("findMember = " + findMember);
    }

    @Test
    public void fetchJoinUse() {
        em.flush();
        em.clear();

        Member findMember = queryFactory
                .selectFrom(member)
                .join(member.team, team).fetchJoin()
                .where(member.username.eq("member1"))
                .fetchOne();

        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());
        assertEquals(loaded, true);

        System.out.println("findMember = " + findMember);
    }






    //나이가 가장 많은 회원 조회
    @Test
    public void subQuery() {

        //같은 엔티티를 서브쿼리로 조회하는 경우에 별칭이 겹치지 않게 새로 별칭 지정한 Q엔티티클래스를 생성해준다.
        QMember memberSub = new QMember("memberSub");

        Member result = queryFactory
                .selectFrom(member)
                .where(member.age.eq(
                        select(memberSub.age.max())
                                .from(memberSub)
                ))
                .fetchOne();

        assertEquals(result.getAge(), 40);

    }

    //나이가 평균 이상인 회원 조회
    @Test
    public void subQueryGoe() {

        QMember memberSub = new QMember("memberSub");

        List<Member> memberList = queryFactory
                .selectFrom(member)
                .where(member.age.goe(
                        select(memberSub.age.avg())
                                .from(memberSub)
                ))
                .fetch();


        for (Member member : memberList) {
            System.out.println("member = " + member.getUsername());
        }

        assertEquals(memberList.get(0).getUsername(), "member3");
        assertEquals(memberList.get(1).getUsername(), "member4");

    }

    //서브쿼리 in 절 사용
    @Test
    public void subQueryIn() {

        QMember memberSub = new QMember("memberSub");

        List<Member> memberList = queryFactory
                .selectFrom(member)
                .where(member.age.in(
                        select(memberSub.age)
                                .from(memberSub)
                                .where(memberSub.age.gt(10))
                ))
                .fetch();

        memberList.forEach(System.out::println);
    }

    //select 절에 서브쿼리 사용
    @Test
    public void selectSubQuery() {

        QMember memberSub = new QMember("memberSub");

        List<Tuple> tupleList = queryFactory
                .select(member.username,
                        (select(memberSub.age.avg())
                                .from(memberSub)
                        ))
                .from(member)
                .fetch();

        for (Tuple tuple : tupleList) {
            System.out.println("tuple = " + tuple);
        }
    }

    //case 문 사용
    @Test
    public void basicCase() {
        List<String> fetch = queryFactory
                .select(member.age.when(10).then("열살")
                        .when(20).then("스무살")
                        .otherwise("기타나이"))
                .from(member)
                .fetch();

        for (String s : fetch) {
            System.out.println("s = " + s);
        }
    }

    @Test
    public void complexCase() {
        List<String> fetch = queryFactory
                .select(new CaseBuilder()
                        .when(member.age.between(0, 20)).then("0~20살")
                        .when(member.age.between(21, 30)).then("21~31살")
                        .otherwise("기타"))
                .from(member)
                .fetch();

        for (String s : fetch) {
            System.out.println("s = " + s);
        }
    }

    //case 문을 활용하여 order by 하기
    @Test
    public void orderByCase() {
        List<Tuple> tupleList = queryFactory
                .select(member.username, member.age)
                .from(member)
                .orderBy(new CaseBuilder()
                        .when(member.age.between(0, 20)).then(2)
                        .when(member.age.between(21, 30)).then(1)
                        .otherwise(3).asc())
                .fetch();

        for (Tuple tuple : tupleList) {
            System.out.println("tuple = " + tuple);
        }
    }

    //상수 출력해주기
    @Test
    public void constant() {
        List<Tuple> tupleList = queryFactory
                .select(member.username, Expressions.constant("ABC"))
                .from(member)
                .fetch();

        for (Tuple tuple : tupleList) {
            System.out.println("tuple = " + tuple);
        }
    }

    //문자 더하기 concat
    @Test
    public void concat() {

        //username_age
        List<String> fetch = queryFactory
                .select(member.username.concat("_").concat(member.age.stringValue()))
                .from(member)
                .fetch();

        for (String s : fetch) {
            System.out.println("s = " + s);
        }
    }





}
