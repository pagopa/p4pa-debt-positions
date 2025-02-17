package it.gov.pagopa.pu.debtpositions.model.view.receipt;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "receipt")
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ReceiptDetailNoPIIView implements Serializable {
  @Id
  private Long receiptId;
  private String iuv;
  private Long paymentAmountCents;
  private String remittanceInformation;
  private String debtPositionDescription;
  private Long debtorPersonalDataId;
  private OffsetDateTime paymentDateTime;
  private String pspCompanyName;
  private String iud;
  private String iur;
}
