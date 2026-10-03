package com.myproject.stocksimulator.service.member;

import com.myproject.stocksimulator.controller.dto.member.MemberPatchCommand;
import com.myproject.stocksimulator.controller.dto.member.MemberPostCommand;
import com.myproject.stocksimulator.crypto.AesEncryptor;
import com.myproject.stocksimulator.domain.member.Member;
import com.myproject.stocksimulator.domain.member.MemberRole;
import com.myproject.stocksimulator.domain.member.MemberStatus;
import com.myproject.stocksimulator.repository.MemberRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;

/**
 * 회원과 관련된 비즈니스 로직을 처리하는 Service
 *
 * Controller로부터 회원 관련 요청을 전달받아 회원 생성, 조회, 수정, 삭제에 필요한 실제 처리 로직을 수행한다.
 *
 * 데이터 조회 및 저장이 필요한 경우 MemberRepository를 사용한다.
 */
@Service
@Transactional
public class MemberService {

    /**
     * 회원 데이터를 DB에서 조회, 저장, 수정, 삭제하기 위해 사용하는 Repository
     *
     * MemberService는 직접 SQL을 작성하는 대신 MemberRepository릁 통해 JPA를 사용하여 DB에 접근한다.
     */
    private final MemberRepository memberRepository;

    /**
     * 비밀번호를 안전하게 저장하기 위해 사용하는 PasswordEncoder
     *
     * 사용자가 입력한 비밀번호를 DB에 그대로 저장하지 않고 BCrypt 등의 방식으로 해시 처리하기 위해 사용한다.
     */
    private final PasswordEncoder passwordEncoder;

    private final AesEncryptor aesEncryptor;

    /**
     * 생성자 주입
     *
     * MemberService가 생성될 때 필요한 MemberRepository와 PasswordEncoder를 Spring으로부터 전달받는다.
     *
     * Spring Container에 등록된 각각의 Bean을 찾아 MemberService 객체를 생성할 때 자동으로 주입한다.
     *
     * @param memberRepository  회원 데이터 접근을 담당하는 Repository
     * @param passwordEncoder   비밀번호 해시 처리를 담당하는 객체
     */
    public MemberService(MemberRepository memberRepository, PasswordEncoder passwordEncoder, AesEncryptor aesEncryptor) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
        this.aesEncryptor = aesEncryptor;
    }

    // ========================================================================
    // C — 생성
    // ========================================================================

    /**
     * 새로운 회원을 생성한다.
     *
     * 회원가입 과정
     * 1. 로그인 아이디 중복 검사
     * 2. 이메일 중복 검사
     * 3. 전화번호 중복 검사
     * 4. 비밀번호 해시 처리
     * 5. MemberEntity 생성
     * 6. DB 저장
     *
     * 신규 회원은 기본적으로 ACTIVE 상태와 USER 권한을 가진다.
     *
     * @param command   회원가입 요청 정보
     * @return          DB에 저장된 Member Entity
     */
    public Member create(MemberPostCommand command) {

        // 로그인 아이디 중복 검사
        // existsByLoginId()는 회원 전체 데이터를 가져오는 것이 아니라 해당 로그인 아이디가 존재하는지만 확인하여 true / false를 반환한다.
        if(memberRepository.existsByLoginId(command.getLoginId())) {

            // 이미 사용 중인 로그인 아이디라면 회원가입을 진행하지 않고 예외를 발생시킨다.
            throw new IllegalStateException(
                    "이미 사용 중인 로그인 아이디입니다. " + command.getLoginId()
            );
        }

        // 이메일 중복 검사
        if(memberRepository.existsByEmail(command.getEmail())) {
            throw new IllegalStateException(
                    "이미 사용 중인 이메일입니다. " + command.getEmail()
            );
        }

        // 전화번호 중복 검사
        if(memberRepository.existsByPhoneNumber(command.getPhoneNumber())) {
            throw new IllegalStateException(
                    "이미 사용 중인 전화번호입니다. " + command.getPhoneNumber()
            );
        }

        // 회원 생성 시점의 시간을 한 번 생성한다.
        // createdAt과 modifiedAt에 같은 값을 넣기 위해 LocalDateTime.now()를 각각 호출하지 않고 하나의 값을 사용한다.
        LocalDateTime now = LocalDateTime.now();

        // Member Entity 생성
        // Builder 패턴을 사용하여 새로운 Member 객체를 생성한다.
        Member member = Member.builder()
                .loginId(command.getLoginId())
                .name(command.getName())
                .email(command.getEmail())
                .phoneNumber(command.getPhoneNumber())
                .rgstNumber(aesEncryptor.encrypt(command.getRgstNumber()))
                .passwd(passwordEncoder.encode(command.getPasswd()))
                .status(MemberStatus.ACTIVE)
                .role(MemberRole.USER)
                .lastAccessTime(null)
                .createdAt(now)
                .modifiedAt(now)
                .withdrawnAt(null)
                .build();

        // DB 저장
        // 생성된 Member Entity를 DB에 저장한다.
        // 현재 member의 id는 null이고 Member.id는 IDENTITY(AUTO_INCREMENT) 전략을 사용하기 때문에 INSERT가 수행되면서 DB가 새로운 id를 생성한다.
        // save()가 반환하는 Member에는 DB에서 생성된 id도 반영되어 있다.
        return memberRepository.save(member);
    }

    // ========================================================================
    // R — 조회
    // ========================================================================

    /**
     * 회원 PK(id)를 이용하여 회원 한 명을 조회한다.
     *
     * 조회 작업은 데이터를 변경하지 않으므로 readOnly = true 트랜잭션으로 설정한다.
     *
     * 해당 ID의 회원이 존재하지 않으면 NoSuchElementException을 발생시킨다.
     *
     * @param id    조회할 회원의 PK
     * @return      조회된 Member Entity
     */
    @Transactional(readOnly = true)
    public Member getById(Long id) {

        // findById()의 반환 타입은 Optional<Member>이다.
        // 회원이 존재하면 Optional 안에 Member 객체가 존재한다.
        // 회원이 존재하지 않으면 비어 있는 Optional이 반환된다.
        return memberRepository.findByIdAndStatus(id, MemberStatus.ACTIVE)
                .orElseThrow(() -> new NoSuchElementException("활성 상태의 회원을 찾을 수 없습니다. id = " + id));
    }

    // ========================================================================
    // U — 수정
    // ========================================================================

    /**
     * 기존 회원 정보를 부분 수정한다.
     *
     * PATCH 방식이므로 command에 값이 전달된 항목만 수정하고 null인 항목은 기존 값을 유지한다.
     *
     * 이메일이나 전화번호를 실제로 변경하는 경우에는 다른 회원이 이미 사용 중인지 중복 검사를 수행한다.
     *
     * 비밀번호가 전달된 경우에는 해시 처리한 후 변경한다.
     *
     * @param id        수정할 회원의 PK
     * @param command   수정할 회원 정보
     * @return          수정된 Member Entity
     */
    public Member update(Long id, MemberPatchCommand command) {

        // 수정 대상 회원을 먼저 조회한다.
        //
        // 회원이 존재하지 않으면 getById() 내부에서 NoSuchElementException이 발생하므로 이후 로직은 실행되지 않는다.
        Member member = getById(id);

        // --------------------------------------------------------------------
        // 1. 이메일 중복 검사
        // --------------------------------------------------------------------

        // 요청으로 전달받은 새로운 이메일을 꺼낸다.
        String newEmail = command.getEmail();

        // 세 조건을 모두 만족하는 경우에만 중복 검사를 수행한다.
        //
        // 1. newEmail != null
        //    -> 이메일 변경 요청이 들어왔는가?
        //
        // 2. !newEmail.equals(member.getEmail())
        //    -> 기존 이메일과 실제로 다른 이메일인가?
        //
        // 3. memberRepository.exitstByEmail(newEmail)
        //    -> 다른 회원이 해당 이메일을 이미 사용하고 있는가?
        if(newEmail != null && !newEmail.equals(member.getEmail())
                && memberRepository.existsByEmail(newEmail)) {
            throw new IllegalStateException("이미 사용 중인 이메일입니다. " + newEmail);
        }

        // --------------------------------------------------------------------
        // 2. 전화번호 중복 검사
        // --------------------------------------------------------------------

        // 요청으로 전달받은 새로운 전화번호를 꺼낸다.
        String newPhoneNumber = command.getPhoneNumber();

        // 전화번호가 전달되었고 기존 전화번호와 실제로 다르며 다른 회원이 이미 사용 중인 경우 수정할 수 없다.
        if(newPhoneNumber != null && !newPhoneNumber.equals(member.getPhoneNumber())
                && memberRepository.existsByPhoneNumber(newPhoneNumber)) {
            throw new IllegalStateException("이미 사용 중인 전화번호입니다. " + newPhoneNumber);
        }

        // --------------------------------------------------------------------
        // 3. 비밀번호 해시 처리
        // --------------------------------------------------------------------

        // 비밀번호 변경 요청이 없을 경우 Member.update()에 null을 전달하기 위해 기본값을 null로 둔다.
        String encodedPasswd = null;

        // 새로운 비밀번호가 전달된 경우에만 해시 처리한다.
        if(command.getPasswd() != null) {
            encodedPasswd = passwordEncoder.encode(command.getPasswd());
        }

        // --------------------------------------------------------------------
        // 4. Member Entity 수정
        // --------------------------------------------------------------------

        // 실제 회원 정보 변경은 Member Entity의 update() 메서드에서 수행한다.
        //
        // 각 값이 null이면 기존 값을 유지하고 null이 아닌 값만 새로운 값으로 변경된다.
        member.update(
                command.getName(),
                command.getEmail(),
                command.getPhoneNumber(),
                encodedPasswd
        );

        // 여기서는 memberRepository,save(member)를 다시 호출하지 않는다.
        //
        // getById()를 통해 조회된 member는 현재 JPA의 영속성 컨텍스트에서 관리되고 있는 Entity이다.
        //
        // 트랜잭션이 끝날 때 JPA가 Entity의 변경 사항을 확인하고 필요한 UPDATE SQL을 자동으로 실행한다.
        //
        // 이것을 Dirty Checking(변경 감지)이라고 한다.
        return member;
    }

    // ========================================================================
    // D — 삭제
    // ========================================================================

    /**
     * 회원을 탈퇴를 처리한다.
     *
     * 회원 데이터를 즉시 DB에서 삭제하지 않고 회원 상태를 INACTIVE로 변경하는 Soft Delete 방식으로 처리한다.
     *
     * 탈퇴 처리 과정
     * 1. getById()를 통해 ACTIVE 상태의 회원을 조회한다.
     * 2. Member.withdraw()을 호출한다.
     * 3. status를 ACTIVE에서 INACTIVE로 변경한다.
     * 4, withdrawnAt에 실제 탈퇴 요청 시간을 저장한다.
     * 5. modifiedAt을 탈퇴 처리 시간으로 갱신한다,
     *
     * getById()는 ACTIVE 상태의 회원만 조회하기 때문에 이미 탈퇴하여 INACTIVE 상태인 회원은 조회되지 않는다.
     *
     * 따라서 이미 탈퇴한 회원이 다시 탈퇴를 요청하더라도 withdrawnAt이 새로운 시간으로 변경되는 것을 방지할 수 있다.
     *
     * 탈퇴 회원의 데이터는 DB에 30일 동안 보관하며 탈퇴 후 30일이 지난 회원은 별도의 별도의 정리 작업을 통해 실제 DB에서 삭제한다.
     *
     * @param id    탈퇴할 회원의 PK
     */
    public void withdraw(Long id) {

        // 탈퇴할 회원을 조회한다.
        //
        // getById()는 id와 ACTIVE 상태를 조건으로 회원으로 조회한다.
        //
        // 다음과 같은 경우에는 회원을 조회할 수 없다.
        // 1. 해당 id의 회원이 존재하지 않는 경우
        // 2. 해당 회원이 이미 탈퇴하여 INACTIVE 상태인 경우
        //
        // 조회할 수 없는 경우 getById()에서 NoSuchElementException이 발생한다.
        Member member = getById(id);

        // Member Entity의 탈퇴 메서드를 호출한다.
        //
        // withdraw()가 호출되면 다음과 같이 회원 상태가 변경된다.
        //
        // status       : ACTIVE -> INACTIVE
        // withdrawnAt  : null -> 현재 시간
        // modifiedAt   : 현재 시간
        //
        // member는 영속 상태이므로 memberRepository.save(member)를 다시 호출하지 않아도 트랜잭션 종료 시 변경 내용이 DB에 반영된다.
        member.withdraw();
    }
}