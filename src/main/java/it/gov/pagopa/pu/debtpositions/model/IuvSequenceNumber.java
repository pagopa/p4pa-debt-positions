package it.gov.pagopa.pu.debtpositions.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
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
  @NotNull
  private Long id;
  @NotNull
  private Long organizationId;
  @NotNull
  private Long sequenceNumber;
}
