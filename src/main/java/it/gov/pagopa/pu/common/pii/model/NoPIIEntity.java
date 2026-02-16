package it.gov.pagopa.pu.common.pii.model;

import it.gov.pagopa.pu.common.pii.dto.FullEntityPIIDTO;
import it.gov.pagopa.pu.common.pii.dto.NoPIIDTO;
import it.gov.pagopa.pu.common.pii.dto.PIIDTO;
import it.gov.pagopa.pu.common.pii.mapper.BaseEntityPIIMapper;
import it.gov.pagopa.pu.common.pii.repository.BasePIIRepository;

/**
 * A specialization of {@link NoPIIDTO} to handle also the store operations.<BR />
 * Through the related {@link BasePIIRepository}, it's PII information will be stored on the related {@link PIIDTO}, of which it will store its identifier.
 * The related {@link BaseEntityPIIMapper} will use the related {@link PIIDTO} to build its full representation ({@link FullEntityPIIDTO}.
 */
public interface NoPIIEntity<P extends PIIDTO> extends NoPIIDTO<P> {
  void setPersonalDataId(Long personalDataId);
}
