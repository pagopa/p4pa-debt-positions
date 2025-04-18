package it.gov.pagopa.pu.debtpositions.service;

import it.gov.pagopa.pu.debtpositions.dto.Installment;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.PaymentOptionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.TransferDTO;
import it.gov.pagopa.pu.debtpositions.mapper.DebtPositionMapper;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;
import it.gov.pagopa.pu.debtpositions.model.PaymentOption;
import it.gov.pagopa.pu.debtpositions.model.Transfer;
import it.gov.pagopa.pu.debtpositions.repository.*;
import it.gov.pagopa.pu.debtpositions.util.Utilities;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.util.Pair;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Map;

import static it.gov.pagopa.pu.debtpositions.util.faker.DebtPositionFaker.buildDebtPosition;
import static it.gov.pagopa.pu.debtpositions.util.faker.DebtPositionFaker.buildDebtPositionDTO;
import static it.gov.pagopa.pu.debtpositions.util.faker.InstallmentFaker.buildInstallment;
import static it.gov.pagopa.pu.debtpositions.util.faker.InstallmentFaker.buildInstallmentNoPII;

@ExtendWith(MockitoExtension.class)
class DebtPositionSaveServiceTest {

  @Mock
  private DebtPositionRepository debtPositionRepositoryMock;
  @Mock
  private PaymentOptionRepository paymentOptionRepositoryMock;
  @Mock
  private InstallmentPIIRepository installmentRepositoryMock;
  @Mock
  private InstallmentNoPIIRepository installmentNoPIIRepositoryMock;
  @Mock
  private TransferRepository transferRepositoryMock;
  @Mock
  private DebtPositionMapper debtPositionMapperMock;

  private DebtPositionSaveService service;

  @BeforeEach
  void setUp() {
    service = new DebtPositionSaveService(
      debtPositionRepositoryMock,
      paymentOptionRepositoryMock,
      installmentRepositoryMock,
      installmentNoPIIRepositoryMock,
      transferRepositoryMock,
      debtPositionMapperMock
    );
  }

  @AfterEach
  void verifyNoMoreInteractions(){
    Mockito.verifyNoMoreInteractions(
      debtPositionRepositoryMock,
      paymentOptionRepositoryMock,
      installmentRepositoryMock,
      installmentNoPIIRepositoryMock,
      transferRepositoryMock,
      debtPositionMapperMock);
  }

  @Test
  void givenValidDebtPositionDTO_WhenSaveDebtPositionDTO_ThenSaveAllEntitiesDTO() {
    String generatedIUD = "0003e93fd3b56b24771850abe935b819ece";
    String generatedIupd = "e04940029-18b140108de0-e39271476234";

    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    PaymentOptionDTO paymentOptionDTO = debtPositionDTO.getPaymentOptions().getFirst();
    InstallmentDTO installmentDTO = paymentOptionDTO.getInstallments().getFirst();
    TransferDTO transferDTO = installmentDTO.getTransfers().getFirst();
    transferDTO.setTransferIndex(1);

    DebtPosition mappedDebtPosition = buildDebtPosition();
    PaymentOption mappedPaymentOption = mappedDebtPosition.getPaymentOptions().getFirst();
    InstallmentNoPII mappedInstallmentNoPII = mappedPaymentOption.getInstallments().getFirst();
    Transfer mappedTransfer = mappedInstallmentNoPII.getTransfers().getFirst();
    mappedInstallmentNoPII.getTransfers().getFirst().setTransferIndex(1);

    InstallmentNoPII installmentNoPIINoIud = buildInstallmentNoPII();
    Installment installmentNoIud = buildInstallment();
    installmentNoIud.setIud("");

    Installment mappedInstallment = buildInstallment();
    mappedInstallment.setIud(mappedInstallmentNoPII.getIud());
    mappedInstallment.setTransfers(new ArrayList<>(mappedInstallmentNoPII.getTransfers()));
    mappedInstallment.setNoPII(mappedInstallmentNoPII);

    DebtPosition savedDebtPosition = mockSavedDebtPosition(mappedDebtPosition);
    PaymentOption savedPaymentOption = mockSavedPaymentOption(mappedPaymentOption);

    Installment savedInstallment = mockSavedInstallment(mappedInstallment);

    Transfer savedTransfer = mockSavedTransfer(mappedTransfer);

    Map<InstallmentNoPII, Installment> installmentMap = Map.of(
      installmentNoPIINoIud, installmentNoIud,
      mappedInstallmentNoPII, mappedInstallment
    );

    Pair<DebtPosition, Map<InstallmentNoPII, Installment>> mappedPair = Pair.of(mappedDebtPosition, installmentMap);

    Mockito.when(debtPositionMapperMock.mapToModel(debtPositionDTO)).thenReturn(mappedPair);

    try (MockedStatic<Utilities> mockedStatic = Mockito.mockStatic(Utilities.class)) {
      mockedStatic.when(Utilities::getRandomIUD).thenReturn(generatedIUD);
      mockedStatic.when(Utilities::getRandomicUUID).thenReturn(generatedIupd);

      service.saveDebtPositionDTO(debtPositionDTO);

      Assertions.assertSame(savedDebtPosition.getDebtPositionId(), mappedDebtPosition.getDebtPositionId());
      assertDebtPositionDtoAlign(savedDebtPosition, debtPositionDTO);

      PaymentOption po = mappedDebtPosition.getPaymentOptions().getFirst();
      Assertions.assertSame(savedDebtPosition.getDebtPositionId(), po.getDebtPositionId());
      assertPaymentOptionDtoAlign(savedPaymentOption, paymentOptionDTO);

      InstallmentNoPII i = po.getInstallments().getFirst();
      Assertions.assertSame(savedInstallment.getPaymentOptionId(), i.getPaymentOptionId());
      assertInstallmentDtoAlign(savedInstallment, installmentDTO);

      Assertions.assertSame(savedTransfer.getInstallmentId(), mappedInstallment.getTransfers().getFirst().getInstallmentId());
      assertTransferDtoAlign(savedTransfer, transferDTO);
    }
  }

  private DebtPosition mockSavedDebtPosition(DebtPosition debtPosition) {
    DebtPosition savedDebtPosition = Mockito.mock(DebtPosition.class, Mockito.RETURNS_DEEP_STUBS);
    Mockito.when(savedDebtPosition.getCreationDate()).thenReturn(LocalDateTime.now().minusDays(41));
    Mockito.when(savedDebtPosition.getUpdateDate()).thenReturn(LocalDateTime.now().minusDays(40));

    Mockito.when(debtPositionRepositoryMock.save(Mockito.same(debtPosition)))
      .thenReturn(savedDebtPosition);
    return savedDebtPosition;
  }

  private PaymentOption mockSavedPaymentOption(PaymentOption paymentOption) {
    PaymentOption savedPaymentOption = Mockito.mock(PaymentOption.class, Mockito.RETURNS_DEEP_STUBS);
    Mockito.when(savedPaymentOption.getPaymentOptionIndex()).thenReturn(paymentOption.getPaymentOptionIndex());
    Mockito.when(savedPaymentOption.getCreationDate()).thenReturn(LocalDateTime.now().minusDays(31));
    Mockito.when(savedPaymentOption.getUpdateDate()).thenReturn(LocalDateTime.now().minusDays(30));

    Mockito.when(paymentOptionRepositoryMock.save(Mockito.same(paymentOption)))
      .thenReturn(savedPaymentOption);
    return savedPaymentOption;
  }

  private Installment mockSavedInstallment(Installment mappedInstallment) {
    Installment savedInstallment = Mockito.mock(Installment.class, Mockito.RETURNS_DEEP_STUBS);
    Mockito.when(savedInstallment.getNoPII().getIud()).thenReturn(mappedInstallment.getIud());
    Mockito.when(savedInstallment.getCreationDate()).thenReturn(LocalDateTime.now().minusDays(21));
    Mockito.when(savedInstallment.getUpdateDate()).thenReturn(LocalDateTime.now().minusDays(20));
    Mockito.when(installmentRepositoryMock.save(Mockito.same(mappedInstallment))).thenReturn(savedInstallment);
    return savedInstallment;
  }

  private Transfer mockSavedTransfer(Transfer transfer) {
    Transfer savedTransfer = Mockito.mock(Transfer.class, Mockito.RETURNS_DEEP_STUBS);
    Mockito.when(savedTransfer.getTransferIndex()).thenReturn(transfer.getTransferIndex());
    Mockito.when(savedTransfer.getCreationDate()).thenReturn(LocalDateTime.now().minusDays(11));
    Mockito.when(savedTransfer.getUpdateDate()).thenReturn(LocalDateTime.now().minusDays(10));

    Mockito.when(transferRepositoryMock.save(Mockito.same(transfer)))
      .thenReturn(savedTransfer);
    return savedTransfer;
  }

  private void assertDebtPositionDtoAlign(DebtPosition savedDebtPosition, DebtPositionDTO debtPositionDTO) {
    Assertions.assertSame(savedDebtPosition.getDebtPositionId(), debtPositionDTO.getDebtPositionId());
    Assertions.assertEquals(Utilities.localDatetimeToOffsetDateTime(savedDebtPosition.getCreationDate()), debtPositionDTO.getCreationDate());
    Assertions.assertEquals(Utilities.localDatetimeToOffsetDateTime(savedDebtPosition.getUpdateDate()), debtPositionDTO.getUpdateDate());
    Assertions.assertSame(savedDebtPosition.getUpdateOperatorExternalId(), debtPositionDTO.getUpdateOperatorExternalId());
  }

  private void assertPaymentOptionDtoAlign(PaymentOption savedPaymentOption, PaymentOptionDTO paymentOptionDTO) {
    Assertions.assertSame(savedPaymentOption.getDebtPositionId(), paymentOptionDTO.getDebtPositionId());
    Assertions.assertSame(savedPaymentOption.getPaymentOptionId(), paymentOptionDTO.getPaymentOptionId());
    Assertions.assertEquals(Utilities.localDatetimeToOffsetDateTime(savedPaymentOption.getCreationDate()), paymentOptionDTO.getCreationDate());
    Assertions.assertEquals(Utilities.localDatetimeToOffsetDateTime(savedPaymentOption.getUpdateDate()), paymentOptionDTO.getUpdateDate());
    Assertions.assertSame(savedPaymentOption.getUpdateOperatorExternalId(), paymentOptionDTO.getUpdateOperatorExternalId());
  }

  private void assertInstallmentDtoAlign(Installment savedInstallment, InstallmentDTO installmentDTO) {
    Assertions.assertSame(savedInstallment.getPaymentOptionId(), installmentDTO.getPaymentOptionId());
    Assertions.assertSame(savedInstallment.getInstallmentId(), installmentDTO.getInstallmentId());
    Assertions.assertEquals(Utilities.localDatetimeToOffsetDateTime(savedInstallment.getCreationDate()), installmentDTO.getCreationDate());
    Assertions.assertEquals(Utilities.localDatetimeToOffsetDateTime(savedInstallment.getUpdateDate()), installmentDTO.getUpdateDate());
    Assertions.assertSame(savedInstallment.getUpdateOperatorExternalId(), installmentDTO.getUpdateOperatorExternalId());
  }

  private void assertTransferDtoAlign(Transfer savedTransfer, TransferDTO transferDTO) {
    Assertions.assertSame(savedTransfer.getInstallmentId(), transferDTO.getInstallmentId());
    Assertions.assertSame(savedTransfer.getTransferId(), transferDTO.getTransferId());
    Assertions.assertEquals(Utilities.localDatetimeToOffsetDateTime(savedTransfer.getCreationDate()), transferDTO.getCreationDate());
    Assertions.assertEquals(Utilities.localDatetimeToOffsetDateTime(savedTransfer.getUpdateDate()), transferDTO.getUpdateDate());
    Assertions.assertSame(savedTransfer.getUpdateOperatorExternalId(), transferDTO.getUpdateOperatorExternalId());
  }

  @Test
  void whenSaveDebtPositionThenSaveAllDTO(){
    // Given
    DebtPosition debtPosition = buildDebtPosition();
    PaymentOption paymentOption = debtPosition.getPaymentOptions().getFirst();
    InstallmentNoPII installmentNoPII = paymentOption.getInstallments().getFirst();
    Transfer transfer = installmentNoPII.getTransfers().getFirst();

    DebtPosition savedDebtPosition = mockSavedDebtPosition(debtPosition);
    PaymentOption savedPaymentOption = mockSavedPaymentOption(paymentOption);
    InstallmentNoPII savedInstallmentNoPII = mockSavedInstallmentNoPII(installmentNoPII);
    Transfer savedTransfer = mockSavedTransfer(transfer);

    // When
    service.saveDebtPosition(debtPosition);

    // Then
    Assertions.assertSame(debtPosition.getDebtPositionId(), savedDebtPosition.getDebtPositionId());
    Assertions.assertSame(savedDebtPosition.getCreationDate(), debtPosition.getCreationDate());
    Assertions.assertSame(savedDebtPosition.getUpdateDate(), debtPosition.getUpdateDate());
    Assertions.assertSame(savedDebtPosition.getUpdateOperatorExternalId(), debtPosition.getUpdateOperatorExternalId());

    Assertions.assertSame(debtPosition.getDebtPositionId(), paymentOption.getDebtPositionId());
    Assertions.assertSame(savedPaymentOption.getPaymentOptionId(), paymentOption.getPaymentOptionId());
    Assertions.assertSame(savedPaymentOption.getCreationDate(), paymentOption.getCreationDate());
    Assertions.assertSame(savedPaymentOption.getUpdateDate(), paymentOption.getUpdateDate());
    Assertions.assertSame(savedPaymentOption.getUpdateOperatorExternalId(), paymentOption.getUpdateOperatorExternalId());

    Assertions.assertSame(paymentOption.getPaymentOptionId(), installmentNoPII.getPaymentOptionId());
    Assertions.assertSame(savedInstallmentNoPII.getInstallmentId(), installmentNoPII.getInstallmentId());
    Assertions.assertSame(savedInstallmentNoPII.getCreationDate(), installmentNoPII.getCreationDate());
    Assertions.assertSame(savedInstallmentNoPII.getUpdateDate(), installmentNoPII.getUpdateDate());
    Assertions.assertSame(savedInstallmentNoPII.getUpdateOperatorExternalId(), installmentNoPII.getUpdateOperatorExternalId());

    Assertions.assertSame(installmentNoPII.getInstallmentId(), transfer.getInstallmentId());
    Assertions.assertSame(savedTransfer.getTransferId(), transfer.getTransferId());
    Assertions.assertSame(savedTransfer.getCreationDate(), transfer.getCreationDate());
    Assertions.assertSame(savedTransfer.getUpdateDate(), transfer.getUpdateDate());
    Assertions.assertSame(savedTransfer.getUpdateOperatorExternalId(), transfer.getUpdateOperatorExternalId());
  }

  private InstallmentNoPII mockSavedInstallmentNoPII(InstallmentNoPII mappedInstallment) {
    InstallmentNoPII savedInstallment = Mockito.mock(InstallmentNoPII.class, Mockito.RETURNS_DEEP_STUBS);
    Mockito.when(savedInstallment.getIud()).thenReturn(mappedInstallment.getIud());
    Mockito.when(savedInstallment.getCreationDate()).thenReturn(LocalDateTime.now().minusDays(21));
    Mockito.when(savedInstallment.getUpdateDate()).thenReturn(LocalDateTime.now().minusDays(20));
    Mockito.when(installmentNoPIIRepositoryMock.save(Mockito.same(mappedInstallment))).thenReturn(savedInstallment);

    return savedInstallment;
  }

}

