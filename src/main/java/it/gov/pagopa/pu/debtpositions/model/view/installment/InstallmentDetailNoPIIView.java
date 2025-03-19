package it.gov.pagopa.pu.debtpositions.model.view.installment;

import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "installment")
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class InstallmentDetailNoPIIView implements Serializable {

  @Id
  private Long installmentId;
  private Long receiptId;
  @NotNull
  private Long paymentOptionId;
  @NotNull
  @Enumerated(EnumType.STRING)
  private InstallmentStatus status;
  private String iuv;
  @NotNull
  private Long amountCents;
  private LocalDate dueDate;
  @NotNull
  private Long personalDataId;
  @NotNull
  private String debtPositionTypeOrgDescription;
  private String debtPositionDescription;
  @NotNull
  private Long debtPositionId;
  private OffsetDateTime paymentDateTime;
  @NotNull
  private Long receiptPersonalDataId;
  @NotNull
  private String pspCompanyName;
  @NotNull
  private String iud;
  private String iur;

}
