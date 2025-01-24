package it.gov.pagopa.pu.debtpositions.service.create.debtposition;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.exception.custom.ConflictErrorException;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrg;
import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;
import it.gov.pagopa.pu.debtpositions.repository.InstallmentNoPIIRepository;
import it.gov.pagopa.pu.debtpositions.service.AuthorizeOperatorOnDebtPositionTypeService;
import it.gov.pagopa.pu.debtpositions.service.DebtPositionService;
import it.gov.pagopa.pu.debtpositions.service.create.GenerateIuvService;
import it.gov.pagopa.pu.debtpositions.service.create.ValidateDebtPositionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static it.gov.pagopa.pu.debtpositions.util.TestUtils.reflectionEqualsByName;
import static it.gov.pagopa.pu.debtpositions.util.faker.DebtPositionFaker.*;
import static it.gov.pagopa.pu.debtpositions.util.faker.DebtPositionTypeOrgFaker.buildDebtPositionTypeOrg;
import static it.gov.pagopa.pu.debtpositions.util.faker.InstallmentFaker.buildInstallmentNoPII;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class CreateDebtPositionServiceImplTest {

  @Mock
  private AuthorizeOperatorOnDebtPositionTypeService authorizeOperatorOnDebtPositionTypeService;
  @Mock
  private ValidateDebtPositionService validateDebtPositionService;
  @Mock
  private InstallmentNoPIIRepository installmentNoPIIRepository;
  @Mock
  private DebtPositionService debtPositionService;
  @Mock
  private GenerateIuvService generateIuvService;

  private CreateDebtPositionService createDebtPositionService;

  private final Long orgId = 500L;
  private final Long debtPositionTypeOrgId = 2L;

  @BeforeEach
  void setUp() {
    createDebtPositionService = new CreateDebtPositionServiceImpl(authorizeOperatorOnDebtPositionTypeService,
      validateDebtPositionService, debtPositionService, generateIuvService, installmentNoPIIRepository);
  }

  @Test
  void givenDebtPositionWhenCreateThenOk() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();

    DebtPosition debtPosition = buildDebtPosition();
    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();
    InstallmentNoPII installmentNoPII = buildInstallmentNoPII();

    Mockito.when(authorizeOperatorOnDebtPositionTypeService.authorize(orgId, debtPositionTypeOrgId, null)).thenReturn(debtPositionTypeOrg);
    Mockito.doNothing().when(validateDebtPositionService).validate(debtPositionDTO, null);
    Mockito.when(installmentNoPIIRepository.countExistingInstallments(debtPosition.getOrganizationId(), installmentNoPII.getIud(), installmentNoPII.getIuv(), installmentNoPII.getNav())).thenReturn(0L);
    Mockito.when(debtPositionService.saveDebtPosition(debtPositionDTO)).thenReturn(debtPositionDTO);

    DebtPositionDTO result = createDebtPositionService.createDebtPosition(debtPositionDTO, false, false, null, null);

    assertEquals(debtPositionDTO, result);
    reflectionEqualsByName(buildDebtPositionDTO(), result);
  }

  @Test
  void givenDebtPositionWithDuplicatesWhenCreateThenThrowConflictErrorException() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();

    DebtPosition debtPosition = buildDebtPosition();
    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();
    InstallmentNoPII installmentNoPII = buildInstallmentNoPII();

    Mockito.when(authorizeOperatorOnDebtPositionTypeService.authorize(orgId, debtPositionTypeOrgId, null)).thenReturn(debtPositionTypeOrg);
    Mockito.doNothing().when(validateDebtPositionService).validate(debtPositionDTO, null);
    Mockito.when(installmentNoPIIRepository.countExistingInstallments(debtPosition.getOrganizationId(), installmentNoPII.getIud(), installmentNoPII.getIuv(), installmentNoPII.getNav())).thenReturn(2L);

    ConflictErrorException exception = assertThrows(ConflictErrorException.class, () ->
      createDebtPositionService.createDebtPosition(debtPositionDTO, false, false, null, null)
    );
    assertEquals("Duplicate records found: the provided data conflicts with existing records.", exception.getMessage());
  }

  @Test
  void givenDebtPositionWhenGenerateIuvThenAssignIuvToInstallments() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();

    DebtPosition debtPosition = buildDebtPosition();
    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();
    InstallmentNoPII installmentNoPII = buildInstallmentNoPII();

    Mockito.when(authorizeOperatorOnDebtPositionTypeService.authorize(orgId, debtPositionTypeOrgId, null)).thenReturn(debtPositionTypeOrg);
    Mockito.doNothing().when(validateDebtPositionService).validate(debtPositionDTO, null);
    Mockito.when(installmentNoPIIRepository.countExistingInstallments(debtPosition.getOrganizationId(), installmentNoPII.getIud(), installmentNoPII.getIuv(), installmentNoPII.getNav())).thenReturn(0L);
    Mockito.when(generateIuvService.generateIuv(debtPositionDTO.getOrganizationId(), null)).thenReturn("generatedIuv");
    Mockito.when(generateIuvService.iuv2Nav("generatedIuv")).thenReturn("generatedNav");
    Mockito.when(debtPositionService.saveDebtPosition(debtPositionDTO)).thenReturn(buildGeneratedIuvDebtPositionDTO());

    DebtPositionDTO result = createDebtPositionService.createDebtPosition(debtPositionDTO, false, true, null, null);

    result.getPaymentOptions().stream()
      .flatMap(po -> po.getInstallments().stream())
      .forEach(inst -> assertEquals("generatedIuv", inst.getIuv()));
    assertEquals(debtPositionDTO, result);
    reflectionEqualsByName(buildGeneratedIuvDebtPositionDTO(), result);
  }
}
