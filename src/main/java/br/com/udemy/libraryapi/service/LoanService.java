package br.com.udemy.libraryapi.service;

import br.com.udemy.libraryapi.api.dto.LoanFilterDTO;
import br.com.udemy.libraryapi.api.resource.BookController;
import br.com.udemy.libraryapi.model.Book;
import br.com.udemy.libraryapi.model.Loan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface LoanService {
    Loan save(Loan loan);

    Optional<Loan> getById(Long id);

    Loan update(Loan loan);

    Page<Loan> find(LoanFilterDTO filter, Pageable pageRequest);

    Page<Loan> getLoansByBook(Book book, Pageable pageable);

    List<Loan> getAllLateLoans();
}
