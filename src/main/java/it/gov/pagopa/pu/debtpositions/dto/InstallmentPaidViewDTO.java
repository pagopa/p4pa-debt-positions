package it.gov.pagopa.pu.debtpositions.dto;

import it.gov.pagopa.pu.debtpositions.dto.generated.PersonDTO;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class InstallmentPaidViewDTO {

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
  private PersonDTO debtor;
  private PersonDTO payer;
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
  private String rtFilePath;

}
