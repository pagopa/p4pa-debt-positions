package it.gov.pagopa.pu.debtpositions.dto.spontaneous;

import jakarta.validation.Valid;
import java.io.Serializable;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SpontaneousFormStructure implements Serializable {
  @Valid
  private List<FieldBean> fields;
  private String amount;
  private boolean amountInForm;
}
