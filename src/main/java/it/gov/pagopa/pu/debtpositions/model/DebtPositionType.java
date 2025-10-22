package it.gov.pagopa.pu.debtpositions.model;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import it.gov.pagopa.pu.debtpositions.model.validator.TaxonomyCodeConstraint;
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
@TaxonomyCodeConstraint
public class DebtPositionType extends BaseEntity implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "debt_position_type_generator")
  @SequenceGenerator(name = "debt_position_type_generator", sequenceName = "debt_position_type_seq", allocationSize = 1)
  private Long debtPositionTypeId;
  @NotNull
  @JsonSetter(nulls = Nulls.SKIP)
  private Long brokerId;
  @NotNull
  @JsonSetter(nulls = Nulls.SKIP)
  private String code;
  @NotNull
  @JsonSetter(nulls = Nulls.SKIP)
  private String description;
  @NotNull
  @JsonSetter(nulls = Nulls.SKIP)
  private String orgType;
  @NotNull
  @JsonSetter(nulls = Nulls.SKIP)
  private String macroArea;
  @NotNull
  @JsonSetter(nulls = Nulls.SKIP)
  private String serviceType;
  @NotNull
  @JsonSetter(nulls = Nulls.SKIP)
  private String collectingReason;
  @NotNull
  @JsonSetter(nulls = Nulls.SKIP)
  private String taxonomyCode;
  private boolean flagAnonymousFiscalCode;
  private boolean flagMandatoryDueDate;
  private boolean flagNotifyIo;
  private String ioTemplateMessage;
  private String ioTemplateSubject;
}
