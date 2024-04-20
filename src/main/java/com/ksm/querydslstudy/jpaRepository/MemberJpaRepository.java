package com.ksm.querydslstudy.jpaRepository;

import com.ksm.querydslstudy.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberJpaRepository extends JpaRepository<Member, Long> {
}
