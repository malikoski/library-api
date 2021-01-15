package br.com.udemy.libraryapi.api.resource;

import br.com.udemy.libraryapi.api.dto.BookDTO;
import br.com.udemy.libraryapi.api.dto.LoanDTO;
import br.com.udemy.libraryapi.api.dto.LoanFilterDTO;
import br.com.udemy.libraryapi.api.dto.ReturnedLoanDTO;
import br.com.udemy.libraryapi.model.Book;
import br.com.udemy.libraryapi.model.Loan;
import br.com.udemy.libraryapi.service.BookService;
import br.com.udemy.libraryapi.service.LoanService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
@Api("Loan API")
public class LoanController {

    private final LoanService loanService;
    private final BookService bookService;
    private final ModelMapper modelMapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @ApiOperation("Create a loan")
    public Long create(@RequestBody LoanDTO dto) {
        var book = bookService.getBookByIsbn(dto.getIsbn()).
                orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Book not found for passed isbn"));
        var entity = Loan.builder()
                .book(book)
                .customer(dto.getCustomer())
                .loanDate(LocalDate.now())
                .build();

        entity = loanService.save(entity);
        return entity.getId();
    }

    @PatchMapping("{id}")
    @ApiOperation("Update returned of loan by id")
    public void returnBook(@PathVariable Long id,
                           @RequestBody ReturnedLoanDTO dto) {
        Loan loan = loanService.getById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        loan.setReturned(dto.getReturned());
        loanService.update(loan);
    }

    @GetMapping
    @ApiOperation("Finds a loan")
    public Page<LoanDTO> find(LoanFilterDTO dto, Pageable pageRequest) {
        var result = loanService.find(dto, pageRequest);
        var loans = result.getContent()
                .stream()
                .map(entity -> {
                    var bookDTO = modelMapper.map(entity.getBook(), BookDTO.class);
                    var loanDTO = modelMapper.map(entity, LoanDTO.class);
                    loanDTO.setBook(bookDTO);
                    return loanDTO;
                }).collect(Collectors.toList());
        return new PageImpl<LoanDTO>(loans, pageRequest, result.getTotalElements());
    }

}
