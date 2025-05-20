package my_test.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Borrow {

    private int id;
    private int student_Id;
    private int book_Id;
    private LocalDate borrow_Date;
    private LocalDate return_date;
}
