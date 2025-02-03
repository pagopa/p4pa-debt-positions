package it.gov.pagopa.pu.debtpositions.service;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionTypeOrgDTO;
import it.gov.pagopa.pu.debtpositions.exception.custom.NotFoundException;
import it.gov.pagopa.pu.debtpositions.mapper.workflow.DebtPositionTypeOrgMapper;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionTypeOrgRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static it.gov.pagopa.pu.debtpositions.util.faker.DebtPositionTypeOrgFaker.buildDebtPositionTypeOrg;
import static it.gov.pagopa.pu.debtpositions.util.faker.DebtPositionTypeOrgFaker.buildDebtPositionTypeOrgDTO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class DebtPositionTypeOrgServiceImplTest {

  @Mock
  private DebtPositionTypeOrgRepository debtPositionTypeOrgRepositoryMock;
  @Mock
  private DebtPositionTypeOrgMapper debtPositionTypeOrgMapperMock;

  @InjectMocks
  private DebtPositionTypeOrgServiceImpl debtPositionTypeOrgService;

  @Test
  void givenValidOrganizationAndCodeWhenFindByOrganizationIdAndCodeThenOk() {
    Long orgId = 1L;
    String code = "code";

    DebtPositionTypeOrgDTO debtPositionTypeOrgDTO = buildDebtPositionTypeOrgDTO();

    Mockito.when(debtPositionTypeOrgRepositoryMock.findByOrganizationIdAndCode(orgId, code)).thenReturn(Optional.of(buildDebtPositionTypeOrg()));
    Mockito.when(debtPositionTypeOrgMapperMock.map(buildDebtPositionTypeOrg())).thenReturn(buildDebtPositionTypeOrgDTO());

    DebtPositionTypeOrgDTO result = debtPositionTypeOrgService.getDebtPositionTypeOrgByOrganizationIdAndCode(orgId, code);

    assertEquals(debtPositionTypeOrgDTO, result);
  }

  @Test
  void givenValidOrganizationAndCodeWhenFindByOrganizationIdAndCodeThenThrowNotFoundException() {
    Long orgId = 1L;
    String code = "code";

    Mockito.when(debtPositionTypeOrgRepositoryMock.findByOrganizationIdAndCode(orgId, code)).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () ->
      debtPositionTypeOrgService.getDebtPositionTypeOrgByOrganizationIdAndCode(orgId, code));
  }
}
