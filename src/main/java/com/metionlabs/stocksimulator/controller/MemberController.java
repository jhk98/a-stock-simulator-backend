package com.metionlabs.stocksimulator.controller;

import com.metionlabs.stocksimulator.controller.dto.member.MemberGetResult;
import com.metionlabs.stocksimulator.controller.dto.member.MemberPatchCommand;
import com.metionlabs.stocksimulator.controller.dto.member.MemberPostCommand;
import com.metionlabs.stocksimulator.controller.dto.member.MemberPostResult;
import com.metionlabs.stocksimulator.domain.Member;
import com.metionlabs.stocksimulator.service.MemberService;
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

    // 회원 정보 부분 수정
    @PatchMapping("/{id}")
    public ResponseEntity<MemberGetResult> update(@PathVariable Long id,
                                                  @RequestBody @Valid MemberPatchCommand command) {
        Member member = memberService.update(id, command);
        return ResponseEntity.ok(MemberGetResult.from(member));
    }

    // ========================================================================
    // D — DELETE /members/{id}
    // ========================================================================

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        memberService.delete(id);
        return ResponseEntity.noContent().build();
    }
}