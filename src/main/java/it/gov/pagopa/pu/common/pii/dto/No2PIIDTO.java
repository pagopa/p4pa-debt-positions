package it.gov.pagopa.pu.common.pii.dto;

/** A specialization of {@link NoPIIDTO} for view having 2 personalDataId */
@SuppressWarnings("unused") // Even if not used here, it's useful in order to relate it with the PIIDTO class which will store its PII data
public interface No2PIIDTO<P1 extends PIIDTO, P2 extends PIIDTO> extends NoPIIDTO<P1> {
  Long getPersonalDataId2();
}
