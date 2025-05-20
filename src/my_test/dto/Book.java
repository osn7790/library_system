package my_test.dto;


import lombok.*;

@Getter
@Setter
@NoArgsConstructor // 기본생성자
@AllArgsConstructor // 전체 생성자  -- 생성자는 총 2개
@ToString

public class Book {
    private int id;
    private String title;
    private String author;
    private String publisher;
    private int publicationYear;
    private String isbn;
    private boolean available;

}
