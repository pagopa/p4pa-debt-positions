package it.gov.pagopa.pu.debtpositions.model;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Stamp {

  private String stampType;
  private String stampHashDocument;
  private String stampProvincialResidence;

}
