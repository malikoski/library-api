package br.com.udemy.libraryapi.api.resource;

import br.com.udemy.libraryapi.api.dto.BookDTO;
import br.com.udemy.libraryapi.api.dto.LoanDTO;
import br.com.udemy.libraryapi.model.Book;
import br.com.udemy.libraryapi.service.BookService;
import br.com.udemy.libraryapi.service.LoanService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
@Api("Book API")
@Slf4j
public class BookController {

    private final BookService bookService;
    private final LoanService loanService;
    private final ModelMapper modelMapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @ApiOperation("Create a book")
    public BookDTO create(@RequestBody @Valid BookDTO dto) {
        log.info("creating a book for isbn: {} ", dto.getIsbn());
        Book entity = modelMapper.map(dto, Book.class);
        entity = bookService.save(entity);

        return modelMapper.map(entity, BookDTO.class);
    }

    @GetMapping("{id}")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation("Obtains a book details by id")
    public BookDTO get(@PathVariable Long id) {
        log.info("obtaining details for book id: {} ", id);
        return bookService.getById(id)
                .map(book -> modelMapper.map(book, BookDTO.class))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiOperation("Delete a book by id")
    @ApiResponses({
            @ApiResponse(code = 204, message = "Book succesfully deleted")
    })
    public void delete(@PathVariable Long id) {
        log.info("deleting book id: {} ", id);
        var book = bookService.getById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        bookService.delete(book);
    }

    @PutMapping("{id}")
    @ApiOperation("Updates a book")
    public BookDTO update(@PathVariable Long id, BookDTO dto) {
        log.info("updating book id: {} ", id);
        return bookService.getById(id).map(book -> {
            book.setAuthor(dto.getAuthor());
            book.setTitle(dto.getTitle());
            var updateBook = bookService.update(book);
            return modelMapper.map(updateBook, BookDTO.class);
        }).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @GetMapping()
    @ApiOperation("Finds a book")
    public Page<BookDTO> find(BookDTO dto, Pageable pagerequest) {
        var filter = modelMapper.map(dto, Book.class);
        var result = bookService.find(filter, pagerequest);
        var list = result.getContent()
                .stream()
                .map(entity -> modelMapper.map(entity, BookDTO.class))
                .collect(Collectors.toList());

        return new PageImpl<BookDTO>(list, pagerequest, result.getTotalElements());
    }

    @GetMapping("{id}/loans")
    @ApiOperation("Loads loans by id book")
    public Page<LoanDTO> loansByBook(@PathVariable Long id, Pageable pageable) {
        var book = bookService.getById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        var result = loanService.getLoansByBook(book, pageable);

        var list = result.getContent()
                .stream()
                .map(loan -> {
                    var loanBook = loan.getBook();
                    var bookDTO = modelMapper.map(loanBook, BookDTO.class);
                    var loanDTO = modelMapper.map(loan, LoanDTO.class);
                    loanDTO.setBook(bookDTO);
                    return loanDTO;
                }).collect(Collectors.toList());
        return new PageImpl<LoanDTO>(list, pageable, result.getTotalElements());
    }

}
