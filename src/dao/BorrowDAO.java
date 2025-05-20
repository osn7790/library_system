package dao;

import dto.Borrow;
import util.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BorrowDAO {

    // 도서 대출을 처리 기능
    public void borrowBook(int bookId, int studentPK) throws SQLException {
        // 대출 가능 여부 -- SELECT(books)
        // 대출 가능하다면 --> INSERT(borrows)
        // 대출이 실행 되었다면 --> UPDATE (books -> available)

        String checkSql = "select available from books where id = ? ";
        try (Connection conn = DatabaseUtil.getConnection(); PreparedStatement checkPstmt = conn.prepareStatement(checkSql)) {
            checkPstmt.setInt(1, bookId);
            ResultSet rs1 = checkPstmt.executeQuery();
            if (rs1.next() && rs1.getBoolean("available")) {
                // insert, update
                String insertSql = "insert into borrows (student_id, book_id, borrow_date) \n" + "values (?, ?, CURRENT_DATE) ";
                String updateSql = "update books set available = FALSE where id = ? ";

                try (PreparedStatement borrowStmt = conn.prepareStatement(insertSql); PreparedStatement updateStmt = conn.prepareStatement(updateSql);) {
                    borrowStmt.setInt(1, studentPK);
                    borrowStmt.setInt(1, bookId);
                    System.out.println("---------------------------------------------");
                    updateStmt.setInt(1, bookId);

                    borrowStmt.executeUpdate();
                    updateStmt.executeUpdate();
                }

            } else {
                throw new SQLException("도서가 대출 불가능 합니다");
            }


        }
    }

    // 현재 대출 중인 도서 목록을 조회
    public List<Borrow> getBorrowedBooks() throws SQLException {
        List<Borrow> borrowList = new ArrayList<>();
        String sql = "select * from borrows where return_date IS NULL ";
        try (Connection conn = DatabaseUtil.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Borrow borrowDTO = new Borrow();
                borrowDTO.setId(rs.getInt("id"));
                borrowDTO.setBookId(rs.getInt("book_id"));
                borrowDTO.setStudentId(rs.getInt("student_id"));
                // JAVA DTO 에서 데이터 타입은 LocalDate 이다.
                // 하지만 JDBC API에서 아직은 LocalDate 타입을 지원하지 않는다.
                // JDBC API 제공하는 날짜 데이터 타입은 Date 이다.
                //  rs.getLocalDate << -- 아직은 지원 안함
                // rs.getDate("borrow_date");
                borrowDTO.setBorrowDate(rs.getDate("borrow_date").toLocalDate());
                borrowList.add(borrowDTO);
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return borrowList;
    }

    // 도서 반납을 처리하는 기능 추가

    // studentPK --> borrows 테이블에 student_id 컬럼이다.
    // 즉, students 테이블에 pk를 의미한다.

    // 1. borrows 테이블에 책 정보 조회 (check) -- SELECT (복합 조건)
    // 2. borrows 테이블에 return_date 수정 -- UPDATE
    // 3. books 테이블에 available 수정 --- UPDATE

    // 1. (book_id)반납하려는 특정 책을 찾아야 된다.
    // 2. (student_id) 책을 빌린 학생을 찾기 위함
    // 2.1 다른 책을 빌린 이력도 있을 수 있다.
    // 3. 아직 반납되지 않은 대출 기록만 찾아야 함.
    // 3. 같은 학생이 예전에 여러번 빌린 이력이 있을 수 있다.

    public void returnBook(int bookId, int studentPK) throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseUtil.getConnection();
            // 트랜잭션 시작
            conn.setAutoCommit(false);

            // 이 쿼리에 결과집합에 필요한 것은 borrows 의 pk(id) 값이다.
            int borrowId = 0;
            String checkSql = "SELECT id FROM borrows WHERE book_id = ? " +
                    "          AND student_id = ? AND return_date IS NULL ";

            try (PreparedStatement checkPstmt = conn.prepareStatement(checkSql)) {
                checkPstmt.setInt(1, bookId);
                checkPstmt.setInt(2, studentPK);
                ResultSet rs = checkPstmt.executeQuery();
                // rs.next() 는 맞는 행이 있다면 이라는 의미
                if (!rs.next()) {
                    throw new SQLException("해당 대출 기록이 존재하지 않거나 이미 반납되었습니다.");
                }
                borrowId = rs.getInt("id");

            }

            String updateBorrowSql = "UPDATE borrows SET return_date = CURRENT_DATE WHERE id = ? ";
            String updateBookSql = "UPDATE books SET available = true WHERE id = ? ";

            try (PreparedStatement borrowPstmt = conn.prepareStatement(updateBorrowSql);
                 PreparedStatement bookPstmt = conn.prepareStatement(updateBookSql)) {
                // borrows 설정
                borrowPstmt.setInt(1, borrowId);
                borrowPstmt.executeUpdate(); // 쿼리 실행

                // books 설정
                bookPstmt.setInt(1, bookId);
                bookPstmt.executeUpdate();
            }
            conn.commit(); // 트렌잭션 처리완료. 영구히 저장

        } catch (Exception e) {
            if (conn != null) {
                conn.rollback(); // 오류 발생시 롤백 처리
            }
            System.err.println("rollback 처리를 하였습니다");
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true); // 다시 오토커밋 설정
                conn.close(); // 자원을 닫아야 메모리 누수가 발생하지 않는다.
            }
        }
    }


    // 메인함수
    public static void main(String[] args) {
        // 대출 실행 테스트
        BorrowDAO borrowDAO = new BorrowDAO();
        try {
            borrowDAO.returnBook(2, 5);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    } // end of main
}


