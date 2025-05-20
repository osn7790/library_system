package dao;

import dto.Borrow;
import util.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BorrowDAO {

    // SQL 상수 정의
    private static final String CHECK_BOOK_AVAILABILITY_SQL = "SELECT available FROM books WHERE id = ?";
    private static final String INSERT_BORROW_SQL = "INSERT INTO borrows (student_id, book_id, borrow_date) VALUES (?, ?, CURRENT_DATE)";
    private static final String UPDATE_BOOK_UNAVAILABLE_SQL = "UPDATE books SET available = FALSE WHERE id = ?";
    private static final String UPDATE_BOOK_AVAILABLE_SQL = "UPDATE books SET available = TRUE WHERE id = ?";
    private static final String SELECT_BORROWED_BOOKS_SQL = "SELECT * FROM borrows WHERE return_date IS NULL";
    private static final String CHECK_BORROW_RECORD_SQL = "SELECT * FROM borrows WHERE book_id = ? AND student_id = ? AND return_date IS NULL";
    private static final String UPDATE_RETURN_DATE_SQL = "UPDATE borrows SET return_date = CURRENT_DATE WHERE book_id = ? AND student_id = ?";

    /**
     * 도서 대출 처리
     */
    public void borrowBook(int bookId, int studentPK) throws SQLException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(CHECK_BOOK_AVAILABILITY_SQL)) {

            checkStmt.setInt(1, bookId);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next() && rs.getBoolean("available")) {
                try (PreparedStatement insertStmt = conn.prepareStatement(INSERT_BORROW_SQL);
                     PreparedStatement updateStmt = conn.prepareStatement(UPDATE_BOOK_UNAVAILABLE_SQL)) {

                    insertStmt.setInt(1, studentPK);
                    insertStmt.setInt(2, bookId);  // FIXED: 잘못된 인덱스 수정
                    updateStmt.setInt(1, bookId);

                    insertStmt.executeUpdate();
                    updateStmt.executeUpdate();
                }
            } else {
                throw new SQLException("도서가 대출 불가능합니다 (이미 대출 중일 수 있음).");
            }
        }
    }

    /**
     * 현재 대출 중인 도서 목록 조회
     */
    public List<Borrow> getBorrowedBooks() throws SQLException {
        List<Borrow> borrowList = new ArrayList<>();

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SELECT_BORROWED_BOOKS_SQL);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Borrow borrow = new Borrow();
                borrow.setId(rs.getInt("id"));
                borrow.setBookId(rs.getInt("book_id"));
                borrow.setStudentId(rs.getInt("student_id"));
                borrow.setBorrowDate(rs.getDate("borrow_date").toLocalDate());
                borrowList.add(borrow);
            }
        }

        return borrowList;
    }

    /**
     * 도서 반납 처리
     */
    public void returnBook(int bookId, int studentPK) throws SQLException {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(CHECK_BORROW_RECORD_SQL)) {

            checkStmt.setInt(1, bookId);
            checkStmt.setInt(2, studentPK);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                try (PreparedStatement returnStmt = conn.prepareStatement(UPDATE_RETURN_DATE_SQL);
                     PreparedStatement updateBookStmt = conn.prepareStatement(UPDATE_BOOK_AVAILABLE_SQL)) {

                    returnStmt.setInt(1, bookId);
                    returnStmt.setInt(2, studentPK);
                    updateBookStmt.setInt(1, bookId);

                    returnStmt.executeUpdate();
                    updateBookStmt.executeUpdate();
                }
            } else {
                throw new SQLException("대출 기록이 없거나 이미 반납 처리된 도서입니다.");
            }
        }
    }

    // 메인 테스트 함수
    public static void main(String[] args) {
        BorrowDAO dao = new BorrowDAO();
        try {
            dao.borrowBook(2, 5);
            System.out.println("도서 대출 완료");

            dao.returnBook(2, 5);
            System.out.println("도서 반납 완료");
        } catch (SQLException e) {
            System.err.println("오류: " + e.getMessage());
        }
    }
}
