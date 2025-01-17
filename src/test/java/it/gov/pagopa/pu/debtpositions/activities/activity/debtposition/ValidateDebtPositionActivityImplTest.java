package it.gov.pagopa.pu.debtpositions.activities.activity.debtposition;

import it.gov.pagopa.pu.debtpositions.activities.activity.ValidateDebtPositionActivity;
import it.gov.pagopa.pu.debtpositions.activities.activity.ValidateDebtPositionActivityImpl;
import it.gov.pagopa.pu.debtpositions.activities.exception.InvalidValueException;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.TransferDTO;
import it.gov.pagopa.pu.debtpositions.repository.TaxonomyRepository;
import it.gov.pagopa.pu.debtpositions.util.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static it.gov.pagopa.pu.debtpositions.util.faker.DebtPositionFaker.buildDebtPositionDTO;
import static it.gov.pagopa.pu.debtpositions.util.faker.TransferFaker.buildTransferDTO;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ValidateDebtPositionActivityImplTest {

    private ValidateDebtPositionActivity activity;

    @Mock
    private TaxonomyRepository taxonomyRepository;

    @BeforeEach
    void init() {
        activity = new ValidateDebtPositionActivityImpl(taxonomyRepository);
    }

    @Test
    void givenDebtPositionTypeOrgNullThenThrowValidationException(){
        DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
        debtPositionDTO.setDebtPositionTypeOrgId(null);

        InvalidValueException invalidValueException = assertThrows(InvalidValueException.class, () -> activity.validate(debtPositionDTO));
        assertEquals("Debt position type organization is mandatory", invalidValueException.getMessage());
    }

    @Test
    void givenDebtPositionTypeNullThenThrowValidationException(){
        DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
        //debtPositionDTO.getDebtPositionTypeOrg().setDebtPositionType(null); TODO to fix in p4pa-debt-position

        InvalidValueException invalidValueException = assertThrows(InvalidValueException.class, () -> activity.validate(debtPositionDTO));
        assertEquals("Debt position type organization is mandatory", invalidValueException.getMessage());
    }

    @Test
    void givenDebtPositionTypeCodeNullThenThrowValidationException(){
        DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
//        debtPositionDTO.getDebtPositionTypeOrg().getDebtPositionType().setCode(null); TODO to fix in p4pa-debt-position

        InvalidValueException invalidValueException = assertThrows(InvalidValueException.class, () -> activity.validate(debtPositionDTO));
        assertEquals("Debt position type organization is mandatory", invalidValueException.getMessage());
    }

//    @Test TODO restore in p4pa-debt-position once integrated DebtPositionTypeOrg
    void givenPaymentOptionsNullThenThrowValidationException(){
        DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
        debtPositionDTO.setPaymentOptions(null);

        InvalidValueException invalidValueException = assertThrows(InvalidValueException.class, () -> activity.validate(debtPositionDTO));
        assertEquals("Debt position payment options is mandatory", invalidValueException.getMessage());
    }

//    @Test TODO restore in p4pa-debt-position once integrated DebtPositionTypeOrg
    void givenInstallmentListEmptyThenThrowValidationException(){
        DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
        debtPositionDTO.getPaymentOptions().getFirst().setInstallments(List.of());

        InvalidValueException invalidValueException = assertThrows(InvalidValueException.class, () -> activity.validate(debtPositionDTO));
        assertEquals("At least one installment of the debt position is mandatory", invalidValueException.getMessage());
    }

//    @Test TODO restore in p4pa-debt-position once integrated DebtPositionTypeOrg
    void givenInstallmentWithoutRemittanceInfoThenThrowValidationException(){
        DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
        debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().setRemittanceInformation(null);

        InvalidValueException invalidValueException = assertThrows(InvalidValueException.class, () -> activity.validate(debtPositionDTO));
        assertEquals("Remittance information is mandatory", invalidValueException.getMessage());
    }

//    @Test TODO restore in p4pa-debt-position once integrated DebtPositionTypeOrg
    void givenInstallmentWithDueDateRetroactiveThenThrowValidationException(){
        DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
        debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().setDueDate(TestUtils.OFFSETDATETIME);

        InvalidValueException invalidValueException = assertThrows(InvalidValueException.class, () -> activity.validate(debtPositionDTO));
        assertEquals("The due date cannot be retroactive", invalidValueException.getMessage());
    }

//    @Test TODO restore in p4pa-debt-position once integrated DebtPositionTypeOrg
    void givenInstallmentWithDueDateNullButMandatoryThenThrowValidationException(){
        DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
//        debtPositionDTO.getDebtPositionTypeOrg().setFlagMandatoryDueDate(true); TODO to fix in p4pa-debt-position
        debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().setDueDate(null);

        InvalidValueException invalidValueException = assertThrows(InvalidValueException.class, () -> activity.validate(debtPositionDTO));
        assertEquals("The due date is mandatory", invalidValueException.getMessage());
    }

//    @Test TODO restore in p4pa-debt-position once integrated DebtPositionTypeOrg
    void givenInstallmentWithAmountNullThenThrowValidationException(){
        DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
//        debtPositionDTO.getDebtPositionTypeOrg().setFlagMandatoryDueDate(false); TODO to fix in p4pa-debt-position
        debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().setDueDate(null);
        debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().setAmountCents(null);

        InvalidValueException invalidValueException = assertThrows(InvalidValueException.class, () -> activity.validate(debtPositionDTO));
        assertEquals("Amount is mandatory", invalidValueException.getMessage());
    }

//    @Test TODO restore in p4pa-debt-position once integrated DebtPositionTypeOrg
    void givenInstallmentWithAmountInvalidThenThrowValidationException(){
        DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
//        debtPositionDTO.getDebtPositionTypeOrg().setFlagMandatoryDueDate(false); TODO to fix in p4pa-debt-position
        debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().setAmountCents(-200L);

        InvalidValueException invalidValueException = assertThrows(InvalidValueException.class, () -> activity.validate(debtPositionDTO));
        assertEquals("Amount is not valid", invalidValueException.getMessage());
    }

//    @Test TODO restore in p4pa-debt-position once integrated DebtPositionTypeOrg
    void givenInstallmentWithAmountInvalidForDebtPositionTypeThenThrowValidationException(){
        DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
//        debtPositionDTO.getDebtPositionTypeOrg().setAmount(200L); TODO to fix in p4pa-debt-position
        debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().setAmountCents(100L);

        InvalidValueException invalidValueException = assertThrows(InvalidValueException.class, () -> activity.validate(debtPositionDTO));
        assertEquals("Amount is not valid for this debt position type org", invalidValueException.getMessage());
    }

//    @Test TODO restore in p4pa-debt-position once integrated DebtPositionTypeOrg
    void givenPersonNullThenThrowValidationException(){
        DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
//        debtPositionDTO.getDebtPositionTypeOrg().setAmount(100L); TODO to fix in p4pa-debt-position
        debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().setAmountCents(100L);
        debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().setDebtor(null);

        InvalidValueException invalidValueException = assertThrows(InvalidValueException.class, () -> activity.validate(debtPositionDTO));
        assertEquals("The debtor is mandatory for installment", invalidValueException.getMessage());
    }

//    @Test TODO restore in p4pa-debt-position once integrated DebtPositionTypeOrg
    void givenPersonWithAnonimousCFButNotAnonymousFlagThenThrowValidationException(){
        DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
//        debtPositionDTO.getDebtPositionTypeOrg().setAmount(null); TODO to fix in p4pa-debt-position
//        debtPositionDTO.getDebtPositionTypeOrg().setFlagAnonymousFiscalCode(false);
        debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().getDebtor().setFiscalCode("ANONIMO");

        InvalidValueException invalidValueException = assertThrows(InvalidValueException.class, () -> activity.validate(debtPositionDTO));
        assertEquals("This organization installment type or installment does not allow an anonymous unique identification code", invalidValueException.getMessage());
    }

//    @Test TODO restore in p4pa-debt-position once integrated DebtPositionTypeOrg
    void givenPersonWithNullCFButNotAnonymousFlagForDebtTypeThenThrowValidationException(){
        DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
//        debtPositionDTO.getDebtPositionTypeOrg().setFlagAnonymousFiscalCode(true); TODO to fix in p4pa-debt-position
        debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().getDebtor().setFiscalCode(null);

        InvalidValueException invalidValueException = assertThrows(InvalidValueException.class, () -> activity.validate(debtPositionDTO));
        assertEquals("Unique identification code is mandatory", invalidValueException.getMessage());
    }

//    @Test TODO restore in p4pa-debt-position once integrated DebtPositionTypeOrg
    void givenPersonWithNullFullNameThenThrowValidationException(){
        DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
//        debtPositionDTO.getDebtPositionTypeOrg().setFlagAnonymousFiscalCode(true); TODO to fix in p4pa-debt-position
        debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().getDebtor().setFullName(null);

        InvalidValueException invalidValueException = assertThrows(InvalidValueException.class, () -> activity.validate(debtPositionDTO));
        assertEquals("Beneficiary name is mandatory", invalidValueException.getMessage());
    }

//    @Test TODO restore in p4pa-debt-position once integrated DebtPositionTypeOrg
    void givenPersonWithNullEmailThenThrowValidationException(){
        DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
        debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().getDebtor().setEmail(null);

        InvalidValueException invalidValueException = assertThrows(InvalidValueException.class, () -> activity.validate(debtPositionDTO));
        assertEquals("Email is not valid", invalidValueException.getMessage());
    }

//    @Test TODO restore in p4pa-debt-position once integrated DebtPositionTypeOrg
    void givenPersonWithInvalidEmailThenThrowValidationException(){
        DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
        debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().getDebtor().setEmail("test&it");

        InvalidValueException invalidValueException = assertThrows(InvalidValueException.class, () -> activity.validate(debtPositionDTO));
        assertEquals("Email is not valid", invalidValueException.getMessage());
    }

//    @Test TODO restore in p4pa-debt-position once integrated DebtPositionTypeOrg
    void givenNoTransfersThenThrowValidationException(){
        DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
        debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().setTransfers(null);

        InvalidValueException invalidValueException = assertThrows(InvalidValueException.class, () -> activity.validate(debtPositionDTO));
        assertEquals("At least one transfer is mandatory for installment", invalidValueException.getMessage());
    }

//    @Test TODO restore in p4pa-debt-position once integrated DebtPositionTypeOrg
    void givenTransfersMismatchThenThrowValidationException(){
        DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
        TransferDTO secondTransfer = buildTransferDTO();
        debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().getTransfers().add(secondTransfer);

        InvalidValueException invalidValueException = assertThrows(InvalidValueException.class, () -> activity.validate(debtPositionDTO));
        assertEquals("Mismatch with transfers list", invalidValueException.getMessage());
    }

//    @Test TODO restore in p4pa-debt-position once integrated DebtPositionTypeOrg
    void givenSecondTransferPIVANullThenThrowValidationException(){
        DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
        TransferDTO secondTransfer = buildTransferDTO();
        secondTransfer.setTransferIndex(1L);
        secondTransfer.setOrgFiscalCode(null);
        debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().getTransfers().add(secondTransfer);

        InvalidValueException invalidValueException = assertThrows(InvalidValueException.class, () -> activity.validate(debtPositionDTO));
        assertEquals("Fiscal code of secondary beneficiary is not valid", invalidValueException.getMessage());
    }

//    @Test TODO restore in p4pa-debt-position once integrated DebtPositionTypeOrg
    void givenSecondTransferPIVANotValidThenThrowValidationException(){
        DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
        TransferDTO secondTransfer = buildTransferDTO();
        secondTransfer.setTransferIndex(1L);
        secondTransfer.setOrgFiscalCode("00000000001");
        debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().getTransfers().add(secondTransfer);

        InvalidValueException invalidValueException = assertThrows(InvalidValueException.class, () -> activity.validate(debtPositionDTO));
        assertEquals("Fiscal code of secondary beneficiary is not valid", invalidValueException.getMessage());
    }

//    @Test TODO restore in p4pa-debt-position once integrated DebtPositionTypeOrg
    void givenSecondTransferIbanNullThenThrowValidationException(){
        DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
        TransferDTO secondTransfer = buildTransferDTO();
        secondTransfer.setTransferIndex(1L);
        secondTransfer.setOrgFiscalCode("31798530361");
        secondTransfer.setIban(null);
        debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().getTransfers().add(secondTransfer);

        InvalidValueException invalidValueException = assertThrows(InvalidValueException.class, () -> activity.validate(debtPositionDTO));
        assertEquals("Iban of secondary beneficiary is not valid", invalidValueException.getMessage());
    }

//    @Test TODO restore in p4pa-debt-position once integrated DebtPositionTypeOrg
    void givenSecondTransferCategoryNullThenThrowValidationException(){
        DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
        TransferDTO secondTransfer = buildTransferDTO();
        secondTransfer.setTransferIndex(1L);
        secondTransfer.setOrgFiscalCode("31798530361");
        secondTransfer.setIban("IT00A0000001234567891234567");
        secondTransfer.setCategory(null);
        debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().getTransfers().add(secondTransfer);

        InvalidValueException invalidValueException = assertThrows(InvalidValueException.class, () -> activity.validate(debtPositionDTO));
        assertEquals("Category of secondary beneficiary is mandatory", invalidValueException.getMessage());
    }

//    @Test TODO restore in p4pa-debt-position once integrated DebtPositionTypeOrg
    void givenSecondTransferCategoryNotFoundThenThrowValidationException(){
        DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
        TransferDTO secondTransfer = buildTransferDTO();
        secondTransfer.setTransferIndex(1L);
        secondTransfer.setOrgFiscalCode("31798530361");
        secondTransfer.setIban("IT00A0000001234567891234567");
        secondTransfer.setCategory("category");
        debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().getTransfers().add(secondTransfer);

        when(taxonomyRepository.existsTaxonomyByTaxonomyCode("category/")).thenReturn(null);

        InvalidValueException invalidValueException = assertThrows(InvalidValueException.class, () -> activity.validate(debtPositionDTO));
        assertEquals("The category code does not exist in the archive", invalidValueException.getMessage());
    }

//    @Test TODO restore in p4pa-debt-position once integrated DebtPositionTypeOrg
    void givenSecondTransferAmountNullThenThrowValidationException(){
        DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
        TransferDTO secondTransfer = buildTransferDTO();
        secondTransfer.setTransferIndex(1L);
        secondTransfer.setOrgFiscalCode("31798530361");
        secondTransfer.setIban("IT00A0000001234567891234567");
        secondTransfer.setCategory("category");
        secondTransfer.setAmountCents(null);
        debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().getTransfers().add(secondTransfer);

        when(taxonomyRepository.existsTaxonomyByTaxonomyCode("category/")).thenReturn(Boolean.TRUE);

        InvalidValueException invalidValueException = assertThrows(InvalidValueException.class, () -> activity.validate(debtPositionDTO));
        assertEquals("The amount of secondary beneficiary is not valid", invalidValueException.getMessage());
    }

//    @Test TODO restore in p4pa-debt-position once integrated DebtPositionTypeOrg
    void givenSecondTransferAmountNegativeThenThrowValidationException(){
        DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
        TransferDTO secondTransfer = buildTransferDTO();
        secondTransfer.setTransferIndex(1L);
        secondTransfer.setOrgFiscalCode("31798530361");
        secondTransfer.setIban("IT00A0000001234567891234567");
        secondTransfer.setCategory("category");
        secondTransfer.setAmountCents(-12L);
        debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().getTransfers().add(secondTransfer);

        when(taxonomyRepository.existsTaxonomyByTaxonomyCode("category/")).thenReturn(Boolean.TRUE);

        InvalidValueException invalidValueException = assertThrows(InvalidValueException.class, () -> activity.validate(debtPositionDTO));
        assertEquals("The amount of secondary beneficiary is not valid", invalidValueException.getMessage());
    }

//    @Test TODO restore in p4pa-debt-position once integrated DebtPositionTypeOrg
    void givenSecondTransferThenSuccess(){
        DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
        TransferDTO secondTransfer = buildTransferDTO();
        secondTransfer.setTransferIndex(1L);
        secondTransfer.setOrgFiscalCode("31798530361");
        secondTransfer.setIban("IT00A0000001234567891234567");
        secondTransfer.setCategory("category");
        secondTransfer.setAmountCents(12L);
        debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().getTransfers().add(secondTransfer);

        when(taxonomyRepository.existsTaxonomyByTaxonomyCode("category/")).thenReturn(Boolean.TRUE);

        assertDoesNotThrow(() -> activity.validate(debtPositionDTO));
    }

//    @Test TODO restore in p4pa-debt-position once integrated DebtPositionTypeOrg
    void testValidateThenSuccess(){
        DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();

        assertDoesNotThrow(() -> activity.validate(debtPositionDTO));
    }
}

