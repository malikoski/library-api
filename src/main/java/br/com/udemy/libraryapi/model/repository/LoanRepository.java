package br.com.udemy.libraryapi.model.repository;

import br.com.udemy.libraryapi.model.Book;
import br.com.udemy.libraryapi.model.Loan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

public interface LoanRepository extends JpaRepository<Loan, Long> {

    @Query(value = "select case when ( count(l.id) > 0 ) then " +
            "true else false end from Loan l where l.book =  :book and " +
            "( l.returned is null or l.returned is false )")
    boolean existsByBookAndReturned(@Param("book") Book book);

    @Query(value = "select l from Loan l join l.book as b where b.isbn = :isbn or l.customer = :customer ")
    Page<Loan> findByBookIsbnOrCustomer(@Param("isbn") String isbn, @Param("customer") String customer,
                                        Pageable pageRequest);

    Page<Loan> findByBook(Book book, Pageable pageable);

    @Query("select l from Loan l where l.loanDate <= :daysAgo and " +
            "( l.returned is null or l.returned is false )")
    List<Loan> findByLoanDateLessThanAndNotReturned(@Param("daysAgo") LocalDate daysAgo);
}
