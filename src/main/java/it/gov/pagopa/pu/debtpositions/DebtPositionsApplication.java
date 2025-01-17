package it.gov.pagopa.pu.debtpositions;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;

@SpringBootApplication(exclude = {ErrorMvcAutoConfiguration.class})
public class DebtPositionsApplication {

	public static void main(String[] args) {
		SpringApplication.run(DebtPositionsApplication.class, args);
	}

}
