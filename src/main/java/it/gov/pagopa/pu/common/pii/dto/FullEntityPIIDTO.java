package it.gov.pagopa.pu.common.pii.dto;

import it.gov.pagopa.pu.common.pii.model.NoPIIEntity;

/**
 * A specialization of {@link FullPIIDTO} to be used for db tables
 * */
public interface FullEntityPIIDTO<E extends NoPIIEntity<P>, P extends PIIDTO> extends FullPIIDTO<E, P> {
  E getNoPII();
  void setNoPII(E noPII);
}
