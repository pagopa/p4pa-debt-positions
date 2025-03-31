package it.gov.pagopa.pu.debtpositions.model.view.receipt;

import it.gov.pagopa.pu.debtpositions.enums.ReceiptOriginType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
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
  @NotNull
  private Long paymentAmountCents;
  private OffsetDateTime paymentDateTime;
  @NotNull
  private Long installmentId;
  @NotNull
  @Enumerated(EnumType.STRING)
  private ReceiptOriginType receiptOrigin;
  private String iuv;
  @NotNull
  private String debtPositionTypeOrgDescription;

}
