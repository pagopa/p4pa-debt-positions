package it.gov.pagopa.pu.debtpositions.model.view.receipt;

import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptOrigin;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.OffsetDateTime;

@Entity
@Table(name = "receipt")
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ReceiptView implements Serializable {

  @Id
  private Long receiptId;
  private Long paymentAmountCents;
  private OffsetDateTime paymentDateTime;
  private Long installmentId;
  @Enumerated(EnumType.STRING)
  private ReceiptOrigin receiptOrigin;
  private String iuv;
  private String description;

}
