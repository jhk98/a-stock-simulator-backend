package com.metionlabs.stocksimulator.service;

import com.metionlabs.stocksimulator.controller.dto.account.AccountPatchCommand;
import com.metionlabs.stocksimulator.controller.dto.account.AccountPostCommand;
import com.metionlabs.stocksimulator.domain.Account;
import com.metionlabs.stocksimulator.domain.DailyPrice;
import com.metionlabs.stocksimulator.domain.Member;
import com.metionlabs.stocksimulator.repository.AccountRepository;
import com.metionlabs.stocksimulator.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;

/**
 * 계좌 서비스 — <b>규칙과 트랜잭션이 사는 곳</b>.
 *
 * <p>이 계층이 책임지는 것 세 가지:
 * <ol>
 *   <li><b>조회</b> — 필요한 다른 엔티티(Member)를 DB 에서 가져온다</li>
 *   <li><b>규칙</b> — 1인 1계좌, 계좌번호 중복 금지, 예수금 음수 금지</li>
 *   <li><b>트랜잭션 경계</b> — 조회부터 저장까지가 하나의 단위여야 한다</li>
 * </ol>
 *
 * <h3>⭐ 왜 Controller 가 아니라 여기서 조립하는가</h3>
 * 계좌를 만들려면 {@code Member} 엔티티가 필요하고, 그건 DB 에서 꺼내야 한다.
 * 만약 Controller 에서 엔티티를 조립하면 Controller 가 {@code MemberRepository} 를
 * 주입받아야 하고, 그 순간 <b>Controller 가 데이터 접근 계층이 된다.</b>
 * 조립을 여기로 옮기면 그 문제가 통째로 사라진다.
 *
 * <p>부수 효과로 트랜잭션도 제자리를 찾는다. 조회와 저장이 Controller 에 흩어져 있으면
 * 서로 <b>다른 트랜잭션</b>이라, 조회 직후 다른 요청이 그 회원을 지워도 막을 방법이 없다.
 *
 * <h3>@Transactional 을 클래스에 걸고 읽기는 readOnly 로 덮는 이유</h3>
 * 기본값을 "쓰기 가능"으로 두면 읽기 메서드에도 쓰기 트랜잭션이 잡힌다.
 * {@code readOnly = true} 를 붙이면 JPA 가 <b>변경 감지(dirty checking)를 생략</b>해서
 * 스냅샷을 안 뜨고, DB 에 따라 읽기 전용 커넥션으로도 갈 수 있다.
 */
@Service
@Transactional
public class AccountService {

    private final AccountRepository accountRepository;
    private final MemberRepository memberRepository;

    /**
     * 생성자 주입.
     *
     * <p>⚠️ {@code @Autowired} 필드 주입 대신 이걸 쓴다. 이유는 취향이 아니다:
     * <ul>
     *   <li>필드를 {@code final} 로 만들 수 있다 → 주입 후 바뀌지 않음이 보장된다</li>
     *   <li>의존이 늘어나면 <b>생성자가 길어져 눈에 보인다</b>. 필드 주입은 10개가 되어도
     *       티가 안 나서 클래스가 비대해지는 걸 놓친다</li>
     *   <li>테스트에서 {@code new AccountService(mockRepo, mockRepo)} 로 바로 만들 수 있다.
     *       필드 주입은 스프링 컨텍스트를 띄우거나 리플렉션을 써야 한다</li>
     * </ul>
     * 생성자가 하나뿐이면 {@code @Autowired} 는 생략해도 스프링이 알아서 쓴다.입
     */
    public AccountService(AccountRepository accountRepository, MemberRepository memberRepository) {
        this.accountRepository = accountRepository;
        this.memberRepository = memberRepository;
    }

    // ========================================================================
    // C — 생성
    // ========================================================================

    /**
     * 계좌를 만든다.
     *
     * <h4>왜 id(Long) 가 아니라 Account 를 돌려주는가</h4>
     * <ul>
     *   <li><b>일관성</b> — {@code getById}, {@code update} 도 Account 를 돌려준다.
     *       하나만 다르면 호출부가 매번 "이건 뭘 주더라" 를 확인해야 한다</li>
     *   <li><b>결정을 호출부에 남긴다</b> — id 만 필요하면 {@code .getId()} 하면 그만이다.
     *       반대로 서비스가 id 만 주면, 나중에 생성 결과 전체를 응답에 넣고 싶어질 때
     *       <b>서비스 시그니처를 고쳐야 한다</b></li>
     * </ul>
     *
     * <p>⚠️ 다만 알아둘 것: 트랜잭션이 끝나면 이 엔티티는 <b>준영속(detached)</b> 상태가 된다.
     * 지금은 {@code @OneToOne} 이 기본 EAGER 라 {@code getMember()} 가 이미 로딩돼 있어
     * 문제가 없지만, 나중에 {@code fetch = LAZY} 로 바꾸면 컨트롤러에서 연관을 건드리는 순간
     * {@code LazyInitializationException} 이 난다.
     * 그때의 해법은 "엔티티 대신 id 를 반환" 이 아니라, <b>트랜잭션 안에서 DTO 로 변환</b>하거나
     * fetch join 으로 필요한 것을 함께 읽어오는 것이다.
     *
     * @return 저장된 계좌 (id 가 채워진 상태)
     * @throws NoSuchElementException   회원이 없을 때
     * @throws IllegalStateException    이미 계좌가 있거나 계좌번호가 중복일 때
     */
    public Account create(AccountPostCommand command) {
        // 1) 조회 — 회원이 실재하는지 확인한다. 없는 회원의 계좌를 만들면 안 된다.
        Member member = memberRepository.findById(command.getMemberId())
                .orElseThrow(() -> new NoSuchElementException(
                        "회원을 찾을 수 없습니다. memberId=" + command.getMemberId()));

        // 2) 규칙 — Account.member 가 @OneToOne 이라 회원당 계좌는 하나다.
        //    DB 제약(unique)만 믿고 넘기면 예외 메시지가 SQL 오류로 나와서
        //    호출자가 원인을 알 수 없다. 여기서 먼저 걸러 뜻이 통하는 메시지를 준다.
        if (accountRepository.existsByMemberId(member.getId())) {
            throw new IllegalStateException("이미 계좌가 있는 회원입니다. memberId=" + member.getId());
        }
        if (accountRepository.existsByAccountNum(command.getAccountNum())) {
            throw new IllegalStateException("이미 사용 중인 계좌번호입니다. " + command.getAccountNum());
        }

        // 3) 조립 — 생성 시각은 서버가 정한다. 클라이언트가 보낸 값을 쓰면
        //    시계를 조작해 과거/미래 계좌를 만들 수 있다.
        LocalDateTime now = LocalDateTime.now();
        Account account = Account.builder()
                .member(member)
                .accountNum(command.getAccountNum())
                .deposit(command.getDeposit())
                .createdAt(now)
                .modifiedAt(now)
                .build();

        // 4) 저장 — id 가 null 인 엔티티라 save() 는 INSERT 로 동작한다.
        //    save() 가 돌려주는 객체를 써야 한다. 인자로 넘긴 account 에는
        //    생성된 id 가 안 채워질 수 있다(구현·전략에 따라 다르다).
        return accountRepository.save(account);
    }

    // ========================================================================
    // R — 조회
    // ========================================================================

    /** id 로 조회. 없으면 예외. */
    @Transactional(readOnly = true)
    public Account getById(Long id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("계좌를 찾을 수 없습니다. id=" + id));
    }

    /** 회원 id 로 조회. 없으면 예외. */
    @Transactional(readOnly = true)
    public Account getByMemberId(Long memberId) {
        return accountRepository.findByMemberId(memberId)
                .orElseThrow(() -> new NoSuchElementException(
                        "해당 회원의 계좌가 없습니다. memberId=" + memberId));
    }

    // ========================================================================
    // U — 수정
    // ========================================================================

    /**
     * 계좌 정보를 부분 수정한다(PATCH).
     *
     * <p><b>PATCH 는 "보낸 것만 바꾼다"</b>. 안 보낸 필드는 그대로 둬야 한다.
     * 그래서 command 의 필드가 null 이면 건드리지 않는다.
     * (전부 덮어쓰는 건 PUT 의 의미다 — 섞으면 안 보낸 필드가 null 로 날아간다)
     *
     * <p>⭐ {@code save()} 를 호출하지 않는 게 핵심이다. 트랜잭션 안에서 조회한 엔티티는
     * 영속 상태라, 값을 바꾸면 커밋 시점에 JPA 가 <b>변경을 감지해 UPDATE 를 자동 발행</b>한다
     * (dirty checking). 여기서 굳이 save() 를 부르면 같은 일을 두 번 지시하는 셈이다.
     */
    public Account update(Long id, AccountPatchCommand command) {
        Account account = getById(id);

        // 계좌번호를 바꾸려는 경우에만 중복 검사한다.
        // 자기 자신과 같은 번호면 중복이 아니다.
        String newAccountNum = command.getAccountNum();
        if (newAccountNum != null && !newAccountNum.equals(account.getAccountNum())
                && accountRepository.existsByAccountNum(newAccountNum)) {
            throw new IllegalStateException("이미 사용 중인 계좌번호입니다. " + newAccountNum);
        }

        // 값 변경은 엔티티가 스스로 한다. 서비스가 setter 로 필드를 하나씩 만지면
        // "수정할 때 modifiedAt 도 같이 갱신" 같은 규칙을 호출부마다 반복해야 하고,
        // 한 군데라도 빠뜨리면 조용히 어긋난다.
        account.update(newAccountNum, command.getDeposit());
        return account;
    }

    // ========================================================================
    // D — 삭제
    // ========================================================================

    /**
     * 계좌를 삭제한다.
     *
     * <p>존재 확인을 먼저 하는 이유: {@code deleteById()} 는 대상이 없어도 조용히 넘어간다.
     * 그러면 없는 계좌를 지워달라는 요청에도 200 이 나가서, 호출자는 지워졌다고 믿는다.
     */
    public void delete(Long id) {
        Account account = getById(id);
        accountRepository.delete(account);
    }
}
