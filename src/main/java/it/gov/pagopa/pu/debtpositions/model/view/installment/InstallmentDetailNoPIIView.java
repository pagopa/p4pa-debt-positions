package it.gov.pagopa.pu.debtpositions.model.view.installment;

import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentStatus;
import jakarta.persistence.*;
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
  private Long paymentOptionId;
  @Enumerated(EnumType.STRING)
  private InstallmentStatus status;
  private String iuv;
  private Long amountCents;
  private LocalDate dueDate;
  private Long personalDataId;
  private String debtPositionTypeOrgDescription;
  private String debtPositionDescription;
  private Long debtPositionId;
  private OffsetDateTime paymentDateTime;
  private Long receiptPersonalDataId;
  private String pspCompanyName;
  private String iud;
  private String iur;

}
