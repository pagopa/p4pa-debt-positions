package it.gov.pagopa.pu.debtpositions.model.view.receipt;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionOrigin;
import it.gov.pagopa.pu.debtpositions.dto.pii.InstallmentPIIDTO;
import it.gov.pagopa.pu.debtpositions.enums.ReceiptOriginType;
import it.gov.pagopa.pu.debtpositions.model.NoPIIEntity;
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
public class ReceiptDetailNoPIIView implements NoPIIEntity<InstallmentPIIDTO> {
  @Id
  private Long receiptId;
  private String iuv;
  private String nav;
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
  private Long feeCents;
  private Long notificationFeeCents;
  @Enumerated(EnumType.STRING)
  @NotNull
  private ReceiptOriginType receiptOrigin;
  @Enumerated(EnumType.STRING)
  @NotNull
  private DebtPositionOrigin debtPositionOrigin;

  @Override
  public void setPersonalDataId(Long personalDataId) {
    this.debtorPersonalDataId = personalDataId;
  }

  @Override
  public Long getPersonalDataId() {
    return this.debtorPersonalDataId;
  }
}
