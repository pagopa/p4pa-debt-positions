package it.gov.pagopa.pu.debtpositions.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Entity
@Table(name = "debt_position_type")
@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
public class DebtPositionType extends BaseEntity implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "debt_position_type_generator")
  @SequenceGenerator(name = "debt_position_type_generator", sequenceName = "debt_position_type_seq", allocationSize = 1)
  @NotNull
  private Long debtPositionTypeId;
  @NotNull
  private Long brokerId;
  @NotNull
  private String code;
  @NotNull
  private String description;
  @NotNull
  private String orgType;
  @NotNull
  private String macroArea;
  @NotNull
  private String serviceType;
  @NotNull
  private String collectingReason;
  @NotNull
  private String taxonomyCode;
  private boolean flagAnonymousFiscalCode;
  private boolean flagMandatoryDueDate;
  private boolean flagNotifyIo;
  private String ioTemplateMessage;
}
