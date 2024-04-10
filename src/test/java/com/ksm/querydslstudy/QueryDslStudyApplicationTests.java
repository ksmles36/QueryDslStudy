package com.ksm.querydslstudy;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Transactional
class QueryDslStudyApplicationTests {

    @Autowired
    EntityManager em;

    @Test
    void contextLoads() {
        Hello hello = new Hello();
        em.persist(hello);

        JPAQueryFactory jpaQueryFactory = new JPAQueryFactory(em);
        QHello qHello = QHello.hello;

        Hello result = jpaQueryFactory
                .selectFrom(qHello)
                .fetchOne();

        Assertions.assertEquals(hello, result);
        Assertions.assertEquals(hello.getId(), result.getId());
    }

}
