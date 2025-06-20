package it.gov.pagopa.pu.debtpositions.service.delete;

import it.gov.pagopa.pu.debtpositions.dto.Installment;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.PaymentOptionDTO;
import it.gov.pagopa.pu.debtpositions.mapper.DebtPositionMapper;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;
import it.gov.pagopa.pu.debtpositions.repository.InstallmentPIIRepository;
import it.gov.pagopa.pu.debtpositions.repository.PaymentOptionRepository;
import it.gov.pagopa.pu.debtpositions.service.DebtPositionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static it.gov.pagopa.pu.debtpositions.util.faker.DebtPositionFaker.buildDebtPosition;
import static it.gov.pagopa.pu.debtpositions.util.faker.DebtPositionFaker.buildDebtPositionDTO;
import static it.gov.pagopa.pu.debtpositions.util.faker.InstallmentFaker.*;
import static it.gov.pagopa.pu.debtpositions.util.faker.PaymentOptionFaker.buildPaymentOptionDTO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InstallmentDeletionServiceTest {
  @Mock
  private PaymentOptionRepository paymentOptionRepositoryMock;
  @Mock
  private InstallmentPIIRepository installmentPIIRepository;
  @Mock
  private DebtPositionService debtPositionServiceMock;
  @Mock
  private DebtPositionMapper debtPositionMapperMock;

  private InstallmentDeletionService installmentDeletionService;

  @BeforeEach
  void setUp() {
    installmentDeletionService = new InstallmentDeletionServiceImpl(
      paymentOptionRepositoryMock,
      installmentPIIRepository,
      debtPositionServiceMock,
      debtPositionMapperMock);
  }

  @Test
  void givenDeleteAllInstallmentsWhenDeleteDraftInstallmentsThenDeleteDebtPosition() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    DebtPosition debtPosition = buildDebtPosition();
    InstallmentNoPII installmentNoPII = buildInstallmentNoPII();
    Map<InstallmentNoPII, Installment> map = Map.of(installmentNoPII, buildInstallment());
    Set<Long> installmentIdsToDelete = Set.of(100L);

    when(debtPositionMapperMock.mapToModel(debtPositionDTO)).thenReturn(Pair.of(debtPosition, map));

    installmentDeletionService.deleteDraftInstallments(debtPositionDTO, installmentIdsToDelete);

    verify(debtPositionServiceMock).delete(debtPositionMapperMock.mapToModel(debtPositionDTO).getFirst());
    assertTrue(debtPositionDTO.getPaymentOptions().isEmpty());
  }

  @Test
  void givenDeleteSomeInstallmentsWhenDeleteDraftInstallmentsThenDeleteOnlyEmptyPaymentOption() {
    DebtPositionDTO debtPositionDTO = getDebtPositionDTO();

    DebtPosition debtPosition = buildDebtPosition();
    InstallmentNoPII installmentNoPII1 = buildInstallmentNoPII();
    installmentNoPII1.setInstallmentId(300L);
    InstallmentNoPII installmentNoPII2 = buildInstallmentNoPII();
    installmentNoPII2.setInstallmentId(400L);

    Installment inst1 = new Installment();
    inst1.setInstallmentId(300L);
    Installment inst2 = new Installment();
    inst2.setInstallmentId(400L);

    Map<InstallmentNoPII, Installment> modelMap = Map.of(
      installmentNoPII1, inst1,
      installmentNoPII2, inst2
    );

    when(debtPositionMapperMock.mapToModel(debtPositionDTO)).thenReturn(Pair.of(debtPosition, modelMap));

    Set<Long> installmentIdsToDelete = Set.of(300L, 400L);

    installmentDeletionService.deleteDraftInstallments(debtPositionDTO, installmentIdsToDelete);

    verify(installmentPIIRepository).delete(installmentNoPII1);
    verify(installmentPIIRepository).delete(installmentNoPII2);
    verify(paymentOptionRepositoryMock).deleteById(20L);

    assertEquals(1, debtPositionDTO.getPaymentOptions().size());
  }

  @Test
  void givenDeleteSomeInstallmentsWhenDeleteDraftInstallmentsThenDoNotDeleteAnyPaymentOption() {
    DebtPositionDTO debtPositionDTO = getDebtPositionDTO();

    DebtPosition debtPosition = buildDebtPosition();
    InstallmentNoPII installmentNoPII1 = buildInstallmentNoPII();
    installmentNoPII1.setInstallmentId(300L);
    InstallmentNoPII installmentNoPII2 = buildInstallmentNoPII();
    installmentNoPII2.setInstallmentId(400L);

    Installment inst1 = new Installment();
    inst1.setInstallmentId(300L);
    Installment inst2 = new Installment();
    inst2.setInstallmentId(400L);

    Map<InstallmentNoPII, Installment> modelMap = Map.of(
      installmentNoPII1, inst1,
      installmentNoPII2, inst2
    );

    when(debtPositionMapperMock.mapToModel(debtPositionDTO)).thenReturn(Pair.of(debtPosition, modelMap));

    Set<Long> installmentIdsToDelete = Set.of(400L);

    installmentDeletionService.deleteDraftInstallments(debtPositionDTO, installmentIdsToDelete);

    verify(installmentPIIRepository).delete(installmentNoPII2);

    assertEquals(2, debtPositionDTO.getPaymentOptions().size());
  }

  private DebtPositionDTO getDebtPositionDTO() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();

    PaymentOptionDTO paymentOptionDTO1 = buildPaymentOptionDTO();
    PaymentOptionDTO paymentOptionDTO2 = buildPaymentOptionDTO();
    paymentOptionDTO2.setPaymentOptionId(20L);

    InstallmentDTO installmentDTO1 = buildInstallmentDTO();
    installmentDTO1.setInstallmentId(300L);
    InstallmentDTO installmentDTO2 = buildInstallmentDTO();
    installmentDTO2.setInstallmentId(400L);

    paymentOptionDTO2.setInstallments(new ArrayList<>(List.of(installmentDTO1, installmentDTO2)));
    debtPositionDTO.setPaymentOptions(new ArrayList<>(List.of(paymentOptionDTO1, paymentOptionDTO2)));
    return debtPositionDTO;
  }
}
