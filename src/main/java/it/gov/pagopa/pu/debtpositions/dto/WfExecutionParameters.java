package it.gov.pagopa.pu.debtpositions.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class WfExecutionParameters {
  private boolean massive;
  private boolean partialChange;
  private String executionConfig;
}
