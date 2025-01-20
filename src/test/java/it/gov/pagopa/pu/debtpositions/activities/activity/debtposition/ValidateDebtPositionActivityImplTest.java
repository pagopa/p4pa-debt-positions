package it.gov.pagopa.pu.debtpositions.activities.activity.debtposition;

import it.gov.pagopa.pu.debtpositions.activities.activity.ValidateDebtPositionActivity;
import it.gov.pagopa.pu.debtpositions.activities.activity.ValidateDebtPositionActivityImpl;
import it.gov.pagopa.pu.debtpositions.activities.exception.InvalidValueException;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.TransferDTO;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrg;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionTypeOrgRepository;
import it.gov.pagopa.pu.debtpositions.repository.TaxonomyRepository;
import it.gov.pagopa.pu.debtpositions.util.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static it.gov.pagopa.pu.debtpositions.util.faker.DebtPositionFaker.buildDebtPositionDTO;
import static it.gov.pagopa.pu.debtpositions.util.faker.DebtPositionTypeOrgFaker.buildDebtPositionTypeOrg;
import static it.gov.pagopa.pu.debtpositions.util.faker.TransferFaker.buildTransferDTO;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ValidateDebtPositionActivityImplTest {

  private ValidateDebtPositionActivity activity;

  @Mock
  private TaxonomyRepository taxonomyRepository;

  @Mock
  private DebtPositionTypeOrgRepository debtPositionTypeOrgRepository;

  @BeforeEach
  void init() {
    activity = new ValidateDebtPositionActivityImpl(taxonomyRepository, debtPositionTypeOrgRepository);
  }

  @Test
  void givenDebtPositionTypeOrgIdNullThenThrowValidationException() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    debtPositionDTO.setDebtPositionTypeOrgId(null);

    InvalidValueException invalidValueException = assertThrows(InvalidValueException.class, () -> activity.validate(debtPositionDTO));
    assertEquals("Debt position type organization is mandatory", invalidValueException.getMessage());
  }

  @Test
  void givenDebtPositionTypeOrgNotFoundThenThrowValidationException() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();

    Mockito.when(debtPositionTypeOrgRepository.findById(2L)).thenReturn(Optional.empty());

    InvalidValueException invalidValueException = assertThrows(InvalidValueException.class, () -> activity.validate(debtPositionDTO));
    assertEquals("Debt position type organization is mandatory", invalidValueException.getMessage());
  }

  @Test
  void givenDebtPositionTypeOrgCodeNullThenThrowValidationException() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    DebtPositionTypeOrg mockDebtPositionTypeOrg = buildDebtPositionTypeOrg();
    mockDebtPositionTypeOrg.setCode(null);

    Mockito.when(debtPositionTypeOrgRepository.findById(2L)).thenReturn(Optional.of(mockDebtPositionTypeOrg));

    InvalidValueException invalidValueException = assertThrows(InvalidValueException.class, () -> activity.validate(debtPositionDTO));
    assertEquals("Debt position type organization is mandatory", invalidValueException.getMessage());
  }

  @Test
  void givenPaymentOptionsNullThenThrowValidationException() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    DebtPositionTypeOrg mockDebtPositionTypeOrg = buildDebtPositionTypeOrg();
    debtPositionDTO.setPaymentOptions(null);

    Mockito.when(debtPositionTypeOrgRepository.findById(2L)).thenReturn(Optional.of(mockDebtPositionTypeOrg));

    InvalidValueException invalidValueException = assertThrows(InvalidValueException.class, () -> activity.validate(debtPositionDTO));
    assertEquals("Debt position payment options is mandatory", invalidValueException.getMessage());
  }

  @Test
  void givenInstallmentListEmptyThenThrowValidationException() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    DebtPositionTypeOrg mockDebtPositionTypeOrg = buildDebtPositionTypeOrg();
    debtPositionDTO.getPaymentOptions().getFirst().setInstallments(List.of());

    Mockito.when(debtPositionTypeOrgRepository.findById(2L)).thenReturn(Optional.of(mockDebtPositionTypeOrg));

    InvalidValueException invalidValueException = assertThrows(InvalidValueException.class, () -> activity.validate(debtPositionDTO));
    assertEquals("At least one installment of the debt position is mandatory", invalidValueException.getMessage());
  }

  @Test
  void givenInstallmentWithoutRemittanceInfoThenThrowValidationException() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    DebtPositionTypeOrg mockDebtPositionTypeOrg = buildDebtPositionTypeOrg();
    debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().setRemittanceInformation(null);

    Mockito.when(debtPositionTypeOrgRepository.findById(2L)).thenReturn(Optional.of(mockDebtPositionTypeOrg));

    InvalidValueException invalidValueException = assertThrows(InvalidValueException.class, () -> activity.validate(debtPositionDTO));
    assertEquals("Remittance information is mandatory", invalidValueException.getMessage());
  }

  @Test
  void givenInstallmentWithDueDateRetroactiveThenThrowValidationException() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    DebtPositionTypeOrg mockDebtPositionTypeOrg = buildDebtPositionTypeOrg();
    debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().setDueDate(TestUtils.OFFSETDATETIME);

    Mockito.when(debtPositionTypeOrgRepository.findById(2L)).thenReturn(Optional.of(mockDebtPositionTypeOrg));

    InvalidValueException invalidValueException = assertThrows(InvalidValueException.class, () -> activity.validate(debtPositionDTO));
    assertEquals("The due date cannot be retroactive", invalidValueException.getMessage());
  }

  @Test
  void givenInstallmentWithDueDateNullButMandatoryThenThrowValidationException() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    DebtPositionTypeOrg mockDebtPositionTypeOrg = buildDebtPositionTypeOrg();
    debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().setDueDate(null);

    Mockito.when(debtPositionTypeOrgRepository.findById(2L)).thenReturn(Optional.of(mockDebtPositionTypeOrg));

    InvalidValueException invalidValueException = assertThrows(InvalidValueException.class, () -> activity.validate(debtPositionDTO));
    assertEquals("The due date is mandatory", invalidValueException.getMessage());
  }

  @Test
  void givenInstallmentWithAmountNullThenThrowValidationException() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    DebtPositionTypeOrg mockDebtPositionTypeOrg = buildDebtPositionTypeOrg();
    debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().setAmountCents(null);

    Mockito.when(debtPositionTypeOrgRepository.findById(2L)).thenReturn(Optional.of(mockDebtPositionTypeOrg));

    InvalidValueException invalidValueException = assertThrows(InvalidValueException.class, () -> activity.validate(debtPositionDTO));
    assertEquals("Amount is mandatory", invalidValueException.getMessage());
  }

  @Test
  void givenInstallmentWithAmountInvalidThenThrowValidationException() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    DebtPositionTypeOrg mockDebtPositionTypeOrg = buildDebtPositionTypeOrg();
    debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().setAmountCents(-200L);

    Mockito.when(debtPositionTypeOrgRepository.findById(2L)).thenReturn(Optional.of(mockDebtPositionTypeOrg));

    InvalidValueException invalidValueException = assertThrows(InvalidValueException.class, () -> activity.validate(debtPositionDTO));
    assertEquals("Amount is not valid", invalidValueException.getMessage());
  }

  @Test
  void givenInstallmentWithAmountInvalidForDebtPositionTypeThenThrowValidationException() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    DebtPositionTypeOrg mockDebtPositionTypeOrg = buildDebtPositionTypeOrg();
    mockDebtPositionTypeOrg.setAmountCents(200L);
    debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().setAmountCents(100L);

    Mockito.when(debtPositionTypeOrgRepository.findById(2L)).thenReturn(Optional.of(mockDebtPositionTypeOrg));

    InvalidValueException invalidValueException = assertThrows(InvalidValueException.class, () -> activity.validate(debtPositionDTO));
    assertEquals("Amount is not valid for this debt position type org", invalidValueException.getMessage());
  }


  @Test
  void givenPersonNullThenThrowValidationException() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    DebtPositionTypeOrg mockDebtPositionTypeOrg = buildDebtPositionTypeOrg();
    debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().setDebtor(null);

    Mockito.when(debtPositionTypeOrgRepository.findById(2L)).thenReturn(Optional.of(mockDebtPositionTypeOrg));

    InvalidValueException invalidValueException = assertThrows(InvalidValueException.class, () -> activity.validate(debtPositionDTO));
    assertEquals("The debtor is mandatory for installment", invalidValueException.getMessage());
  }

  @Test
  void givenPersonWithAnonimousCFButNotAnonymousFlagThenThrowValidationException() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    DebtPositionTypeOrg mockDebtPositionTypeOrg = buildDebtPositionTypeOrg();
    mockDebtPositionTypeOrg.setFlagAnonymousFiscalCode(false);
    debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().getDebtor().setFiscalCode("ANONIMO");

    Mockito.when(debtPositionTypeOrgRepository.findById(2L)).thenReturn(Optional.of(mockDebtPositionTypeOrg));

    InvalidValueException invalidValueException = assertThrows(InvalidValueException.class, () -> activity.validate(debtPositionDTO));
    assertEquals("This organization installment type or installment does not allow an anonymous unique identification code", invalidValueException.getMessage());
  }

  @Test
  void givenPersonWithNullFullNameThenThrowValidationException() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    DebtPositionTypeOrg mockDebtPositionTypeOrg = buildDebtPositionTypeOrg();
    debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().getDebtor().setFullName(null);

    Mockito.when(debtPositionTypeOrgRepository.findById(2L)).thenReturn(Optional.of(mockDebtPositionTypeOrg));

    InvalidValueException invalidValueException = assertThrows(InvalidValueException.class, () -> activity.validate(debtPositionDTO));
    assertEquals("Beneficiary name is mandatory", invalidValueException.getMessage());
  }

  @Test
  void givenPersonWithNullEmailThenThrowValidationException() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    DebtPositionTypeOrg mockDebtPositionTypeOrg = buildDebtPositionTypeOrg();
    debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().getDebtor().setEmail(null);

    Mockito.when(debtPositionTypeOrgRepository.findById(2L)).thenReturn(Optional.of(mockDebtPositionTypeOrg));

    InvalidValueException invalidValueException = assertThrows(InvalidValueException.class, () -> activity.validate(debtPositionDTO));
    assertEquals("Email is not valid", invalidValueException.getMessage());
  }

  @Test
  void givenPersonWithInvalidEmailThenThrowValidationException() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    DebtPositionTypeOrg mockDebtPositionTypeOrg = buildDebtPositionTypeOrg();
    debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().getDebtor().setEmail("test&it");

    Mockito.when(debtPositionTypeOrgRepository.findById(2L)).thenReturn(Optional.of(mockDebtPositionTypeOrg));

    InvalidValueException invalidValueException = assertThrows(InvalidValueException.class, () -> activity.validate(debtPositionDTO));
    assertEquals("Email is not valid", invalidValueException.getMessage());
  }

  @Test
  void givenNoTransfersThenThrowValidationException() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    DebtPositionTypeOrg mockDebtPositionTypeOrg = buildDebtPositionTypeOrg();
    debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().setTransfers(null);

    Mockito.when(debtPositionTypeOrgRepository.findById(2L)).thenReturn(Optional.of(mockDebtPositionTypeOrg));

    InvalidValueException invalidValueException = assertThrows(InvalidValueException.class, () -> activity.validate(debtPositionDTO));
    assertEquals("At least one transfer is mandatory for installment", invalidValueException.getMessage());
  }

  @Test
  void givenTransfersMismatchThenThrowValidationException() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    DebtPositionTypeOrg mockDebtPositionTypeOrg = buildDebtPositionTypeOrg();
    TransferDTO secondTransfer = buildTransferDTO();
    secondTransfer.setTransferIndex(1L);
    List<TransferDTO> transfers = List.of(secondTransfer, secondTransfer);
    debtPositionDTO.getPaymentOptions()
      .getFirst()
      .getInstallments()
      .getFirst()
      .setTransfers(new ArrayList<>(transfers));

    Mockito.when(debtPositionTypeOrgRepository.findById(2L)).thenReturn(Optional.of(mockDebtPositionTypeOrg));

    InvalidValueException invalidValueException = assertThrows(InvalidValueException.class, () -> activity.validate(debtPositionDTO));
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

    InvalidValueException invalidValueException = assertThrows(InvalidValueException.class, () -> activity.validate(debtPositionDTO));
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

    InvalidValueException invalidValueException = assertThrows(InvalidValueException.class, () -> activity.validate(debtPositionDTO));
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

    InvalidValueException invalidValueException = assertThrows(InvalidValueException.class, () -> activity.validate(debtPositionDTO));
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

    InvalidValueException invalidValueException = assertThrows(InvalidValueException.class, () -> activity.validate(debtPositionDTO));
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

    when(taxonomyRepository.existsTaxonomyByTaxonomyCode("category/")).thenReturn(Boolean.FALSE);

    InvalidValueException invalidValueException = assertThrows(InvalidValueException.class, () -> activity.validate(debtPositionDTO));
    assertEquals("The category code does not exist in the archive", invalidValueException.getMessage());
  }

  @Test
  void givenSecondTransferAmountNullThenThrowValidationException() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    DebtPositionTypeOrg mockDebtPositionTypeOrg = buildDebtPositionTypeOrg();
    TransferDTO secondTransfer = buildTransferDTO();
    secondTransfer.setAmountCents(null);
    List<TransferDTO> transfers = List.of(secondTransfer, secondTransfer);
    debtPositionDTO.getPaymentOptions()
      .getFirst()
      .getInstallments()
      .getFirst()
      .setTransfers(new ArrayList<>(transfers));

    Mockito.when(debtPositionTypeOrgRepository.findById(2L)).thenReturn(Optional.of(mockDebtPositionTypeOrg));

    when(taxonomyRepository.existsTaxonomyByTaxonomyCode("category/")).thenReturn(Boolean.TRUE);

    InvalidValueException invalidValueException = assertThrows(InvalidValueException.class, () -> activity.validate(debtPositionDTO));
    assertEquals("The amount of secondary beneficiary is not valid", invalidValueException.getMessage());
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

    when(taxonomyRepository.existsTaxonomyByTaxonomyCode("category/")).thenReturn(Boolean.TRUE);

    InvalidValueException invalidValueException = assertThrows(InvalidValueException.class, () -> activity.validate(debtPositionDTO));
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

    when(taxonomyRepository.existsTaxonomyByTaxonomyCode("category/")).thenReturn(Boolean.TRUE);

    assertDoesNotThrow(() -> activity.validate(debtPositionDTO));
  }

  @Test
  void testValidateThenSuccess() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    DebtPositionTypeOrg mockDebtPositionTypeOrg = buildDebtPositionTypeOrg();

    Mockito.when(debtPositionTypeOrgRepository.findById(2L)).thenReturn(Optional.of(mockDebtPositionTypeOrg));

    assertDoesNotThrow(() -> activity.validate(debtPositionDTO));
  }
}

