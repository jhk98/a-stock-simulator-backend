package com.myproject.stocksimulator.controller;

import com.myproject.stocksimulator.controller.dto.member.MemberGetResult;
import com.myproject.stocksimulator.controller.dto.member.MemberPatchCommand;
import com.myproject.stocksimulator.controller.dto.member.MemberPostCommand;
import com.myproject.stocksimulator.controller.dto.member.MemberPostResult;
import com.myproject.stocksimulator.domain.member.Member;
import com.myproject.stocksimulator.service.member.MemberService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 회원 관련 HTTP 요청을 처리하는 Controller
 *
 * /members 경로로 들어오는 회원 관련 요청을 받아 요청에 맞는 MemberService 메서드를 호출하고 처리 결과를 HTTP Response로 클라이언트에게 반환한다.
 *
 * Controller는 HTTP 요청/응답 처리를 담당하며 실제 회원 생성, 조회, 수정, 탈퇴와 같은 비즈니스 로직은 MemberService에게 위임한다.
 *
 * 따라서 Controller에서 Repository를 직접 사용하지 않는다.
 */
@RestController
@RequestMapping("/members")
public class MemberController {

    /**
     * 회원 관련 비즈니스 로직을 처리하는 Service
     *
     * Controller는 DB에 직접 접근하지 않고 MemberService를 통해 필요한 작업을 요청한다.
     */
    private final MemberService memberService;

    /**
     * 생성자 주입을 통해 Spring이 관리하는 MemberService 객체를 전달받는다.
     *
     * MemberController가 생성될 때 Spring Container에 등록되어 있는 MemberService Bean이 자동으로 주입된다.
     *
     * @param memberService 회원 관련 비즈니스 로직을 처리하는 Service
     */
    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    // ========================================================================
    // C — POST /members
    // ========================================================================

    /**
     * 새로운 회원을 생성한다.
     *
     * @RequestBody
     * - 클라이언트가 HTTP Body에 JSON으로 전달한 데이터를 MemberPostCommand 객체로 변환한다.
     *
     * @Valid
     * - MemberPostCommand에 선언된 @NotBlank 등의 Validation 조건을 검사한다.
     *
     * @param command   회원가입 요청 데이터
     * @return          생성된 회원의 ID를 담은 MemberPostResult
     */
    @PostMapping
    public ResponseEntity<MemberPostResult> create(@RequestBody @Valid MemberPostCommand command) {

        // 회원가입 요청 DTO를 Service에 전달한다.
        Member member = memberService.create(command);

        // 생성된 Member의 PK를 이용하여 MemberPostResult 응답 DTO를 생성한다.
        // ResponseEntity.ok(...)
        // -> HTTP Status 200 OK와 함께 응답 데이터를 반환한다.
        return ResponseEntity.ok(new MemberPostResult(member.getId()));
    }

    // ========================================================================
    // R — GET /members/{id}
    // ========================================================================

    /**
     * 회원 ID를 이용하여 회원 한 명을 조회한다.
     *
     * @PathVariable
     * - URL 경로에 포함된 값을 메서드 매개변수로 전달받는다.
     *
     * @param id    조회할 회원의 PK
     * @return      조회된 회원 정보를 담은 MemberGetResult
     */
    @GetMapping("/{id}")
    public ResponseEntity<MemberGetResult> get(@PathVariable Long id) {

        // URL로 전달받은 회원 ID를 Service에 전달하여 회원을 조회한다.
        Member member = memberService.getById(id);

        // 조회된 Member Entity를 그대로 반환하지 않고 MemberGetResult.from()을 이용하여 응답 DTO로 변환한다.
        // 민감정보가 응답에 포함되는 것을 방지할 수 있다.
        return ResponseEntity.ok(MemberGetResult.from(member));
    }

    // ========================================================================
    // U — PATCH /members/{id}
    // ========================================================================

    /**
     * 회원 정보를 부분 수정한다.
     *
     * @param id        수정할 회원의 PK
     * @param command   수정할 회원 정보
     * @return          수정이 완료된 회원 정보
     */
    @PatchMapping("/{id}")
    public ResponseEntity<MemberGetResult> update(@PathVariable Long id,
                                                  @RequestBody @Valid MemberPatchCommand command) {

        // 수정할 회원 ID와 수정 내용을 Service에 전달한다.
        Member member = memberService.update(id, command);

        // 수정이 완료된 Member Entity를 클라이언트에 반환하기 위한 MemberGetResult DTO로 변환한다.
        return ResponseEntity.ok(MemberGetResult.from(member));
    }

    // ========================================================================
    // D — DELETE /members/{id}
    // ========================================================================

    /**
     * 회원 탈퇴를 처리한다.
     *
     * DELETE 요청을 받으면 MemberService에 회원 탈퇴 처리를 요청한다.
     *
     * 회원 데이터는 즉시 DB에서 삭제하지 않고 MemberService에서 회원 상태를 INACTIVE로 변경하여 Soft Delete 방식으로 탈퇴 처리한다.
     *
     * 탈퇴 일시를 기준으로 30일 동안 데이터를 보관한 뒤 별도의 스케줄러를 통해 실제 데이터가 삭제된다.
     *
     * Controller는 탈퇴 정책을 직접 처리하지 않고 실제 탈퇴처리는 MemberService에 위임한다.
     *
     * @param id    탈퇴할 회원의 PK
     * @return      HTTP 204 No Content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> withdraw(@PathVariable Long id) {

        // 회원 ID를 Service에 전달하여 회원 탈퇴를 요청한다.
        //
        // 여기서는 DB 데이터를 즉시 삭제하지 않는다.
        // 실제 Soft Delete 처리는 MemberService에서 수행한다.
        memberService.withdraw(id);

        // 탈퇴 요청이 정상적으로 처리되면 별도의 응답 데이터 없이 HTTP 204 No Content를 반환한다.
        return ResponseEntity.noContent().build();
    }
}