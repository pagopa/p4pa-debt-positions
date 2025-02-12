package it.gov.pagopa.pu.debtpositions.model.view.receipt;

import it.gov.pagopa.pu.debtpositions.enums.ReceiptOriginType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

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
  private ReceiptOriginType receiptOrigin;
  private String iuv;
  private String description;

}
