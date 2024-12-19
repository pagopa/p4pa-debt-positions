package it.gov.pagopa.pu.debtpositions.citizen.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PersonalData {
  @Id
  private Long id;
  private String type;
  private byte[] data;
}
