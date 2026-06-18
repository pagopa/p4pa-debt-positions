package it.gov.pagopa.pu.debtpositions.dto.spontaneous;

import com.fasterxml.jackson.annotation.JsonAlias;
import it.gov.pagopa.pu.debtpositions.config.json.JsonUnknownPropertiesNotAllowed;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonUnknownPropertiesNotAllowed
public class SpontaneousFormStructure implements Serializable {
  @JsonAlias("fieldBeans")
  @Valid
  private List<SpontaneousFormField> fields;
  @JsonAlias("campoTotaleInclusoInXSD")
  private String amountFieldName;
  private List<String> summaryFields;
  private List<SpontaneousFormItemDTO> submitFields;
}
