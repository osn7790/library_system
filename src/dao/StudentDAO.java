package dao;

import dto.Student;
import util.DatabaseUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class StudentDAO {

    public void addstudent (Student student) {
        String sql = "INSERT INTO students (name, student_id) VALUES (?, ?) ";
                try(Connection conn = DatabaseUtil.getConnection();
                    PreparedStatement pstmt = conn.prepareStatement(sql)) {

                    pstmt.setString(1, student.getName());
                    pstmt.setString(2, student.getStudent_Id());



                } catch (Exception e) {}
    }

}
