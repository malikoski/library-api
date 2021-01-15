package br.com.udemy.libraryapi.model.repository;

import br.com.udemy.libraryapi.model.Book;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@DataJpaTest
public class BookRepositoryTest {

    @Autowired
    TestEntityManager entityManager;

    @Autowired
    BookRepository bookRepository;

    @Test
    @DisplayName("Deve retornar verdadeiro quando existir um livro na base com o isbn informado")
    public void returnTrueWhenIsIsbnExists() {

        var isbn = "123";
        var book = createNewBook(isbn);
        entityManager.persist(book);

        boolean exists = bookRepository.existsByIsbn(isbn);

        assertThat(exists).isTrue();
    }

    public static Book createNewBook(String isbn) {
        return Book.builder().title("Aventuras").author("Fulano").isbn(isbn).build();
    }

    @Test
    @DisplayName("Deve retornar falso quando não existir um livro na base com o isbn informado")
    public void returnTrueWhenIsIsbnDoesnExists() {

        var isbn = "123";
        boolean exists = bookRepository.existsByIsbn(isbn);
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("Deve obter um livro por id")
    public void findByIdTest() {

        var book = createNewBook("123");
        entityManager.persist(book);

        var foundBook = bookRepository.findById(book.getId());
        assertThat(foundBook.isPresent()).isTrue();
    }


    @Test
    @DisplayName("Deve salvar um livro")
    public void saveBookTest() {

        var book = createNewBook("123");

        Book savedBook = bookRepository.save(book);

        assertThat(savedBook.getId()).isNotNull();
    }

    @Test
    @DisplayName("Deve deletar um livro")
    public void deleteBookTest() {

        var book = createNewBook("123");
        entityManager.persist(book);

        var foundBook = entityManager.find(Book.class, book.getId());

        bookRepository.delete(foundBook);
        var deletedBook = entityManager.find(Book.class, book.getId());
        assertThat(deletedBook).isNull();
    }
}
