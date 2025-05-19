package dto;

import lombok.*;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

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
