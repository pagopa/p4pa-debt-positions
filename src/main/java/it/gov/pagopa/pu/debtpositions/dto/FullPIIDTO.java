package it.gov.pagopa.pu.debtpositions.dto;

import it.gov.pagopa.pu.debtpositions.mapper.pii.BasePIIMapper;
import it.gov.pagopa.pu.debtpositions.model.NoPIIEntity;

/**
 * A full representation of {@link NoPIIEntity} having its PII fields retrieved from the related {@link PIIDTO} object.<BR />
 * If retrieved from the DB and obtained from the related {@link BasePIIMapper}, it will store the original {@link NoPIIEntity} entity, inside which it could be possibile to recover {@link PIIDTO}'s identified ({@link NoPIIEntity#getPersonalDataId()})
 * */
public interface FullPIIDTO <E extends NoPIIEntity<P>, P extends PIIDTO> {
  E getNoPII();
  void setNoPII(E noPII);
}
