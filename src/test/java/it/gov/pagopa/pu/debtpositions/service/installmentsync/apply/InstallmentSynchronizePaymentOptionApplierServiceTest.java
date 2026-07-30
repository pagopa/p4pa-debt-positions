package it.gov.pagopa.pu.debtpositions.service.installmentsync.apply;

import it.gov.pagopa.pu.debtpositions.dto.generated.ErrorFieldDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentSynchronizeDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.PaymentOptionDTO;
import it.gov.pagopa.pu.debtpositions.exception.custom.ConflictErrorException;
import it.gov.pagopa.pu.debtpositions.util.ErrorCodeConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static it.gov.pagopa.pu.debtpositions.util.faker.InstallmentSynchronizeFaker.buildInstallmentSynchronizeDTO;
import static it.gov.pagopa.pu.debtpositions.util.faker.PaymentOptionFaker.buildSyncPaymentOptionDTO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class InstallmentSynchronizePaymentOptionApplierServiceTest {

  private InstallmentSynchronizePaymentOptionApplierService applierPaymentOptionService;

  @BeforeEach
  void setUp() {
    applierPaymentOptionService = new InstallmentSynchronizePaymentOptionApplierService();
  }

  @Test
  void testMergePaymentOptionThenOk(){
    InstallmentSynchronizeDTO installmentSynchronizeDTO = buildInstallmentSynchronizeDTO();
    installmentSynchronizeDTO.setPaymentOptionDescription("New description");
    PaymentOptionDTO paymentOptionDTO = buildSyncPaymentOptionDTO();

    applierPaymentOptionService.merge(installmentSynchronizeDTO, paymentOptionDTO);

    assertEquals("New description", paymentOptionDTO.getDescription());
  }

  @Test
  void testMergePaymentOptionImmutableFieldThenException(){
    InstallmentSynchronizeDTO installmentSynchronizeDTO = buildInstallmentSynchronizeDTO();
    installmentSynchronizeDTO.setDescription("New description");
    installmentSynchronizeDTO.setPaymentOptionType("New payment option type");
    PaymentOptionDTO paymentOptionDTO = buildSyncPaymentOptionDTO();

    ConflictErrorException exception = assertThrows(ConflictErrorException.class,
      () -> applierPaymentOptionService.merge(installmentSynchronizeDTO, paymentOptionDTO));
    assertEquals("IMMUTABLE_FIELD",exception.getCode());
    assertEquals("These fields for payment option with index 1 of debt position with iupd IUPD_ORG are not mutable: [paymentOptionType]", exception.getMessage());
    assertEquals(List.of(
        new ErrorFieldDTO("paymentOptionType", ErrorCodeConstants.ERROR_CODE_IMMUTABLE_FIELD, "Cannot be updated")
      ),
      exception.getFields());
  }
}
