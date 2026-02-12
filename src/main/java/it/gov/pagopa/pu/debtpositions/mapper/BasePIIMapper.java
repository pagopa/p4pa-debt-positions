package it.gov.pagopa.pu.debtpositions.mapper;

import it.gov.pagopa.pu.debtpositions.citizen.service.PersonalDataService;
import it.gov.pagopa.pu.debtpositions.dto.FullPIIDTO;
import it.gov.pagopa.pu.debtpositions.dto.PIIDTO;
import it.gov.pagopa.pu.debtpositions.model.NoPIIEntity;
import org.springframework.data.util.Pair;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/** A mapper to scompose/recompose {@link FullPIIDTO} into/from {@link NoPIIEntity} and {@link PIIDTO}*/
public abstract class BasePIIMapper <F extends FullPIIDTO<E, P>, E extends NoPIIEntity<P>, P extends PIIDTO> {

  private final Class<P> piiDtoClass;
  protected final PersonalDataService personalDataService;

  protected BasePIIMapper(Class<P> piiDtoClass, PersonalDataService personalDataService) {
    this.piiDtoClass = piiDtoClass;
    this.personalDataService = personalDataService;
  }

  /**
   * It will scompose {@link FullPIIDTO} into {@link NoPIIEntity} and {@link PIIDTO}.<BR/>
   * If its {@link FullPIIDTO#getNoPII()} method return a not null object (if it was retrieved from the db), it will also set the NoPIIEntity' personalDataId field
   */
  public final Pair<E, P> map(F fullDTO){
    E noPii = extractNoPiiEntity(fullDTO);
    if (fullDTO.getNoPII() != null) {
      noPii.setPersonalDataId(fullDTO.getNoPII().getPersonalDataId());
    }

    P pii = extractPiiDto(fullDTO);
    return Pair.of(noPii, pii);
  }

  /** It will return no PII data from the input {@link FullPIIDTO}*/
  protected abstract E extractNoPiiEntity(F fullDTO);
  /** It will return PII data from the input {@link FullPIIDTO}*/
  protected abstract P extractPiiDto(F fullDTO);

  /** Given a {@link NoPIIEntity}, it will build the {@link FullPIIDTO} fetching the {@link PIIDTO} using its NoPIIEntity personalDataId field */
  public abstract F map(E noPii);
  /** Given a {@link NoPIIEntity} and related {@link PIIDTO}, it will build the {@link FullPIIDTO} */
  protected abstract F map(E noPii, P pii);

  /** Given a list {@link NoPIIEntity}, it will map each element into {@link FullPIIDTO} fetching as first the entire {@link PIIDTO} using their NoPIIEntity personalDataId field using just one access on {@link PersonalDataService} */
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
