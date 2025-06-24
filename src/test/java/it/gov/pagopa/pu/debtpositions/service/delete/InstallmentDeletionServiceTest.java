package it.gov.pagopa.pu.debtpositions.service.delete;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.PaymentOptionDTO;
import it.gov.pagopa.pu.debtpositions.mapper.DebtPositionMapper;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;
import it.gov.pagopa.pu.debtpositions.model.PaymentOption;
import it.gov.pagopa.pu.debtpositions.repository.InstallmentPIIRepository;
import it.gov.pagopa.pu.debtpositions.repository.PaymentOptionRepository;
import it.gov.pagopa.pu.debtpositions.repository.TransferRepository;
import it.gov.pagopa.pu.debtpositions.service.DebtPositionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static it.gov.pagopa.pu.debtpositions.util.faker.DebtPositionFaker.buildDebtPosition;
import static it.gov.pagopa.pu.debtpositions.util.faker.DebtPositionFaker.buildDebtPositionDTO;
import static it.gov.pagopa.pu.debtpositions.util.faker.InstallmentFaker.buildInstallmentDTO;
import static it.gov.pagopa.pu.debtpositions.util.faker.InstallmentFaker.buildInstallmentNoPII;
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
  private TransferRepository transferRepository;
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
      transferRepository,
      debtPositionServiceMock,
      debtPositionMapperMock);
  }

  @Test
  void givenDeleteAllInstallmentsWhenDeleteDraftInstallmentsThenDeleteDebtPosition() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    DebtPosition debtPosition = buildDebtPosition();
    Set<Long> installmentIdsToDelete = Set.of(100L);

    when(debtPositionMapperMock.mapToModel(debtPositionDTO)).thenReturn(debtPosition);

    installmentDeletionService.deleteDraftInstallments(debtPositionDTO, installmentIdsToDelete);

    verify(debtPositionServiceMock).delete(debtPositionMapperMock.mapToModel(debtPositionDTO));
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

    PaymentOption po = debtPosition.getPaymentOptions().getFirst();
    po.setPaymentOptionId(20L);
    po.setInstallments(new TreeSet<>(List.of(installmentNoPII1, installmentNoPII2)));

    when(debtPositionMapperMock.mapToModel(debtPositionDTO)).thenReturn(debtPosition);

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

    PaymentOption po = debtPosition.getPaymentOptions().getFirst();
    po.setPaymentOptionId(20L);
    po.setInstallments(new TreeSet<>(List.of(installmentNoPII1, installmentNoPII2)));

    when(debtPositionMapperMock.mapToModel(debtPositionDTO)).thenReturn(debtPosition);

    Set<Long> installmentIdsToDelete = Set.of(400L);

    installmentDeletionService.deleteDraftInstallments(debtPositionDTO, installmentIdsToDelete);

    verify(transferRepository).delete(installmentNoPII2.getTransfers().getFirst());
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
