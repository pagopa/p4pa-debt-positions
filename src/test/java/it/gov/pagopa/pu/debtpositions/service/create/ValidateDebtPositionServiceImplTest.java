package it.gov.pagopa.pu.debtpositions.service.create;

import it.gov.pagopa.pu.debtpositions.connector.classification.service.BalanceService;
import it.gov.pagopa.pu.debtpositions.connector.organization.service.BrokerService;
import it.gov.pagopa.pu.debtpositions.connector.organization.service.OrganizationService;
import it.gov.pagopa.pu.debtpositions.dto.generated.*;
import it.gov.pagopa.pu.debtpositions.exception.custom.ConflictErrorException;
import it.gov.pagopa.pu.debtpositions.exception.custom.InvalidValueException;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrg;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionRepository;
import it.gov.pagopa.pu.debtpositions.service.TaxonomyValidatorService;
import it.gov.pagopa.pu.debtpositions.util.faker.InstallmentFaker;
import it.gov.pagopa.pu.debtpositions.util.faker.PaymentOptionFaker;
import it.gov.pagopa.pu.organization.dto.generated.Broker;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionOrigin.*;
import static it.gov.pagopa.pu.debtpositions.util.faker.DebtPositionFaker.buildDebtPosition;
import static it.gov.pagopa.pu.debtpositions.util.faker.DebtPositionFaker.buildDebtPositionDTO;
import static it.gov.pagopa.pu.debtpositions.util.faker.DebtPositionTypeOrgFaker.buildDebtPositionTypeOrg;
import static it.gov.pagopa.pu.debtpositions.util.faker.OrganizationFaker.buildBroker;
import static it.gov.pagopa.pu.debtpositions.util.faker.OrganizationFaker.buildOrganization;
import static it.gov.pagopa.pu.debtpositions.util.faker.TransferFaker.buildTransferDTO;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ValidateDebtPositionServiceImplTest {

  private ValidateDebtPositionService service;

  @Mock
  private DebtPositionRepository debtPositionRepository;

  @Mock
  private TaxonomyValidatorService taxonomyValidatorService;

  @Mock
  private BalanceService balanceServiceMock;

  @Mock
  private OrganizationService organizationServiceMock;

  @Mock
  private BrokerService brokerServiceMock;

  private final String accessToken = "ACCESSTOKEN";
  private final Organization orgOwner = buildOrganization();
  private final Broker broker = buildBroker();

  @BeforeEach
  void init() {
    service = new ValidateDebtPositionServiceImpl(taxonomyValidatorService, debtPositionRepository, balanceServiceMock, organizationServiceMock, brokerServiceMock,false, "9/", "9");
  }

  @Test
  void givenDebtPositionDuplicatedThenThrowValidationException() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();

    Mockito.when(debtPositionRepository.findEntityGraphByIupdOrgAndOrganizationId(debtPositionDTO.getIupdOrg(), debtPositionDTO.getOrganizationId())).thenReturn(buildDebtPosition());

    ConflictErrorException conflictErrorException = assertThrows(ConflictErrorException.class, () -> service.validate(debtPositionDTO, orgOwner, accessToken, debtPositionTypeOrg));
    assertEquals("[DEBT_POSITION_ALREADY_EXISTS] Duplicate records found: DebtPosition with same iupdOrg " + debtPositionDTO.getIupdOrg() + " conflicts with existing records.", conflictErrorException.getMessage());
  }

  @Test
  void givenDebtPositionTypeOrgNotFoundThenThrowValidationException() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();

    InvalidValueException invalidValueException = assertThrows(InvalidValueException.class, () -> service.validate(debtPositionDTO, orgOwner, accessToken, null));
    assertEquals("[MISSING_DEBT_POSITION_TYPE_ORG] Debt position type organization is mandatory", invalidValueException.getMessage());
  }

  @Test
  void givenDebtPositionTypeOrgCodeNullThenThrowValidationException() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();
    debtPositionTypeOrg.setCode(null);

    Mockito.when(debtPositionRepository.findEntityGraphByIupdOrgAndOrganizationId(debtPositionDTO.getIupdOrg(), debtPositionDTO.getOrganizationId())).thenReturn(null);

    InvalidValueException invalidValueException = assertThrows(InvalidValueException.class, () -> service.validate(debtPositionDTO, orgOwner, accessToken, debtPositionTypeOrg));
    assertEquals("[MISSING_DEBT_POSITION_TYPE_ORG] Debt position type organization is mandatory", invalidValueException.getMessage());
  }

  @Test
  void givenPaymentOptionsNullThenThrowValidationException() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();
    debtPositionDTO.setPaymentOptions(null);

    Mockito.when(debtPositionRepository.findEntityGraphByIupdOrgAndOrganizationId(debtPositionDTO.getIupdOrg(), debtPositionDTO.getOrganizationId())).thenReturn(null);

    InvalidValueException invalidValueException = assertThrows(InvalidValueException.class, () -> service.validate(debtPositionDTO, orgOwner, accessToken, debtPositionTypeOrg));
    assertEquals("[MISSING_PAYMENT_OPTION] Debt position payment options is mandatory", invalidValueException.getMessage());
  }

  @Test
  void givenDuplicatePaymentOptionIndexThenThrowValidationException() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();
    Organization org = buildOrganization();

    debtPositionDTO.setPaymentOptions(List.of(PaymentOptionFaker.buildPaymentOptionDTO(), PaymentOptionFaker.buildPaymentOptionDTO()));
    InstallmentDTO firstInstallment = debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst();

    Mockito.when(debtPositionRepository.findEntityGraphByIupdOrgAndOrganizationId(debtPositionDTO.getIupdOrg(), debtPositionDTO.getOrganizationId())).thenReturn(null);
    Mockito.when(balanceServiceMock.isValidBalance(firstInstallment.getBalance(), firstInstallment.getAmountCents(), accessToken)).thenReturn(Boolean.TRUE);
    Mockito.when(brokerServiceMock.findById(orgOwner.getBrokerId(), accessToken)).thenReturn(broker);
    Mockito.when(organizationServiceMock.getOrganizationByFiscalCode(Mockito.anyString(), Mockito.anyString())).thenReturn(Optional.ofNullable(org));
    Mockito.when(taxonomyValidatorService.isTaxonomyCategoryValid("001122233", org.getOrgTypeCode())).thenReturn(true);


    InvalidValueException invalidValueException = assertThrows(InvalidValueException.class, () -> service.validate(debtPositionDTO, orgOwner, accessToken, debtPositionTypeOrg));
    assertEquals("[DUPLICATED_PAYMENT_OPTION_INDEX] PaymentOption index duplicated: 1", invalidValueException.getMessage());
  }

  @Test
  void givenInstallmentListEmptyThenThrowValidationException() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();
    debtPositionDTO.getPaymentOptions().getFirst().setInstallments(List.of());

    Mockito.when(debtPositionRepository.findEntityGraphByIupdOrgAndOrganizationId(debtPositionDTO.getIupdOrg(), debtPositionDTO.getOrganizationId())).thenReturn(null);

    InvalidValueException invalidValueException = assertThrows(InvalidValueException.class, () -> service.validate(debtPositionDTO, orgOwner, accessToken, debtPositionTypeOrg));
    assertEquals("[MISSING_INSTALLMENT] At least one installment of the debt position is mandatory", invalidValueException.getMessage());
  }

  @Test
  void givenInstallmentWithoutRemittanceInfoThenThrowValidationException() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();
    debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().setRemittanceInformation(null);

    Mockito.when(debtPositionRepository.findEntityGraphByIupdOrgAndOrganizationId(debtPositionDTO.getIupdOrg(), debtPositionDTO.getOrganizationId())).thenReturn(null);

    InvalidValueException invalidValueException = assertThrows(InvalidValueException.class, () -> service.validate(debtPositionDTO, orgOwner, accessToken, debtPositionTypeOrg));
    assertEquals("[MISSING_REMITTANCE_INFORMATION] Remittance information is mandatory", invalidValueException.getMessage());
  }

  @Test
  void givenInstallmentWithDueDateRetroactiveThenThrowValidationException() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();
    LocalDate localDate = LocalDate.of(2024, 5, 15);
    debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().setDueDate(localDate);

    Mockito.when(debtPositionRepository.findEntityGraphByIupdOrgAndOrganizationId(debtPositionDTO.getIupdOrg(), debtPositionDTO.getOrganizationId())).thenReturn(null);

    InvalidValueException invalidValueException = assertThrows(InvalidValueException.class, () -> service.validate(debtPositionDTO, orgOwner, accessToken, debtPositionTypeOrg));
    assertEquals("[INVALID_DUE_DATE] The due date cannot be retroactive", invalidValueException.getMessage());
  }

  @Test
  void givenInstallmentWithDueDateNullButMandatoryThenThrowValidationException() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();
    debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().setDueDate(null);

    Mockito.when(debtPositionRepository.findEntityGraphByIupdOrgAndOrganizationId(debtPositionDTO.getIupdOrg(), debtPositionDTO.getOrganizationId())).thenReturn(null);

    InvalidValueException invalidValueException = assertThrows(InvalidValueException.class, () -> service.validate(debtPositionDTO, orgOwner, accessToken, debtPositionTypeOrg));
    assertEquals("[MISSING_DUE_DATE] The due date is mandatory", invalidValueException.getMessage());
  }

  @Test
  void givenInstallmentWithNegativeAmountThenThrowInvalidValueException() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();
    debtPositionDTO.setDebtPositionOrigin(DebtPositionOrigin.SPONTANEOUS);
    debtPositionTypeOrg.setFlagMandatoryDueDate(false);
    debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().setAmountCents(-200L);

    Mockito.when(debtPositionRepository.findEntityGraphByIupdOrgAndOrganizationId(debtPositionDTO.getIupdOrg(), debtPositionDTO.getOrganizationId())).thenReturn(null);

    InvalidValueException invalidValueException = assertThrows(InvalidValueException.class, () -> service.validate(debtPositionDTO, orgOwner, accessToken, debtPositionTypeOrg));
    assertEquals("[INVALID_CENTS_AMOUNT] The installment amount must be greater than 0", invalidValueException.getMessage());
  }

  @Test
  void givenInstallmentWithZeroAmountThenThrowInvalidValueException() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();
    debtPositionDTO.setDebtPositionOrigin(DebtPositionOrigin.SPONTANEOUS);
    debtPositionTypeOrg.setFlagMandatoryDueDate(false);
    debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().setAmountCents(0L);

    Mockito.when(debtPositionRepository.findEntityGraphByIupdOrgAndOrganizationId(debtPositionDTO.getIupdOrg(), debtPositionDTO.getOrganizationId())).thenReturn(null);

    InvalidValueException invalidValueException = assertThrows(InvalidValueException.class, () -> service.validate(debtPositionDTO, orgOwner, accessToken, debtPositionTypeOrg));
    assertEquals("[INVALID_CENTS_AMOUNT] The installment amount must be greater than 0", invalidValueException.getMessage());
  }

  @Test
  void givenInstallmentWithAmountInvalidForDebtPositionTypeThenThrowValidationException() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    debtPositionDTO.setDebtPositionOrigin(DebtPositionOrigin.SPONTANEOUS_PSP);
    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();
    debtPositionTypeOrg.setAmountCents(200L);
    debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().setAmountCents(100L);

    Mockito.when(debtPositionRepository.findEntityGraphByIupdOrgAndOrganizationId(debtPositionDTO.getIupdOrg(), debtPositionDTO.getOrganizationId())).thenReturn(null);

    InvalidValueException invalidValueException = assertThrows(InvalidValueException.class, () -> service.validate(debtPositionDTO, orgOwner, accessToken, debtPositionTypeOrg));
    assertEquals("[INVALID_AMOUNT] Amount is not valid for this debt position type org", invalidValueException.getMessage());
  }

  @Test
  void givenInstallmentWithInvalidLegacyPaymentMetadataThenThrowValidationException() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();
    PaymentOptionDTO paymentOption = debtPositionDTO.getPaymentOptions().getFirst();
    InstallmentDTO installment = paymentOption.getInstallments().getFirst();
    installment.setLegacyPaymentMetadata("invalidMetadata");
    paymentOption.setInstallments(List.of(installment));

    Mockito.when(debtPositionRepository.findEntityGraphByIupdOrgAndOrganizationId(debtPositionDTO.getIupdOrg(), debtPositionDTO.getOrganizationId())).thenReturn(null);
    Mockito.when(balanceServiceMock.isValidBalance(installment.getBalance(), installment.getAmountCents(), accessToken)).thenReturn(Boolean.TRUE);

    InvalidValueException invalidValueException = assertThrows(InvalidValueException.class, () -> service.validate(debtPositionDTO, orgOwner, accessToken, debtPositionTypeOrg));
    assertEquals("[INVALID_LEGACY_PAYMENT_METADATA] Legacy payment metadata is not valid", invalidValueException.getMessage());
  }

  @Test
  void givenInstallmentWithBalanceInvalidThenThrowValidationException() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    debtPositionDTO.setDebtPositionOrigin(DebtPositionOrigin.SPONTANEOUS);
    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();
    InstallmentDTO firstInstallment = debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst();

    Mockito.when(debtPositionRepository.findEntityGraphByIupdOrgAndOrganizationId(debtPositionDTO.getIupdOrg(), debtPositionDTO.getOrganizationId())).thenReturn(null);
    Mockito.when(balanceServiceMock.isValidBalance(firstInstallment.getBalance(), firstInstallment.getAmountCents(), accessToken)).thenReturn(Boolean.FALSE);

    InvalidValueException invalidValueException = assertThrows(InvalidValueException.class, () -> service.validate(debtPositionDTO, orgOwner, accessToken, debtPositionTypeOrg));
    assertEquals("[INVALID_BALANCE] Balance is not formally valid", invalidValueException.getMessage());
  }

  @Test
  void givenPersonNullThenThrowValidationException() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    debtPositionDTO.setDebtPositionOrigin(DebtPositionOrigin.SPONTANEOUS);
    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();
    debtPositionTypeOrg.setAmountCents(null);
    InstallmentDTO firstInstallment = debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst();
    firstInstallment.setDebtor(null);

    Mockito.when(debtPositionRepository.findEntityGraphByIupdOrgAndOrganizationId(debtPositionDTO.getIupdOrg(), debtPositionDTO.getOrganizationId())).thenReturn(null);
    Mockito.when(balanceServiceMock.isValidBalance(firstInstallment.getBalance(), firstInstallment.getAmountCents(), accessToken)).thenReturn(Boolean.TRUE);

    InvalidValueException invalidValueException = assertThrows(InvalidValueException.class, () -> service.validate(debtPositionDTO, orgOwner, accessToken, debtPositionTypeOrg));
    assertEquals("[MISSING_DEBTOR] The debtor is mandatory for installment", invalidValueException.getMessage());
  }

  @Test
  void givenFiscalCodeNullThenThrowValidationException() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();
    InstallmentDTO firstInstallment = debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst();
    firstInstallment.getDebtor().setFiscalCode(null);
    firstInstallment.setBalance(null);

    Mockito.when(debtPositionRepository.findEntityGraphByIupdOrgAndOrganizationId(debtPositionDTO.getIupdOrg(), debtPositionDTO.getOrganizationId())).thenReturn(null);

    InvalidValueException invalidValueException = assertThrows(InvalidValueException.class, () -> service.validate(debtPositionDTO, orgOwner, accessToken, debtPositionTypeOrg));
    assertEquals("[INVALID_VAT_CODE] Fiscal code is mandatory", invalidValueException.getMessage());
  }

  @Test
  void givenPersonWithAnonymousCFButNotAnonymousFlagThenThrowValidationException() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();
    debtPositionTypeOrg.setFlagAnonymousFiscalCode(false);
    InstallmentDTO firstInstallment = debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst();
    firstInstallment.getDebtor().setFiscalCode("ANONIMO");

    Mockito.when(debtPositionRepository.findEntityGraphByIupdOrgAndOrganizationId(debtPositionDTO.getIupdOrg(), debtPositionDTO.getOrganizationId())).thenReturn(null);
    Mockito.when(balanceServiceMock.isValidBalance(firstInstallment.getBalance(), firstInstallment.getAmountCents(), accessToken)).thenReturn(Boolean.TRUE);

    InvalidValueException invalidValueException = assertThrows(InvalidValueException.class, () -> service.validate(debtPositionDTO, orgOwner, accessToken, debtPositionTypeOrg));
    assertEquals("[INVALID_VAT_CODE] The debt position type org does not allow an anonymous unique identification code", invalidValueException.getMessage());
  }

  @Test
  void givenPersonWithInvalidFiscalCodeThenThrowValidationException() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();
    debtPositionTypeOrg.setFlagAnonymousFiscalCode(false);
    InstallmentDTO firstInstallment = debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst();
    firstInstallment.getDebtor().setFiscalCode("INVALID_VAT_CODE");

    Mockito.when(debtPositionRepository.findEntityGraphByIupdOrgAndOrganizationId(debtPositionDTO.getIupdOrg(), debtPositionDTO.getOrganizationId())).thenReturn(null);
    Mockito.when(balanceServiceMock.isValidBalance(firstInstallment.getBalance(), firstInstallment.getAmountCents(), accessToken)).thenReturn(Boolean.TRUE);

    InvalidValueException invalidValueException = assertThrows(InvalidValueException.class, () -> service.validate(debtPositionDTO, orgOwner, accessToken, debtPositionTypeOrg));
    assertEquals("[INVALID_VAT_CODE] Fiscal code of person is not valid", invalidValueException.getMessage());
  }

  @Test
  void givenLegalPersonWithInvalidFiscalCodeThenThrowValidationException() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();
    debtPositionTypeOrg.setFlagAnonymousFiscalCode(false);
    InstallmentDTO firstInstallment = debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst();
    firstInstallment.getDebtor().setEntityType(PersonEntityType.G);
    firstInstallment.getDebtor().setFiscalCode("00000");

    Mockito.when(debtPositionRepository.findEntityGraphByIupdOrgAndOrganizationId(debtPositionDTO.getIupdOrg(), debtPositionDTO.getOrganizationId())).thenReturn(null);
    Mockito.when(balanceServiceMock.isValidBalance(firstInstallment.getBalance(), firstInstallment.getAmountCents(), accessToken)).thenReturn(Boolean.TRUE);

    InvalidValueException invalidValueException = assertThrows(InvalidValueException.class, () -> service.validate(debtPositionDTO, orgOwner, accessToken, debtPositionTypeOrg));
    assertEquals("[INVALID_VAT_CODE] Fiscal code or p. iva of legal person is not valid", invalidValueException.getMessage());
  }

  @Test
  void givenPersonWithNullFullNameThenThrowValidationException() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    debtPositionDTO.setDebtPositionOrigin(DebtPositionOrigin.REPORTING_PAGOPA);
    debtPositionDTO.setStatus(DebtPositionStatus.PAID);
    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();
    debtPositionTypeOrg.setFlagAnonymousFiscalCode(true);
    InstallmentDTO firstInstallment = debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst();
    firstInstallment.getDebtor().setFiscalCode("ANONIMO");
    firstInstallment.getDebtor().setFullName(null);

    Mockito.when(debtPositionRepository.findEntityGraphByIupdOrgAndOrganizationId(debtPositionDTO.getIupdOrg(), debtPositionDTO.getOrganizationId())).thenReturn(null);
    Mockito.when(balanceServiceMock.isValidBalance(firstInstallment.getBalance(), firstInstallment.getAmountCents(), accessToken)).thenReturn(Boolean.TRUE);

    InvalidValueException invalidValueException = assertThrows(InvalidValueException.class, () -> service.validate(debtPositionDTO, orgOwner, accessToken, debtPositionTypeOrg));
    assertEquals("[INVALID_FULLNAME] Beneficiary name is mandatory", invalidValueException.getMessage());
  }

  @Test
  void givenPersonWithInvalidEmailThenThrowValidationException() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    debtPositionDTO.setDebtPositionOrigin(DebtPositionOrigin.RECEIPT_PAGOPA);
    debtPositionDTO.setStatus(DebtPositionStatus.PAID);
    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();
    InstallmentDTO firstInstallment = debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst();
    firstInstallment.getDebtor().setEntityType(PersonEntityType.G);
    firstInstallment.getDebtor().setFiscalCode("01234567890");
    firstInstallment.getDebtor().setEmail("test&it");

    Mockito.when(debtPositionRepository.findEntityGraphByIupdOrgAndOrganizationId(debtPositionDTO.getIupdOrg(), debtPositionDTO.getOrganizationId())).thenReturn(null);
    Mockito.when(balanceServiceMock.isValidBalance(firstInstallment.getBalance(), firstInstallment.getAmountCents(), accessToken)).thenReturn(Boolean.TRUE);

    InvalidValueException invalidValueException = assertThrows(InvalidValueException.class, () -> service.validate(debtPositionDTO, orgOwner, accessToken, debtPositionTypeOrg));
    assertEquals("[INVALID_EMAIL] Email is not valid", invalidValueException.getMessage());
  }

  @Test
  void givenNoTransfersThenThrowValidationException() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    debtPositionDTO.setDebtPositionOrigin(DebtPositionOrigin.SECONDARY_ORG);
    debtPositionDTO.setStatus(DebtPositionStatus.PAID);
    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();
    InstallmentDTO firstInstallment = debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst();
    firstInstallment.setTransfers(null);

    Mockito.when(debtPositionRepository.findEntityGraphByIupdOrgAndOrganizationId(debtPositionDTO.getIupdOrg(), debtPositionDTO.getOrganizationId())).thenReturn(null);
    Mockito.when(balanceServiceMock.isValidBalance(firstInstallment.getBalance(), firstInstallment.getAmountCents(), accessToken)).thenReturn(Boolean.TRUE);

    InvalidValueException invalidValueException = assertThrows(InvalidValueException.class, () -> service.validate(debtPositionDTO, orgOwner, accessToken, debtPositionTypeOrg));
    assertEquals("[MISSING_TRANSFER] At least one transfer is mandatory for installment", invalidValueException.getMessage());
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
    InstallmentDTO firstInstallment = debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst();
    firstInstallment.setTransfers(new ArrayList<>(transfers));

    Mockito.when(debtPositionRepository.findEntityGraphByIupdOrgAndOrganizationId(debtPositionDTO.getIupdOrg(), debtPositionDTO.getOrganizationId())).thenReturn(null);
    Mockito.when(balanceServiceMock.isValidBalance(firstInstallment.getBalance(), firstInstallment.getAmountCents(), accessToken)).thenReturn(Boolean.TRUE);

    InvalidValueException invalidValueException = assertThrows(InvalidValueException.class, () -> service.validate(debtPositionDTO, orgOwner, accessToken, debtPositionTypeOrg));
    assertEquals("[TOO_MANY_TRANSFERS_FOR_INSTALLMENT] At most 5 transfers is allowed for installment", invalidValueException.getMessage());
  }

  @Test
  void givenInvalidTransferIndexLowerBoundThenThrowValidationException() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    debtPositionDTO.setDebtPositionOrigin(DebtPositionOrigin.SPONTANEOUS);
    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();
    InstallmentDTO firstInstallment = debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst();
    TransferDTO transfer = firstInstallment.getTransfers().getFirst();
    transfer.setTransferIndex(0);

    Mockito.when(debtPositionRepository.findEntityGraphByIupdOrgAndOrganizationId(debtPositionDTO.getIupdOrg(), debtPositionDTO.getOrganizationId())).thenReturn(null);
    Mockito.when(balanceServiceMock.isValidBalance(firstInstallment.getBalance(), firstInstallment.getAmountCents(), accessToken)).thenReturn(Boolean.TRUE);

    InvalidValueException invalidValueException = assertThrows(InvalidValueException.class, () -> service.validate(debtPositionDTO, orgOwner, accessToken, debtPositionTypeOrg));
    assertEquals("[INVALID_TRANSFER_INDEX] Transfer index should be between 1 and 5, provided: 0", invalidValueException.getMessage());
  }

  @Test
  void givenInvalidTransferIndexUpperBoundThenThrowValidationException() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    debtPositionDTO.setDebtPositionOrigin(DebtPositionOrigin.SPONTANEOUS);
    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();
    InstallmentDTO firstInstallment = debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst();
    TransferDTO transfer = firstInstallment.getTransfers().getFirst();
    transfer.setTransferIndex(6);

    Mockito.when(debtPositionRepository.findEntityGraphByIupdOrgAndOrganizationId(debtPositionDTO.getIupdOrg(), debtPositionDTO.getOrganizationId())).thenReturn(null);
    Mockito.when(balanceServiceMock.isValidBalance(firstInstallment.getBalance(), firstInstallment.getAmountCents(), accessToken)).thenReturn(Boolean.TRUE);

    InvalidValueException invalidValueException = assertThrows(InvalidValueException.class, () -> service.validate(debtPositionDTO, orgOwner, accessToken, debtPositionTypeOrg));
    assertEquals("[INVALID_TRANSFER_INDEX] Transfer index should be between 1 and 5, provided: 6", invalidValueException.getMessage());
  }

  @Test
  void givenTransferPIVANullThenThrowValidationException() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    debtPositionDTO.setDebtPositionOrigin(ORDINARY_SIL);
    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();
    InstallmentDTO firstInstallment = debtPositionDTO.getPaymentOptions()
      .getFirst()
      .getInstallments()
      .getFirst();
    TransferDTO transfer = firstInstallment
      .getTransfers().getFirst();
    transfer.setOrgFiscalCode(null);

    Mockito.when(debtPositionRepository.findEntityGraphByIupdOrgAndOrganizationId(debtPositionDTO.getIupdOrg(), debtPositionDTO.getOrganizationId())).thenReturn(null);
    Mockito.when(balanceServiceMock.isValidBalance(firstInstallment.getBalance(), firstInstallment.getAmountCents(), accessToken)).thenReturn(Boolean.TRUE);

    InvalidValueException invalidValueException = assertThrows(InvalidValueException.class, () -> service.validate(debtPositionDTO, orgOwner, accessToken, debtPositionTypeOrg));
    assertEquals("[INVALID_VAT_CODE] Fiscal code of transfer with index 1 is not valid", invalidValueException.getMessage());
  }

  @Test
  void givenTransferPIVANotValidThenThrowValidationException() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    debtPositionDTO.setDebtPositionOrigin(ORDINARY_SIL);
    debtPositionDTO.setStatus(DebtPositionStatus.DRAFT);
    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();
    InstallmentDTO firstInstallment = debtPositionDTO.getPaymentOptions()
      .getFirst()
      .getInstallments()
      .getFirst();
    TransferDTO transfer = firstInstallment
      .getTransfers().getFirst();
    transfer.setOrgFiscalCode("111111");

    Mockito.when(debtPositionRepository.findEntityGraphByIupdOrgAndOrganizationId(debtPositionDTO.getIupdOrg(), debtPositionDTO.getOrganizationId())).thenReturn(null);
    Mockito.when(balanceServiceMock.isValidBalance(firstInstallment.getBalance(), firstInstallment.getAmountCents(), accessToken)).thenReturn(Boolean.TRUE);

    InvalidValueException invalidValueException = assertThrows(InvalidValueException.class, () -> service.validate(debtPositionDTO, orgOwner, accessToken, debtPositionTypeOrg));
    assertEquals("[INVALID_VAT_CODE] Fiscal code of transfer with index 1 is not valid", invalidValueException.getMessage());
  }

  @ParameterizedTest
  @ValueSource(strings = {"stampType", "stampHashDocument", "stampProvincialResidence"})
  void givenTransferIbanWithStampValuedThenThrowValidationException(String stampField) {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();
    InstallmentDTO firstInstallment = debtPositionDTO.getPaymentOptions()
      .getFirst()
      .getInstallments()
      .getFirst();
    TransferDTO transfer = firstInstallment
      .getTransfers().getFirst();

    switch (stampField) {
      case "stampType" -> transfer.setStampType("test");
      case "stampHashDocument" -> transfer.setStampHashDocument("test");
      case "stampProvincialResidence" -> transfer.setStampProvincialResidence("test");
      default -> { return; }
    }

    Mockito.when(debtPositionRepository.findEntityGraphByIupdOrgAndOrganizationId(debtPositionDTO.getIupdOrg(), debtPositionDTO.getOrganizationId())).thenReturn(null);
    Mockito.when(balanceServiceMock.isValidBalance(firstInstallment.getBalance(), firstInstallment.getAmountCents(), accessToken)).thenReturn(Boolean.TRUE);

    InvalidValueException invalidValueException = assertThrows(InvalidValueException.class, () -> service.validate(debtPositionDTO, orgOwner, accessToken, debtPositionTypeOrg));
    assertEquals("[INVALID_FIELDS] Stamp attributes of transfer with index 1 has to be null when iban is valued", invalidValueException.getMessage());
  }

  @Test
  void givenTransferIbanInvalidThenThrowValidationException() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();
    InstallmentDTO firstInstallment = debtPositionDTO.getPaymentOptions()
      .getFirst()
      .getInstallments()
      .getFirst();
    TransferDTO transfer = firstInstallment
      .getTransfers().getFirst();
    transfer.setIban("ITkb");
    transfer.setStampType(null);

    Mockito.when(debtPositionRepository.findEntityGraphByIupdOrgAndOrganizationId(debtPositionDTO.getIupdOrg(), debtPositionDTO.getOrganizationId())).thenReturn(null);
    Mockito.when(balanceServiceMock.isValidBalance(firstInstallment.getBalance(), firstInstallment.getAmountCents(), accessToken)).thenReturn(Boolean.TRUE);

    InvalidValueException invalidValueException = assertThrows(InvalidValueException.class, () -> service.validate(debtPositionDTO, orgOwner, accessToken, debtPositionTypeOrg));
    assertEquals("[INVALID_IBAN] Iban of transfer with index 1 is not valid", invalidValueException.getMessage());
  }

  @Test
  void givenTransferPostalIbanInvalidThenThrowValidationException() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();
    InstallmentDTO firstInstallment = debtPositionDTO.getPaymentOptions()
      .getFirst()
      .getInstallments()
      .getFirst();
    TransferDTO transfer = firstInstallment
      .getTransfers().getFirst();
    transfer.setPostalIban("ITkb");
    transfer.setStampType(null);

    Mockito.when(debtPositionRepository.findEntityGraphByIupdOrgAndOrganizationId(debtPositionDTO.getIupdOrg(), debtPositionDTO.getOrganizationId())).thenReturn(null);
    Mockito.when(balanceServiceMock.isValidBalance(firstInstallment.getBalance(), firstInstallment.getAmountCents(), accessToken)).thenReturn(Boolean.TRUE);

    InvalidValueException invalidValueException = assertThrows(InvalidValueException.class, () -> service.validate(debtPositionDTO, orgOwner, accessToken, debtPositionTypeOrg));
    assertEquals("[INVALID_POSTAL_IBAN] Postal iban of transfer with index 1 is not valid", invalidValueException.getMessage());
  }


  @ParameterizedTest
  @ValueSource(strings = {"stampType", "stampHashDocument", "stampProvincialResidence"})
  void givenTransferStampFieldsNotAllValuedThenThrowValidationException(String stampField) {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();
    InstallmentDTO firstInstallment = debtPositionDTO.getPaymentOptions()
      .getFirst()
      .getInstallments()
      .getFirst();
    TransferDTO transfer = firstInstallment
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
    Mockito.when(balanceServiceMock.isValidBalance(firstInstallment.getBalance(), firstInstallment.getAmountCents(), accessToken)).thenReturn(Boolean.TRUE);

    InvalidValueException invalidValueException = assertThrows(InvalidValueException.class, () -> service.validate(debtPositionDTO, orgOwner, accessToken, debtPositionTypeOrg));
    assertEquals("[INVALID_FIELDS] Stamp attributes of transfer with index 1 has to be all valued when iban is null", invalidValueException.getMessage());
  }

  @Test
  void givenTransferCategoryNullThenThrowValidationException() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();
    InstallmentDTO firstInstallment = debtPositionDTO.getPaymentOptions()
      .getFirst()
      .getInstallments()
      .getFirst();
    TransferDTO transfer = firstInstallment
      .getTransfers().getFirst();
    transfer.setCategory(null);
    transfer.setStampType(null);
    transfer.setPostalIban(null);

    Mockito.when(debtPositionRepository.findEntityGraphByIupdOrgAndOrganizationId(debtPositionDTO.getIupdOrg(), debtPositionDTO.getOrganizationId())).thenReturn(null);
    Mockito.when(balanceServiceMock.isValidBalance(firstInstallment.getBalance(), firstInstallment.getAmountCents(), accessToken)).thenReturn(Boolean.TRUE);
    Mockito.when(brokerServiceMock.findById(orgOwner.getBrokerId(), accessToken)).thenReturn(broker);
    Mockito.when(organizationServiceMock.getOrganizationByFiscalCode(Mockito.anyString(), Mockito.anyString())).thenReturn(Optional.ofNullable(buildOrganization()));

    InvalidValueException invalidValueException = assertThrows(InvalidValueException.class, () -> service.validate(debtPositionDTO, orgOwner, accessToken, debtPositionTypeOrg));
    assertEquals("[MISSING_TAXONOMY_CATEGORY] Category of transfer with index 1 is mandatory", invalidValueException.getMessage());
  }

  @Test
  void givenTransferAmountNegativeWithBrokerNotDelegateThenThrowValidationException() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();
    InstallmentDTO firstInstallment = debtPositionDTO.getPaymentOptions()
      .getFirst()
      .getInstallments()
      .getFirst();
    TransferDTO transfer = firstInstallment
      .getTransfers().getFirst();
    transfer.setAmountCents(-12L);

    Mockito.when(debtPositionRepository.findEntityGraphByIupdOrgAndOrganizationId(debtPositionDTO.getIupdOrg(), debtPositionDTO.getOrganizationId())).thenReturn(null);
    Mockito.when(balanceServiceMock.isValidBalance(firstInstallment.getBalance(), firstInstallment.getAmountCents(), accessToken)).thenReturn(Boolean.TRUE);
    Mockito.when(brokerServiceMock.findById(orgOwner.getBrokerId(), accessToken)).thenReturn(broker);

    InvalidValueException invalidValueException = assertThrows(InvalidValueException.class, () -> service.validate(debtPositionDTO, orgOwner, accessToken, debtPositionTypeOrg));
    assertEquals("[INVALID_CENTS_AMOUNT] The amount of transfer with index 1 must be greater than 0", invalidValueException.getMessage());
  }

  @Test
  void givenTransferNotOwnerWithAmountNegativeThenThrowValidationException() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();
    InstallmentDTO firstInstallment = debtPositionDTO.getPaymentOptions()
      .getFirst()
      .getInstallments()
      .getFirst();
    TransferDTO transfer = firstInstallment
      .getTransfers().getFirst();
    transfer.setAmountCents(-12L);
    transfer.setOrgFiscalCode("98765432109");
    transfer.setFlagOwner(null);

    Broker brokerDelegate = buildBroker();
    brokerDelegate.setFlagDelegate(Boolean.TRUE);

    Mockito.when(debtPositionRepository.findEntityGraphByIupdOrgAndOrganizationId(debtPositionDTO.getIupdOrg(), debtPositionDTO.getOrganizationId())).thenReturn(null);
    Mockito.when(balanceServiceMock.isValidBalance(firstInstallment.getBalance(), firstInstallment.getAmountCents(), accessToken)).thenReturn(Boolean.TRUE);
    Mockito.when(brokerServiceMock.findById(orgOwner.getBrokerId(), accessToken)).thenReturn(brokerDelegate);

    InvalidValueException invalidValueException = assertThrows(InvalidValueException.class, () -> service.validate(debtPositionDTO, orgOwner, accessToken, debtPositionTypeOrg));
    assertEquals("[INVALID_CENTS_AMOUNT] The amount of transfer with index 1 must be greater than 0", invalidValueException.getMessage());
  }

  @Test
  void givenTransferAmountNegativeWithBrokerDelegateThenThrowValidationException() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();
    InstallmentDTO firstInstallment = debtPositionDTO.getPaymentOptions()
      .getFirst()
      .getInstallments()
      .getFirst();
    TransferDTO transfer = firstInstallment
      .getTransfers().getFirst();
    transfer.setAmountCents(-12L);

    Broker brokerDelegate = buildBroker();
    brokerDelegate.setFlagDelegate(Boolean.TRUE);

    Mockito.when(debtPositionRepository.findEntityGraphByIupdOrgAndOrganizationId(debtPositionDTO.getIupdOrg(), debtPositionDTO.getOrganizationId())).thenReturn(null);
    Mockito.when(balanceServiceMock.isValidBalance(firstInstallment.getBalance(), firstInstallment.getAmountCents(), accessToken)).thenReturn(Boolean.TRUE);
    Mockito.when(brokerServiceMock.findById(orgOwner.getBrokerId(), accessToken)).thenReturn(brokerDelegate);

    InvalidValueException invalidValueException = assertThrows(InvalidValueException.class, () -> service.validate(debtPositionDTO, orgOwner, accessToken, debtPositionTypeOrg));
    assertEquals("[INVALID_CENTS_AMOUNT] The amount of transfer with index 1 must be greater than or equal to 0", invalidValueException.getMessage());
  }

  @Test
  void givenTransfersAmountsDifferentToInstallmentAmountThenThrowValidationException() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();
    InstallmentDTO firstInstallment = debtPositionDTO.getPaymentOptions()
      .getFirst()
      .getInstallments()
      .getFirst();
    TransferDTO transfer = firstInstallment
      .getTransfers().getFirst();
    transfer.setAmountCents(120L);

    Mockito.when(debtPositionRepository.findEntityGraphByIupdOrgAndOrganizationId(debtPositionDTO.getIupdOrg(), debtPositionDTO.getOrganizationId())).thenReturn(null);
    Mockito.when(balanceServiceMock.isValidBalance(firstInstallment.getBalance(), firstInstallment.getAmountCents(), accessToken)).thenReturn(Boolean.TRUE);
    Mockito.when(brokerServiceMock.findById(orgOwner.getBrokerId(), accessToken)).thenReturn(broker);
    Mockito.when(organizationServiceMock.getOrganizationByFiscalCode(Mockito.anyString(), Mockito.anyString())).thenReturn(Optional.empty());
    Mockito.when(taxonomyValidatorService.isTaxonomyCategoryValid("001122233", null)).thenReturn(true);

    InvalidValueException invalidValueException = assertThrows(InvalidValueException.class, () -> service.validate(debtPositionDTO, orgOwner, accessToken, debtPositionTypeOrg));
    assertEquals("[INVALID_INSTALLMENT_AMOUNT_CENTS] The sum of transfers amounts has to be equal to installment amount", invalidValueException.getMessage());
  }

  @Test
  void givenSecondTransferThenSuccess() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    debtPositionDTO.setStatus(DebtPositionStatus.DRAFT);
    debtPositionDTO.setDebtPositionOrigin(DebtPositionOrigin.SPONTANEOUS_SIL);
    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();
    debtPositionTypeOrg.setAmountCents(200L);
    TransferDTO firstTransfer = buildTransferDTO();
    TransferDTO secondTransfer = buildTransferDTO();
    secondTransfer.setTransferIndex(2);
    List<TransferDTO> transfers = List.of(firstTransfer, secondTransfer);
    InstallmentDTO firstInstallment = debtPositionDTO.getPaymentOptions()
      .getFirst()
      .getInstallments()
      .getFirst();
    firstInstallment
      .setTransfers(new ArrayList<>(transfers));
    firstInstallment.setAmountCents(200L);
    Organization org = buildOrganization();

    Mockito.when(debtPositionRepository.findEntityGraphByIupdOrgAndOrganizationId(debtPositionDTO.getIupdOrg(), debtPositionDTO.getOrganizationId())).thenReturn(null);
    Mockito.when(balanceServiceMock.isValidBalance(firstInstallment.getBalance(), firstInstallment.getAmountCents(), accessToken)).thenReturn(Boolean.TRUE);
    Mockito.when(brokerServiceMock.findById(orgOwner.getBrokerId(), accessToken)).thenReturn(broker);
    Mockito.when(organizationServiceMock.getOrganizationByFiscalCode(Mockito.anyString(), Mockito.anyString())).thenReturn(Optional.ofNullable(org));
    Mockito.when(taxonomyValidatorService.isTaxonomyCategoryValid("001122233", org.getOrgTypeCode())).thenReturn(true);

    assertDoesNotThrow(() -> service.validate(debtPositionDTO, orgOwner, accessToken, debtPositionTypeOrg));
  }

  @Test
  void testValidateThenSuccess() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    InstallmentDTO firstInstallment = debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst();
    firstInstallment.getDebtor().setEmail(null);
    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();
    Organization org = buildOrganization();

    Mockito.when(debtPositionRepository.findEntityGraphByIupdOrgAndOrganizationId(debtPositionDTO.getIupdOrg(), debtPositionDTO.getOrganizationId())).thenReturn(null);
    Mockito.when(balanceServiceMock.isValidBalance(firstInstallment.getBalance(), firstInstallment.getAmountCents(), accessToken)).thenReturn(Boolean.TRUE);
    Mockito.when(brokerServiceMock.findById(orgOwner.getBrokerId(), accessToken)).thenReturn(broker);
    Mockito.when(organizationServiceMock.getOrganizationByFiscalCode(Mockito.anyString(), Mockito.anyString())).thenReturn(Optional.ofNullable(org));
    Mockito.when(taxonomyValidatorService.isTaxonomyCategoryValid("001122233", org.getOrgTypeCode())).thenReturn(true);

    assertDoesNotThrow(() -> service.validate(debtPositionDTO, orgOwner, accessToken, debtPositionTypeOrg));
    assertEquals(Boolean.TRUE, firstInstallment.getSwitchToExpired());
  }

  @Test
  void givenInvalidTransferCategoryWhenValidateThenThrowInvalidValueException() {
    // Given
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();
    Organization org = buildOrganization();
    InstallmentDTO firstInstallment = debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst();

    Mockito.when(debtPositionRepository.findEntityGraphByIupdOrgAndOrganizationId(debtPositionDTO.getIupdOrg(), debtPositionDTO.getOrganizationId())).thenReturn(null);
    Mockito.when(balanceServiceMock.isValidBalance(firstInstallment.getBalance(), firstInstallment.getAmountCents(), accessToken)).thenReturn(Boolean.TRUE);
    Mockito.when(brokerServiceMock.findById(orgOwner.getBrokerId(), accessToken)).thenReturn(broker);
    Mockito.when(organizationServiceMock.getOrganizationByFiscalCode(Mockito.anyString(), Mockito.anyString())).thenReturn(Optional.ofNullable(org));
    Mockito.when(brokerServiceMock.findById(orgOwner.getBrokerId(), accessToken)).thenReturn(broker);
    Mockito.when(taxonomyValidatorService.isTaxonomyCategoryValid("001122233", org.getOrgTypeCode())).thenReturn(false);
    // When, Then
    InvalidValueException invalidValueException = assertThrows(
      InvalidValueException.class,
      () -> service.validate(debtPositionDTO, orgOwner, accessToken, debtPositionTypeOrg)
    );
    assertEquals(
      "[INVALID_TAXONOMY_CATEGORY] Taxonomy category of transfer with index 1 is not valid",
      invalidValueException.getMessage()
    );
  }

  @ParameterizedTest
  @MethodSource("provideOrdinaryOrigins")
  void testOtherValidateThenSuccess(DebtPositionOrigin origin, boolean expected) {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    debtPositionDTO.setDebtPositionOrigin(origin);
    InstallmentDTO firstInstallment = debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst();
    firstInstallment.getDebtor().setEmail(null);
    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();
    debtPositionTypeOrg.setFlagMandatoryDueDate(false);
    Organization org = buildOrganization();

    Mockito.when(debtPositionRepository.findEntityGraphByIupdOrgAndOrganizationId(debtPositionDTO.getIupdOrg(), debtPositionDTO.getOrganizationId())).thenReturn(null);
    Mockito.when(balanceServiceMock.isValidBalance(firstInstallment.getBalance(), firstInstallment.getAmountCents(), accessToken)).thenReturn(Boolean.TRUE);
    Mockito.when(brokerServiceMock.findById(orgOwner.getBrokerId(), accessToken)).thenReturn(broker);
    Mockito.when(organizationServiceMock.getOrganizationByFiscalCode(Mockito.anyString(), Mockito.anyString())).thenReturn(Optional.ofNullable(org));
    Mockito.when(taxonomyValidatorService.isTaxonomyCategoryValid("001122233", org.getOrgTypeCode())).thenReturn(true);

    assertDoesNotThrow(() -> service.validate(debtPositionDTO, orgOwner, accessToken, debtPositionTypeOrg));
    assertEquals(expected, firstInstallment.getSwitchToExpired());
  }

  private static Stream<Arguments> provideOrdinaryOrigins() {
    return Stream.of(
      Arguments.of(ORDINARY, false),
      Arguments.of(ORDINARY_SIL, false),
      Arguments.of(SPONTANEOUS, true),
      Arguments.of(SPONTANEOUS_SIL, true)
    );
  }

  @Test
  void testValidateWhenDPOriginOrdinaryAndStatusPaidThenThrowInvalidValueException() {
    testValidateDPOrigin(ORDINARY, DebtPositionStatus.PAID, "[INVALID_DEBT_POSITION_STATUS] A Debt Position with origin ORDINARY or ORDINARY_SIL can only be created in UNPAID or DRAFT state");
  }

  @Test
  void testValidateWhenDPOriginOrdinarySilAndStatusPaidThenThrowInvalidValueException() {
    testValidateDPOrigin(ORDINARY_SIL,DebtPositionStatus.PAID, "[INVALID_DEBT_POSITION_STATUS] A Debt Position with origin ORDINARY or ORDINARY_SIL can only be created in UNPAID or DRAFT state");
  }

  @Test
  void testValidateWhenDPOriginSpontaneousAndStatusUnpaidThenThrowInvalidValueException() {
    testValidateDPOrigin(DebtPositionOrigin.SPONTANEOUS, DebtPositionStatus.PAID, "[INVALID_DEBT_POSITION_STATUS] A Debt Position with origin SPONTANEOUS can only be created in UNPAID state");
  }

  @Test
  void testValidateWhenDPOriginSecondaryOrgAndStatusUnpaidThenThrowInvalidValueException() {
    testValidateDPOrigin(DebtPositionOrigin.SECONDARY_ORG, DebtPositionStatus.UNPAID, "[INVALID_DEBT_POSITION_STATUS] A Debt Position with origin SECONDARY_ORG, RECEIPT_PAGO_PA, RECEIPT_FILE, or REPORTING_PAGOPA can only be created in PAID state");
  }

  @Test
  void testValidateWhenDPReceiptPagoPaAndStatusUnpaidThenThrowInvalidValueException() {
    testValidateDPOrigin(DebtPositionOrigin.RECEIPT_PAGOPA, DebtPositionStatus.UNPAID, "[INVALID_DEBT_POSITION_STATUS] A Debt Position with origin SECONDARY_ORG, RECEIPT_PAGO_PA, RECEIPT_FILE, or REPORTING_PAGOPA can only be created in PAID state");
  }

  @Test
  void testValidateWhenDPReceiptFileAndStatusUnpaidThenThrowInvalidValueException() {
    testValidateDPOrigin(DebtPositionOrigin.RECEIPT_FILE, DebtPositionStatus.UNPAID, "[INVALID_DEBT_POSITION_STATUS] A Debt Position with origin SECONDARY_ORG, RECEIPT_PAGO_PA, RECEIPT_FILE, or REPORTING_PAGOPA can only be created in PAID state");
  }

  @Test
  void testValidateWhenDPReportingPagoPaAndStatusUnpaidThenThrowInvalidValueException() {
    testValidateDPOrigin(DebtPositionOrigin.REPORTING_PAGOPA, DebtPositionStatus.UNPAID, "[INVALID_DEBT_POSITION_STATUS] A Debt Position with origin SECONDARY_ORG, RECEIPT_PAGO_PA, RECEIPT_FILE, or REPORTING_PAGOPA can only be created in PAID state");
  }

  @Test
  void givenFlagPuPagoPaPaymentFalseAndBlankIuvThenThrowValidationException() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    debtPositionDTO.setFlagPuPagoPaPayment(false);
    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();
    InstallmentDTO firstInstallment = debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst();
    firstInstallment.setIuv("");
    Organization org = buildOrganization();

    Mockito.when(debtPositionRepository.findEntityGraphByIupdOrgAndOrganizationId(debtPositionDTO.getIupdOrg(), debtPositionDTO.getOrganizationId())).thenReturn(null);
    Mockito.when(balanceServiceMock.isValidBalance(firstInstallment.getBalance(), firstInstallment.getAmountCents(), accessToken)).thenReturn(Boolean.TRUE);
    Mockito.when(brokerServiceMock.findById(orgOwner.getBrokerId(), accessToken)).thenReturn(broker);
    Mockito.when(organizationServiceMock.getOrganizationByFiscalCode(Mockito.anyString(), Mockito.anyString())).thenReturn(Optional.ofNullable(org));
    Mockito.when(taxonomyValidatorService.isTaxonomyCategoryValid("001122233", org.getOrgTypeCode())).thenReturn(true);

    InvalidValueException invalidValueException = assertThrows(InvalidValueException.class, () -> service.validate(debtPositionDTO, orgOwner, accessToken, debtPositionTypeOrg));
    assertEquals("[MISSING_IUV] Iuv cannot be empty if flagPuPagoPaPayment is false", invalidValueException.getMessage());
  }

  private void testValidateDPOrigin(DebtPositionOrigin origin, DebtPositionStatus status, String errorMessage) {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    debtPositionDTO.setDebtPositionOrigin(origin);
    debtPositionDTO.setStatus(status);
    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();

    InvalidValueException invalidValueException = assertThrows(InvalidValueException.class, () -> service.validate(debtPositionDTO, orgOwner, accessToken, debtPositionTypeOrg));
    assertEquals(errorMessage, invalidValueException.getMessage());
  }

  @Test
  void givenMultiDebtorFalseAndSameDebtorEverywhereWhenValidateThenSuccess() {
    // Given
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    debtPositionDTO.setMultiDebtor(false);
    InstallmentDTO firstInstallment = debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst();
    firstInstallment.getTransfers().getFirst().setFlagOwner(null);
    firstInstallment.getTransfers().getFirst().setOrgFiscalCode("98765432109");
    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();
    Organization org = buildOrganization();

    String commonFiscalCode = "RSSMRA80A01H501U";
    debtPositionDTO.getPaymentOptions().forEach(po ->
      po.getInstallments().forEach(inst -> inst.getDebtor().setFiscalCode(commonFiscalCode))
    );

    Mockito.when(debtPositionRepository.findEntityGraphByIupdOrgAndOrganizationId(debtPositionDTO.getIupdOrg(), debtPositionDTO.getOrganizationId())).thenReturn(null);
    Mockito.when(balanceServiceMock.isValidBalance(firstInstallment.getBalance(), firstInstallment.getAmountCents(), accessToken)).thenReturn(Boolean.TRUE);
    Mockito.when(brokerServiceMock.findById(orgOwner.getBrokerId(), accessToken)).thenReturn(broker);
    Mockito.when(organizationServiceMock.getOrganizationByFiscalCode(Mockito.anyString(), Mockito.anyString())).thenReturn(Optional.of(org));
    Mockito.when(taxonomyValidatorService.isTaxonomyCategoryValid(Mockito.anyString(), Mockito.anyString())).thenReturn(true);

    // When / Then
    assertDoesNotThrow(() -> service.validate(debtPositionDTO, orgOwner, accessToken, debtPositionTypeOrg));
    assertNull(firstInstallment.getTransfers().getFirst().getFlagOwner());
  }

  @Test
  void givenDifferentDebtorsInSamePOWhenValidateThenThrowInvalidValueException() {
    // Given
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();

    PaymentOptionDTO po = debtPositionDTO.getPaymentOptions().getFirst();
    po.setPaymentOptionIndex(99);

    InstallmentDTO inst1 = po.getInstallments().getFirst();
    inst1.getDebtor().setFiscalCode("RSSMRA80A01H501U");

    InstallmentDTO inst2 = InstallmentFaker.buildInstallmentDTO();
    inst2.getDebtor().setFiscalCode("BBBBBB11B11B111B");

    po.setInstallments(List.of(inst1, inst2));

    Mockito.when(debtPositionRepository.findEntityGraphByIupdOrgAndOrganizationId(debtPositionDTO.getIupdOrg(), debtPositionDTO.getOrganizationId())).thenReturn(null);

    // When / Then
    InvalidValueException ex = assertThrows(
      InvalidValueException.class,
      () -> service.validate(debtPositionDTO, orgOwner, accessToken, debtPositionTypeOrg)
    );

    assertEquals("[DIFFERENT_DEBTORS_IN_SAME_PO] All installments in a PaymentOption must have the same debtor. PO Index: 99", ex.getMessage());
  }

  @Test
  void givenMultiDebtorDisabledAndDifferentDebtorsInPOsWhenValidateThenThrowInvalidValueException() {
    // Given
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    debtPositionDTO.setMultiDebtor(false);
    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();
    debtPositionDTO.getPaymentOptions().get(0).getInstallments().getFirst().getDebtor().setFiscalCode("RSSMRA80A01H501U");

    if(debtPositionDTO.getPaymentOptions().size() < 2) {
      debtPositionDTO.getPaymentOptions().add(PaymentOptionFaker.buildPaymentOptionDTO());
    }
    debtPositionDTO.getPaymentOptions().get(1).getInstallments().getFirst().getDebtor().setFiscalCode("BBBBBB11B11B111B");

    Mockito.when(debtPositionRepository.findEntityGraphByIupdOrgAndOrganizationId(debtPositionDTO.getIupdOrg(), debtPositionDTO.getOrganizationId())).thenReturn(null);

    // When / Then
    InvalidValueException ex = assertThrows(
      InvalidValueException.class,
      () -> service.validate(debtPositionDTO, orgOwner, accessToken, debtPositionTypeOrg)
    );

    assertEquals("[MULTIDEBTOR_DISABLED] Different debtors found but multiDebtor flag is False for this Debt Position", ex.getMessage());
  }

  @Test
  void givenSameDebtorInMultipleInstallmentsWhenValidateThenSuccess() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    debtPositionDTO.setMultiDebtor(false);
    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();
    Organization org = buildOrganization();

    String commonCf = "RSSMRA80A01H501U";
    PaymentOptionDTO po = debtPositionDTO.getPaymentOptions().getFirst();
    InstallmentDTO firstInstallment = po.getInstallments().getFirst();
    firstInstallment.getDebtor().setFiscalCode(commonCf);

    InstallmentDTO inst2 = InstallmentFaker.buildInstallmentDTO();
    inst2.getDebtor().setFiscalCode(commonCf);
    po.setInstallments(List.of(firstInstallment, inst2));

    Mockito.when(debtPositionRepository.findEntityGraphByIupdOrgAndOrganizationId(Mockito.anyString(), Mockito.anyLong())).thenReturn(null);
    Mockito.when(balanceServiceMock.isValidBalance(firstInstallment.getBalance(), firstInstallment.getAmountCents(), accessToken)).thenReturn(Boolean.TRUE);
    Mockito.when(brokerServiceMock.findById(orgOwner.getBrokerId(), accessToken)).thenReturn(broker);
    Mockito.when(organizationServiceMock.getOrganizationByFiscalCode(Mockito.anyString(), Mockito.anyString())).thenReturn(Optional.ofNullable(org));
    Mockito.when(taxonomyValidatorService.isTaxonomyCategoryValid(Mockito.anyString(), Mockito.anyString())).thenReturn(true);

    assertDoesNotThrow(() -> service.validate(debtPositionDTO, orgOwner, accessToken, debtPositionTypeOrg));
  }

  @Test
  void givenMultiDebtorTrueWhenValidateThenSuccess() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    debtPositionDTO.setMultiDebtor(true);
    InstallmentDTO firstInstallment = debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst();
    firstInstallment.getTransfers().getFirst().setFlagOwner(null);
    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();

    Broker brokerDelegate = buildBroker();
    brokerDelegate.setFlagDelegate(Boolean.TRUE);

    Mockito.when(debtPositionRepository.findEntityGraphByIupdOrgAndOrganizationId(Mockito.anyString(), Mockito.anyLong())).thenReturn(null);
    Mockito.when(balanceServiceMock.isValidBalance(firstInstallment.getBalance(), firstInstallment.getAmountCents(), accessToken)).thenReturn(Boolean.TRUE);
    Mockito.when(brokerServiceMock.findById(orgOwner.getBrokerId(), accessToken)).thenReturn(brokerDelegate);

    assertDoesNotThrow(() -> service.validate(debtPositionDTO, orgOwner, accessToken, debtPositionTypeOrg));
    verify(organizationServiceMock, times(0)).getOrganizationByFiscalCode(Mockito.anyString(), Mockito.anyString());
    verify(taxonomyValidatorService, times(0)).isTaxonomyCategoryValid(Mockito.anyString(), Mockito.anyString());
    assertEquals(Boolean.TRUE, firstInstallment.getTransfers().getFirst().getFlagOwner());
  }

  @Test
  void givenNullDebtorWhenIsDifferentDebtorThenReturnCorrectBoolean() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().setDebtor(null);

    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();

    Mockito.when(debtPositionRepository.findEntityGraphByIupdOrgAndOrganizationId(Mockito.anyString(), Mockito.anyLong())).thenReturn(null);
    assertThrows(Exception.class, () -> service.validate(debtPositionDTO, orgOwner, accessToken, debtPositionTypeOrg));
  }
}

