package it.gov.pagopa.pu.debtpositions.dto;

import it.gov.pagopa.pu.debtpositions.model.NoPIIEntity;

public interface FullPIIDTO <E extends NoPIIEntity<P>, P extends PIIDTO> {
  E getNoPII();
  void setNoPII(E noPII);
}
