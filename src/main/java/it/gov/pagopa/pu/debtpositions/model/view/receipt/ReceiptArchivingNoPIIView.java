package it.gov.pagopa.pu.debtpositions.model.view.receipt;

import it.gov.pagopa.pu.common.pii.dto.No2PIIDTO;
import it.gov.pagopa.pu.debtpositions.dto.pii.InstallmentPIIDTO;
import it.gov.pagopa.pu.debtpositions.dto.pii.ReceiptPIIDTO;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "receipt")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
public class ReceiptArchivingNoPIIView implements No2PIIDTO<InstallmentPIIDTO, ReceiptPIIDTO> {

  @Id
  private Long receiptId;
  @NotNull
  private String paymentReceiptId;
  private OffsetDateTime paymentDateTime;
  @NotNull
  private String creditorReferenceId;
  private String iuv;
  @NotNull
  private Long installmentPersonalDataId;
  @NotNull
  private String remittanceInformation;
  @NotNull
  private Long organizationId;
  @NotNull
  private String orgFiscalCode;
  @NotNull
  private Long receiptPersonalDataId;
  private String rtFilePath;

  @Override
  public Long getPersonalDataId() {
    return installmentPersonalDataId;
  }

  @Override
  public Long getPersonalDataId2() {
    return receiptPersonalDataId;
  }
}
