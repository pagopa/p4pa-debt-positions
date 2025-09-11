package it.gov.pagopa.pu.debtpositions.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class MixedDpAdditionalData {
  private Integer transferIndex;
  private String iud;
  private String legacyPaymentMetadata;
  private String balance;
}
