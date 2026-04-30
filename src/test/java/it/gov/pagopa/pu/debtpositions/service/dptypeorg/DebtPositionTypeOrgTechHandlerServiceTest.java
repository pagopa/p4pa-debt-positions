package it.gov.pagopa.pu.debtpositions.service.dptypeorg;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrg;
import it.gov.pagopa.pu.debtpositions.util.TestUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.jemos.podam.api.PodamFactory;

@ExtendWith(MockitoExtension.class)
class DebtPositionTypeOrgTechHandlerServiceTest {

  @InjectMocks
  DebtPositionTypeOrgTechHandlerService service;

  @Mock
  UnknownDebtPositionTypeOrgRetrieverService unknownDebtPositionTypeOrgRetrieverServiceMock;
  @Mock
  MixedDebtPositionTypeOrgRetrieverService mixedDebtPositionTypeOrgRetrieverServiceMock;

  private final PodamFactory podamFactory = TestUtils.getPodamFactory();

  @Test
  void givenOrganizationIdWhenCreateTechnicalDebtPositionTypeOrgThenReturnNewDPTypeOrg() {
    // given
    Long organizationId = 1L;
    DebtPositionTypeOrg expectedDPTypeOrg = podamFactory.manufacturePojo(DebtPositionTypeOrg.class);
    DebtPositionTypeOrg expectedMixedDPTypeOrg = podamFactory.manufacturePojo(DebtPositionTypeOrg.class);

    Mockito.when(unknownDebtPositionTypeOrgRetrieverServiceMock.getUnknownDebtPositionTypeOrg(organizationId))
      .thenReturn(expectedDPTypeOrg);
    Mockito.when(mixedDebtPositionTypeOrgRetrieverServiceMock.getMixedDebtPositionTypeOrg(organizationId))
      .thenReturn(expectedMixedDPTypeOrg);

    // when
    DebtPositionTypeOrg result = service.createTechnicalDebtPositionTypeOrg(organizationId);

    // then
    assertEquals(expectedDPTypeOrg, result);
    verify(mixedDebtPositionTypeOrgRetrieverServiceMock, times(1)).getMixedDebtPositionTypeOrg(organizationId);
  }
}
