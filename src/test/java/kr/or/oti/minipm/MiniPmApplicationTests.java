package kr.or.oti.minipm;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import kr.or.oti.minipm.mapper.MemberMapper;
import kr.or.oti.minipm.vo.MemberVO;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class MiniPmApplicationTests {

    @Autowired
    private DataSource dataSource;
    
    @Autowired
    private MemberMapper memberMapper;

    @Test
    void oracleConnectionTest() throws Exception {

        System.out.println(
                "DataSource 종류: " + dataSource.getClass().getName()
        );

        String sql = "SELECT 1 FROM DUAL";

        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()
        ) {
            assertTrue(resultSet.next(), "조회 결과가 있어야 합니다.");

            int result = resultSet.getInt(1);

            System.out.println("Oracle 조회 결과: " + result);

            assertEquals(1, result);
        }
    }
    
    @Test
    void countMembersTest() {
        int count = memberMapper.countMembers();

        System.out.println("회원 수: " + count);

        assertTrue(count >= 0);
    }
    
    @Test
    @Transactional
    void insertMemberTest() {
        int beforeCount = memberMapper.countMembers();

        MemberVO member = new MemberVO();
        member.setLoginId("test_user_01");
        member.setPassword("temporary-test-password");
        member.setName("테스트 회원");
        member.setEmail("test01@example.com");

        int insertedRows = memberMapper.insertMember(member);
        int afterCount = memberMapper.countMembers();

        System.out.println("생성된 회원 번호: " + member.getMemberId());
        System.out.println("등록된 행 개수: " + insertedRows);
        System.out.println("등록 전 회원 수: " + beforeCount);
        System.out.println("등록 후 회원 수: " + afterCount);

        assertNotNull(member.getMemberId());
        assertEquals(1, insertedRows);
        assertEquals(beforeCount + 1, afterCount);
    }
    
    @Test
    @Transactional
    void selectMemberByLoginIdTest() {
        MemberVO newMember = new MemberVO();
        newMember.setLoginId("search_test_user");
        newMember.setPassword("temporary-test-password");
        newMember.setName("조회 테스트");
        newMember.setEmail("search@example.com");

        memberMapper.insertMember(newMember);

        MemberVO foundMember =
                memberMapper.selectMemberByLoginId("search_test_user");

        assertNotNull(foundMember);
        assertEquals(newMember.getMemberId(), foundMember.getMemberId());
        assertEquals("search_test_user", foundMember.getLoginId());
        assertEquals("조회 테스트", foundMember.getName());

        System.out.println("조회한 회원 번호: " + foundMember.getMemberId());
        System.out.println("조회한 로그인 아이디: " + foundMember.getLoginId());
        System.out.println("조회한 회원 이름: " + foundMember.getName());
    }
}