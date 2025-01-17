package it.gov.pagopa.pu.debtpositions.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.OffsetDateTime;

@Entity
@Table(name = "taxonomy")
@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
public class Taxonomy extends BaseEntity implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "taxonomy_generator")
  @SequenceGenerator(name = "taxonomy_generator", sequenceName = "taxonomy_seq", allocationSize = 1)
  private Long taxonomyId;
  private String organizationType;
  private String organizationTypeDescription;
  private String macroAreaCode;
  private String macroAreaName;
  private String macroAreaDescription;
  private String serviceTypeCode;
  private String serviceType;
  private String serviceTypeDescription;
  private String collectionReason;
  private OffsetDateTime startDateValidity;
  private OffsetDateTime endDateOfValidity;
  private String taxonomyCode;

}
