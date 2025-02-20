package it.gov.pagopa.pu.debtpositions.model.view.installment;

import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentStatus;
import jakarta.persistence.*;
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
  private Long paymentOptionId;
  private String iuv;
  @Enumerated(EnumType.STRING)
  private InstallmentStatus status;
  private OffsetDateTime dueDate;
  private Long amountCents;
  private String remittanceInformation;
  private byte[] debtorFiscalCodeHash;
  private String debtPositionTypeOrgDescription;

}
