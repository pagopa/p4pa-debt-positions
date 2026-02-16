package it.gov.pagopa.pu.common.pii.mapper;

import it.gov.pagopa.pu.common.pii.citizen.service.PersonalDataService;
import it.gov.pagopa.pu.common.pii.dto.Full2PIIDTO;
import it.gov.pagopa.pu.common.pii.dto.No2PIIDTO;
import it.gov.pagopa.pu.common.pii.dto.PIIDTO;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** A mapper to compose {@link Full2PIIDTO} from {@link No2PIIDTO} and 2 kind of {@link PIIDTO} */
public abstract class Base2PIIMapper<F extends Full2PIIDTO<E, P1, P2>, E extends No2PIIDTO<P1, P2>, P1 extends PIIDTO, P2 extends PIIDTO> {
  protected final Class<P1> pii1DtoClass;
  protected final Class<P2> pii2DtoClass;
  protected final PersonalDataService personalDataService;

  protected Base2PIIMapper(Class<P1> pii1DtoClass, Class<P2> pii2DtoClass, PersonalDataService personalDataService) {
    this.pii1DtoClass = pii1DtoClass;
    this.pii2DtoClass = pii2DtoClass;
    this.personalDataService = personalDataService;
  }

  /** Given a {@link No2PIIDTO}, it will build the {@link Full2PIIDTO} fetching the {@link PIIDTO} using its NoPIIEntity personalDataId field */
  public abstract F map(E noPii);
  /** Given a {@link No2PIIDTO} and related {@link PIIDTO}s, it will build the {@link Full2PIIDTO} */
  protected abstract F map(E noPii, P1 pii1, P2 pii2);

  /** Given a list {@link No2PIIDTO}, it will map each element into {@link Full2PIIDTO} fetching as first the entire {@link PIIDTO}s using their NoPIIEntity personalDataId field using just one access on {@link PersonalDataService} */
  public List<F> mapAll(List<E> noPiiDtos) {
    Set<Long> personalDataIds1 = HashSet.newHashSet(noPiiDtos.size());
    Set<Long> personalDataIds2 = HashSet.newHashSet(noPiiDtos.size());

    noPiiDtos.forEach(noPii2 -> {
      if (noPii2.getPersonalDataId() != null) {
        personalDataIds1.add(noPii2.getPersonalDataId());
      }
      if (noPii2.getPersonalDataId2() != null) {
        personalDataIds2.add(noPii2.getPersonalDataId2());
      }
    });

    Pair<Map<Long, P1>, Map<Long, P2>> piiId2dtoPair = personalDataService.get2All(
      personalDataIds1, pii1DtoClass,
      personalDataIds2, pii2DtoClass
    );

    Map<Long, P1> piiId1Map = piiId2dtoPair.getLeft();
    Map<Long, P2> piiId2Map = piiId2dtoPair.getRight();

    return noPiiDtos.stream()
      .map(noPii -> map(
          noPii,
          piiId1Map.get(noPii.getPersonalDataId()),
          piiId2Map.get(noPii.getPersonalDataId2())
        )
      )
      .toList();
  }
}
