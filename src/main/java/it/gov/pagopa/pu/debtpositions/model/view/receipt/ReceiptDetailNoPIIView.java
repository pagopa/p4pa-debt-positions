package it.gov.pagopa.pu.debtpositions.model.view.receipt;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
public class ReceiptDetailNoPIIView implements Serializable {
  @Id
  private Long receiptId;
  private String iuv;
  @NotNull
  private Long paymentAmountCents;
  @NotNull
  private String remittanceInformation;
  @NotNull
  private String debtPositionTypeOrgDescription;
  @NotNull
  private Long debtorPersonalDataId;
  private OffsetDateTime paymentDateTime;
  @NotNull
  private String pspCompanyName;
  @NotNull
  private String iud;
  private String iur;
}
