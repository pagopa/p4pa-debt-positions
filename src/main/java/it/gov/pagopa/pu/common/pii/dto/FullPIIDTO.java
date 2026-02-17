package it.gov.pagopa.pu.common.pii.dto;

import it.gov.pagopa.pu.common.pii.mapper.BaseEntityPIIMapper;
import it.gov.pagopa.pu.common.pii.model.NoPIIEntity;

/**
 * A full representation of {@link NoPIIEntity} having its PII fields retrieved from the related {@link PIIDTO} object.<BR />
 * If retrieved from the DB and obtained from the related {@link BaseEntityPIIMapper}, it will store the original {@link NoPIIEntity} entity, inside which it could be possibile to recover {@link PIIDTO}'s identified ({@link NoPIIEntity#getPersonalDataId()})
 * */
@SuppressWarnings("unused") // Even if not used here, it's useful in order to relate it with the NoPIIDTO and PIIDTO
public interface FullPIIDTO<E extends NoPIIDTO<P>, P extends PIIDTO> {
}
