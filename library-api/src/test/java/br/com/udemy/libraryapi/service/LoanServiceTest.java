package br.com.udemy.libraryapi.service;

import br.com.udemy.libraryapi.api.dto.LoanFilterDTO;
import br.com.udemy.libraryapi.api.dto.ReturnedLoanDTO;
import br.com.udemy.libraryapi.api.exception.BusinessException;
import br.com.udemy.libraryapi.model.Book;
import br.com.udemy.libraryapi.model.Loan;
import br.com.udemy.libraryapi.model.repository.BookRepositoryTest;
import br.com.udemy.libraryapi.model.repository.LoanRepository;
import br.com.udemy.libraryapi.service.impl.LoanServiceImpl;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class LoanServiceTest {

    LoanService loanService;

    @MockBean
    private LoanRepository loanRepository;

    @BeforeEach
    public void setUp() {
        this.loanService = new LoanServiceImpl(loanRepository);
    }

    @Test
    @DisplayName("Deve salver um empréstimo")
    public void saveLoanTest() {
        final String customer = "Fulano";

        final Book book = Book.builder().id(1l).build();

        var savingLoan = Loan.builder()
                .book(book)
                .customer(customer)
                .loanDate(LocalDate.now())
                .build();

        var savedLoan = Loan.builder()
                .id(1l)
                .loanDate(LocalDate.now())
                .customer(customer)
                .book(book)
                .build();

        when(loanRepository.existsByBookAndReturned(book)).thenReturn(false);
        when(loanRepository.save(savingLoan)).thenReturn(savedLoan);

        var loan = loanService.save(savingLoan);

        assertThat(loan.getId()).isEqualTo(savedLoan.getId());
        assertThat(loan.getBook().getId()).isEqualTo(savedLoan.getBook().getId());
        assertThat(loan.getCustomer()).isEqualTo(savedLoan.getCustomer());
        assertThat(loan.getLoanDate()).isEqualTo(savedLoan.getLoanDate());
    }


    @Test
    @DisplayName("Deve lançar erro de negócio ao salvar um empréstimo com livro já emprestado")
    public void loanedBookSaveTest() {
        final String customer = "Fulano";

        final Book book = Book.builder().id(1l).build();

        var savingLoan = Loan.builder()
                .book(book)
                .customer(customer)
                .loanDate(LocalDate.now())
                .build();

        when(loanRepository.existsByBookAndReturned(book)).thenReturn(true);

        Throwable exception =  catchThrowable(() -> loanService.save(savingLoan));

        assertThat(exception).isInstanceOf(BusinessException.class).hasMessage("Book already loaned");

        verify(loanRepository, never()).save(savingLoan);
    }

    @Test
    @DisplayName("Deve obter as informações de um empréstimo pelo Id")
    public void getLoanDetailsTest() {
        var id = 1l;

        var loan = createLoan();
        loan.setId(id);

        when(loanRepository.findById(id)).thenReturn(Optional.of(loan));

        var result = loanService.getById(id);

        assertThat(result.isPresent()).isTrue();
        assertThat(result.get().getId()).isEqualTo(id);
        assertThat(result.get().getCustomer()).isEqualTo(loan.getCustomer());
        assertThat(result.get().getBook()).isEqualTo(loan.getBook());
        assertThat(result.get().getLoanDate()).isEqualTo(loan.getLoanDate());

        verify(loanRepository).findById(id);
    }

    @Test
    @DisplayName("Deve atualizar um empréstimo")
    public void updateLoanTest() {
        var id = 1l;

        var loan = createLoan();
        loan.setId(id);
        loan.setReturned(true);

        when(loanRepository.save(loan)).thenReturn(loan);

        var updatedLoan = loanService.update(loan);

        assertThat(updatedLoan.getReturned()).isTrue();

        verify(loanRepository).save(loan);
    }

    @Test
    @DisplayName("Deve filtrar livros pelas propriedades")
    public void findLoanTest() {
        var loanFilterDTO = LoanFilterDTO.builder().customer("Fulano").isbn("321").build();
        var loan = createLoan();
        loan.setId(1l);

        var pageRequest = PageRequest.of(0, 10);
        final List<Loan> lista = Arrays.asList(loan);

        var page = new PageImpl<Loan>(lista, pageRequest, 1);
        when(loanRepository.findByBookIsbnOrCustomer(any(String.class), any(String.class),
                any(PageRequest.class))).thenReturn(page);

        var result = loanService.find( loanFilterDTO, pageRequest);

        Assertions.assertThat(result.getTotalElements()).isEqualTo(1);
        Assertions.assertThat(result.getContent()).isEqualTo(lista);
        Assertions.assertThat(result.getPageable().getPageNumber()).isEqualTo(0);
        Assertions.assertThat(result.getPageable().getPageSize()).isEqualTo(10);

    }

    public static Loan createLoan() {
        final Book book = Book.builder().id(1l).build();

        return Loan.builder()
                .book(book)
                .customer("Fulano")
                .loanDate(LocalDate.now())
                .build();
    }
}
