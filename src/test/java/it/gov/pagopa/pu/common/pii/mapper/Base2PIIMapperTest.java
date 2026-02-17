package it.gov.pagopa.pu.common.pii.mapper;

import it.gov.pagopa.pu.common.pii.citizen.service.PersonalDataService;
import it.gov.pagopa.pu.common.pii.dto.Full2PIIDTO;
import it.gov.pagopa.pu.common.pii.dto.No2PIIDTO;
import it.gov.pagopa.pu.common.pii.dto.PIIDTO;
import it.gov.pagopa.pu.debtpositions.util.TestUtils;
import org.apache.commons.lang3.tuple.Pair;
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
public abstract class Base2PIIMapperTest<F extends Full2PIIDTO<E, P1, P2>, E extends No2PIIDTO<P1, P2>, P1 extends PIIDTO, P2 extends PIIDTO> {

  protected final PodamFactory podamFactory = TestUtils.getPodamFactory();

  @Mock
  protected PersonalDataService personalDataServiceMock;

  private final Class<F> fullPIIDTOClass;
  private final Class<E> noPIIDTOClass;
  private final Class<P1> pii1dtoClass;
  private final Class<P2> pii2dtoClass;

  @SuppressWarnings("unchecked")
  protected Base2PIIMapperTest() {
    this.fullPIIDTOClass = (Class<F>) ResolvableType.forClass(getClass()).getSuperType().getGeneric(0).toClass();
    this.noPIIDTOClass = (Class<E>) ResolvableType.forClass(getClass()).getSuperType().getGeneric(1).toClass();
    this.pii1dtoClass = (Class<P1>) ResolvableType.forClass(getClass()).getSuperType().getGeneric(2).toClass();
    this.pii2dtoClass = (Class<P2>) ResolvableType.forClass(getClass()).getSuperType().getGeneric(3).toClass();
  }

  @AfterEach
  void verifyNotMoreInvocationSuper() {
    Mockito.verifyNoMoreInteractions(
      personalDataServiceMock);
  }

  protected abstract Base2PIIMapper<F, E, P1, P2> getMapper();

  @Test
  void testMapAll() {
    //given
    E noPii1 = podamFactory.manufacturePojo(noPIIDTOClass);
    E noPii2 = podamFactory.manufacturePojo(noPIIDTOClass);
    List<E> noPiiDtos = List.of(noPii1, noPii2);

    P1 pii1Dto1 = podamFactory.manufacturePojo(pii1dtoClass);
    P2 pii2Dto1 = podamFactory.manufacturePojo(pii2dtoClass);

    P1 pii1Dto2 = podamFactory.manufacturePojo(pii1dtoClass);
    P2 pii2Dto2 = podamFactory.manufacturePojo(pii2dtoClass);

    Mockito.when(personalDataServiceMock.get2All(
        Set.of(noPii1.getPersonalDataId(), noPii2.getPersonalDataId()), pii1dtoClass,
        Set.of(noPii1.getPersonalDataId2(), noPii2.getPersonalDataId2()), pii2dtoClass
      ))
      .thenReturn(Pair.of(
        Map.of(
          noPii1.getPersonalDataId(), pii1Dto1,
          noPii2.getPersonalDataId(), pii1Dto2
        ),
        Map.of(
          noPii1.getPersonalDataId2(), pii2Dto1,
          noPii2.getPersonalDataId2(), pii2Dto2
        )
      ));

    Base2PIIMapper<F, E, P1, P2> mapper = Mockito.spy(getMapper());

    F expectedFullDto1 = podamFactory.manufacturePojo(fullPIIDTOClass);
    Mockito.doReturn(expectedFullDto1)
      .when(mapper)
      .map(noPii1, pii1Dto1, pii2Dto1);

    F expectedFullDto2 = podamFactory.manufacturePojo(fullPIIDTOClass);
    Mockito.doReturn(expectedFullDto2)
      .when(mapper)
      .map(noPii2, pii1Dto2, pii2Dto2);

    //when
    List<F> result = mapper.mapAll(noPiiDtos);
    //then
    assertEquals(
      List.of(expectedFullDto1, expectedFullDto2),
      result
    );
  }
}
