package hello.jdbc.connection;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.sql.Connection;

import static org.assertj.core.api.Assertions.*;

@Slf4j
public class DBConnectionUtilTest {


    @Test
    void connection() {
        /*

            여기서 반환해주는게, h2.database 라이브러리의 JdbcConnection.class 이다.


         */
        Connection conn = DBConnectionUtil.getConnection();
        assertThat(conn).isNotNull();
    }

}
