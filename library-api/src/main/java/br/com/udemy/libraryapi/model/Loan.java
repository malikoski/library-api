package br.com.udemy.libraryapi.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100)
    private String customer;

    @Column(name = "customer_email")
    private String customerEmail;

    @JoinColumn(name = "id_book")
    @ManyToOne
    private Book book;

    private LocalDate loanDate;

    private Boolean returned;

}
