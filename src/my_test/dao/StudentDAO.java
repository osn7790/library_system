package my_test.dao;

import dto.Student;
import util.DatabaseUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class StudentDAO {

    // 학생 정보를 데이터베이스에 추가
    public void addstudent(Student student) throws SQLException {
        String sql = "INSERT INTO students (name, student_id) VALUES (?, ?) ";
        try (Connection conn = DatabaseUtil.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, student.getName());
            pstmt.setString(2, student.getStudentId());
            pstmt.executeUpdate();
        }
    }

    // 학생 정보 조회 기능

    public List<Student> getAllStudents() throws SQLException {
        List<Student> studentList = new ArrayList<>();
        String sql = "SELECT * FROM students ";
        try (Connection conn = DatabaseUtil.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Student studentDto = new Student();
                // int sId = rs.getint("id")
                studentDto.setId(rs.getInt("id"));
                studentDto.setName(rs.getString("name"));
                studentDto.setStudentId(rs.getString("student_id"));
                studentList.add(studentDto);
            }

        }
        return studentList;
    }

    // 학생 student_id로 학생 인증(로그인 용) 기능 만들기
    public Student authenticateStudent(String studentId) throws SQLException {


        String sql = "SELECT * FROM students where student_id = ? ";
        try (Connection conn = DatabaseUtil.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, studentId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Student studentDTO = new Student();
                studentDTO.setId(rs.getInt("id"));
                studentDTO.setName(rs.getString("name"));
                studentDTO.setStudentId(rs.getString("student_id"));
                return studentDTO;
            }

        }


        return null;
    }

}
