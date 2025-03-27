package it.gov.pagopa.pu.debtpositions.service.create.debtposition;

import it.gov.pagopa.pu.debtpositions.dto.generated.*;
import it.gov.pagopa.pu.debtpositions.exception.custom.NotFoundException;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrg;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionTypeRepository;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static it.gov.pagopa.pu.debtpositions.util.faker.DebtPositionFaker.buildDebtPositionDTO;
import static it.gov.pagopa.pu.debtpositions.util.faker.DebtPositionTypeFaker.buildDebtPositionType;
import static it.gov.pagopa.pu.debtpositions.util.faker.DebtPositionTypeOrgFaker.buildDebtPositionTypeOrg;
import static it.gov.pagopa.pu.debtpositions.util.faker.InstallmentFaker.buildInstallmentDTO;
import static it.gov.pagopa.pu.debtpositions.util.faker.OrganizationFaker.buildOrganization;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class DebtPositionProcessorServiceImplTest {

  @Mock
  private DebtPositionTypeRepository debtPositionTypeRepositoryMock;

  private DebtPositionProcessorServiceImpl debtPositionProcessorService;

  @BeforeEach
  void setUp() {
    debtPositionProcessorService = new DebtPositionProcessorServiceImpl(debtPositionTypeRepositoryMock);
  }

  @Test
  void givenDebtPositionWhenUpdateAmountsThenOk() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    debtPositionDTO.getPaymentOptions().getFirst().getInstallments().addAll(IntStream.range(0, 2)
      .mapToObj(i -> buildInstallmentDTO())
      .collect(Collectors.toCollection(ArrayList::new)));

    DebtPositionDTO result = debtPositionProcessorService.updateAmounts(debtPositionDTO);

    assertEquals(30000, result.getPaymentOptions().getFirst().getTotalAmountCents());
  }

  @Test
  void givenDebtPositionWithInstallmentCancelledWhenUpdateAmountsThenOk() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    InstallmentDTO firstInstallment = debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst();
    debtPositionDTO.getPaymentOptions().getFirst().getInstallments().addAll(IntStream.range(0, 2)
      .mapToObj(i -> buildInstallmentDTO())
      .collect(Collectors.toCollection(ArrayList::new)));
    firstInstallment.setStatus(InstallmentStatus.CANCELLED);
    firstInstallment.setSyncStatus(null);

    DebtPositionDTO result = debtPositionProcessorService.updateAmounts(debtPositionDTO);

    assertEquals(20000, result.getPaymentOptions().getFirst().getTotalAmountCents());
  }

  @Test
  void givenDebtPositionWithInstallmentWithStatusToCancelledWhenUpdateAmountsThenOk() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    InstallmentDTO firstInstallment = debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst();
    debtPositionDTO.getPaymentOptions().getFirst().getInstallments().addAll(IntStream.range(0, 2)
      .mapToObj(i -> buildInstallmentDTO())
      .collect(Collectors.toCollection(ArrayList::new)));
    firstInstallment.setSyncStatus(InstallmentSyncStatus.builder()
      .syncStatusFrom(InstallmentStatus.UNPAID).syncStatusTo(InstallmentStatus.CANCELLED).build());

    DebtPositionDTO result = debtPositionProcessorService.updateAmounts(debtPositionDTO);

    assertEquals(20000, result.getPaymentOptions().getFirst().getTotalAmountCents());
  }

  @Test
  void populateFirstTransferThenOk() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    Organization organization = buildOrganization();
    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();

    TransferDTO firstTransferExpected = TransferDTO.builder()
      .transferIndex(1)
      .orgFiscalCode(organization.getOrgFiscalCode())
      .orgName(organization.getOrgName())
      .iban("IT60X0542811101000000123456")
      .category("category")
      .amountCents(9000L)
      .remittanceInformation("remittanceInformation")
      .build();

    Mockito.when(debtPositionTypeRepositoryMock.findById(debtPositionTypeOrg.getDebtPositionTypeId())).thenReturn(Optional.of(buildDebtPositionType()));

    debtPositionProcessorService.populateFirstTransfer(debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst(),
      organization, debtPositionTypeOrg);

    assertEquals(debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst().getTransfers().getLast(), firstTransferExpected);
  }

  @Test
  void givenNoDPTypeFoundWhenPopulateFirstTransferThenException() {
    DebtPositionDTO debtPositionDTO = buildDebtPositionDTO();
    Organization organization = buildOrganization();
    DebtPositionTypeOrg debtPositionTypeOrg = buildDebtPositionTypeOrg();
    InstallmentDTO installmentDTO = debtPositionDTO.getPaymentOptions().getFirst().getInstallments().getFirst();

    Mockito.when(debtPositionTypeRepositoryMock.findById(debtPositionTypeOrg.getDebtPositionTypeId())).thenReturn(Optional.empty());

    NotFoundException exception = assertThrows(NotFoundException.class,
      () -> debtPositionProcessorService.populateFirstTransfer(installmentDTO,
        organization, debtPositionTypeOrg));

    assertEquals("The debt position type with id 100 is not found", exception.getMessage());
  }

}

