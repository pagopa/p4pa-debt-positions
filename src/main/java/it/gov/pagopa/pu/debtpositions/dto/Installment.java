package it.gov.pagopa.pu.debtpositions.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentStatus;
import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;
import it.gov.pagopa.pu.debtpositions.model.InstallmentSyncStatus;
import it.gov.pagopa.pu.debtpositions.model.Transfer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Installment implements FullPIIDTO<InstallmentNoPII, InstallmentPIIDTO>, BaseInstallment {

  private Long installmentId;
  private Long paymentOptionId;
  private InstallmentStatus status;
  private InstallmentSyncStatus syncStatus;
  private String iupdPagopa;
  private String iud;
  private String iuv;
  private String iur;
  private String iuf;
  private String nav;
  private String iun;
  private LocalDate dueDate;
  private Long notificationFeeCents;
  private Long amountCents;
  private String remittanceInformation;
  private String balance;
  private String legacyPaymentMetadata;
  private Person debtor;
  private List<Transfer> transfers;
  private OffsetDateTime notificationDate;
  private Long ingestionFlowFileId;
  private Long ingestionFlowFileLineNumber;
  private Long receiptId;
  private LocalDateTime creationDate;
  private LocalDateTime updateDate;
  private String updateOperatorExternalId;
  private String updateTraceId;

  @JsonIgnore
  private InstallmentNoPII noPII;
}
