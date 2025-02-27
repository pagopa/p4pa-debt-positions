package it.gov.pagopa.pu.debtpositions.service.create.receipt;

import it.gov.pagopa.pu.debtpositions.exception.custom.NotFoundException;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrg;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionTypeOrgRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class DebtPositionTypeOrgRetrieverServiceTest {

  @Mock
  private DebtPositionTypeOrgRepository debtPositionTypeOrgRepositoryMock;

  private DebtPositionTypeOrgRetrieverService debtPositionTypeOrgRetrieverService;

  @BeforeEach
  void setUp() {
    debtPositionTypeOrgRetrieverService = new DebtPositionTypeOrgRetrieverService(debtPositionTypeOrgRepositoryMock, "pagopaReceiptDebtPositionTypeOrgCode");
  }

  @Test
  void givenOrganizationIdWhenGetPagopaReceiptDebtPositionTypeOrgThenOk() {
    // given
    Long organizationId = 1L;
    DebtPositionTypeOrg debtPositionTypeOrg = new DebtPositionTypeOrg();
    Mockito.when(debtPositionTypeOrgRepositoryMock.findByOrganizationIdAndCode(organizationId, "pagopaReceiptDebtPositionTypeOrgCode"))
      .thenReturn(Optional.of(debtPositionTypeOrg));

    // when
    DebtPositionTypeOrg response = debtPositionTypeOrgRetrieverService.getPagopaReceiptDebtPositionTypeOrg(organizationId);

    // verify
    Assertions.assertEquals(debtPositionTypeOrg, response);
    Mockito.verify(debtPositionTypeOrgRepositoryMock, Mockito.times(1))
      .findByOrganizationIdAndCode(organizationId, "pagopaReceiptDebtPositionTypeOrgCode");
  }

  @Test
  void givenNotFoundOrganizationIdWhenGetPagopaReceiptDebtPositionTypeOrgThenException() {
    // given
    Long organizationId = 1L;
    Mockito.when(debtPositionTypeOrgRepositoryMock.findByOrganizationIdAndCode(organizationId, "pagopaReceiptDebtPositionTypeOrgCode"))
      .thenReturn(Optional.empty());

    // when
    Assertions.assertThrows(NotFoundException.class,() -> debtPositionTypeOrgRetrieverService.getPagopaReceiptDebtPositionTypeOrg(organizationId));

    // verify
    Mockito.verify(debtPositionTypeOrgRepositoryMock, Mockito.times(1))
      .findByOrganizationIdAndCode(organizationId, "pagopaReceiptDebtPositionTypeOrgCode");
  }

}
