package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV2;
import hello.jdbc.repository.MemberRepositoryV3;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.sql.SQLException;


/**
 * 트랜잭션 - 트랜잭션 매니저
 */
@Slf4j
@RequiredArgsConstructor
public class MemberServiceV3_1 {

//    private final DataSource dataSource;
    private final PlatformTransactionManager transactionManager;
    private final MemberRepositoryV3 memberRepository;

    public void accountTransfer(String fromId, String toId, int money) throws SQLException {

//        Connection con = dataSource.getConnection();

        // 트랜잭션 시작    status에는 트랜잭션의 상태 정보가 포함되어있다.         트랜잭션과 관련된 옵션을 지정할 수 있다.
        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());

        try {
            bizLogic(fromId, toId, money);

            transactionManager.commit(status);   // 성공시 커밋

        } catch (Exception e) {
            transactionManager.rollback(status);  // 실패시 롤백
            throw new IllegalStateException(e);
        }

    }

    /**
     * bizLogic
     *   트랜잭션이 시작된 커넥션을 전달하면서 비즈니스 로직을 수행한다.
     *   이렇게 분리한 이유는 트랜잭션을 관리하는 로직과 실제 비즈니스 로직을 구분하기 위함이다.
     *   memberRepository.update(con..) : 비즈니스 로직을 보면 리포지토리를 호출할 때 커넥션을
     *   전달하는 것을 확인할 수 있다.
     */
    private void bizLogic(String fromId, String toId, int money) throws SQLException {
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
