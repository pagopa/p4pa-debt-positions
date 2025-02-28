package it.gov.pagopa.pu.debtpositions.service.create.receipt;

import it.gov.pagopa.pu.debtpositions.mapper.DebtPositionTypeOrgMapper;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionType;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrg;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionTypeOrgRepository;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionTypeRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static it.gov.pagopa.pu.debtpositions.service.create.receipt.UnknownDebtPositionTypeOrgRetrieverService.DEBT_POSITION_TYPE_UNKNOWN;

@ExtendWith(MockitoExtension.class)
class UnknownDebtPositionTypeOrgRetrieverServiceTest {

  @Mock
  private DebtPositionTypeOrgRepository debtPositionTypeOrgRepositoryMock;
  @Mock
  private DebtPositionTypeRepository debtPositionTypeRepositoryMock;
  @Mock
  private DebtPositionTypeOrgMapper debtPositionTypeOrgMapperMock;

  @InjectMocks
  private UnknownDebtPositionTypeOrgRetrieverService unknownDebtPositionTypeOrgRetrieverService;

  @Test
  void givenOrganizationIdWhenGetUnknownDebtPositionTypeOrgThenOk() {
    // given
    Long organizationId = 1L;
    DebtPositionType debtPositionType = new DebtPositionType();
    debtPositionType.setCode("TYPE_CODE");
    Mockito.when(debtPositionTypeRepositoryMock.findById(DEBT_POSITION_TYPE_UNKNOWN)).thenReturn(Optional.of(debtPositionType));
    DebtPositionTypeOrg debtPositionTypeOrg = new DebtPositionTypeOrg();
    Mockito.when(debtPositionTypeOrgRepositoryMock.findByOrganizationIdAndCode(organizationId, debtPositionType.getCode()))
      .thenReturn(Optional.of(debtPositionTypeOrg));

    // when
    DebtPositionTypeOrg response = unknownDebtPositionTypeOrgRetrieverService.getUnknownDebtPositionTypeOrg(organizationId);

    // verify
    Assertions.assertEquals(debtPositionTypeOrg, response);
    Mockito.verify(debtPositionTypeRepositoryMock, Mockito.times(1)).findById(DEBT_POSITION_TYPE_UNKNOWN);
    Mockito.verify(debtPositionTypeOrgRepositoryMock, Mockito.times(1))
      .findByOrganizationIdAndCode(organizationId, debtPositionType.getCode());
    Mockito.verifyNoInteractions(debtPositionTypeOrgMapperMock);
  }

  @Test
  void givenNotFoundOrganizationIdWhenGetUnknownDebtPositionTypeOrgThenException() {
    // given
    Long organizationId = 1L;
    DebtPositionType debtPositionType = new DebtPositionType();
    debtPositionType.setCode("TYPE_CODE");
    Mockito.when(debtPositionTypeRepositoryMock.findById(DEBT_POSITION_TYPE_UNKNOWN)).thenReturn(Optional.of(debtPositionType));
    DebtPositionTypeOrg debtPositionTypeOrg = new DebtPositionTypeOrg();
    Mockito.when(debtPositionTypeOrgRepositoryMock.findByOrganizationIdAndCode(organizationId, debtPositionType.getCode()))
      .thenReturn(Optional.empty());
    Mockito.when(debtPositionTypeOrgMapperMock.mapFromDebtPositionType(debtPositionType, organizationId))
      .thenReturn(debtPositionTypeOrg);
    Mockito.when(debtPositionTypeOrgRepositoryMock.save(debtPositionTypeOrg)).thenReturn(debtPositionTypeOrg);

    // when
    DebtPositionTypeOrg response = unknownDebtPositionTypeOrgRetrieverService.getUnknownDebtPositionTypeOrg(organizationId);

    // verify
    Assertions.assertEquals(debtPositionTypeOrg, response);
    Mockito.verify(debtPositionTypeRepositoryMock, Mockito.times(1)).findById(DEBT_POSITION_TYPE_UNKNOWN);
    Mockito.verify(debtPositionTypeOrgRepositoryMock, Mockito.times(1))
      .findByOrganizationIdAndCode(organizationId, debtPositionType.getCode());
    Mockito.verify(debtPositionTypeOrgMapperMock, Mockito.times(1))
      .mapFromDebtPositionType(debtPositionType, organizationId);
    Mockito.verify(debtPositionTypeOrgRepositoryMock, Mockito.times(1)).save(debtPositionTypeOrg);
  }

}
