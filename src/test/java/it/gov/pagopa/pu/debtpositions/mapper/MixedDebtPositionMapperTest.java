package it.gov.pagopa.pu.debtpositions.mapper;

import it.gov.pagopa.pu.debtpositions.connector.organization.service.OrganizationService;
import it.gov.pagopa.pu.debtpositions.dto.MixedDpAdditionalData;
import it.gov.pagopa.pu.debtpositions.dto.generated.*;
import it.gov.pagopa.pu.debtpositions.enums.PaymentOptionType;
import it.gov.pagopa.pu.debtpositions.exception.custom.InvalidParamException;
import it.gov.pagopa.pu.debtpositions.exception.custom.NotFoundException;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrg;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionTypeOrgRepository;
import it.gov.pagopa.pu.debtpositions.service.CategoryResolverService;
import it.gov.pagopa.pu.debtpositions.service.dptypeorg.MixedDebtPositionTypeOrgRetrieverService;
import it.gov.pagopa.pu.debtpositions.util.ErrorCodeConstants;
import it.gov.pagopa.pu.debtpositions.util.SecurityUtilsTest;
import it.gov.pagopa.pu.debtpositions.util.Utilities;
import it.gov.pagopa.pu.debtpositions.util.faker.PersonFaker;
import it.gov.pagopa.pu.debtpositions.util.faker.TransferFaker;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import it.gov.pagopa.pu.organization.dto.generated.OrganizationStation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static it.gov.pagopa.pu.debtpositions.util.Utilities.getTaxonomyCodeFromCategory;
import static it.gov.pagopa.pu.debtpositions.util.faker.DebtPositionFaker.buildMixedDebtPositionDTO;
import static it.gov.pagopa.pu.debtpositions.util.faker.DebtPositionTypeOrgFaker.buildDebtPositionTypeOrg;
import static it.gov.pagopa.pu.debtpositions.util.faker.OrganizationFaker.buildOrganization;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MixedDebtPositionMapperTest {

  private static final LocalDate DATE = LocalDate.of(2099, 1, 1);

  private final String accessToken = "accessToken";

  @Mock
  private MixedDebtPositionTypeOrgRetrieverService mixedDebtPositionTypeOrgRetrieverServiceMock;
  @Mock
  private CategoryResolverService categoryResolverServiceMock;
  @Mock
  private DebtPositionTypeOrgRepository debtPositionTypeOrgRepositoryMock;
  @Mock
  private OrganizationService organizationServiceMock;

  private MixedDebtPositionMapper mapper;

  @BeforeEach
  void init() {
    mapper = new MixedDebtPositionMapper(
      mixedDebtPositionTypeOrgRetrieverServiceMock,
      categoryResolverServiceMock,
      debtPositionTypeOrgRepositoryMock,
      organizationServiceMock
    );
    SecurityUtilsTest.configureSecurityContext(accessToken, "USERID");
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(
      mixedDebtPositionTypeOrgRetrieverServiceMock,
      categoryResolverServiceMock,
      debtPositionTypeOrgRepositoryMock,
      organizationServiceMock
    );
  }

  @Test
  void whenMapToDebtPositionDTOThenCorrectMapping() {
    Organization organization = buildOrganization();
    MixedDebtPositionDTO mixedDebtPositionDTO = buildMixedDebtPositionDTO();
    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();
    DebtPositionTypeOrg dpTypeOrg = new DebtPositionTypeOrg();
    dpTypeOrg.setDebtPositionTypeOrgId(100L);
    String iud = "IUD";
    String category = "9/01234567/";

    TransferDTO expectedTransfer = TransferDTO.builder()
      .transferIndex(1)
      .orgFiscalCode(organization.getOrgFiscalCode())
      .orgName(organization.getOrgName())
      .amountCents(50L)
      .stampType("stampType")
      .stampHashDocument("stampHashDocument")
      .stampProvincialResidence("stampProvincialResidence")
      .iban("IT60X0542811101000000123456")
      .postalIban("IT60X0542811101000000123456")
      .category(category)
      .remittanceInformation("Payment Info")
      .build();
    InstallmentDTO expectedInstallment = InstallmentDTO.builder()
      .status(InstallmentStatus.UNPAID)
      .amountCents(50L)
      .iud(iud)
      .balance(null)
      .dueDate(DATE)
      .debtor(PersonFaker.buildPerson())
      .legacyPaymentMetadata(null)
      .remittanceInformation("Causali multiple")
      .sourceFlowName("sourceFlowName")
      .transfers(List.of(expectedTransfer))
      .build();
    PaymentOptionDTO expectedPaymentOption = PaymentOptionDTO.builder()
      .status(PaymentOptionStatus.UNPAID)
      .paymentOptionIndex(1)
      .paymentOptionType(PaymentOptionType.SINGLE_INSTALLMENT)
      .installments(List.of(expectedInstallment))
      .build();
    DebtPositionDTO expectedResult = DebtPositionDTO.builder()
      .status(DebtPositionStatus.UNPAID)
      .debtPositionOrigin(DebtPositionOrigin.ORDINARY)
      .organizationId(500L)
      .description("Test Description")
      .flagPuPagoPaPayment(true)
      .multiDebtor(false)
      .debtPositionTypeOrgId(dpTypeOrg.getDebtPositionTypeOrgId())
      .paymentOptions(List.of(expectedPaymentOption))
      .stationId("stationId")
      .build();

    try (MockedStatic<Utilities> utilities = Mockito.mockStatic(Utilities.class)) {
      utilities.when(Utilities::getRandomIUD).thenReturn(iud);
      utilities.when(() -> getTaxonomyCodeFromCategory("9/01234567/", "9/", "/")).thenReturn(category);

      when(
        mixedDebtPositionTypeOrgRetrieverServiceMock.getMixedDebtPositionTypeOrg(
          anyLong()))
        .thenReturn(dpTypeOrg);

      when(
        debtPositionTypeOrgRepositoryMock.findById(
          anyLong()))
        .thenReturn(Optional.of(debtPositionTypeOrg));

      when(categoryResolverServiceMock.resolveCategory("9/01234567/xxxxx", debtPositionTypeOrg.getDebtPositionTypeId(), organization.getOrgTypeCode()))
        .thenReturn("9/01234567/");

      when(organizationServiceMock.getOrganizationStationByOrganizationIdAndStationId(
        mixedDebtPositionDTO.getOrganizationId(), mixedDebtPositionDTO.getStationId(), accessToken
      )).thenReturn(Optional.of(OrganizationStation.builder().organizationId(1L).stationId("stationId").build()));

      DebtPositionDTO result = mapper.mapToDebtPositionDTO(organization,
          mixedDebtPositionDTO);

      assertEquals(expectedResult, result);
    }
  }

  @Test
  void givenInvalidStationIdWhenMapToDebtPositionDTOThenThrowInvalidParamException() {
    //GIVEN
    Organization organization = buildOrganization();
    MixedDebtPositionDTO mixedDebtPositionDTO = buildMixedDebtPositionDTO();
    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();
    DebtPositionTypeOrg dpTypeOrg = new DebtPositionTypeOrg();
    dpTypeOrg.setDebtPositionTypeOrgId(100L);
    String iud = "IUD";
    String category = "9/01234567/";

    try (MockedStatic<Utilities> utilities = Mockito.mockStatic(Utilities.class)) {
      utilities.when(Utilities::getRandomIUD).thenReturn(iud);
      utilities.when(() -> getTaxonomyCodeFromCategory("9/01234567/", "9/", "/")).thenReturn(category);

      when(
        mixedDebtPositionTypeOrgRetrieverServiceMock.getMixedDebtPositionTypeOrg(
          anyLong()))
        .thenReturn(dpTypeOrg);

      when(
        debtPositionTypeOrgRepositoryMock.findById(
          anyLong()))
        .thenReturn(Optional.of(debtPositionTypeOrg));

      when(categoryResolverServiceMock.resolveCategory("9/01234567/xxxxx", debtPositionTypeOrg.getDebtPositionTypeId(), organization.getOrgTypeCode()))
        .thenReturn("9/01234567/");

      when(organizationServiceMock.getOrganizationStationByOrganizationIdAndStationId(
        mixedDebtPositionDTO.getOrganizationId(), mixedDebtPositionDTO.getStationId(), accessToken
      )).thenReturn(Optional.empty());

      //WHEN
      InvalidParamException exception = Assertions.assertThrows(InvalidParamException.class, () -> mapper.mapToDebtPositionDTO(organization,
        mixedDebtPositionDTO));
      //THEN
      Assertions.assertEquals(ErrorCodeConstants.ERROR_CODE_INVALID_STATION_ID, exception.getCode());
      Assertions.assertEquals("Invalid station id %s for Organization %d".formatted(mixedDebtPositionDTO.getStationId(), mixedDebtPositionDTO.getOrganizationId()), exception.getMessage());
    }
  }

  @Test
  void givenNoStationIdWhenMapToDebtPositionDTOThenDefaultStationId() {
    Organization organization = buildOrganization();
    MixedDebtPositionDTO mixedDebtPositionDTO = buildMixedDebtPositionDTO();
    mixedDebtPositionDTO.setStationId(null);
    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();
    DebtPositionTypeOrg dpTypeOrg = new DebtPositionTypeOrg();
    dpTypeOrg.setDebtPositionTypeOrgId(100L);
    String iud = "IUD";
    String category = "9/01234567/";

    TransferDTO expectedTransfer = TransferDTO.builder()
      .transferIndex(1)
      .orgFiscalCode(organization.getOrgFiscalCode())
      .orgName(organization.getOrgName())
      .amountCents(50L)
      .stampType("stampType")
      .stampHashDocument("stampHashDocument")
      .stampProvincialResidence("stampProvincialResidence")
      .iban("IT60X0542811101000000123456")
      .postalIban("IT60X0542811101000000123456")
      .category(category)
      .remittanceInformation("Payment Info")
      .build();
    InstallmentDTO expectedInstallment = InstallmentDTO.builder()
      .status(InstallmentStatus.UNPAID)
      .amountCents(50L)
      .iud(iud)
      .balance(null)
      .dueDate(DATE)
      .debtor(PersonFaker.buildPerson())
      .legacyPaymentMetadata(null)
      .remittanceInformation("Causali multiple")
      .sourceFlowName("sourceFlowName")
      .transfers(List.of(expectedTransfer))
      .build();
    PaymentOptionDTO expectedPaymentOption = PaymentOptionDTO.builder()
      .status(PaymentOptionStatus.UNPAID)
      .paymentOptionIndex(1)
      .paymentOptionType(PaymentOptionType.SINGLE_INSTALLMENT)
      .installments(List.of(expectedInstallment))
      .build();
    DebtPositionDTO expectedResult = DebtPositionDTO.builder()
      .status(DebtPositionStatus.UNPAID)
      .debtPositionOrigin(DebtPositionOrigin.ORDINARY)
      .organizationId(500L)
      .description("Test Description")
      .flagPuPagoPaPayment(true)
      .multiDebtor(false)
      .debtPositionTypeOrgId(dpTypeOrg.getDebtPositionTypeOrgId())
      .paymentOptions(List.of(expectedPaymentOption))
      .stationId("defaultStationId")
      .build();

    try (MockedStatic<Utilities> utilities = Mockito.mockStatic(Utilities.class)) {
      utilities.when(Utilities::getRandomIUD).thenReturn(iud);
      utilities.when(() -> getTaxonomyCodeFromCategory("9/01234567/", "9/", "/")).thenReturn(category);

      when(
        mixedDebtPositionTypeOrgRetrieverServiceMock.getMixedDebtPositionTypeOrg(
          anyLong()))
        .thenReturn(dpTypeOrg);

      when(
        debtPositionTypeOrgRepositoryMock.findById(
          anyLong()))
        .thenReturn(Optional.of(debtPositionTypeOrg));

      when(categoryResolverServiceMock.resolveCategory("9/01234567/xxxxx", debtPositionTypeOrg.getDebtPositionTypeId(), organization.getOrgTypeCode()))
        .thenReturn("9/01234567/");

      when(organizationServiceMock.getOrganizationStationByOrganizationIdAndStationId(
        ArgumentMatchers.eq(organization.getOrganizationId()),
        ArgumentMatchers.isNull(),
        ArgumentMatchers.eq(accessToken)
      )).thenReturn(Optional.of(OrganizationStation.builder().organizationId(1L).stationId("defaultStationId").build()));

      DebtPositionDTO result = mapper.mapToDebtPositionDTO(organization,
        mixedDebtPositionDTO);

      assertEquals(expectedResult, result);
    }
  }

  @Test
  void givenDPWithNoStationIdAndNoDefaultStationIdWhenMapToDebtPositionDTOThenThrowNotFoundException() {
    //GIVEN
    Organization organization = buildOrganization();
    MixedDebtPositionDTO mixedDebtPositionDTO = buildMixedDebtPositionDTO();
    mixedDebtPositionDTO.setStationId(null);
    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();
    DebtPositionTypeOrg dpTypeOrg = new DebtPositionTypeOrg();
    dpTypeOrg.setDebtPositionTypeOrgId(100L);
    String iud = "IUD";
    String category = "9/01234567/";

    try (MockedStatic<Utilities> utilities = Mockito.mockStatic(Utilities.class)) {
      utilities.when(Utilities::getRandomIUD).thenReturn(iud);
      utilities.when(() -> getTaxonomyCodeFromCategory("9/01234567/", "9/", "/")).thenReturn(category);

      when(
        mixedDebtPositionTypeOrgRetrieverServiceMock.getMixedDebtPositionTypeOrg(
          anyLong()))
        .thenReturn(dpTypeOrg);

      when(
        debtPositionTypeOrgRepositoryMock.findById(
          anyLong()))
        .thenReturn(Optional.of(debtPositionTypeOrg));

      when(categoryResolverServiceMock.resolveCategory("9/01234567/xxxxx", debtPositionTypeOrg.getDebtPositionTypeId(), organization.getOrgTypeCode()))
        .thenReturn("9/01234567/");

      when(organizationServiceMock.getOrganizationStationByOrganizationIdAndStationId(
        ArgumentMatchers.eq(organization.getOrganizationId()),
        ArgumentMatchers.isNull(),
        ArgumentMatchers.eq(accessToken)
      )).thenReturn(Optional.empty());

      //WHEN
      NotFoundException exception = Assertions.assertThrows(NotFoundException.class, () -> mapper.mapToDebtPositionDTO(organization,
        mixedDebtPositionDTO));
      //THEN
      Assertions.assertEquals(ErrorCodeConstants.ERROR_CODE_DEFAULT_ORGANIZATION_STATION_NOT_FOUND, exception.getCode());
      Assertions.assertEquals("Unable to find a default organization station for Organization %d".formatted(organization.getOrganizationId()), exception.getMessage());
    }
  }

  @Test
  void givenNullWhenMapToDebtPositionDTOThenNull() {
    assertNull(mapper.mapToDebtPositionDTO(new Organization(), null));
  }

  @Test
  void whenBuildDebtPositionTypeOrgId2TransfersDataThenOk() {
    List<MixedTransferDTO> transfers = List.of(
      TransferFaker.buildMixedTransferDTO());

    MixedDpAdditionalData mixedDpAdditionalData = MixedDpAdditionalData.builder()
      .transferIndex(1)
      .iud("IUD")
      .legacyPaymentMetadata("9/01234567/xxxxx")
      .balance("Test Balance")
      .build();
    Map<Long, List<MixedDpAdditionalData>> expected = Map.of(100L,
      List.of(mixedDpAdditionalData));

    Map<Long, List<MixedDpAdditionalData>> result = mapper.buildDebtPositionTypeOrgId2TransfersData(
      transfers);

    assertEquals(expected, result);
  }

}
