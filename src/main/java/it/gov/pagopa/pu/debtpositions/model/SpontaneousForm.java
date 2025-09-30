package it.gov.pagopa.pu.debtpositions.model;

import it.gov.pagopa.pu.debtpositions.dto.spontaneous.SpontaneousFormStructure;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "spontaneous_form")
@AllArgsConstructor
@NoArgsConstructor
@Data
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode(callSuper = false)
public class SpontaneousForm extends BaseEntity implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "spontaneous_form_generator")
  @SequenceGenerator(name = "spontaneous_form_generator", sequenceName = "spontaneous_form_seq", allocationSize = 1)
  private Long spontaneousFormId;
  @NotNull
  private Long organizationId;
  @NotNull
  private String code;
  @Valid
  @NotNull
  @JdbcTypeCode(SqlTypes.JSON)
  private SpontaneousFormStructure structure;
  @JdbcTypeCode(SqlTypes.JSON)
  private Map<String, Map<String,Map<String,String>>> dictionary;
}
