package it.gov.pagopa.pu.debtpositions.service.delete;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.PaymentOptionDTO;
import it.gov.pagopa.pu.debtpositions.repository.InstallmentNoPIIRepository;
import it.gov.pagopa.pu.debtpositions.repository.PaymentOptionRepository;
import it.gov.pagopa.pu.debtpositions.repository.TransferRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static it.gov.pagopa.pu.debtpositions.util.faker.DebtPositionFaker.buildDebtPositionDTO;
import static it.gov.pagopa.pu.debtpositions.util.faker.InstallmentFaker.buildInstallmentDTO;
import static it.gov.pagopa.pu.debtpositions.util.faker.PaymentOptionFaker.buildPaymentOptionDTO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class InstallmentDeletionServiceTest {
  @Mock
  private PaymentOptionRepository paymentOptionRepositoryMock;
  @Mock
  private InstallmentNoPIIRepository installmentNoPIIRepositoryMock;
  @Mock
  private TransferRepository transferRepositoryMock;

  private InstallmentDeletionService installmentDeletionService;

  @BeforeEach
  void setUp() {
    installmentDeletionService = new InstallmentDeletionServiceImpl(paymentOptionRepositoryMock, installmentNoPIIRepositoryMock, transferRepositoryMock);
  }

  @Test
  void givenDeleteSomeInstallmentsWhenDeleteDraftInstallmentsThenDeleteOnlyEmptyPaymentOption() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    PaymentOptionDTO paymentOptionDTO1 = buildPaymentOptionDTO();
    PaymentOptionDTO paymentOptionDTO2 = buildPaymentOptionDTO();
    paymentOptionDTO2.setPaymentOptionId(20L);
    InstallmentDTO installmentDTO1 = buildInstallmentDTO();
    installmentDTO1.setInstallmentId(200L);
    InstallmentDTO installmentDTO2 = buildInstallmentDTO();
    installmentDTO2.setInstallmentId(300L);
    paymentOptionDTO2.setInstallments(new ArrayList<>(List.of(installmentDTO1, installmentDTO2)));
    debtPositionDTO.setPaymentOptions(new ArrayList<>(List.of(paymentOptionDTO1, paymentOptionDTO2)));

    Set<Long> installmentIdsToDelete = Set.of(100L);

    installmentDeletionService.deleteDraftInstallments(debtPositionDTO, installmentIdsToDelete);

    verify(transferRepositoryMock).deleteById(1000L);
    verify(installmentNoPIIRepositoryMock).deleteById(100L);
    verify(paymentOptionRepositoryMock).deleteById(10L);

    assertEquals(1, debtPositionDTO.getPaymentOptions().size());
  }

  @Test
  void givenDeleteSomeInstallmentsWhenDeleteDraftInstallmentsThenDoNotDeleteAnyPaymentOption() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    PaymentOptionDTO paymentOptionDTO1 = buildPaymentOptionDTO();
    InstallmentDTO installmentDTO1 = buildInstallmentDTO();
    installmentDTO1.setInstallmentId(100L);
    InstallmentDTO installmentDTO2 = buildInstallmentDTO();
    installmentDTO2.setInstallmentId(400L);
    paymentOptionDTO1.setInstallments(new ArrayList<>(List.of(installmentDTO1, installmentDTO2)));

    PaymentOptionDTO paymentOptionDTO2 = buildPaymentOptionDTO();
    paymentOptionDTO2.setPaymentOptionId(20L);
    InstallmentDTO installmentDTO3 = buildInstallmentDTO();
    installmentDTO3.setInstallmentId(200L);
    InstallmentDTO installmentDTO4 = buildInstallmentDTO();
    installmentDTO4.setInstallmentId(300L);
    paymentOptionDTO2.setInstallments(new ArrayList<>(List.of(installmentDTO3, installmentDTO4)));
    debtPositionDTO.setPaymentOptions(new ArrayList<>(List.of(paymentOptionDTO1, paymentOptionDTO2)));

    Set<Long> installmentIdsToDelete = Set.of(100L);

    installmentDeletionService.deleteDraftInstallments(debtPositionDTO, installmentIdsToDelete);

    verify(transferRepositoryMock).deleteById(1000L);
    verify(installmentNoPIIRepositoryMock).deleteById(100L);

    assertEquals(2, debtPositionDTO.getPaymentOptions().size());
  }
}
