package it.gov.pagopa.pu.debtpositions.model;

import it.gov.pagopa.pu.debtpositions.dto.PIIDTO;

public interface NoPIIEntity<P extends PIIDTO> {
  void setPersonalDataId(Long personalDataId);
  Long getPersonalDataId();
}
