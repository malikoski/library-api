package br.com.udemy.libraryapi.api.resource;

import br.com.udemy.libraryapi.api.dto.LoanDTO;
import br.com.udemy.libraryapi.api.dto.LoanFilterDTO;
import br.com.udemy.libraryapi.api.dto.ReturnedLoanDTO;
import br.com.udemy.libraryapi.api.exception.BusinessException;
import br.com.udemy.libraryapi.model.Book;
import br.com.udemy.libraryapi.model.Loan;
import br.com.udemy.libraryapi.model.repository.BookRepositoryTest;
import br.com.udemy.libraryapi.service.BookService;
import br.com.udemy.libraryapi.service.EmailService;
import br.com.udemy.libraryapi.service.LoanService;
import br.com.udemy.libraryapi.service.LoanServiceTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc
@WebMvcTest(controllers = {LoanController.class})
public class LoanControllerTest {

    static final String LOAN_API = "/api/loans";

    @Autowired
    MockMvc mockMvc;

    @MockBean
    private BookService bookService;

    @MockBean
    private LoanService loanService;

    @MockBean
    EmailService emailService;

    @Test
    @DisplayName("Deve realizar um empréstimo")
    public void createLoanTest() throws Exception {

        var dto = LoanDTO.builder().isbn("123").email("customer@email.com").customer("Fulano").build();
        var json = new ObjectMapper().writeValueAsString(dto);

        var id = 1l;
        var book = Book.builder().id(id).isbn("123").build();
        var loan = Loan.builder().id(id).customer("Fulano").book(book).loanDate(LocalDate.now()).build();

        BDDMockito.given(bookService.getBookByIsbn("123")).willReturn(Optional.of(book));
        BDDMockito.given(loanService.save(any(Loan.class))).willReturn(loan);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post(LOAN_API)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json);

        mockMvc.perform(request)
                .andExpect(status().isCreated())
                .andExpect(content().string("1"));
    }

    @Test
    @DisplayName("Deve retornar erro ao tentar fazer um empréstimo de um livro inexistente")
    public void invalidIsbnCreateLoanTest() throws Exception {

        var dto = LoanDTO.builder().isbn("123").customer("Fulano").build();
        var json = new ObjectMapper().writeValueAsString(dto);

        BDDMockito.given(bookService.getBookByIsbn("123")).willReturn(Optional.empty());

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post(LOAN_API)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json);

        mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors", Matchers.hasSize(1)))
                .andExpect(jsonPath("errors[0]").value("Book not found for passed isbn"));
    }


    @Test
    @DisplayName("Deve retornar erro ao tentar fazer um empréstimo de um livro inexistente")
    public void loanedBookErrorOnCreateLoanTest() throws Exception {

        var dto = LoanDTO.builder().isbn("123").customer("Fulano").build();
        var json = new ObjectMapper().writeValueAsString(dto);

        var book = Book.builder().id(1l).isbn("123").build();

        BDDMockito.given(bookService.getBookByIsbn("123")).willReturn(Optional.of(book));
        BDDMockito.given(loanService.save(any(Loan.class))).willThrow(
                new BusinessException("Book alrealy loaned"));

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post(LOAN_API)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json);

        mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors", Matchers.hasSize(1)))
                .andExpect(jsonPath("errors[0]").value("Book alrealy loaned"));
    }


    @Test
    @DisplayName("Deve retornar um livro")
    public void returnBookTest() throws Exception {

        var dto = ReturnedLoanDTO.builder().returned(true).build();
        val loan = Loan.builder().id(1l).build();
        BDDMockito.given(loanService.getById(Mockito.anyLong()))
                .willReturn(Optional.of(loan));

        var json = new ObjectMapper().writeValueAsString(dto);

        mockMvc.perform(
                patch(LOAN_API.concat("/1"))
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
        ).andExpect(status().isOk());

        verify(loanService, times(1)).update(loan);
    }


    @Test
    @DisplayName("Deve retornar 404 quando tentar devolver um livro inexistente")
    public void returnNotFoundBookTest() throws Exception {

        var dto = ReturnedLoanDTO.builder().returned(true).build();
        BDDMockito.given(loanService.getById(Mockito.anyLong())).willReturn(Optional.empty());

        var json = new ObjectMapper().writeValueAsString(dto);

        mockMvc.perform(
                patch(LOAN_API.concat("/1"))
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
        ).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Deve filtrar emprestimos")
    public void findLoansTest() throws Exception {
        Long id = 1l;
        final Book book = Book.builder().id(id).isbn("321").build();

        var loan = LoanServiceTest.createLoan();
        loan.setId(id);
        loan.setBook(book);

        BDDMockito.given(loanService.find(any(LoanFilterDTO.class), any(Pageable.class)))
                .willReturn(new PageImpl<Loan>(Arrays.asList(loan), PageRequest.of(0,10), 1));

        var queryString = String.format("?isbn=%s&customer=%s&page=0&size=10",
                loan.getBook().getIsbn(), book.getAuthor());

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get(LOAN_API.concat(queryString))
                .accept(MediaType.APPLICATION_JSON);

        mockMvc
                .perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("content", hasSize(1)))
                .andExpect(jsonPath("totalElements").value(1))
                .andExpect(jsonPath("pageable.pageSize").value(10))
                .andExpect(jsonPath("pageable.pageNumber").value(0))
        ;
    }



}
