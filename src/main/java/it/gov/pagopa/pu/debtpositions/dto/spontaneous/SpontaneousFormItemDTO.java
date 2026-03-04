package it.gov.pagopa.pu.debtpositions.dto.spontaneous;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SpontaneousFormItemDTO implements Serializable {
  private String name;
  private String key;
}
