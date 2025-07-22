package it.gov.pagopa.pu.debtpositions.service.create;

import it.gov.pagopa.pu.debtpositions.connector.classification.service.BalanceService;
import it.gov.pagopa.pu.debtpositions.connector.organization.service.TaxonomyService;
import it.gov.pagopa.pu.debtpositions.dto.generated.*;
import it.gov.pagopa.pu.debtpositions.exception.custom.ConflictErrorException;
import it.gov.pagopa.pu.debtpositions.exception.custom.InvalidValueException;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrg;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionRepository;
import it.gov.pagopa.pu.debtpositions.util.faker.PaymentOptionFaker;
import it.gov.pagopa.pu.organization.dto.generated.PagedModelTaxonomy;
import it.gov.pagopa.pu.organization.dto.generated.PagedModelTaxonomyEmbedded;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static it.gov.pagopa.pu.debtpositions.util.faker.DebtPositionFaker.buildDebtPosition;
import static it.gov.pagopa.pu.debtpositions.util.faker.DebtPositionFaker.buildDebtPositionDTO;
import static it.gov.pagopa.pu.debtpositions.util.faker.DebtPositionTypeOrgFaker.buildDebtPositionTypeOrg;
import static it.gov.pagopa.pu.debtpositions.util.faker.TaxonomyFaker.buildTaxonomy;
import static it.gov.pagopa.pu.debtpositions.util.faker.TransferFaker.buildTransferDTO;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ValidateDebtPositionServiceImplTest {

  private ValidateDebtPositionService service;

  @Mock
  private DebtPositionRepository debtPositionRepository;

  @Mock
  private TaxonomyService taxonomyService;

  @Mock
  private BalanceService balanceServiceMock;

  private final String accessToken = "ACCESSTOKEN";

  @BeforeEach
  void init() {
    service = new ValidateDebtPositionServiceImpl(taxonomyService, debtPositionRepository, balanceServiceMock, false);
  }

  @Test
  void givenDebtPositionDuplicatedThenThrowValidationException() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();

    Mockito.when(debtPositionRepository.findEntityGraphByIupdOrgAndOrganizationId(debtPositionDTO.getIupdOrg(), debtPositionDTO.getOrganizationId())).thenReturn(buildDebtPosition());

    ConflictErrorException conflictErrorException = assertThrows(ConflictErrorException.class, () -> service.validate(debtPositionDTO, accessToken, debtPositionTypeOrg));
    assertEquals("Duplicate records found: DebtPosition with same iupdOrg " + debtPositionDTO.getIupdOrg() + " conflicts with existing records.", conflictErrorException.getMessage());
  }

  @Test
  void givenDebtPositionTypeOrgNotFoundThenThrowValidationException() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();

    InvalidValueException invalidValueException = assertThrows(InvalidValueException.class, () -> service.validate(debtPositionDTO, accessToken, null));
    assertEquals("Debt position type organization is mandatory", invalidValueException.getMessage());
  }

  @Test
  void givenDebtPositionTypeOrgCodeNullThenThrowValidationException() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();
    debtPositionTypeOrg.setCode(null);

    Mockito.when(debtPositionRepository.findEntityGraphByIupdOrgAndOrganizationId(debtPositionDTO.getIupdOrg(), debtPositionDTO.getOrganizationId())).thenReturn(null);

    InvalidValueException invalidValueException = assertThrows(InvalidValueException.class, () -> service.validate(debtPositionDTO, accessToken, debtPositionTypeOrg));
    assertEquals("Debt position type organization is mandatory", invalidValueException.getMessage());
  }

  @Test
  void givenPaymentOptionsNullThenThrowValidationException() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();
    debtPositionDTO.setPaymentOptions(null);

    Mockito.when(debtPositionRepository.findEntityGraphByIupdOrgAndOrganizationId(debtPositionDTO.getIupdOrg(), debtPositionDTO.getOrganizationId())).thenReturn(null);

    InvalidValueException invalidValueException = assertThrows(InvalidValueException.class, () -> service.validate(debtPositionDTO, accessToken, debtPositionTypeOrg));
    assertEquals("Debt position payment options is mandatory", invalidValueException.getMessage());
  }

  @Test
  void givenDuplicatePaymentOptionIndexThenThrowValidationException() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();

    debtPositionDTO.setPaymentOptions(List.of(PaymentOptionFaker.buildPaymentOptionDTO(), PaymentOptionFaker.buildPaymentOptionDTO()));
    PagedModelTaxonomy pagedModelTaxonomy = PagedModelTaxonomy.builder()
      .embedded(PagedModelTaxonomyEmbedded.builder()
        .taxonomies(List.of(buildTaxonomy()))
        .build())
      .build();

    Mockito.when(debtPositionRepository.findEntityGraphByIupdOrgAndOrganizationId(debtPositionDTO.getIupdOrg(), debtPositionDTO.getOrganizationId())).thenReturn(null);
    Mockito.when(balanceServiceMock.isValidBalance(Mockito.anyString(), Mockito.anyString())).thenReturn(Boolean.TRUE);
    Mockito.when(taxonomyService.getTaxonomies("00", "11", "222", "33", 0, 5, null, accessToken)).thenReturn(pagedModelTaxonomy);


    InvalidValueException invalidValueException = assertThrows(InvalidValueException.class, () -> service.validate(debtPositionDTO, accessToken, debtPositionTypeOrg));
    assertEquals("PaymentOption index duplicated: 1", invalidValueException.getMessage());
  }

  @Test
  void givenInstallmentListEmptyThenThrowValidationException() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();
    debtPositionDTO.getPaymentOptions().getFirst().setInstallments(List.of());

    Mockito.when(debtPositionRepository.findEntityGraphByIupdOrgAndOrganizationId(debtPositionDTO.getIupdOrg(), debtPositionDTO.getOrganizationId())).thenReturn(null);

    InvalidValueException invalidValueException = assertThrows(InvalidValueException.class, () -> service.validate(debtPositionDTO, accessToken, debtPositionTypeOrg));
    assertEquals("At least one installment of the debt position is mandatory", invalidValueException.getMessage());
  }

  @Test
  void givenInstallmentWithoutRemittanceInfoThenThrowValidationException() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();
    debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().setRemittanceInformation(null);

    Mockito.when(debtPositionRepository.findEntityGraphByIupdOrgAndOrganizationId(debtPositionDTO.getIupdOrg(), debtPositionDTO.getOrganizationId())).thenReturn(null);

    InvalidValueException invalidValueException = assertThrows(InvalidValueException.class, () -> service.validate(debtPositionDTO, accessToken, debtPositionTypeOrg));
    assertEquals("Remittance information is mandatory", invalidValueException.getMessage());
  }

  @Test
  void givenInstallmentWithDueDateRetroactiveThenThrowValidationException() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();
    LocalDate localDate = LocalDate.of(2024, 5, 15);
    debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().setDueDate(localDate);

    Mockito.when(debtPositionRepository.findEntityGraphByIupdOrgAndOrganizationId(debtPositionDTO.getIupdOrg(), debtPositionDTO.getOrganizationId())).thenReturn(null);

    InvalidValueException invalidValueException = assertThrows(InvalidValueException.class, () -> service.validate(debtPositionDTO, accessToken, debtPositionTypeOrg));
    assertEquals("The due date cannot be retroactive", invalidValueException.getMessage());
  }

  @Test
  void givenInstallmentWithDueDateNullButMandatoryThenThrowValidationException() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();
    debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().setDueDate(null);

    Mockito.when(debtPositionRepository.findEntityGraphByIupdOrgAndOrganizationId(debtPositionDTO.getIupdOrg(), debtPositionDTO.getOrganizationId())).thenReturn(null);

    InvalidValueException invalidValueException = assertThrows(InvalidValueException.class, () -> service.validate(debtPositionDTO, accessToken, debtPositionTypeOrg));
    assertEquals("The due date is mandatory", invalidValueException.getMessage());
  }

  @Test
  void givenInstallmentWithAmountInvalidThenThrowValidationException() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();
    debtPositionDTO.setDebtPositionOrigin(DebtPositionOrigin.SPONTANEOUS);
    debtPositionTypeOrg.setFlagMandatoryDueDate(false);
    debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().setAmountCents(-200L);

    Mockito.when(debtPositionRepository.findEntityGraphByIupdOrgAndOrganizationId(debtPositionDTO.getIupdOrg(), debtPositionDTO.getOrganizationId())).thenReturn(null);

    InvalidValueException invalidValueException = assertThrows(InvalidValueException.class, () -> service.validate(debtPositionDTO, accessToken, debtPositionTypeOrg));
    assertEquals("Amount is not valid", invalidValueException.getMessage());
  }

  @Test
  void givenInstallmentWithAmountInvalidForDebtPositionTypeThenThrowValidationException() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    debtPositionDTO.setDebtPositionOrigin(DebtPositionOrigin.SPONTANEOUS);
    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();
    debtPositionTypeOrg.setAmountCents(200L);
    debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().setAmountCents(100L);

    Mockito.when(debtPositionRepository.findEntityGraphByIupdOrgAndOrganizationId(debtPositionDTO.getIupdOrg(), debtPositionDTO.getOrganizationId())).thenReturn(null);

    InvalidValueException invalidValueException = assertThrows(InvalidValueException.class, () -> service.validate(debtPositionDTO, accessToken, debtPositionTypeOrg));
    assertEquals("Amount is not valid for this debt position type org", invalidValueException.getMessage());
  }

  @Test
  void givenInstallmentWithBalanceInvalidThenThrowValidationException() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    debtPositionDTO.setDebtPositionOrigin(DebtPositionOrigin.SPONTANEOUS);
    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();

    Mockito.when(debtPositionRepository.findEntityGraphByIupdOrgAndOrganizationId(debtPositionDTO.getIupdOrg(), debtPositionDTO.getOrganizationId())).thenReturn(null);
    Mockito.when(balanceServiceMock.isValidBalance(Mockito.anyString(), Mockito.anyString())).thenReturn(Boolean.FALSE);

    InvalidValueException invalidValueException = assertThrows(InvalidValueException.class, () -> service.validate(debtPositionDTO, accessToken, debtPositionTypeOrg));
    assertEquals("Balance is not formally valid", invalidValueException.getMessage());
  }

  @Test
  void givenPersonNullThenThrowValidationException() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    debtPositionDTO.setDebtPositionOrigin(DebtPositionOrigin.SPONTANEOUS);
    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();
    debtPositionTypeOrg.setAmountCents(null);
    debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().setDebtor(null);

    Mockito.when(debtPositionRepository.findEntityGraphByIupdOrgAndOrganizationId(debtPositionDTO.getIupdOrg(), debtPositionDTO.getOrganizationId())).thenReturn(null);
    Mockito.when(balanceServiceMock.isValidBalance(Mockito.anyString(), Mockito.anyString())).thenReturn(Boolean.TRUE);

    InvalidValueException invalidValueException = assertThrows(InvalidValueException.class, () -> service.validate(debtPositionDTO, accessToken, debtPositionTypeOrg));
    assertEquals("The debtor is mandatory for installment", invalidValueException.getMessage());
  }

  @Test
  void givenFiscalCodeNullThenThrowValidationException() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();
    debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().getDebtor().setFiscalCode(null);
    debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().setBalance(null);

    Mockito.when(debtPositionRepository.findEntityGraphByIupdOrgAndOrganizationId(debtPositionDTO.getIupdOrg(), debtPositionDTO.getOrganizationId())).thenReturn(null);

    InvalidValueException invalidValueException = assertThrows(InvalidValueException.class, () -> service.validate(debtPositionDTO, accessToken, debtPositionTypeOrg));
    assertEquals("Fiscal code is mandatory", invalidValueException.getMessage());
  }

  @Test
  void givenPersonWithAnonymousCFButNotAnonymousFlagThenThrowValidationException() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();
    debtPositionTypeOrg.setFlagAnonymousFiscalCode(false);
    debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().getDebtor().setFiscalCode("ANONIMO");

    Mockito.when(debtPositionRepository.findEntityGraphByIupdOrgAndOrganizationId(debtPositionDTO.getIupdOrg(), debtPositionDTO.getOrganizationId())).thenReturn(null);
    Mockito.when(balanceServiceMock.isValidBalance(Mockito.anyString(), Mockito.anyString())).thenReturn(Boolean.TRUE);

    InvalidValueException invalidValueException = assertThrows(InvalidValueException.class, () -> service.validate(debtPositionDTO, accessToken, debtPositionTypeOrg));
    assertEquals("The debt position type org does not allow an anonymous unique identification code", invalidValueException.getMessage());
  }

  @Test
  void givenPersonWithInvalidFiscalCodeThenThrowValidationException() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();
    debtPositionTypeOrg.setFlagAnonymousFiscalCode(false);
    debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().getDebtor().setFiscalCode("INVALID_FISCAL_CODE");

    Mockito.when(debtPositionRepository.findEntityGraphByIupdOrgAndOrganizationId(debtPositionDTO.getIupdOrg(), debtPositionDTO.getOrganizationId())).thenReturn(null);
    Mockito.when(balanceServiceMock.isValidBalance(Mockito.anyString(), Mockito.anyString())).thenReturn(Boolean.TRUE);

    InvalidValueException invalidValueException = assertThrows(InvalidValueException.class, () -> service.validate(debtPositionDTO, accessToken, debtPositionTypeOrg));
    assertEquals("Fiscal code of person is not valid", invalidValueException.getMessage());
  }

  @Test
  void givenLegalPersonWithInvalidFiscalCodeThenThrowValidationException() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();
    debtPositionTypeOrg.setFlagAnonymousFiscalCode(false);
    debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().getDebtor().setEntityType(PersonEntityType.G);
    debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().getDebtor().setFiscalCode("00000");

    Mockito.when(debtPositionRepository.findEntityGraphByIupdOrgAndOrganizationId(debtPositionDTO.getIupdOrg(), debtPositionDTO.getOrganizationId())).thenReturn(null);
    Mockito.when(balanceServiceMock.isValidBalance(Mockito.anyString(), Mockito.anyString())).thenReturn(Boolean.TRUE);

    InvalidValueException invalidValueException = assertThrows(InvalidValueException.class, () -> service.validate(debtPositionDTO, accessToken, debtPositionTypeOrg));
    assertEquals("P. iva of legal person is not valid", invalidValueException.getMessage());
  }

  @Test
  void givenPersonWithNullFullNameThenThrowValidationException() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    debtPositionDTO.setDebtPositionOrigin(DebtPositionOrigin.REPORTING_PAGOPA);
    debtPositionDTO.setStatus(DebtPositionStatus.PAID);
    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();
    debtPositionTypeOrg.setFlagAnonymousFiscalCode(true);
    debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().getDebtor().setFiscalCode("ANONIMO");
    debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().getDebtor().setFullName(null);

    Mockito.when(debtPositionRepository.findEntityGraphByIupdOrgAndOrganizationId(debtPositionDTO.getIupdOrg(), debtPositionDTO.getOrganizationId())).thenReturn(null);
    Mockito.when(balanceServiceMock.isValidBalance(Mockito.anyString(), Mockito.anyString())).thenReturn(Boolean.TRUE);

    InvalidValueException invalidValueException = assertThrows(InvalidValueException.class, () -> service.validate(debtPositionDTO, accessToken, debtPositionTypeOrg));
    assertEquals("Beneficiary name is mandatory", invalidValueException.getMessage());
  }

  @Test
  void givenPersonWithInvalidEmailThenThrowValidationException() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    debtPositionDTO.setDebtPositionOrigin(DebtPositionOrigin.RECEIPT_PAGOPA);
    debtPositionDTO.setStatus(DebtPositionStatus.PAID);
    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();
    debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().getDebtor().setEntityType(PersonEntityType.G);
    debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().getDebtor().setFiscalCode("01234567890");
    debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().getDebtor().setEmail("test&it");

    Mockito.when(debtPositionRepository.findEntityGraphByIupdOrgAndOrganizationId(debtPositionDTO.getIupdOrg(), debtPositionDTO.getOrganizationId())).thenReturn(null);
    Mockito.when(balanceServiceMock.isValidBalance(Mockito.anyString(), Mockito.anyString())).thenReturn(Boolean.TRUE);

    InvalidValueException invalidValueException = assertThrows(InvalidValueException.class, () -> service.validate(debtPositionDTO, accessToken, debtPositionTypeOrg));
    assertEquals("Email is not valid", invalidValueException.getMessage());
  }

  @Test
  void givenNoTransfersThenThrowValidationException() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    debtPositionDTO.setDebtPositionOrigin(DebtPositionOrigin.SECONDARY_ORG);
    debtPositionDTO.setStatus(DebtPositionStatus.PAID);
    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();
    debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().setTransfers(null);

    Mockito.when(debtPositionRepository.findEntityGraphByIupdOrgAndOrganizationId(debtPositionDTO.getIupdOrg(), debtPositionDTO.getOrganizationId())).thenReturn(null);
    Mockito.when(balanceServiceMock.isValidBalance(Mockito.anyString(), Mockito.anyString())).thenReturn(Boolean.TRUE);

    InvalidValueException invalidValueException = assertThrows(InvalidValueException.class, () -> service.validate(debtPositionDTO, accessToken, debtPositionTypeOrg));
    assertEquals("At least one transfer is mandatory for installment", invalidValueException.getMessage());
  }

  @Test
  void givenMoreTransfersThan5ThenThrowValidationException() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    debtPositionDTO.setDebtPositionOrigin(DebtPositionOrigin.SPONTANEOUS);
    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();
    TransferDTO firstTransfer = buildTransferDTO();
    TransferDTO secondTransfer = buildTransferDTO();
    TransferDTO thirdTransfer = buildTransferDTO();
    TransferDTO fourthTransfer = buildTransferDTO();
    TransferDTO fifthTransfer = buildTransferDTO();
    TransferDTO sixthTransfer = buildTransferDTO();
    List<TransferDTO> transfers = List.of(firstTransfer, secondTransfer, thirdTransfer, fourthTransfer, fifthTransfer, sixthTransfer);
    debtPositionDTO.getPaymentOptions()
      .getFirst()
      .getInstallments()
      .getFirst()
      .setTransfers(new ArrayList<>(transfers));

    Mockito.when(debtPositionRepository.findEntityGraphByIupdOrgAndOrganizationId(debtPositionDTO.getIupdOrg(), debtPositionDTO.getOrganizationId())).thenReturn(null);
    Mockito.when(balanceServiceMock.isValidBalance(Mockito.anyString(), Mockito.anyString())).thenReturn(Boolean.TRUE);

    InvalidValueException invalidValueException = assertThrows(InvalidValueException.class, () -> service.validate(debtPositionDTO, accessToken, debtPositionTypeOrg));
    assertEquals("At most 5 transfers is allowed for installment", invalidValueException.getMessage());
  }

  @Test
  void givenInvalidTransferIndexLowerBoundThenThrowValidationException() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    debtPositionDTO.setDebtPositionOrigin(DebtPositionOrigin.SPONTANEOUS);
    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();
    TransferDTO transfer = debtPositionDTO.getPaymentOptions()
      .getFirst()
      .getInstallments()
      .getFirst()
      .getTransfers().getFirst();
    transfer.setTransferIndex(0);

    Mockito.when(debtPositionRepository.findEntityGraphByIupdOrgAndOrganizationId(debtPositionDTO.getIupdOrg(), debtPositionDTO.getOrganizationId())).thenReturn(null);
    Mockito.when(balanceServiceMock.isValidBalance(Mockito.anyString(), Mockito.anyString())).thenReturn(Boolean.TRUE);

    InvalidValueException invalidValueException = assertThrows(InvalidValueException.class, () -> service.validate(debtPositionDTO, accessToken, debtPositionTypeOrg));
    assertEquals("Transfer index should be between 1 and 5, provided: 0", invalidValueException.getMessage());
  }

  @Test
  void givenInvalidTransferIndexUpperBoundThenThrowValidationException() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    debtPositionDTO.setDebtPositionOrigin(DebtPositionOrigin.SPONTANEOUS);
    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();
    TransferDTO transfer = debtPositionDTO.getPaymentOptions()
      .getFirst()
      .getInstallments()
      .getFirst()
      .getTransfers().getFirst();
    transfer.setTransferIndex(6);

    Mockito.when(debtPositionRepository.findEntityGraphByIupdOrgAndOrganizationId(debtPositionDTO.getIupdOrg(), debtPositionDTO.getOrganizationId())).thenReturn(null);
    Mockito.when(balanceServiceMock.isValidBalance(Mockito.anyString(), Mockito.anyString())).thenReturn(Boolean.TRUE);

    InvalidValueException invalidValueException = assertThrows(InvalidValueException.class, () -> service.validate(debtPositionDTO, accessToken, debtPositionTypeOrg));
    assertEquals("Transfer index should be between 1 and 5, provided: 6", invalidValueException.getMessage());
  }

  @Test
  void givenTransferPIVANullThenThrowValidationException() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    debtPositionDTO.setDebtPositionOrigin(DebtPositionOrigin.ORDINARY_SIL);
    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();
    TransferDTO transfer = debtPositionDTO.getPaymentOptions()
      .getFirst()
      .getInstallments()
      .getFirst()
      .getTransfers().getFirst();
    transfer.setOrgFiscalCode(null);

    Mockito.when(debtPositionRepository.findEntityGraphByIupdOrgAndOrganizationId(debtPositionDTO.getIupdOrg(), debtPositionDTO.getOrganizationId())).thenReturn(null);
    Mockito.when(balanceServiceMock.isValidBalance(Mockito.anyString(), Mockito.anyString())).thenReturn(Boolean.TRUE);

    InvalidValueException invalidValueException = assertThrows(InvalidValueException.class, () -> service.validate(debtPositionDTO, accessToken, debtPositionTypeOrg));
    assertEquals("Fiscal code of transfer with index 1 is not valid", invalidValueException.getMessage());
  }

  @Test
  void givenTransferPIVANotValidThenThrowValidationException() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    debtPositionDTO.setDebtPositionOrigin(DebtPositionOrigin.ORDINARY_SIL);
    debtPositionDTO.setStatus(DebtPositionStatus.DRAFT);
    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();
    TransferDTO transfer = debtPositionDTO.getPaymentOptions()
      .getFirst()
      .getInstallments()
      .getFirst()
      .getTransfers().getFirst();
    transfer.setOrgFiscalCode("111111");

    Mockito.when(debtPositionRepository.findEntityGraphByIupdOrgAndOrganizationId(debtPositionDTO.getIupdOrg(), debtPositionDTO.getOrganizationId())).thenReturn(null);
    Mockito.when(balanceServiceMock.isValidBalance(Mockito.anyString(), Mockito.anyString())).thenReturn(Boolean.TRUE);

    InvalidValueException invalidValueException = assertThrows(InvalidValueException.class, () -> service.validate(debtPositionDTO, accessToken, debtPositionTypeOrg));
    assertEquals("Fiscal code of transfer with index 1 is not valid", invalidValueException.getMessage());
  }

  @ParameterizedTest
  @ValueSource(strings = {"stampType", "stampHashDocument", "stampProvincialResidence"})
  void givenTransferIbanWithStampValuedThenThrowValidationException(String stampField) {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();
    TransferDTO transfer = debtPositionDTO.getPaymentOptions()
      .getFirst()
      .getInstallments()
      .getFirst()
      .getTransfers().getFirst();

    switch (stampField) {
      case "stampType" -> transfer.setStampType("test");
      case "stampHashDocument" -> transfer.setStampHashDocument("test");
      case "stampProvincialResidence" -> transfer.setStampProvincialResidence("test");
      default -> { return; }
    }

    Mockito.when(debtPositionRepository.findEntityGraphByIupdOrgAndOrganizationId(debtPositionDTO.getIupdOrg(), debtPositionDTO.getOrganizationId())).thenReturn(null);
    Mockito.when(balanceServiceMock.isValidBalance(Mockito.anyString(), Mockito.anyString())).thenReturn(Boolean.TRUE);

    InvalidValueException invalidValueException = assertThrows(InvalidValueException.class, () -> service.validate(debtPositionDTO, accessToken, debtPositionTypeOrg));
    assertEquals("Stamp attributes of transfer with index 1 has to be null when iban is valued", invalidValueException.getMessage());
  }

  @Test
  void givenTransferIbanInvalidThenThrowValidationException() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();
    TransferDTO transfer = debtPositionDTO.getPaymentOptions()
      .getFirst()
      .getInstallments()
      .getFirst()
      .getTransfers().getFirst();
    transfer.setIban("ITkb");
    transfer.setStampType(null);

    Mockito.when(debtPositionRepository.findEntityGraphByIupdOrgAndOrganizationId(debtPositionDTO.getIupdOrg(), debtPositionDTO.getOrganizationId())).thenReturn(null);
    Mockito.when(balanceServiceMock.isValidBalance(Mockito.anyString(), Mockito.anyString())).thenReturn(Boolean.TRUE);

    InvalidValueException invalidValueException = assertThrows(InvalidValueException.class, () -> service.validate(debtPositionDTO, accessToken, debtPositionTypeOrg));
    assertEquals("Iban of transfer with index 1 is not valid", invalidValueException.getMessage());
  }

  @Test
  void givenTransferPostalIbanInvalidThenThrowValidationException() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();
    TransferDTO transfer = debtPositionDTO.getPaymentOptions()
      .getFirst()
      .getInstallments()
      .getFirst()
      .getTransfers().getFirst();
    transfer.setPostalIban("ITkb");
    transfer.setStampType(null);

    Mockito.when(debtPositionRepository.findEntityGraphByIupdOrgAndOrganizationId(debtPositionDTO.getIupdOrg(), debtPositionDTO.getOrganizationId())).thenReturn(null);
    Mockito.when(balanceServiceMock.isValidBalance(Mockito.anyString(), Mockito.anyString())).thenReturn(Boolean.TRUE);

    InvalidValueException invalidValueException = assertThrows(InvalidValueException.class, () -> service.validate(debtPositionDTO, accessToken, debtPositionTypeOrg));
    assertEquals("Postal iban of transfer with index 1 is not valid", invalidValueException.getMessage());
  }


  @ParameterizedTest
  @ValueSource(strings = {"stampType", "stampHashDocument", "stampProvincialResidence"})
  void givenTransferStampFieldsNotAllValuedThenThrowValidationException(String stampField) {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();
    TransferDTO transfer = debtPositionDTO.getPaymentOptions()
      .getFirst()
      .getInstallments()
      .getFirst()
      .getTransfers().getFirst();
    transfer.setIban(null);
    transfer.setStampType("stampType");
    transfer.setStampHashDocument("stampHash");
    transfer.setStampProvincialResidence("stampProvRes");

    switch (stampField) {
      case "stampType" -> transfer.setStampType(null);
      case "stampHashDocument" -> transfer.setStampHashDocument(null);
      case "stampProvincialResidence" -> transfer.setStampProvincialResidence(null);
      default -> { return; }
    }

    Mockito.when(debtPositionRepository.findEntityGraphByIupdOrgAndOrganizationId(debtPositionDTO.getIupdOrg(), debtPositionDTO.getOrganizationId())).thenReturn(null);
    Mockito.when(balanceServiceMock.isValidBalance(Mockito.anyString(), Mockito.anyString())).thenReturn(Boolean.TRUE);

    InvalidValueException invalidValueException = assertThrows(InvalidValueException.class, () -> service.validate(debtPositionDTO, accessToken, debtPositionTypeOrg));
    assertEquals("Stamp attributes of transfer with index 1 has to be all valued when iban is null", invalidValueException.getMessage());
  }

  @Test
  void givenTransferCategoryNullThenThrowValidationException() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();
    TransferDTO transfer = debtPositionDTO.getPaymentOptions()
      .getFirst()
      .getInstallments()
      .getFirst()
      .getTransfers().getFirst();
    transfer.setCategory(null);
    transfer.setStampType(null);
    transfer.setPostalIban(null);

    Mockito.when(debtPositionRepository.findEntityGraphByIupdOrgAndOrganizationId(debtPositionDTO.getIupdOrg(), debtPositionDTO.getOrganizationId())).thenReturn(null);
    Mockito.when(balanceServiceMock.isValidBalance(Mockito.anyString(), Mockito.anyString())).thenReturn(Boolean.TRUE);

    InvalidValueException invalidValueException = assertThrows(InvalidValueException.class, () -> service.validate(debtPositionDTO, accessToken, debtPositionTypeOrg));
    assertEquals("Category of transfer with index 1 is mandatory", invalidValueException.getMessage());
  }

  @Test
  void givenPagedTaxonomiesNullThenThrowValidationException() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();

    Mockito.when(debtPositionRepository.findEntityGraphByIupdOrgAndOrganizationId(debtPositionDTO.getIupdOrg(), debtPositionDTO.getOrganizationId())).thenReturn(null);
    Mockito.when(balanceServiceMock.isValidBalance(Mockito.anyString(), Mockito.anyString())).thenReturn(Boolean.TRUE);
    Mockito.when(taxonomyService.getTaxonomies("00", "11", "222", "33", 0, 5, null, accessToken)).thenReturn(null);

    InvalidValueException invalidValueException = assertThrows(InvalidValueException.class, () -> service.validate(debtPositionDTO, accessToken, debtPositionTypeOrg));
    assertEquals("The category code 001122233 does not exist in the archive", invalidValueException.getMessage());
  }

  @Test
  void givenPagedTaxonomiesEmbeddedNullThenThrowValidationException() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();

    PagedModelTaxonomy pagedModelTaxonomy = PagedModelTaxonomy.builder()
      .embedded(null).build();

    Mockito.when(debtPositionRepository.findEntityGraphByIupdOrgAndOrganizationId(debtPositionDTO.getIupdOrg(), debtPositionDTO.getOrganizationId())).thenReturn(null);
    Mockito.when(balanceServiceMock.isValidBalance(Mockito.anyString(), Mockito.anyString())).thenReturn(Boolean.TRUE);
    Mockito.when(taxonomyService.getTaxonomies("00", "11", "222", "33", 0, 5, null, accessToken)).thenReturn(pagedModelTaxonomy);

    InvalidValueException invalidValueException = assertThrows(InvalidValueException.class, () -> service.validate(debtPositionDTO, accessToken, debtPositionTypeOrg));
    assertEquals("The category code 001122233 does not exist in the archive", invalidValueException.getMessage());
  }

  @Test
  void givenPagedTaxonomiesEmbeddedEmptyThenThrowValidationException() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();

    PagedModelTaxonomy pagedModelTaxonomy = PagedModelTaxonomy.builder()
      .embedded(PagedModelTaxonomyEmbedded.builder()
        .taxonomies(List.of()).build()).build();

    Mockito.when(debtPositionRepository.findEntityGraphByIupdOrgAndOrganizationId(debtPositionDTO.getIupdOrg(), debtPositionDTO.getOrganizationId())).thenReturn(null);
    Mockito.when(balanceServiceMock.isValidBalance(Mockito.anyString(), Mockito.anyString())).thenReturn(Boolean.TRUE);
    Mockito.when(taxonomyService.getTaxonomies("00", "11", "222", "33", 0, 5, null, accessToken)).thenReturn(pagedModelTaxonomy);

    InvalidValueException invalidValueException = assertThrows(InvalidValueException.class, () -> service.validate(debtPositionDTO, accessToken, debtPositionTypeOrg));
    assertEquals("The category code 001122233 does not exist in the archive", invalidValueException.getMessage());
  }

  @Test
  void givenTransferCategoryInvalidEmptyThenThrowValidationException() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();
    debtPositionDTO.getPaymentOptions()
      .getFirst()
      .getInstallments()
      .getFirst()
      .getTransfers().getFirst().setCategory("0011223");

    Mockito.when(debtPositionRepository.findEntityGraphByIupdOrgAndOrganizationId(debtPositionDTO.getIupdOrg(), debtPositionDTO.getOrganizationId())).thenReturn(null);
    Mockito.when(balanceServiceMock.isValidBalance(Mockito.anyString(), Mockito.anyString())).thenReturn(Boolean.TRUE);

    InvalidValueException invalidValueException = assertThrows(InvalidValueException.class, () -> service.validate(debtPositionDTO, accessToken, debtPositionTypeOrg));
    assertEquals("The category code 0011223 does not meet the required length or format", invalidValueException.getMessage());
  }

  @Test
  void givenTransferAmountNegativeThenThrowValidationException() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();
    TransferDTO transfer = debtPositionDTO.getPaymentOptions()
      .getFirst()
      .getInstallments()
      .getFirst()
      .getTransfers().getFirst();
    transfer.setAmountCents(-12L);

    Mockito.when(debtPositionRepository.findEntityGraphByIupdOrgAndOrganizationId(debtPositionDTO.getIupdOrg(), debtPositionDTO.getOrganizationId())).thenReturn(null);
    Mockito.when(balanceServiceMock.isValidBalance(Mockito.anyString(), Mockito.anyString())).thenReturn(Boolean.TRUE);

    InvalidValueException invalidValueException = assertThrows(InvalidValueException.class, () -> service.validate(debtPositionDTO, accessToken, debtPositionTypeOrg));
    assertEquals("The amount of transfer with index 1 must be greater than 0", invalidValueException.getMessage());
  }

  @Test
  void givenTransfersAmountsDifferentToInstallmentAmountThenThrowValidationException() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();
    TransferDTO transfer = debtPositionDTO.getPaymentOptions()
      .getFirst()
      .getInstallments()
      .getFirst()
      .getTransfers().getFirst();
    transfer.setAmountCents(120L);

    PagedModelTaxonomy pagedModelTaxonomy = PagedModelTaxonomy.builder()
      .embedded(PagedModelTaxonomyEmbedded.builder()
        .taxonomies(List.of(buildTaxonomy()))
        .build())
      .build();

    Mockito.when(debtPositionRepository.findEntityGraphByIupdOrgAndOrganizationId(debtPositionDTO.getIupdOrg(), debtPositionDTO.getOrganizationId())).thenReturn(null);
    Mockito.when(balanceServiceMock.isValidBalance(Mockito.anyString(), Mockito.anyString())).thenReturn(Boolean.TRUE);
    Mockito.when(taxonomyService.getTaxonomies("00", "11", "222", "33", 0, 5, null, accessToken)).thenReturn(pagedModelTaxonomy);

    InvalidValueException invalidValueException = assertThrows(InvalidValueException.class, () -> service.validate(debtPositionDTO, accessToken, debtPositionTypeOrg));
    assertEquals("The sum of transfers amounts has to be equal to installment amount", invalidValueException.getMessage());
  }

  @Test
  void givenSecondTransferThenSuccess() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    debtPositionDTO.setStatus(DebtPositionStatus.DRAFT);
    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();
    TransferDTO firstTransfer = buildTransferDTO();
    TransferDTO secondTransfer = buildTransferDTO();
    secondTransfer.setTransferIndex(2);
    List<TransferDTO> transfers = List.of(firstTransfer, secondTransfer);
    debtPositionDTO.getPaymentOptions()
      .getFirst()
      .getInstallments()
      .getFirst()
      .setTransfers(new ArrayList<>(transfers));
    debtPositionDTO.getPaymentOptions()
      .getFirst()
      .getInstallments().getFirst().setAmountCents(200L);

    PagedModelTaxonomy pagedModelTaxonomy = PagedModelTaxonomy.builder()
      .embedded(PagedModelTaxonomyEmbedded.builder()
        .taxonomies(List.of(buildTaxonomy()))
        .build())
      .build();

    Mockito.when(debtPositionRepository.findEntityGraphByIupdOrgAndOrganizationId(debtPositionDTO.getIupdOrg(), debtPositionDTO.getOrganizationId())).thenReturn(null);
    Mockito.when(balanceServiceMock.isValidBalance(Mockito.anyString(), Mockito.anyString())).thenReturn(Boolean.TRUE);
    Mockito.when(taxonomyService.getTaxonomies("00", "11", "222", "33", 0, 5, null, accessToken)).thenReturn(pagedModelTaxonomy);

    assertDoesNotThrow(() -> service.validate(debtPositionDTO, accessToken, debtPositionTypeOrg));
  }

  @Test
  void testValidateThenSuccess() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().getDebtor().setEmail(null);
    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();

    PagedModelTaxonomy pagedModelTaxonomy = PagedModelTaxonomy.builder()
      .embedded(PagedModelTaxonomyEmbedded.builder()
        .taxonomies(List.of(buildTaxonomy()))
        .build())
      .build();

    Mockito.when(debtPositionRepository.findEntityGraphByIupdOrgAndOrganizationId(debtPositionDTO.getIupdOrg(), debtPositionDTO.getOrganizationId())).thenReturn(null);
    Mockito.when(balanceServiceMock.isValidBalance(Mockito.anyString(), Mockito.anyString())).thenReturn(Boolean.TRUE);
    Mockito.when(taxonomyService.getTaxonomies("00", "11", "222", "33", 0, 5, null, accessToken)).thenReturn(pagedModelTaxonomy);

    assertDoesNotThrow(() -> service.validate(debtPositionDTO, accessToken, debtPositionTypeOrg));
  }

  @Test
  void testValidateWhenDPOriginOrdinaryAndStatusPaidThenThrowInvalidValueException() {
    testValidateDPOrigin(DebtPositionOrigin.ORDINARY, DebtPositionStatus.PAID, "A Debt Position with origin ORDINARY or ORDINARY_SIL can only be created in UNPAID or DRAFT state");
  }

  @Test
  void testValidateWhenDPOriginOrdinarySilAndStatusPaidThenThrowInvalidValueException() {
    testValidateDPOrigin(DebtPositionOrigin.ORDINARY_SIL,DebtPositionStatus.PAID, "A Debt Position with origin ORDINARY or ORDINARY_SIL can only be created in UNPAID or DRAFT state");
  }

  @Test
  void testValidateWhenDPOriginSpontaneousAndStatusUnpaidThenThrowInvalidValueException() {
    testValidateDPOrigin(DebtPositionOrigin.SPONTANEOUS, DebtPositionStatus.PAID, "A Debt Position with origin SPONTANEOUS can only be created in UNPAID state");
  }

  @Test
  void testValidateWhenDPOriginSecondaryOrgAndStatusUnpaidThenThrowInvalidValueException() {
    testValidateDPOrigin(DebtPositionOrigin.SECONDARY_ORG, DebtPositionStatus.UNPAID, "A Debt Position with origin SECONDARY_ORG, RECEIPT_PAGO_PA, RECEIPT_FILE, or REPORTING_PAGOPA can only be created in PAID state");
  }

  @Test
  void testValidateWhenDPReceiptPagoPaAndStatusUnpaidThenThrowInvalidValueException() {
    testValidateDPOrigin(DebtPositionOrigin.RECEIPT_PAGOPA, DebtPositionStatus.UNPAID, "A Debt Position with origin SECONDARY_ORG, RECEIPT_PAGO_PA, RECEIPT_FILE, or REPORTING_PAGOPA can only be created in PAID state");
  }

  @Test
  void testValidateWhenDPReceiptFileAndStatusUnpaidThenThrowInvalidValueException() {
    testValidateDPOrigin(DebtPositionOrigin.RECEIPT_FILE, DebtPositionStatus.UNPAID, "A Debt Position with origin SECONDARY_ORG, RECEIPT_PAGO_PA, RECEIPT_FILE, or REPORTING_PAGOPA can only be created in PAID state");
  }

  @Test
  void testValidateWhenDPReportingPagoPaAndStatusUnpaidThenThrowInvalidValueException() {
    testValidateDPOrigin(DebtPositionOrigin.REPORTING_PAGOPA, DebtPositionStatus.UNPAID, "A Debt Position with origin SECONDARY_ORG, RECEIPT_PAGO_PA, RECEIPT_FILE, or REPORTING_PAGOPA can only be created in PAID state");
  }

  private void testValidateDPOrigin(DebtPositionOrigin origin, DebtPositionStatus status, String errorMessage) {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    debtPositionDTO.setDebtPositionOrigin(origin);
    debtPositionDTO.setStatus(status);
    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();

    InvalidValueException invalidValueException = assertThrows(InvalidValueException.class, () -> service.validate(debtPositionDTO, accessToken, debtPositionTypeOrg));
    assertEquals(errorMessage, invalidValueException.getMessage());
  }
}

