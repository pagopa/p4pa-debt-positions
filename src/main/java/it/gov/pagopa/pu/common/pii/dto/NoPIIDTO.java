package it.gov.pagopa.pu.common.pii.dto;

import it.gov.pagopa.pu.common.pii.mapper.BasePIIMapper;

/**
 * A no PII representation of the entity.<BR />
 * The related {@link BasePIIMapper} will use the related {@link PIIDTO} to build its full representation ({@link FullPIIDTO}.
 */
@SuppressWarnings("unused") // Even if not used here, it's useful in order to relate it with the PIIDTO class which will store its PII data
public interface NoPIIDTO<P extends PIIDTO> {
  Long getPersonalDataId();
}
