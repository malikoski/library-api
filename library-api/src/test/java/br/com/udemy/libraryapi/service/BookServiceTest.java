package br.com.udemy.libraryapi.service;

import br.com.udemy.libraryapi.api.exception.BusinessException;
import br.com.udemy.libraryapi.model.Book;
import br.com.udemy.libraryapi.model.repository.BookRepository;
import br.com.udemy.libraryapi.service.impl.BookServiceImpl;
import lombok.val;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class BookServiceTest {

    BookService bookService;

    @MockBean
    BookRepository bookRepository;

    @BeforeEach
    public void setUp() {
        this.bookService = new BookServiceImpl(bookRepository);
    }

    @Test
    @DisplayName("Deve salvar um livro")
    public void saveBookTest() {

        var book = createValidBook();
        var returnBook = createValidBook();
        returnBook.setId(1l);

        when(bookRepository.existsByIsbn(anyString())).thenReturn(false);
        when(bookRepository.save(book)).thenReturn(returnBook);

        var savedBook = bookService.save(book);

        assertThat(savedBook.getId()).isNotNull();
        assertThat(savedBook.getIsbn()).isEqualTo("123");
        assertThat(savedBook.getTitle()).isEqualTo("As aventuras");
        assertThat(savedBook.getAuthor()).isEqualTo("Fulano");

    }

    @Test
    @DisplayName("Deve lançar erro de negócio ao tentar salvar um livro com isbn duplicado.")
    public void shouldNotSaveABookWithDuplicatedISBN() {

        var book = createValidBook();

        when(bookRepository.existsByIsbn(anyString())).thenReturn(true);

        var exception = Assertions.catchThrowable(() -> bookService.save(book));
        assertThat(exception)
                .isInstanceOf(BusinessException.class)
                .hasMessage("Isbn já cadastrado.");

        verify(bookRepository, Mockito.never()).save(book);
    }


    @Test
    @DisplayName("Deve obter um livro por Id")
    public void getByIdTest() {
        var id = 1l;

        Book book = createValidBook();
        book.setId(id);
        when(bookRepository.findById(id)).thenReturn(Optional.of(book));

        var foundBook = bookService.getById(id);

        assertThat(foundBook.isPresent()).isTrue();
        assertThat(foundBook.get().getId()).isEqualTo(id);
        assertThat(foundBook.get().getAuthor()).isEqualTo(book.getAuthor());
        assertThat(foundBook.get().getTitle()).isEqualTo(book.getTitle());
        assertThat(foundBook.get().getIsbn()).isEqualTo(book.getIsbn());
    }

    @Test
    @DisplayName("Deve retornar vazion ao obter um livro por Id quando ele não existe na base")
    public void bookNotFoundTest() {
        var id = 1l;

        when(bookRepository.findById(id)).thenReturn(Optional.empty());

        var foundBook = bookService.getById(id);

        assertThat(foundBook.isPresent()).isFalse();
    }

    @Test
    @DisplayName("Deve deletar um livro")
    public void deleteBookTest() {
        var book = Book.builder().id(1l).build();

        org.junit.jupiter.api.Assertions.assertDoesNotThrow( () -> bookService.delete(book));

        verify(bookRepository, times(1)).delete(book);
    }

    @Test
    @DisplayName("Deve ocorrer erro ao tentar deletar um livro inexistente")
    public void deleteInvalidBookTest() {
        var exception = Assertions.catchThrowable(() -> bookService.delete(null));
        assertThat(exception)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Book id can't be null.");

        verify(bookRepository, Mockito.never()).delete(null);
    }

    @Test
    @DisplayName("Deve ocorrer erro ao tentar deletar um livro sem id")
    public void deleteInvalidBookIdTest() {
        var book = Book.builder().id(null).build();

        var exception = Assertions.catchThrowable(() -> bookService.delete(book));
        assertThat(exception)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Book id can't be null.");

        verify(bookRepository, Mockito.never()).delete(book);
    }


    @Test
    @DisplayName("Deve atualizar um livro")
    public void updateBookTest() {
        long id = 1l;
        var updatingBook = Book.builder().id(id).build();

        var updatedBook = createValidBook();
        updatedBook.setId(id);

        when(bookRepository.save(updatingBook)).thenReturn(updatedBook);

        var book = bookService.update(updatingBook);

        assertThat(book.getId()).isEqualTo(updatedBook.getId());
        assertThat(book.getTitle()).isEqualTo(updatedBook.getTitle());
        assertThat(book.getIsbn()).isEqualTo(updatedBook.getIsbn());
        assertThat(book.getAuthor()).isEqualTo(updatedBook.getAuthor());

    }

    @Test
    @DisplayName("Deve ocorrer erro ao tentar atualizar um livro inexistente")
    public void updateInvalidBookTest() {
        var exception = Assertions.catchThrowable(() -> bookService.update(null));
        assertThat(exception)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Book id can't be null.");

        verify(bookRepository, Mockito.never()).save(null);
    }

    @Test
    @DisplayName("Deve ocorrer erro ao tentar atualizar um livro sem id")
    public void updateInvalidBookIdTest() {
        var book = createValidBook();

        var exception = Assertions.catchThrowable(() -> bookService.update(book));
        assertThat(exception)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Book id can't be null.");

        verify(bookRepository, Mockito.never()).save(book);
    }

    @Test
    @DisplayName("Deve filtrar livros pelas propriedades")
    public void findBookTest() {
        var book = createValidBook();

        var pageRequest = PageRequest.of(0, 10);
        final List<Book> lista = Arrays.asList(book);
        var page = new PageImpl<Book>(lista, pageRequest, 1);
        when(bookRepository.findAll(any(Example.class), any(PageRequest.class))).thenReturn(page);

        var result = bookService.find(book, pageRequest);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).isEqualTo(lista);
        assertThat(result.getPageable().getPageNumber()).isEqualTo(0);
        assertThat(result.getPageable().getPageSize()).isEqualTo(10);

    }



    @Test
    @DisplayName("Deve obter um livro pelo isbn")
    public void getBookByIsbnTest() {
        String isbn = "1230";

        when(bookRepository.findByIsbn(isbn)).thenReturn(Optional.of(Book.builder().id(1l).isbn(isbn).build()));

        var book = bookService.getBookByIsbn(isbn);

        assertThat(book.isPresent()).isTrue();
        assertThat(book.get().getId()).isEqualTo(1l);
        assertThat(book.get().getIsbn()).isEqualTo(isbn);

        verify(bookRepository, times(1)).findByIsbn(isbn);

    }

    private Book createValidBook() {
        return Book.builder().isbn("123").author("Fulano").title("As aventuras").build();
    }


}
