package it.gov.pagopa.pu.debtpositions;

import it.gov.pagopa.pu.debtpositions.util.Constants;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.webmvc.autoconfigure.error.ErrorMvcAutoConfiguration;

import java.util.TimeZone;

@SpringBootApplication(
  exclude = {ErrorMvcAutoConfiguration.class},
  scanBasePackages = "it.gov.pagopa.pu"
)
public class DebtPositionsApplication {

	public static void main(String[] args) {
    TimeZone.setDefault(Constants.DEFAULT_TIMEZONE);
		SpringApplication.run(DebtPositionsApplication.class, args);
	}

}
