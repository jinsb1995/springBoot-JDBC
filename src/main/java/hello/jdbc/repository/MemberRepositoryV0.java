package hello.jdbc.repository;

import hello.jdbc.connection.DBConnectionUtil;
import hello.jdbc.domain.Member;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;


/**
 *  JDBC - DriverManager 사용
 */
@Slf4j
public class MemberRepositoryV0 {

    public Member save(Member member) throws SQLException {

        String sql = "insert into member(member_id, money) values (?, ?)";

        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);

            pstmt.setString(1, member.getMemberId());
            pstmt.setInt(2, member.getMoney());


            // 위에서 준비된게 쿼리가 실제 데이터베이스에서 실행이 되게 된다.
            // 반환값이 있는데, insert 1건을 하면 1이 반환된다.
            pstmt.executeUpdate();

            return member;
        } catch (SQLException e) {
            log.error("db error ", e);
            throw e;
        } finally {
            close(conn, pstmt, null);
        }


    }


    // 이게 지금 tcp/ip에 걸려서 네트워크를 사용하고 있는건데, 이걸 안닫아주면
    // 계속 네트워크에 연결에 안끊어지고 유지가 될 수 있기 떄문에 닫아줘야 한다.
    private void close(Connection conn, Statement stmt, ResultSet rs) {

        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                log.info("에러가 터지면 여기서 할 수 있는게 없기 때문에 로그만 남긴다. ", e);
            }
        }

        if (stmt != null) {
            try {
                stmt.close();  // Exception
            } catch (SQLException e) {
                log.info("에러가 터지면 여기서 할 수 있는게 없기 때문에 로그만 남긴다. ", e);
            }
        }

        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                log.info("에러가 터지면 여기서 할 수 있는게 없기 때문에 로그만 남긴다. ", e);
            }

        }

    }


    private Connection getConnection() {
        return DBConnectionUtil.getConnection();
    }


}
