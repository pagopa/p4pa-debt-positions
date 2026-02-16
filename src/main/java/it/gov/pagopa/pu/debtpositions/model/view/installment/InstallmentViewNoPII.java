package it.gov.pagopa.pu.debtpositions.model.view.installment;

import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentStatus;
import it.gov.pagopa.pu.debtpositions.dto.pii.InstallmentPIIDTO;
import it.gov.pagopa.pu.debtpositions.model.NoPIIEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "installment")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class InstallmentViewNoPII implements NoPIIEntity<InstallmentPIIDTO> {

  @Id
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
  @NotNull
  private byte[] debtorFiscalCodeHash;
  @NotNull
  @NotNull
  private Long personalDataId;
  private String debtPositionTypeOrgDescription;

}
