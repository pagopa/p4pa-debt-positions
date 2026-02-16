package it.gov.pagopa.pu.debtpositions.model;

import it.gov.pagopa.pu.debtpositions.dto.FullPIIDTO;
import it.gov.pagopa.pu.debtpositions.dto.PIIDTO;
import it.gov.pagopa.pu.debtpositions.mapper.pii.BasePIIMapper;

/**
 * A no PII representation of the entity.<BR />
 * Through the related {@link it.gov.pagopa.pu.debtpositions.repository.BasePIIRepository}, it's PII information will be stored on the related {@link PIIDTO}, of which it will store its identifier.
 * The related {@link BasePIIMapper} will use the related {@link PIIDTO} to build its full representation ({@link FullPIIDTO}.
 */
@SuppressWarnings("unused") // Even if not used here, it's useful in order to relate it with the PIIDTO class which will store its PII data
public interface NoPIIEntity<P extends PIIDTO> {
  void setPersonalDataId(Long personalDataId);
  Long getPersonalDataId();
}
