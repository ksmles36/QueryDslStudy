package com.ksm.querydslstudy.controller;

import com.ksm.querydslstudy.dto.MemberSearchCondition;
import com.ksm.querydslstudy.dto.MemberTeamDto;
import com.ksm.querydslstudy.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MemberController {

    private final MemberRepository memberRepository;

    @GetMapping(value = "/v2/members")
    public Page<MemberTeamDto> searchMembersV2(MemberSearchCondition condition, Pageable pageable) {
        return memberRepository.searchPageSimple(condition, pageable);
    }

    @GetMapping(value = "/v3/members")
    public Page<MemberTeamDto> searchMembersV3(MemberSearchCondition condition, Pageable pageable) {
        return memberRepository.searchPageComplex(condition, pageable);
    }



}
