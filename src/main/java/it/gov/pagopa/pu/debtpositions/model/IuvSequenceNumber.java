package it.gov.pagopa.pu.debtpositions.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Entity
@Table(name = "iuv_sequence_number")
@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
public class IuvSequenceNumber extends BaseEntity implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "iuv_sequence_number_generator")
  @SequenceGenerator(name = "iuv_sequence_number_generator", sequenceName = "iuv_sequence_number_seq", allocationSize = 1)
  private Long id;
  private Long organizationId;
  private Long sequenceNumber;
}
