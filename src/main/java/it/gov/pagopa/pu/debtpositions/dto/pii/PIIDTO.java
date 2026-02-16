package it.gov.pagopa.pu.debtpositions.dto.pii;

import it.gov.pagopa.pu.debtpositions.mapper.pii.BasePIIMapper;
import it.gov.pagopa.pu.debtpositions.repository.pii.BasePIIRepository;

/**
 * It will contain the PII related to a {@link it.gov.pagopa.pu.debtpositions.model.NoPIIEntity} entity.<BR/>
 * It will be stored on a separate DB through the related {@link BasePIIRepository}, which will set its identifier on the {@link it.gov.pagopa.pu.debtpositions.model.NoPIIEntity} personalDataId's field.<BR/>
 * The related {@link BasePIIMapper} will use it to build the full representation of the related {@link it.gov.pagopa.pu.debtpositions.model.NoPIIEntity} ({@link FullPIIDTO}.
 */
public interface PIIDTO {
}
