package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;


/**
 * 예외 누수 문제 해결
 * SQLException 제거
 *
 * MemberRepository interface에 의존
 */
@Slf4j
public class MemberServiceV4 {

    private final MemberRepository memberRepository;


    public MemberServiceV4(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }


    @Transactional  // 이거 쓰려면 스프링 컨테이너가 필요하다.
    public void accountTransfer(String fromId, String toId, int money) {
        bizLogic(fromId, toId, money);
    }

    /**
     * bizLogic
     *   트랜잭션이 시작된 커넥션을 전달하면서 비즈니스 로직을 수행한다.
     *   이렇게 분리한 이유는 트랜잭션을 관리하는 로직과 실제 비즈니스 로직을 구분하기 위함이다.
     *   memberRepository.update(con..) : 비즈니스 로직을 보면 리포지토리를 호출할 때 커넥션을
     *   전달하는 것을 확인할 수 있다.
     */
    private void bizLogic(String fromId, String toId, int money) {
        // 비즈니스 로직
        Member fromMember = memberRepository.findById(fromId);
        Member toMember = memberRepository.findById(toId);

        memberRepository.update(fromId, fromMember.getMoney() - money);

        validation(toMember);

        memberRepository.update(toId, toMember.getMoney() + money);
    }

    private void validation(Member toMember) {
        if (toMember.getMemberId().equals("ex")) {
            throw new IllegalStateException("이체중 예외 발생");
        }
    }
}
