package br.com.udemy.libraryapi;

import br.com.udemy.libraryapi.service.EmailService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class LibraryApiApplication {

/*	@Autowired
	private EmailService emailService;*/

	@Bean
	public ModelMapper modelMappepr() {
		return new ModelMapper();
	}

/*	@Bean
	public CommandLineRunner runner() {
		return args -> {
			var emails = Arrays.asList("udemy-library-api-704461@inbox.mailtrap.io");
			emailService.sendMails("Testando serviço de emails.", emails);
			System.out.println("EMAILS ENVIADOS");
		};
	}*/

	public static void main(String[] args) {
		SpringApplication.run(LibraryApiApplication.class, args);
	}

}
