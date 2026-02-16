package it.gov.pagopa.pu.common.pii.dto;

import it.gov.pagopa.pu.common.pii.mapper.BaseEntityPIIMapper;
import it.gov.pagopa.pu.common.pii.model.NoPIIEntity;
import it.gov.pagopa.pu.common.pii.repository.BasePIIRepository;

/**
 * It will contain the PII related to a {@link NoPIIEntity} entity.<BR/>
 * It will be stored on a separate DB through the related {@link BasePIIRepository}, which will set its identifier on the {@link NoPIIEntity} personalDataId's field.<BR/>
 * The related {@link BaseEntityPIIMapper} will use it to build the full representation of the related {@link NoPIIEntity} ({@link FullEntityPIIDTO}.
 */
public interface PIIDTO {
}
