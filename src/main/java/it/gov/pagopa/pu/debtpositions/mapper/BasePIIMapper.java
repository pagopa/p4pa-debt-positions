package it.gov.pagopa.pu.debtpositions.mapper;

import it.gov.pagopa.pu.debtpositions.dto.FullPIIDTO;
import it.gov.pagopa.pu.debtpositions.dto.PIIDTO;
import it.gov.pagopa.pu.debtpositions.model.NoPIIEntity;
import org.springframework.data.util.Pair;

public abstract class BasePIIMapper <F extends FullPIIDTO<E, P>, E extends NoPIIEntity<P>, P extends PIIDTO> {

  public final Pair<E, P> map(F fullDTO){
    E noPii = extractNoPiiEntity(fullDTO);
    if (fullDTO.getNoPII() != null) {
      noPii.setPersonalDataId(fullDTO.getNoPII().getPersonalDataId());
    }

    P pii = extractPiiDto(fullDTO);
    return Pair.of(noPii, pii);
  }

  protected abstract E extractNoPiiEntity(F fullDTO);
  protected abstract P extractPiiDto(F fullDTO);

  public abstract F map(E noPii);
}
