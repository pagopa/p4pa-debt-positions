package it.gov.pagopa.pu.debtpositions.model;

import io.swagger.v3.oas.annotations.media.Schema;
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
  /**
  * Dictionary of localized messages for spontaneous form fields.
  * The map has three levels of nesting:
  * <ul>
  *   <li><b>First level</b>: language code (e.g. "EN").</li>
  *   <li><b>Second level</b>: field name (e.g. "payment_field").</li>
  *   <li><b>Third level</b>: attribute name and message for that field (e.g. "label", "error", "help").</li>
  * </ul>
  * Example:
  * <pre>
  * {
  *   "EN": {
  *     "payment_field": {
  *       "label": "Payment description",
  *       "error": "Specify the payment description",
  *       "help":  "Specify the payment description"
  *     }
  *   }
  * }
  * </pre>
  */
  @Schema(
      description = """
            Dictionary of localized messages for spontaneous form fields.
            The map has three levels of nesting:
            1. First level: language code (e.g. "EN")
            2. Second level: field name (e.g. "payment_field")
            3. Third level: attribute name and message for that field (e.g. "label", "error", "help")

            Example:
            {
              "EN": {
                "payment_field": {
                  "label": "Payment description",
                  "error": "Specify the payment description",
                  "help": "Specify the payment description"
                }
              }
            }
            """
  )
  @JdbcTypeCode(SqlTypes.JSON)
  private Map<String, Map<String,Map<String,String>>> dictionary;
}
