package br.com.udemy.libraryapi.service.impl;

import br.com.udemy.libraryapi.api.dto.LoanFilterDTO;
import br.com.udemy.libraryapi.api.exception.BusinessException;
import br.com.udemy.libraryapi.model.Book;
import br.com.udemy.libraryapi.model.Loan;
import br.com.udemy.libraryapi.model.repository.LoanRepository;
import br.com.udemy.libraryapi.service.LoanService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class LoanServiceImpl implements LoanService {

    private final LoanRepository loanRepository;

    @Override
    public Loan save(Loan loan) {
        if (loanRepository.existsByBookAndReturned(loan.getBook())) {
            throw new BusinessException("Book already loaned");
        }
        return loanRepository.save(loan) ;
    }

    @Override
    public Optional<Loan> getById(Long id) {
        return loanRepository.findById(id);
    }

    @Override
    public Loan update(Loan loan) {
        return loanRepository.save(loan);
    }

    @Override
    public Page<Loan> find(LoanFilterDTO filterDTO, Pageable pageRequest) {
        return loanRepository.findByBookIsbnOrCustomer(filterDTO.getIsbn(), filterDTO.getCustomer(), pageRequest);
    }

    @Override
    public Page<Loan> getLoansByBook(Book book, Pageable pageable) {
        return loanRepository.findByBook(book, pageable);
    }

    @Override
    public List<Loan> getAllLateLoans() {
        final Integer loanDays = 4;
        LocalDate threeDaysAgo = LocalDate.now().minusDays(loanDays);
        return loanRepository.findByLoanDateLessThanAndNotReturned(threeDaysAgo);
    }
}
