package it.gov.pagopa.pu.common.pii.dto;

/**
 * A specialization of {@link FullPIIDTO} to handle view having 2 source for PIIs
 * */
public interface Full2PIIDTO<E extends No2PIIDTO<P1, P2>, P1 extends PIIDTO, P2 extends PIIDTO> extends FullPIIDTO<E, P1> {
}
