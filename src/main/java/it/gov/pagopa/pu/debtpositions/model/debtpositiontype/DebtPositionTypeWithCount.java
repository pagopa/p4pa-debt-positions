package it.gov.pagopa.pu.debtpositions.model.debtpositiontype;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import it.gov.pagopa.pu.debtpositions.config.json.LocalDateTimeToOffsetDateTimeSerializer;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Formula;

@Entity
@Table(name = "debt_position_type")
@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
public class DebtPositionTypeWithCount implements Serializable {

  @Id
  private Long debtPositionTypeId;
  private String code;
  private String description;
  @JsonSerialize(using = LocalDateTimeToOffsetDateTimeSerializer.class)
  private LocalDateTime updateDate;
  @Formula("(SELECT COUNT(*) "
    + "FROM debt_position_type_org org "
    + "WHERE debt_position_type_id = org.debt_position_type_id)")
  private Integer activeOrganizations;
  private Long brokerId;
}
