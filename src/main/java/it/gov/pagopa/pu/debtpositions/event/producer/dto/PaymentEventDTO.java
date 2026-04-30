package it.gov.pagopa.pu.debtpositions.event.producer.dto;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.workflowhub.dto.generated.PaymentEventType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentEventDTO {
  private String eventId;
  private String traceId;
  private PaymentEventType eventType;
  private OffsetDateTime eventDateTime;
  private DebtPositionDTO payload;
  private String eventDescription;
}
