package com.ksm.querydslstudy.jpaRepository;

import com.ksm.querydslstudy.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TeamJpaRepository extends JpaRepository<Team, Long> {
    @Query("select t " +
            "from Team t " +
            "join fetch t.members ")
    List<Team> findAllByKsm();
}
