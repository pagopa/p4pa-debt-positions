package it.gov.pagopa.pu.debtpositions.service;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.IudSyncStatusUpdateDTO;
import it.gov.pagopa.pu.debtpositions.mapper.DebtPositionMapper;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionRepository;
import it.gov.pagopa.pu.debtpositions.repository.InstallmentNoPIIRepository;
import it.gov.pagopa.pu.debtpositions.repository.PaymentOptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static it.gov.pagopa.pu.debtpositions.util.faker.DebtPositionFaker.buildDebtPosition;
import static it.gov.pagopa.pu.debtpositions.util.faker.DebtPositionFaker.buildDebtPositionDTO;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class DebtPositionServiceTest {

  @Mock
  private DebtPositionRepository debtPositionRepositoryMock;
  @Mock
  private PaymentOptionRepository paymentOptionRepositoryMock;
  @Mock
  private InstallmentNoPIIRepository installmentNoPIIRepositoryMock;
  @Mock
  private DebtPositionMapper debtPositionMapperMock;

  private DebtPositionService service;

  @BeforeEach
  void setUp(){
    service = new DebtPositionServiceImpl(debtPositionRepositoryMock, paymentOptionRepositoryMock, installmentNoPIIRepositoryMock, debtPositionMapperMock);
  }

  @Test
  void givenFinalizeSyncStatusThenOk(){
    Long id = 1L;
    String newStatus = "newStatus";
    DebtPosition debtPosition = buildDebtPosition();
    Mockito.when(debtPositionRepositoryMock.findOneWithAllDataByDebtPositionId(id)).thenReturn(debtPosition);

    Mockito.doNothing().when(installmentNoPIIRepositoryMock).updateStatus(id, newStatus);

    Mockito.doNothing().when(paymentOptionRepositoryMock).updateStatus(id, newStatus);

    Mockito.doNothing().when(debtPositionRepositoryMock).updateStatus(id, newStatus);

    Mockito.when(debtPositionMapperMock.map(debtPosition)).thenReturn(buildDebtPositionDTO());

    Map<String, IudSyncStatusUpdateDTO> syncStatusDTO = new HashMap<>();
    IudSyncStatusUpdateDTO iudSyncStatusUpdateDTO = IudSyncStatusUpdateDTO.builder()
      .newStatus(newStatus)
      .iupdPagopa("iupdPagopa")
      .build();

    syncStatusDTO.put("iud", iudSyncStatusUpdateDTO);

    DebtPositionDTO debtPositionDTO = service.finalizeSyncStatus(id, syncStatusDTO);
    System.out.println("debtPositionDTO: " + debtPositionDTO);

    assertNotNull(debtPositionDTO);
  }
}
