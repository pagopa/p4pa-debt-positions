package it.gov.pagopa.pu.debtpositions.config.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import it.gov.pagopa.pu.debtpositions.util.Constants;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Configuration
public class LocalDateTimeToOffsetDateTimeDeserializer extends JsonDeserializer<OffsetDateTime> {

  @Override
  public OffsetDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
    String dateString = p.getValueAsString();
    if (dateString.contains("+") || dateString.endsWith("Z")) {
      return OffsetDateTime.parse(dateString);
    } else {
      return OffsetDateTime.of(LocalDateTime.parse(dateString),
        OffsetDateTime.now().atZoneSameInstant(Constants.ZONEID).getOffset());
    }
  }
}
