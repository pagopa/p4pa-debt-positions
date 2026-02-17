package it.gov.pagopa.pu.common.pii.mapper;

import it.gov.pagopa.pu.common.pii.citizen.service.PersonalDataService;
import it.gov.pagopa.pu.common.pii.dto.FullPIIDTO;
import it.gov.pagopa.pu.common.pii.dto.NoPIIDTO;
import it.gov.pagopa.pu.common.pii.dto.PIIDTO;
import it.gov.pagopa.pu.debtpositions.util.TestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ResolvableType;
import uk.co.jemos.podam.api.PodamFactory;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public abstract class BasePIIMapperTest<F extends FullPIIDTO<E, P>, E extends NoPIIDTO<P>, P extends PIIDTO> {

  protected final PodamFactory podamFactory = TestUtils.getPodamFactory();

  @Mock
  protected PersonalDataService personalDataServiceMock;

  private final  Class<F> fullPIIDTOClass;
  private final Class<E> noPIIDTOClass;
  private final Class<P> piidtoClass;

  @SuppressWarnings("unchecked")
  protected BasePIIMapperTest() {
    this.fullPIIDTOClass = (Class<F>) ResolvableType.forClass(getClass()).getSuperType().getGeneric(0).toClass();
    this.noPIIDTOClass = (Class<E>) ResolvableType.forClass(getClass()).getSuperType().getGeneric(1).toClass();
    this.piidtoClass = (Class<P>) ResolvableType.forClass(getClass()).getSuperType().getGeneric(2).toClass();
  }

  @AfterEach
  void verifyNotMoreInvocationSuper() {
    Mockito.verifyNoMoreInteractions(
      personalDataServiceMock);
  }

  protected abstract BasePIIMapper<F, E, P> getMapper();

  @Test
  void testMapAll() {
    //given
    E noPii1 = podamFactory.manufacturePojo(noPIIDTOClass);
    E noPii2 = podamFactory.manufacturePojo(noPIIDTOClass);
    List<E> noPiiDtos = List.of(noPii1, noPii2);

    P piiDto1 = podamFactory.manufacturePojo(piidtoClass);
    P piiDto2 = podamFactory.manufacturePojo(piidtoClass);
    Mockito.when(personalDataServiceMock.getAll(Set.of(noPii1.getPersonalDataId(), noPii2.getPersonalDataId()), piidtoClass))
      .thenReturn(Map.of(
        noPii1.getPersonalDataId(), piiDto1,
        noPii2.getPersonalDataId(), piiDto2
      ));

    BasePIIMapper<F, E, P> mapper = Mockito.spy(getMapper());

    F expectedFullDto1 = podamFactory.manufacturePojo(fullPIIDTOClass);
    Mockito.doReturn(expectedFullDto1)
      .when(mapper)
      .map(noPii1, piiDto1);

    F expectedFullDto2 = podamFactory.manufacturePojo(fullPIIDTOClass);
    Mockito.doReturn(expectedFullDto2)
      .when(mapper)
      .map(noPii2, piiDto2);

    //when
    List<F> result = mapper.mapAll(noPiiDtos);
    //then
    assertEquals(
      List.of(expectedFullDto1, expectedFullDto2),
      result
    );
  }
}
