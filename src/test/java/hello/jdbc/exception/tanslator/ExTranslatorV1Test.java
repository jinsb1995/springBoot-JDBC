package hello.jdbc.exception.tanslator;

import hello.jdbc.connection.ConnectionConst;
import hello.jdbc.domain.Member;
import hello.jdbc.repository.ex.MyDbException;
import hello.jdbc.repository.ex.MyDuplicateKeyException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.support.JdbcUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Random;

import static hello.jdbc.connection.ConnectionConst.*;

@Slf4j
public class ExTranslatorV1Test {


    /**
     * 키 중복 오류 테스트 코드
     */

    Repository repository;
    Service service;

    @BeforeEach
    void init() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource(URL, USERNAME, PASSWORD);
        repository = new Repository(dataSource);
        service = new Service(repository);
    }

    @Test
    void duplicateKeySave() {
        service.create("myId");
        service.create("myId");
    }


    @Slf4j
    @RequiredArgsConstructor
    static class Service {
        private final Repository repository;

        public void create(String memberId) {


            /**
             *  - 처음에는 저장을 시도한다. 만약 Repository에서 MyDuplicateKeyException이 올라오면 이 예외를 잡아서
             *    generateNewId(memberId)로 새로운 ID 생성을 시도한다. 그리고 다시 저장한다. 여기가 예외를 복구하는 부분이다.
             *
             *  - 만약 복구할 수 없는 예외(MyDbException)면 log만 남기고 다시 예외를 던진다.
             *      - 참고로 이 경우 여기서 예외 로그를 남기지 않아도 된다.
             *        어차피 복구할 수 없는 예외는 예외를 공통으로 처리하는 부분까지 전달되기 때문이다.
             *        따라서 이렇게 복구 할 수 없는 예외는 공통으로 예외를 처리하는 곳에서 에외 로그를 남기는 것이 좋다.
             *        여기의 코드는 단순히 다양한 예외를 잡을 수 있다 가 전부이다.
             */
            try {

                repository.save(new Member(memberId, 0));
                log.info("savedId = {}", memberId);

            } catch (MyDuplicateKeyException e) {

                log.info("키 중복, 다시 시도");
                String retryId = generateNewId(memberId);
                log.info("retryId = {}", retryId);
                repository.save(new Member(retryId, 0));

            } catch (MyDbException e) {

                log.info("데이터 접근 계층 예외 ", e);
                throw e;

            }

        }

        private String generateNewId(String memberId) {
            return memberId + new Random().nextInt(10000);
        }

    }


    @RequiredArgsConstructor
    static class Repository {

        private final DataSource dataSource;

        public Member save(Member member) {
            String sql = "insert into member(member_id, money) values(?, ?)";
            Connection con = null;
            PreparedStatement pstmt = null;


            try {
                con = dataSource.getConnection();
                pstmt = con.prepareStatement(sql);
                pstmt.setString(1, member.getMemberId());
                pstmt.setInt(2, member.getMoney());
                pstmt.executeUpdate();

                return member;
            } catch (SQLException e) {
                /**
                 * h2 DB
                 *
                 * e.getErrorCode() == 23505: 오류코드가 키 중복 오류(23505)인 경우
                 * MyDuplicateKeyException을 새로 만들어서 서비스 계층에 던진다.
                 *
                 * 나머지 경우 기본에 만들었던 MyDbException을 던진다.
                 */
                if (e.getErrorCode() == 23505) {
                    throw new MyDuplicateKeyException(e);
                }

                throw new MyDbException(e);
            } finally {
                JdbcUtils.closeStatement(pstmt);
                JdbcUtils.closeConnection(con);
            }

        }
    }
}
