package it.gov.pagopa.pu.debtpositions.model.view.receipt;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
  private String receiptOrigin;
  private String iuv;
  private String description;

}
