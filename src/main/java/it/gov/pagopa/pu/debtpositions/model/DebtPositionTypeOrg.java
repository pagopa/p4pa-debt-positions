package it.gov.pagopa.pu.debtpositions.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "debt_position_type_org")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class DebtPositionTypeOrg {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "debt_position_type_org_generator")
  @SequenceGenerator(name = "debt_position_type_org_generator", sequenceName = "debt_position_type_org_seq", allocationSize = 1)
  private Long debtPositionTypeOrgId;
  private Long debtPositionTypeId;
  private Long organizationId;
  private String balance;
  private String code;
  private String description;
  private String iban;
  private String postalIban;
  private String postalAccountCode;
  private String holderPostalCC;
  private String orgSector;
  private String xsdDefinitionRef;
  private Long amountCents;
  private String externalPaymentUrl;
  private boolean flagAnonymousFiscalCode;
  private boolean flagMandatoryDueDate;
  private boolean flagSpontaneous;
  private boolean flagNotifyIO;
  private boolean flagActive;
  private boolean flagNotifyOutcomePush;
  private Long notifyOutcomePushOrgSilServiceId;
  private boolean flagAmountActualization;
  private Long amountActualizationOrgSilServiceId;
  private LocalDateTime creationDate;
  private LocalDateTime updateDate;
  private Long operatorExternalUserId;

}
