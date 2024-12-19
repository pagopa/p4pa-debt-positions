package it.gov.pagopa.pu.debtpositions.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Entity
@Table(name = "debt_position_type")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class DebtPositionType {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "debt_position_type_generator")
  @SequenceGenerator(name = "debt_position_type_generator", sequenceName = "debt_position_type_seq", allocationSize = 1)
  private Long debtPositionTypeId;
  private Long brokerId;
  private String code;
  private String description;
  private String orgType;
  private String macroArea;
  private String serviceType;
  private String collectingReason;
  private String taxonomyCode;
  private boolean flagAnonymousFiscalCode;
  private boolean flagMandatoryDueDate;
  private boolean flagNotifyIo;
  private String ioTemplateMessage;
  private OffsetDateTime creationDate;
  private OffsetDateTime updateDate;
  private Long updateOperatorId;

}
