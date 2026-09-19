package com.myproject.stocksimulator.controller;

import com.myproject.stocksimulator.controller.dto.member.MemberGetResult;
import com.myproject.stocksimulator.controller.dto.member.MemberPatchCommand;
import com.myproject.stocksimulator.controller.dto.member.MemberPostCommand;
import com.myproject.stocksimulator.controller.dto.member.MemberPostResult;
import com.myproject.stocksimulator.domain.member.Member;
import com.myproject.stocksimulator.service.MemberService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/members")
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    // jenkins test

    // ========================================================================
    // C — POST /members
    // ========================================================================

    // 회원 생성
    @PostMapping
    public ResponseEntity<MemberPostResult> create(@RequestBody @Valid MemberPostCommand command) {
        Member member = memberService.create(command);
        return ResponseEntity.ok(new MemberPostResult(member.getId()));
    }

    // ========================================================================
    // R — GET /members/{id}
    // ========================================================================

    // 회원 조회
    @GetMapping("/{id}")
    public ResponseEntity<MemberGetResult> get(@PathVariable Long id) {
        Member member = memberService.getById(id);
        return ResponseEntity.ok(MemberGetResult.from(member));
    }

    // ========================================================================
    // U — PATCH /members/{id}
    // ========================================================================

    // 회원 정보 수정
    @PatchMapping("/{id}")
    public ResponseEntity<MemberGetResult> update(@PathVariable Long id,
                                                  @RequestBody @Valid MemberPatchCommand command) {
        Member member = memberService.update(id, command);
        return ResponseEntity.ok(MemberGetResult.from(member));
    }

    // ========================================================================
    // D — DELETE /members/{id}
    // ========================================================================

    // 회원 정보 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        memberService.delete(id);
        return ResponseEntity.noContent().build();
    }
}