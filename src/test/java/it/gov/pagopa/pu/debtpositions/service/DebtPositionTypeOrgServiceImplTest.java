package it.gov.pagopa.pu.debtpositions.service;

import it.gov.pagopa.pu.debtpositions.dto.generated.IONotificationDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.IONotificationOperationType;
import it.gov.pagopa.pu.debtpositions.exception.custom.NotFoundException;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrg;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionTypeOrgRepository;
import it.gov.pagopa.pu.debtpositions.util.TestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.jemos.podam.api.PodamFactory;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class DebtPositionTypeOrgServiceImplTest {

  @Mock
  private DebtPositionTypeOrgRepository debtPositionTypeOrgRepository;

  private DebtPositionTypeOrgService debtPositionTypeOrgService;

  private final PodamFactory podamFactory = TestUtils.getPodamFactory();

  @BeforeEach
  void setUp() {
    debtPositionTypeOrgService = Mockito.spy(new DebtPositionTypeOrgServiceImpl(debtPositionTypeOrgRepository));
  }

  @Test
  void givenExistingDebtPositionTypeOrgWhenGetIONotificationDetailThenOk() {
    Long debtPositionTypeOrgId = 1L;
    DebtPositionTypeOrg debtPositionTypeOrg = podamFactory.manufacturePojo(DebtPositionTypeOrg.class);
    IONotificationDTO expectedResult = IONotificationDTO.builder()
      .serviceId(debtPositionTypeOrg.getServiceId())
      .ioTemplateMessage(debtPositionTypeOrg.getIoTemplateMessage())
      .ioTemplateSubject(debtPositionTypeOrg.getIoTemplateSubject())
      .build();

    Mockito.when(debtPositionTypeOrgRepository.findById(debtPositionTypeOrgId)).thenReturn(Optional.of(debtPositionTypeOrg));

    IONotificationDTO result = debtPositionTypeOrgService.getIONotificationDetails(debtPositionTypeOrgId, IONotificationOperationType.CREATE_DP);

    Assertions.assertNotNull(result);
    Assertions.assertEquals(expectedResult, result);
    Mockito.verifyNoMoreInteractions(debtPositionTypeOrgRepository);
  }

  @Test
  void givenNonExistingDebtPositionTypeOrgWhenGetIONotificationDetailThenNotFoundException() {
    Long debtPositionTypeOrgId = 1L;

    Mockito.when(debtPositionTypeOrgRepository.findById(debtPositionTypeOrgId)).thenReturn(Optional.empty());

    NotFoundException notFoundException = Assertions.assertThrows(NotFoundException.class, () -> debtPositionTypeOrgService.getIONotificationDetails(debtPositionTypeOrgId, IONotificationOperationType.CREATE_DP));

    Assertions.assertEquals("DebtPositionTypeOrg having id %d was not found".formatted(debtPositionTypeOrgId), notFoundException.getMessage());
    Mockito.verifyNoMoreInteractions(debtPositionTypeOrgRepository);
  }

  @Test
  void givenFlagNotifyIoFalseWhenGetIONotificationDetailThenNull() {
    Long debtPositionTypeOrgId = 1L;
    DebtPositionTypeOrg debtPositionTypeOrg = podamFactory.manufacturePojo(DebtPositionTypeOrg.class);
    debtPositionTypeOrg.setFlagNotifyIo(false);

    Mockito.when(debtPositionTypeOrgRepository.findById(debtPositionTypeOrgId)).thenReturn(Optional.of(debtPositionTypeOrg));

    IONotificationDTO result = debtPositionTypeOrgService.getIONotificationDetails(debtPositionTypeOrgId, IONotificationOperationType.CREATE_DP);

    Assertions.assertNull(result);
  }

  @Test
  void givenContextUpdateWhenGetIONotificationDetailThenNull() {
    Long debtPositionTypeOrgId = 1L;
    DebtPositionTypeOrg debtPositionTypeOrg = podamFactory.manufacturePojo(DebtPositionTypeOrg.class);

    Mockito.when(debtPositionTypeOrgRepository.findById(debtPositionTypeOrgId)).thenReturn(Optional.of(debtPositionTypeOrg));

    IONotificationDTO result = debtPositionTypeOrgService.getIONotificationDetails(debtPositionTypeOrgId, IONotificationOperationType.UPDATE_DP);

    Assertions.assertNull(result);
  }
}
