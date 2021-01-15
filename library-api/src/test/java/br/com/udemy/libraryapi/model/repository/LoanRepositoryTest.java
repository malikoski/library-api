package br.com.udemy.libraryapi.model.repository;

import br.com.udemy.libraryapi.api.dto.ReturnedLoanDTO;
import br.com.udemy.libraryapi.model.Loan;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.persistence.EntityManager;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@DataJpaTest
public class LoanRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private LoanRepository loanRepository;

    @Test
    @DisplayName("Deve verificar se existe emprestimo para o livro")
    public void existsByBookAndNotReturnedTest() {
        var loan = createAndPersistLoan(LocalDate.now());
        var book = loan.getBook();

        var exists = loanRepository.existsByBookAndReturned(book);

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Deve buscar empréstimo pelo isbn do livro ou customer")
    public void findByBookIsbnOrCustomerTest() {
        var loan = createAndPersistLoan(LocalDate.now());

        var result = loanRepository.findByBookIsbnOrCustomer("123", "Fulano",
                PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent()).contains(loan);
        assertThat(result.getPageable().getPageSize()).isEqualTo(10);
        assertThat(result.getPageable().getPageNumber()).isEqualTo(0);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("Deve obter empréstimos cuja data empréstimo for menor ou igual a três dias atrás e não retornados")
    public void findByLoanDateLessThanAndNotReturnedTest() {
        var loan = createAndPersistLoan( LocalDate.now().minusDays(5));

        var result = loanRepository.findByLoanDateLessThanAndNotReturned(LocalDate.now().minusDays(4));

        assertThat(result).hasSize(1).contains(loan);
    }

    @Test
    @DisplayName("Deve retornar vazio quando não houver empréstimos atrasados")
    public void notFindByLoanDateLessThanAndNotReturnedTest() {
        var loan = createAndPersistLoan( LocalDate.now());

        var result = loanRepository.findByLoanDateLessThanAndNotReturned(LocalDate.now().minusDays(4));

        assertThat(result).isEmpty();
    }


    public Loan createAndPersistLoan(LocalDate loanDate) {
        var book = BookRepositoryTest.createNewBook("123");
        entityManager.persist(book);

        var loan = Loan.builder().book(book).customer("Fulano").loanDate(loanDate).build();
        entityManager.persist(loan);

        return loan;
    }
}
