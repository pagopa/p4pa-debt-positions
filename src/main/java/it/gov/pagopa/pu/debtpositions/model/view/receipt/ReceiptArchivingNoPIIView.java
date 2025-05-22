package it.gov.pagopa.pu.debtpositions.model.view.receipt;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serializable;
import java.time.OffsetDateTime;

@Entity
@Table(name = "receipt")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
public class ReceiptArchivingNoPIIView implements Serializable {

  @Id
  private Long receiptId;
  @NotNull
  private String paymentReceiptId;
  private OffsetDateTime paymentDateTime;
  @NotNull
  private String creditorReferenceId;
  private String iuv;
  @NotNull
  private String remittanceInformation;
  @NotNull
  private Long organizationId;
  @NotNull
  private String orgFiscalCode;
  @NotNull
  private Long receiptPersonalDataId;
  private String rtFilePath;
}
