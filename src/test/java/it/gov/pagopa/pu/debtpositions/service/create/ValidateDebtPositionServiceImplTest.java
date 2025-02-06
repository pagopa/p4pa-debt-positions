package it.gov.pagopa.pu.debtpositions.service.create;

import it.gov.pagopa.pu.debtpositions.connector.organization.TaxonomyService;
import it.gov.pagopa.pu.debtpositions.exception.custom.InvalidValueException;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.TransferDTO;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrg;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionTypeOrgRepository;
import it.gov.pagopa.pu.organization.dto.generated.Taxonomy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static it.gov.pagopa.pu.debtpositions.util.faker.DebtPositionFaker.buildDebtPositionDTO;
import static it.gov.pagopa.pu.debtpositions.util.faker.DebtPositionTypeOrgFaker.buildDebtPositionTypeOrg;
import static it.gov.pagopa.pu.debtpositions.util.faker.TransferFaker.buildTransferDTO;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ValidateDebtPositionServiceImplTest {

  private ValidateDebtPositionService service;

  @Mock
  private DebtPositionTypeOrgRepository debtPositionTypeOrgRepository;

  @Mock
  private TaxonomyService taxonomyService;

  private final String accessToken = "ACCESSTOKEN";

  @BeforeEach
  void init() {
    service = new ValidateDebtPositionServiceImpl(debtPositionTypeOrgRepository, taxonomyService);
  }

  @Test
  void givenDebtPositionTypeOrgIdNullThenThrowValidationException() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    debtPositionDTO.setDebtPositionTypeOrgId(null);

    InvalidValueException invalidValueException = assertThrows(InvalidValueException.class, () -> service.validate(debtPositionDTO, accessToken));
    assertEquals("Debt position type organization is mandatory", invalidValueException.getMessage());
  }

  @Test
  void givenDebtPositionTypeOrgNotFoundThenThrowValidationException() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();

    Mockito.when(debtPositionTypeOrgRepository.findById(2L)).thenReturn(Optional.empty());

    InvalidValueException invalidValueException = assertThrows(InvalidValueException.class, () -> service.validate(debtPositionDTO, accessToken));
    assertEquals("Debt position type organization is mandatory", invalidValueException.getMessage());
  }

  @Test
  void givenDebtPositionTypeOrgCodeNullThenThrowValidationException() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    DebtPositionTypeOrg mockDebtPositionTypeOrg = buildDebtPositionTypeOrg();
    mockDebtPositionTypeOrg.setCode(null);

    Mockito.when(debtPositionTypeOrgRepository.findById(2L)).thenReturn(Optional.of(mockDebtPositionTypeOrg));

    InvalidValueException invalidValueException = assertThrows(InvalidValueException.class, () -> service.validate(debtPositionDTO, accessToken));
    assertEquals("Debt position type organization is mandatory", invalidValueException.getMessage());
  }

  @Test
  void givenPaymentOptionsNullThenThrowValidationException() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    DebtPositionTypeOrg mockDebtPositionTypeOrg = buildDebtPositionTypeOrg();
    debtPositionDTO.setPaymentOptions(null);

    Mockito.when(debtPositionTypeOrgRepository.findById(2L)).thenReturn(Optional.of(mockDebtPositionTypeOrg));

    InvalidValueException invalidValueException = assertThrows(InvalidValueException.class, () -> service.validate(debtPositionDTO, accessToken));
    assertEquals("Debt position payment options is mandatory", invalidValueException.getMessage());
  }

  @Test
  void givenInstallmentListEmptyThenThrowValidationException() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    DebtPositionTypeOrg mockDebtPositionTypeOrg = buildDebtPositionTypeOrg();
    debtPositionDTO.getPaymentOptions().getFirst().setInstallments(List.of());

    Mockito.when(debtPositionTypeOrgRepository.findById(2L)).thenReturn(Optional.of(mockDebtPositionTypeOrg));

    InvalidValueException invalidValueException = assertThrows(InvalidValueException.class, () -> service.validate(debtPositionDTO, accessToken));
    assertEquals("At least one installment of the debt position is mandatory", invalidValueException.getMessage());
  }

  @Test
  void givenInstallmentWithoutRemittanceInfoThenThrowValidationException() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    DebtPositionTypeOrg mockDebtPositionTypeOrg = buildDebtPositionTypeOrg();
    debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().setRemittanceInformation(null);

    Mockito.when(debtPositionTypeOrgRepository.findById(2L)).thenReturn(Optional.of(mockDebtPositionTypeOrg));

    InvalidValueException invalidValueException = assertThrows(InvalidValueException.class, () -> service.validate(debtPositionDTO, accessToken));
    assertEquals("Remittance information is mandatory", invalidValueException.getMessage());
  }

  @Test
  void givenInstallmentWithDueDateRetroactiveThenThrowValidationException() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    DebtPositionTypeOrg mockDebtPositionTypeOrg = buildDebtPositionTypeOrg();
    LocalDateTime localDateTime = LocalDateTime.of(2024, 5, 15, 10, 30, 0);
    OffsetDateTime offsetDateTime = ZonedDateTime.of(localDateTime, ZoneId.of("Europe/Rome")).toOffsetDateTime();
    debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().setDueDate(offsetDateTime);

    Mockito.when(debtPositionTypeOrgRepository.findById(2L)).thenReturn(Optional.of(mockDebtPositionTypeOrg));

    InvalidValueException invalidValueException = assertThrows(InvalidValueException.class, () -> service.validate(debtPositionDTO, accessToken));
    assertEquals("The due date cannot be retroactive", invalidValueException.getMessage());
  }

  @Test
  void givenInstallmentWithDueDateNullButMandatoryThenThrowValidationException() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    DebtPositionTypeOrg mockDebtPositionTypeOrg = buildDebtPositionTypeOrg();
    debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().setDueDate(null);

    Mockito.when(debtPositionTypeOrgRepository.findById(2L)).thenReturn(Optional.of(mockDebtPositionTypeOrg));

    InvalidValueException invalidValueException = assertThrows(InvalidValueException.class, () -> service.validate(debtPositionDTO, accessToken));
    assertEquals("The due date is mandatory", invalidValueException.getMessage());
  }

  @Test
  void givenInstallmentWithAmountInvalidThenThrowValidationException() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    DebtPositionTypeOrg mockDebtPositionTypeOrg = buildDebtPositionTypeOrg();
    debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().setAmountCents(-200L);

    Mockito.when(debtPositionTypeOrgRepository.findById(2L)).thenReturn(Optional.of(mockDebtPositionTypeOrg));

    InvalidValueException invalidValueException = assertThrows(InvalidValueException.class, () -> service.validate(debtPositionDTO, accessToken));
    assertEquals("Amount is not valid", invalidValueException.getMessage());
  }

  @Test
  void givenInstallmentWithAmountInvalidForDebtPositionTypeThenThrowValidationException() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    DebtPositionTypeOrg mockDebtPositionTypeOrg = buildDebtPositionTypeOrg();
    mockDebtPositionTypeOrg.setAmountCents(200L);
    debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().setAmountCents(100L);

    Mockito.when(debtPositionTypeOrgRepository.findById(2L)).thenReturn(Optional.of(mockDebtPositionTypeOrg));

    InvalidValueException invalidValueException = assertThrows(InvalidValueException.class, () -> service.validate(debtPositionDTO, accessToken));
    assertEquals("Amount is not valid for this debt position type org", invalidValueException.getMessage());
  }


  @Test
  void givenPersonNullThenThrowValidationException() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    DebtPositionTypeOrg mockDebtPositionTypeOrg = buildDebtPositionTypeOrg();
    debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().setDebtor(null);

    Mockito.when(debtPositionTypeOrgRepository.findById(2L)).thenReturn(Optional.of(mockDebtPositionTypeOrg));

    InvalidValueException invalidValueException = assertThrows(InvalidValueException.class, () -> service.validate(debtPositionDTO, accessToken));
    assertEquals("The debtor is mandatory for installment", invalidValueException.getMessage());
  }

  @Test
  void givenPersonWithAnonimousCFButNotAnonymousFlagThenThrowValidationException() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    DebtPositionTypeOrg mockDebtPositionTypeOrg = buildDebtPositionTypeOrg();
    mockDebtPositionTypeOrg.setFlagAnonymousFiscalCode(false);
    debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().getDebtor().setFiscalCode("ANONIMO");

    Mockito.when(debtPositionTypeOrgRepository.findById(2L)).thenReturn(Optional.of(mockDebtPositionTypeOrg));

    InvalidValueException invalidValueException = assertThrows(InvalidValueException.class, () -> service.validate(debtPositionDTO, accessToken));
    assertEquals("This organization installment type or installment does not allow an anonymous unique identification code", invalidValueException.getMessage());
  }

  @Test
  void givenPersonWithNullFullNameThenThrowValidationException() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    DebtPositionTypeOrg mockDebtPositionTypeOrg = buildDebtPositionTypeOrg();
    debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().getDebtor().setFullName(null);

    Mockito.when(debtPositionTypeOrgRepository.findById(2L)).thenReturn(Optional.of(mockDebtPositionTypeOrg));

    InvalidValueException invalidValueException = assertThrows(InvalidValueException.class, () -> service.validate(debtPositionDTO, accessToken));
    assertEquals("Beneficiary name is mandatory", invalidValueException.getMessage());
  }

  @Test
  void givenPersonWithNullEmailThenThrowValidationException() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    DebtPositionTypeOrg mockDebtPositionTypeOrg = buildDebtPositionTypeOrg();
    debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().getDebtor().setEmail(null);

    Mockito.when(debtPositionTypeOrgRepository.findById(2L)).thenReturn(Optional.of(mockDebtPositionTypeOrg));

    InvalidValueException invalidValueException = assertThrows(InvalidValueException.class, () -> service.validate(debtPositionDTO, accessToken));
    assertEquals("Email is not valid", invalidValueException.getMessage());
  }

  @Test
  void givenPersonWithInvalidEmailThenThrowValidationException() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    DebtPositionTypeOrg mockDebtPositionTypeOrg = buildDebtPositionTypeOrg();
    debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().getDebtor().setEmail("test&it");

    Mockito.when(debtPositionTypeOrgRepository.findById(2L)).thenReturn(Optional.of(mockDebtPositionTypeOrg));

    InvalidValueException invalidValueException = assertThrows(InvalidValueException.class, () -> service.validate(debtPositionDTO, accessToken));
    assertEquals("Email is not valid", invalidValueException.getMessage());
  }

  @Test
  void givenNoTransfersThenThrowValidationException() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    DebtPositionTypeOrg mockDebtPositionTypeOrg = buildDebtPositionTypeOrg();
    debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().setTransfers(null);

    Mockito.when(debtPositionTypeOrgRepository.findById(2L)).thenReturn(Optional.of(mockDebtPositionTypeOrg));

    InvalidValueException invalidValueException = assertThrows(InvalidValueException.class, () -> service.validate(debtPositionDTO, accessToken));
    assertEquals("At least one transfer is mandatory for installment", invalidValueException.getMessage());
  }

  @Test
  void givenTransfersMismatchThenThrowValidationException() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    DebtPositionTypeOrg mockDebtPositionTypeOrg = buildDebtPositionTypeOrg();
    TransferDTO secondTransfer = buildTransferDTO();
    secondTransfer.setTransferIndex(1);
    List<TransferDTO> transfers = List.of(secondTransfer, secondTransfer);
    debtPositionDTO.getPaymentOptions()
      .getFirst()
      .getInstallments()
      .getFirst()
      .setTransfers(new ArrayList<>(transfers));

    Mockito.when(debtPositionTypeOrgRepository.findById(2L)).thenReturn(Optional.of(mockDebtPositionTypeOrg));

    InvalidValueException invalidValueException = assertThrows(InvalidValueException.class, () -> service.validate(debtPositionDTO, accessToken));
    assertEquals("Mismatch with transfers list", invalidValueException.getMessage());
  }

  @Test
  void givenSecondTransferPIVANullThenThrowValidationException() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    DebtPositionTypeOrg mockDebtPositionTypeOrg = buildDebtPositionTypeOrg();
    TransferDTO secondTransfer = buildTransferDTO();
    secondTransfer.setOrgFiscalCode(null);
    List<TransferDTO> transfers = List.of(secondTransfer, secondTransfer);
    debtPositionDTO.getPaymentOptions()
      .getFirst()
      .getInstallments()
      .getFirst()
      .setTransfers(new ArrayList<>(transfers));

    Mockito.when(debtPositionTypeOrgRepository.findById(2L)).thenReturn(Optional.of(mockDebtPositionTypeOrg));

    InvalidValueException invalidValueException = assertThrows(InvalidValueException.class, () -> service.validate(debtPositionDTO, accessToken));
    assertEquals("Fiscal code of secondary beneficiary is not valid", invalidValueException.getMessage());
  }

  @Test
  void givenSecondTransferPIVANotValidThenThrowValidationException() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    DebtPositionTypeOrg mockDebtPositionTypeOrg = buildDebtPositionTypeOrg();
    TransferDTO secondTransfer = buildTransferDTO();
    secondTransfer.setOrgFiscalCode("00000000001");
    List<TransferDTO> transfers = List.of(secondTransfer, secondTransfer);
    debtPositionDTO.getPaymentOptions()
      .getFirst()
      .getInstallments()
      .getFirst()
      .setTransfers(new ArrayList<>(transfers));

    Mockito.when(debtPositionTypeOrgRepository.findById(2L)).thenReturn(Optional.of(mockDebtPositionTypeOrg));

    InvalidValueException invalidValueException = assertThrows(InvalidValueException.class, () -> service.validate(debtPositionDTO, accessToken));
    assertEquals("Fiscal code of secondary beneficiary is not valid", invalidValueException.getMessage());
  }

  @Test
  void givenSecondTransferIbanNullThenThrowValidationException() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    DebtPositionTypeOrg mockDebtPositionTypeOrg = buildDebtPositionTypeOrg();
    TransferDTO secondTransfer = buildTransferDTO();
    secondTransfer.setIban("ITkb");
    List<TransferDTO> transfers = List.of(secondTransfer, secondTransfer);
    debtPositionDTO.getPaymentOptions()
      .getFirst()
      .getInstallments()
      .getFirst()
      .setTransfers(new ArrayList<>(transfers));

    Mockito.when(debtPositionTypeOrgRepository.findById(2L)).thenReturn(Optional.of(mockDebtPositionTypeOrg));

    InvalidValueException invalidValueException = assertThrows(InvalidValueException.class, () -> service.validate(debtPositionDTO, accessToken));
    assertEquals("Iban of secondary beneficiary is not valid", invalidValueException.getMessage());
  }

  @Test
  void givenSecondTransferCategoryNullThenThrowValidationException() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    DebtPositionTypeOrg mockDebtPositionTypeOrg = buildDebtPositionTypeOrg();
    TransferDTO secondTransfer = buildTransferDTO();
    secondTransfer.setCategory(null);
    List<TransferDTO> transfers = List.of(secondTransfer, secondTransfer);
    debtPositionDTO.getPaymentOptions()
      .getFirst()
      .getInstallments()
      .getFirst()
      .setTransfers(new ArrayList<>(transfers));

    Mockito.when(debtPositionTypeOrgRepository.findById(2L)).thenReturn(Optional.of(mockDebtPositionTypeOrg));

    InvalidValueException invalidValueException = assertThrows(InvalidValueException.class, () -> service.validate(debtPositionDTO, accessToken));
    assertEquals("Category of secondary beneficiary is mandatory", invalidValueException.getMessage());
  }

  @Test
  void givenSecondTransferCategoryNotFoundThenThrowValidationException() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    DebtPositionTypeOrg mockDebtPositionTypeOrg = buildDebtPositionTypeOrg();
    TransferDTO secondTransfer = buildTransferDTO();
    secondTransfer.setCategory("category");
    List<TransferDTO> transfers = List.of(secondTransfer, secondTransfer);
    debtPositionDTO.getPaymentOptions()
      .getFirst()
      .getInstallments()
      .getFirst()
      .setTransfers(new ArrayList<>(transfers));

    Mockito.when(debtPositionTypeOrgRepository.findById(2L)).thenReturn(Optional.of(mockDebtPositionTypeOrg));

    when(taxonomyService.getTaxonomyByTaxonomyCode("category/", accessToken)).thenReturn(Optional.empty());

    InvalidValueException invalidValueException = assertThrows(InvalidValueException.class, () -> service.validate(debtPositionDTO, accessToken));
    assertEquals("The category code does not exist in the archive", invalidValueException.getMessage());
  }

  @Test
  void givenSecondTransferAmountNegativeThenThrowValidationException() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    DebtPositionTypeOrg mockDebtPositionTypeOrg = buildDebtPositionTypeOrg();
    TransferDTO secondTransfer = buildTransferDTO();
    secondTransfer.setCategory("category");
    secondTransfer.setAmountCents(-12L);
    List<TransferDTO> transfers = List.of(secondTransfer, secondTransfer);
    debtPositionDTO.getPaymentOptions()
      .getFirst()
      .getInstallments()
      .getFirst()
      .setTransfers(new ArrayList<>(transfers));

    Mockito.when(debtPositionTypeOrgRepository.findById(2L)).thenReturn(Optional.of(mockDebtPositionTypeOrg));

    when(taxonomyService.getTaxonomyByTaxonomyCode("category/", accessToken)).thenReturn(Optional.of(new Taxonomy()));

    InvalidValueException invalidValueException = assertThrows(InvalidValueException.class, () -> service.validate(debtPositionDTO, accessToken));
    assertEquals("The amount of secondary beneficiary is not valid", invalidValueException.getMessage());
  }

  @Test
  void givenSecondTransferThenSuccess() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    DebtPositionTypeOrg mockDebtPositionTypeOrg = buildDebtPositionTypeOrg();
    TransferDTO secondTransfer = buildTransferDTO();
    List<TransferDTO> transfers = List.of(secondTransfer, secondTransfer);
    debtPositionDTO.getPaymentOptions()
      .getFirst()
      .getInstallments()
      .getFirst()
      .setTransfers(new ArrayList<>(transfers));

    Mockito.when(debtPositionTypeOrgRepository.findById(2L)).thenReturn(Optional.of(mockDebtPositionTypeOrg));

    when(taxonomyService.getTaxonomyByTaxonomyCode("category/", accessToken)).thenReturn(Optional.of(new Taxonomy()));

    assertDoesNotThrow(() -> service.validate(debtPositionDTO, accessToken));
  }

  @Test
  void testValidateThenSuccess() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    DebtPositionTypeOrg mockDebtPositionTypeOrg = buildDebtPositionTypeOrg();

    Mockito.when(debtPositionTypeOrgRepository.findById(2L)).thenReturn(Optional.of(mockDebtPositionTypeOrg));

    assertDoesNotThrow(() -> service.validate(debtPositionDTO, accessToken));
  }
}

