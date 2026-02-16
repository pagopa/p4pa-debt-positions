package it.gov.pagopa.pu.debtpositions.dto.pii;

import it.gov.pagopa.pu.debtpositions.mapper.pii.BaseEntityPIIMapper;
import it.gov.pagopa.pu.debtpositions.model.NoPIIEntity;

/**
 * A full representation of {@link NoPIIEntity} having its PII fields retrieved from the related {@link PIIDTO} object.<BR />
 * If retrieved from the DB and obtained from the related {@link BaseEntityPIIMapper}, it will store the original {@link NoPIIEntity} entity, inside which it could be possibile to recover {@link PIIDTO}'s identified ({@link NoPIIEntity#getPersonalDataId()})
 * */
public interface FullEntityPIIDTO<E extends NoPIIEntity<P>, P extends PIIDTO> extends FullPIIDTO<E, P> {
  E getNoPII();
  void setNoPII(E noPII);
}
