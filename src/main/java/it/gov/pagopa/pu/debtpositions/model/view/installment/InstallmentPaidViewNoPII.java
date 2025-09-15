package it.gov.pagopa.pu.debtpositions.model.view.installment;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serializable;
import java.time.OffsetDateTime;

@Entity
@Table(name = "installment")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
public class InstallmentPaidViewNoPII implements Serializable {

  @Id
  private Long installmentId;
  private String iuf;
  @NotNull
  private String iud;
  @NotNull
  private String noticeNumber;
  @NotNull
  private Long organizationId;
  @NotNull
  private String orgFiscalCode;
  @NotNull
  private String paymentReceiptId;
  private OffsetDateTime paymentDateTime;
  @NotNull
  private String idPsp;
  @NotNull
  private String pspCompanyName;
  @NotNull
  private Long paymentAmountCents;
  @NotNull
  private String creditorReferenceId;
  @NotNull
  private Long amountCents;
  @Min(1)
  @Max(140)
  @NotNull
  private String remittanceInformation;
  @Min(5)
  @Max(140)
  @NotNull
  private String category;
  @NotBlank
  @Min(1)
  @Max(1024)
  private String code;
  @NotNull
  private Integer transferIndex;
  private Long feeCents;
  private String balance;
  @NotNull
  private String companyName;
  @NotNull
  private Long receiptPersonalDataId;
  private String rtFilePath;
  private String iun;
  private OffsetDateTime notificationDate;
  private Long notificationFeeCents;
}
