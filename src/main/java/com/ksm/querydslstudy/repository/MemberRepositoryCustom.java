package com.ksm.querydslstudy.repository;

import com.ksm.querydslstudy.dto.MemberSearchCondition;
import com.ksm.querydslstudy.dto.MemberTeamDto;

import java.util.List;

public interface MemberRepositoryCustom {

    List<MemberTeamDto> search(MemberSearchCondition condition);

}
