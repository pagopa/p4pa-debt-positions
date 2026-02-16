package it.gov.pagopa.pu.debtpositions.dto.view;

import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentStatus;
import it.gov.pagopa.pu.common.pii.dto.FullPIIDTO;
import it.gov.pagopa.pu.debtpositions.dto.pii.InstallmentPIIDTO;
import it.gov.pagopa.pu.debtpositions.model.view.installment.InstallmentViewNoPII;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class InstallmentViewDTO implements FullPIIDTO<InstallmentViewNoPII, InstallmentPIIDTO> {

  private Long installmentId;
  @NotNull
  private Long debtPositionId;
  @NotNull
  private Long paymentOptionId;
  private Long receiptId;
  private String iuv;
  private String iud;
  @Enumerated(EnumType.STRING)
  @NotNull
  private InstallmentStatus status;
  private String nav;
  private LocalDate dueDate;
  @NotNull
  private Long amountCents;
  @NotNull
  private String remittanceInformation;
  private String originalRemittanceInformation;
  @NotNull
  private byte[] debtorFiscalCodeHash;
  @NotNull
  private String debtPositionTypeOrgDescription;
}
