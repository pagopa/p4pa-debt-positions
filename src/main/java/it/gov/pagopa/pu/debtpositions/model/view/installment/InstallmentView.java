package it.gov.pagopa.pu.debtpositions.model.view.installment;

import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Entity
@Table(name = "installment")
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class InstallmentView {

  @Id
  private Long installmentId;
  @NotNull
  private Long paymentOptionId;
  private String iuv;
  @Enumerated(EnumType.STRING)
  @NotNull
  private InstallmentStatus status;
  private OffsetDateTime dueDate;
  @NotNull
  private Long amountCents;
  @NotNull
  private String remittanceInformation;
  @NotNull
  private byte[] debtorFiscalCodeHash;
  @NotNull
  private String debtPositionTypeOrgDescription;

}
