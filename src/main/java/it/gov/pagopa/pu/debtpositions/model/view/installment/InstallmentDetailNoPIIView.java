package it.gov.pagopa.pu.debtpositions.model.view.installment;

import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
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
  private InstallmentStatus status;
  private String iuv;
  private Long amountCents;
  private OffsetDateTime dueDate;
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
