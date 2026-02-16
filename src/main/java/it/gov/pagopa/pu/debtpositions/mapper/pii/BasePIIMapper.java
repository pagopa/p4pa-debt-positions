package it.gov.pagopa.pu.debtpositions.mapper.pii;

import it.gov.pagopa.pu.debtpositions.citizen.service.PersonalDataService;
import it.gov.pagopa.pu.debtpositions.dto.pii.FullEntityPIIDTO;
import it.gov.pagopa.pu.debtpositions.dto.pii.FullPIIDTO;
import it.gov.pagopa.pu.debtpositions.dto.pii.PIIDTO;
import it.gov.pagopa.pu.debtpositions.model.NoPIIEntity;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/** A mapper to compose {@link FullPIIDTO} from {@link NoPIIEntity} and {@link PIIDTO}*/
public abstract class BasePIIMapper<F extends FullPIIDTO<E, P>, E extends NoPIIEntity<P>, P extends PIIDTO> {

  protected final Class<P> piiDtoClass;
  protected final PersonalDataService personalDataService;

  protected BasePIIMapper(Class<P> piiDtoClass, PersonalDataService personalDataService) {
    this.piiDtoClass = piiDtoClass;
    this.personalDataService = personalDataService;
  }

  /** Given a {@link NoPIIEntity}, it will build the {@link FullEntityPIIDTO} fetching the {@link PIIDTO} using its NoPIIEntity personalDataId field */
  public abstract F map(E noPii);
  /** Given a {@link NoPIIEntity} and related {@link PIIDTO}, it will build the {@link FullEntityPIIDTO} */
  protected abstract F map(E noPii, P pii);

  /** Given a list {@link NoPIIEntity}, it will map each element into {@link FullEntityPIIDTO} fetching as first the entire {@link PIIDTO} using their NoPIIEntity personalDataId field using just one access on {@link PersonalDataService} */
  public List<F> mapAll(List<E> noPiiDtos) {
    Set<Long> personalDataIds = noPiiDtos.stream()
      .map(NoPIIEntity::getPersonalDataId)
      .filter(Objects::nonNull)
      .collect(Collectors.toSet());

    Map<Long, P> piiId2dto = personalDataService.getAll(personalDataIds, piiDtoClass);

    return noPiiDtos.stream()
      .map(noPii -> map(noPii, piiId2dto.get(noPii.getPersonalDataId())))
      .toList();
  }
}
