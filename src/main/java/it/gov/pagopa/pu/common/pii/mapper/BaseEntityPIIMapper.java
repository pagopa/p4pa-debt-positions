package it.gov.pagopa.pu.common.pii.mapper;

import it.gov.pagopa.pu.common.pii.citizen.service.PersonalDataService;
import it.gov.pagopa.pu.common.pii.dto.FullEntityPIIDTO;
import it.gov.pagopa.pu.common.pii.dto.PIIDTO;
import it.gov.pagopa.pu.common.pii.model.NoPIIEntity;
import org.springframework.data.util.Pair;

/** A mapper to scompose/recompose {@link FullEntityPIIDTO} into/from {@link NoPIIEntity} and {@link PIIDTO}*/
public abstract class BaseEntityPIIMapper <F extends FullEntityPIIDTO<E, P>, E extends NoPIIEntity<P>, P extends PIIDTO> extends BasePIIMapper<F, E, P> {

  protected BaseEntityPIIMapper(Class<P> piiDtoClass, PersonalDataService personalDataService) {
    super(piiDtoClass, personalDataService);
  }

  /**
   * It will scompose {@link FullEntityPIIDTO} into {@link NoPIIEntity} and {@link PIIDTO}.<BR/>
   * If its {@link FullEntityPIIDTO#getNoPII()} method return a not null object (if it was retrieved from the db), it will also set the NoPIIEntity' personalDataId field
   */
  public final Pair<E, P> map(F fullDTO){
    E noPii = extractNoPiiEntity(fullDTO);
    if (fullDTO.getNoPII() != null) {
      noPii.setPersonalDataId(fullDTO.getNoPII().getPersonalDataId());
    }

    P pii = extractPiiDto(fullDTO);
    return Pair.of(noPii, pii);
  }

  /** It will return no PII data from the input {@link FullEntityPIIDTO}*/
  protected abstract E extractNoPiiEntity(F fullDTO);
  /** It will return PII data from the input {@link FullEntityPIIDTO}*/
  protected abstract P extractPiiDto(F fullDTO);

}
