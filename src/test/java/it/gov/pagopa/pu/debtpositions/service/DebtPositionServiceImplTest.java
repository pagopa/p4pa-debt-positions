package it.gov.pagopa.pu.debtpositions.service;

import it.gov.pagopa.pu.debtpositions.dto.Installment;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.exception.custom.NotFoundException;
import it.gov.pagopa.pu.debtpositions.mapper.DebtPositionMapper;
import it.gov.pagopa.pu.debtpositions.model.*;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionRepository;
import it.gov.pagopa.pu.debtpositions.repository.InstallmentPIIRepository;
import it.gov.pagopa.pu.debtpositions.repository.PaymentOptionRepository;
import it.gov.pagopa.pu.debtpositions.repository.TransferRepository;
import it.gov.pagopa.pu.debtpositions.util.TestUtils;
import it.gov.pagopa.pu.debtpositions.util.Utilities;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.util.Pair;
import uk.co.jemos.podam.api.PodamFactory;

import java.util.Map;

import static it.gov.pagopa.pu.debtpositions.util.faker.DebtPositionFaker.buildDebtPosition;
import static it.gov.pagopa.pu.debtpositions.util.faker.DebtPositionFaker.buildDebtPositionDTO;
import static it.gov.pagopa.pu.debtpositions.util.faker.DebtPositionTypeOrgFaker.buildDebtPositionTypeOrg;
import static it.gov.pagopa.pu.debtpositions.util.faker.InstallmentFaker.buildInstallment;
import static it.gov.pagopa.pu.debtpositions.util.faker.InstallmentFaker.buildInstallmentNoPII;
import static it.gov.pagopa.pu.debtpositions.util.faker.OrganizationFaker.buildOrganization;
import static it.gov.pagopa.pu.debtpositions.util.faker.PaymentOptionFaker.buildPaymentOption;
import static it.gov.pagopa.pu.debtpositions.util.faker.TransferFaker.buildTransfer;

@ExtendWith(MockitoExtension.class)
class DebtPositionServiceImplTest {

  @Mock
  private DebtPositionRepository debtPositionRepository;

  @Mock
  private PaymentOptionRepository paymentOptionRepository;

  @Mock
  private InstallmentPIIRepository installmentRepository;

  @Mock
  private TransferRepository transferRepository;

  @Mock
  private DebtPositionMapper debtPositionMapper;

  private DebtPositionServiceImpl debtPositionService;

  private final PodamFactory podamFactory = TestUtils.getPodamFactory();

  @BeforeEach
  void setUp() {
    debtPositionService = Mockito.spy(new DebtPositionServiceImpl(
      debtPositionRepository, paymentOptionRepository, installmentRepository, transferRepository,
      debtPositionMapper
    ));
  }

  @Test
  void givenValidDebtPositionDTO_WhenSaveDebtPosition_ThenSaveAllEntities() {
    String generatedIUD = "0003e93fd3b56b24771850abe935b819ece";
    String generatedIupd = "e04940029-18b140108de0-e39271476234";

    Organization org = buildOrganization();
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().getTransfers().getFirst().setTransferIndex(1);

    DebtPosition debtPosition = buildDebtPosition();
    PaymentOption paymentOption = buildPaymentOption();

    InstallmentNoPII installmentNoPIINoIud = buildInstallmentNoPII();
    Installment installmentNoIud = buildInstallment();
    installmentNoIud.setIud("");

    InstallmentNoPII installmentNoPII = buildInstallmentNoPII();
    installmentNoPII.getTransfers().getFirst().setTransferIndex(1);
    Installment installment = buildInstallment();
    installment.getTransfers().getFirst().setTransferIndex(1);
    installment.setInstallmentId(10L);
    installmentNoPII.setInstallmentId(10L);
    installment.setNoPII(installmentNoPII);

    Transfer transfer = buildTransfer();

    Map<InstallmentNoPII, Installment> installmentMap = Map.of(
      installmentNoPIINoIud, installmentNoIud,
      installmentNoPII, installment
    );

    Pair<DebtPosition, Map<InstallmentNoPII, Installment>> mappedPair = Pair.of(debtPosition, installmentMap);

    Mockito.when(debtPositionMapper.mapToModel(debtPositionDTO)).thenReturn(mappedPair);
    Mockito.when(debtPositionRepository.save(Mockito.any(DebtPosition.class))).thenReturn(debtPosition);
    Mockito.when(paymentOptionRepository.save(Mockito.any(PaymentOption.class))).thenReturn(paymentOption);
    Mockito.when(installmentRepository.save(Mockito.any(Installment.class))).thenReturn(installment);
    Mockito.when(transferRepository.save(Mockito.any(Transfer.class))).thenReturn(transfer);

    try (MockedStatic<Utilities> mockedStatic = Mockito.mockStatic(Utilities.class)) {
      mockedStatic.when(Utilities::getRandomIUD).thenReturn(generatedIUD);
      mockedStatic.when(Utilities::getRandomicUUID).thenReturn(generatedIupd);

      DebtPosition result = debtPositionService.saveDebtPosition(debtPositionDTO, org);

      Assertions.assertSame(debtPosition, result);

      Mockito.verify(debtPositionRepository, Mockito.times(1)).save(debtPosition);
      Mockito.verify(paymentOptionRepository, Mockito.times(1)).save(paymentOption);
      Mockito.verify(installmentRepository, Mockito.times(1)).save(Mockito.any(Installment.class));
      Mockito.verify(transferRepository, Mockito.times(1)).save(transfer);
    }
  }

  @Test
  void givenValidDebtPositionDTONoOrg_WhenSaveDebtPosition_ThenSaveAllEntities() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();
    debtPositionTypeOrg.setIban("");

    DebtPosition debtPosition = buildDebtPosition();
    PaymentOption paymentOption = buildPaymentOption();

    InstallmentNoPII installmentNoPIINoIud = buildInstallmentNoPII();
    Installment installmentNoIud = buildInstallment();
    installmentNoIud.setIud("");

    InstallmentNoPII installmentNoPII = buildInstallmentNoPII();
    installmentNoPII.getTransfers().getFirst().setTransferIndex(1);
    Installment installment = buildInstallment();
    installment.getTransfers().getFirst().setTransferIndex(1);
    installment.setInstallmentId(10L);
    installmentNoPII.setInstallmentId(10L);
    installment.setBalance("");

    Transfer transfer = buildTransfer();

    Map<InstallmentNoPII, Installment> installmentMap = Map.of(
      installmentNoPIINoIud, installmentNoIud,
      installmentNoPII, installment
    );

    Pair<DebtPosition, Map<InstallmentNoPII, Installment>> mappedPair = Pair.of(debtPosition, installmentMap);

    String generatedIUD = "0003e93fd3b56b24771850abe935b819ece";

    Mockito.when(debtPositionMapper.mapToModel(debtPositionDTO)).thenReturn(mappedPair);
    Mockito.when(debtPositionRepository.save(Mockito.any(DebtPosition.class))).thenReturn(debtPosition);
    Mockito.when(paymentOptionRepository.save(Mockito.any(PaymentOption.class))).thenReturn(paymentOption);
    Mockito.when(installmentRepository.save(Mockito.any(Installment.class))).thenReturn(installment);
    Mockito.when(transferRepository.save(Mockito.any(Transfer.class))).thenReturn(transfer);

    try (MockedStatic<Utilities> mockedStatic = Mockito.mockStatic(Utilities.class)) {
      mockedStatic.when(Utilities::getRandomIUD).thenReturn(generatedIUD);

      Organization org = buildOrganization();
      org.setIban("IT60X0542811101000000123456");
      DebtPosition result = debtPositionService.saveDebtPosition(debtPositionDTO, org);

      Assertions.assertSame(debtPosition, result);

      Mockito.verify(debtPositionRepository, Mockito.times(1)).save(debtPosition);
      Mockito.verify(paymentOptionRepository, Mockito.times(1)).save(paymentOption);
      Mockito.verify(installmentRepository, Mockito.times(1)).save(Mockito.any(Installment.class));
      Mockito.verify(transferRepository, Mockito.times(1)).save(transfer);
    }
  }

  @Test
  void whenSaveDebtPositionAndRemapThenReturnReMap() {
    // Given
    DebtPositionDTO inputDP = new DebtPositionDTO();
    DebtPosition saved = new DebtPosition();
    Organization org = new Organization();

    DebtPositionDTO expectedResult = new DebtPositionDTO();

    Mockito.doReturn(saved)
      .when(debtPositionService)
      .saveDebtPosition(Mockito.same(inputDP), Mockito.same(org));

    Mockito.when(debtPositionMapper.mapToDto(Mockito.same(saved)))
      .thenReturn(expectedResult);

    // When
    DebtPositionDTO result = debtPositionService.saveDebtPositionAndRemap(inputDP, org);

    // Then
    Assertions.assertSame(expectedResult, result);
  }

  @Test
  void givenExistingDebtPositionWhenGetDebtPositionThenOk() {
    Long debtPositionId = 1L;
    DebtPositionDTO expectedResult = podamFactory.manufacturePojo(DebtPositionDTO.class);
    DebtPosition debtPosition = podamFactory.manufacturePojo(DebtPosition.class);

    Mockito.when(debtPositionRepository.findOneWithAllDataByDebtPositionId(debtPositionId)).thenReturn(debtPosition);
    Mockito.when(debtPositionMapper.mapToDto(debtPosition)).thenReturn(expectedResult);

    DebtPositionDTO result = debtPositionService.getDebtPosition(
      debtPositionId);

    Assertions.assertNotNull(result);
    Assertions.assertSame(expectedResult, result);
    Mockito.verifyNoMoreInteractions(debtPositionRepository, debtPositionMapper);
  }

  @Test
  void givenNonExistingDebtPositionDetailWhenGetDebtPositionThenThrowNotFoundException() {
    Long debtPositionId = 1L;

    Mockito.when(debtPositionRepository.findOneWithAllDataByDebtPositionId(debtPositionId)).thenReturn(null);

    Assertions.assertThrows(NotFoundException.class, () -> debtPositionService.getDebtPosition(
      debtPositionId));

    Mockito.verifyNoMoreInteractions(debtPositionRepository);
    Mockito.verifyNoInteractions(debtPositionMapper);
  }
}

