package it.gov.pagopa.pu.debtpositions.mapper;

import static it.gov.pagopa.pu.debtpositions.service.dptypeorg.MixedDebtPositionTypeOrgRetrieverService.DEBT_POSITION_TYPE_MIXED;
import static it.gov.pagopa.pu.debtpositions.util.faker.DebtPositionFaker.buildMixedDebtPositionDTO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import it.gov.pagopa.pu.debtpositions.dto.MixedDpAdditionalData;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionOrigin;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionStatus;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentStatus;
import it.gov.pagopa.pu.debtpositions.dto.generated.MixedDebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.MixedTransferDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.PaymentOptionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.PaymentOptionDTO.PaymentOptionTypeEnum;
import it.gov.pagopa.pu.debtpositions.dto.generated.PaymentOptionStatus;
import it.gov.pagopa.pu.debtpositions.dto.generated.TransferDTO;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrg;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionTypeOrgRepository;
import it.gov.pagopa.pu.debtpositions.service.dptypeorg.MixedDebtPositionTypeOrgRetrieverService;
import it.gov.pagopa.pu.debtpositions.util.Utilities;
import it.gov.pagopa.pu.debtpositions.util.faker.PersonFaker;
import it.gov.pagopa.pu.debtpositions.util.faker.TransferFaker;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MixedDebtPositionMapperTest {

  private static final LocalDate DATE = LocalDate.of(2099, 1, 1);

  @Mock
  private DebtPositionTypeOrgRepository debtPositionTypeOrgRepositoryMock;
  @Mock
  private MixedDebtPositionTypeOrgRetrieverService mixedDebtPositionTypeOrgRetrieverServiceMock;

  private MixedDebtPositionMapper mapper;

  @BeforeEach
  void init() {
    mapper = new MixedDebtPositionMapper(debtPositionTypeOrgRepositoryMock,
      mixedDebtPositionTypeOrgRetrieverServiceMock);
  }

  @Test
  void givenDPTypeOrgIdWhenMapToDebtPositionDTOThenCorrectMapping() {
    MixedDebtPositionDTO mixedDebtPositionDTO = buildMixedDebtPositionDTO();
    DebtPositionTypeOrg dpTypeOrg = new DebtPositionTypeOrg();
    dpTypeOrg.setDebtPositionTypeOrgId(100L);
    String iud = "IUD";

    TransferDTO expectedTransfer = TransferDTO.builder()
      .transferIndex(0)
      .amountCents(50L)
      .stampType("stampType")
      .stampHashDocument("stampHashDocument")
      .stampProvincialResidence("stampProvincialResidence")
      .iban("IT60X0542811101000000123456")
      .postalIban("IT60X0542811101000000123456")
      .build();
    InstallmentDTO expectedInstallment = InstallmentDTO.builder()
      .status(InstallmentStatus.UNPAID)
      .amountCents(50L)
      .iud(iud)
      .balance(null)
      .dueDate(DATE)
      .debtor(PersonFaker.buildPerson())
      .legacyPaymentMetadata(null)
      .remittanceInformation("Payment Info")
      .sourceFlowName("sourceFlowName")
      .transfers(List.of(expectedTransfer))
      .build();
    PaymentOptionDTO expectedPaymentOption = PaymentOptionDTO.builder()
      .status(PaymentOptionStatus.UNPAID)
      .paymentOptionIndex(0)
      .paymentOptionType(PaymentOptionTypeEnum.SINGLE_INSTALLMENT)
      .installments(List.of(expectedInstallment))
      .build();
    DebtPositionDTO expectedResult = DebtPositionDTO.builder()
      .status(DebtPositionStatus.UNPAID)
      .debtPositionOrigin(DebtPositionOrigin.ORDINARY)
      .organizationId(500L)
      .flagIuvVolatile(true)
      .flagPuPagoPaPayment(true)
      .multiDebtor(false)
      .debtPositionTypeOrgId(dpTypeOrg.getDebtPositionTypeOrgId())
      .paymentOptions(List.of(expectedPaymentOption))
      .build();

    try (MockedStatic<Utilities> utilities = Mockito.mockStatic(
      Utilities.class)) {
      utilities.when(Utilities::getRandomIUD).thenReturn(iud);

      when(
        debtPositionTypeOrgRepositoryMock.findByOrganizationIdAndDebtPositionTypeOrgId(
          anyLong(), eq(DEBT_POSITION_TYPE_MIXED)))
        .thenReturn(Optional.of(dpTypeOrg));

      DebtPositionDTO result = mapper.mapToDebtPositionDTO(
        mixedDebtPositionDTO);

      assertEquals(expectedResult, result);

      verifyNoInteractions(mixedDebtPositionTypeOrgRetrieverServiceMock);
    }
  }

  @Test
  void givenMissingDPTypeOrgIdWhenMapToDebtPositionDTOThenCorrectMapping() {
    MixedDebtPositionDTO mixedDebtPositionDTO = buildMixedDebtPositionDTO();
    DebtPositionTypeOrg dpTypeOrg = new DebtPositionTypeOrg();
    dpTypeOrg.setDebtPositionTypeOrgId(100L);
    String iud = "IUD";

    TransferDTO expectedTransfer = TransferDTO.builder()
      .transferIndex(0)
      .amountCents(50L)
      .stampType("stampType")
      .stampHashDocument("stampHashDocument")
      .stampProvincialResidence("stampProvincialResidence")
      .iban("IT60X0542811101000000123456")
      .postalIban("IT60X0542811101000000123456")
      .build();
    InstallmentDTO expectedInstallment = InstallmentDTO.builder()
      .status(InstallmentStatus.UNPAID)
      .amountCents(50L)
      .iud(iud)
      .balance(null)
      .dueDate(DATE)
      .debtor(PersonFaker.buildPerson())
      .legacyPaymentMetadata(null)
      .remittanceInformation("Payment Info")
      .sourceFlowName("sourceFlowName")
      .transfers(List.of(expectedTransfer))
      .build();
    PaymentOptionDTO expectedPaymentOption = PaymentOptionDTO.builder()
      .status(PaymentOptionStatus.UNPAID)
      .paymentOptionIndex(0)
      .paymentOptionType(PaymentOptionTypeEnum.SINGLE_INSTALLMENT)
      .installments(List.of(expectedInstallment))
      .build();
    DebtPositionDTO expectedResult = DebtPositionDTO.builder()
      .status(DebtPositionStatus.UNPAID)
      .debtPositionOrigin(DebtPositionOrigin.ORDINARY)
      .organizationId(500L)
      .flagIuvVolatile(true)
      .flagPuPagoPaPayment(true)
      .multiDebtor(false)
      .debtPositionTypeOrgId(dpTypeOrg.getDebtPositionTypeOrgId())
      .paymentOptions(List.of(expectedPaymentOption))
      .build();

    try (MockedStatic<Utilities> utilities = Mockito.mockStatic(
      Utilities.class)) {
      utilities.when(Utilities::getRandomIUD).thenReturn(iud);

      when(
        debtPositionTypeOrgRepositoryMock.findByOrganizationIdAndDebtPositionTypeOrgId(
          anyLong(), eq(DEBT_POSITION_TYPE_MIXED)))
        .thenReturn(Optional.empty());
      when(
        mixedDebtPositionTypeOrgRetrieverServiceMock.getMixedDebtPositionTypeOrg(
          anyLong()))
        .thenReturn(dpTypeOrg);

      DebtPositionDTO result = mapper.mapToDebtPositionDTO(
        mixedDebtPositionDTO);

      assertEquals(expectedResult, result);
    }
  }

  @Test
  void givenNullWhenMapToDebtPositionDTOThenNull() {
    assertNull(mapper.mapToDebtPositionDTO(null));
  }

  @Test
  void whenBuildDebtPositionTypeOrgId2TransfersDataThenOk() {
    List<MixedTransferDTO> transfers = List.of(
      TransferFaker.buildMixedTransferDTO());

    MixedDpAdditionalData mixedDpAdditionalData = MixedDpAdditionalData.builder()
      .transferIndex(0)
      .iud("IUD")
      .legacyPaymentMetadata("legacyPaymentMetadata")
      .balance("Test Balance")
      .build();
    Map<Long, List<MixedDpAdditionalData>> expected = Map.of(100L,
      List.of(mixedDpAdditionalData));

    Map<Long, List<MixedDpAdditionalData>> result = mapper.buildDebtPositionTypeOrgId2TransfersData(
      transfers);

    assertEquals(expected, result);
  }

  @Test
  void givenNullDebtPositionTypeOrgIdWhenBuildDebtPositionTypeOrgId2TransfersDataThenEmptyMap() {
    MixedTransferDTO transfer = TransferFaker.buildMixedTransferDTO();
    transfer.setDebtPositionTypeOrgId(null);
    List<MixedTransferDTO> transfers = List.of(transfer);

    Map<Long, List<MixedDpAdditionalData>> result = mapper.buildDebtPositionTypeOrgId2TransfersData(
      transfers);

    assertTrue(result.isEmpty());
  }
}
