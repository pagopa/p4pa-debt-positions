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
@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
public class PersonalReceiptNoPIIView implements Serializable {

  @Id
  private Long receiptId;
  @NotNull
  private String orgFiscalCode;
  @NotNull
  private Long paymentAmountCents;
  @NotNull
  private OffsetDateTime paymentDateTime;
  @NotNull
  @Enumerated(EnumType.STRING)
  private ReceiptOriginType receiptOrigin;
  @NotNull
  private Long installmentId;
  @NotNull
  private String remittanceInformation;
  @NotNull
  private String debtPositionTypeOrgDescription;
  @NotNull
  private String debtPositionTypeDescription;
  private String serviceType;

}
