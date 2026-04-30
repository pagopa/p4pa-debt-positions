package it.gov.pagopa.pu.debtpositions.service.dptypeorg;

import static it.gov.pagopa.pu.debtpositions.util.Constants.DEBT_POSITION_TYPE_MIXED;

import it.gov.pagopa.pu.debtpositions.mapper.DebtPositionTypeOrgMapper;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionType;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrg;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionTypeOrgRepository;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionTypeRepository;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MixedDebtPositionTypeOrgRetrieverServiceTest {

  @Mock
  private DebtPositionTypeOrgRepository debtPositionTypeOrgRepositoryMock;
  @Mock
  private DebtPositionTypeRepository debtPositionTypeRepositoryMock;
  @Mock
  private DebtPositionTypeOrgMapper debtPositionTypeOrgMapperMock;

  @InjectMocks
  private MixedDebtPositionTypeOrgRetrieverService mixedDebtPositionTypeOrgRetrieverService;

  @Test
  void givenOrganizationIdWhenGetUnknownDebtPositionTypeOrgThenOk() {
    // given
    Long organizationId = 1L;
    DebtPositionType debtPositionType = new DebtPositionType();
    debtPositionType.setCode("TYPE_CODE");
    Mockito.when(debtPositionTypeRepositoryMock.findById(DEBT_POSITION_TYPE_MIXED)).thenReturn(Optional.of(debtPositionType));
    DebtPositionTypeOrg debtPositionTypeOrg = new DebtPositionTypeOrg();
    Mockito.when(debtPositionTypeOrgRepositoryMock.findByOrganizationIdAndCode(organizationId, debtPositionType.getCode()))
      .thenReturn(Optional.of(debtPositionTypeOrg));

    // when
    DebtPositionTypeOrg response = mixedDebtPositionTypeOrgRetrieverService.getMixedDebtPositionTypeOrg(organizationId);

    // verify
    Assertions.assertEquals(debtPositionTypeOrg, response);
    Mockito.verify(debtPositionTypeRepositoryMock, Mockito.times(1)).findById(DEBT_POSITION_TYPE_MIXED);
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
    Mockito.when(debtPositionTypeRepositoryMock.findById(DEBT_POSITION_TYPE_MIXED)).thenReturn(Optional.of(debtPositionType));
    DebtPositionTypeOrg debtPositionTypeOrg = new DebtPositionTypeOrg();
    Mockito.when(debtPositionTypeOrgRepositoryMock.findByOrganizationIdAndCode(organizationId, debtPositionType.getCode()))
      .thenReturn(Optional.empty());
    Mockito.when(debtPositionTypeOrgMapperMock.mapFromDebtPositionType(debtPositionType, organizationId))
      .thenReturn(debtPositionTypeOrg);
    Mockito.when(debtPositionTypeOrgRepositoryMock.save(debtPositionTypeOrg)).thenReturn(debtPositionTypeOrg);

    // when
    DebtPositionTypeOrg response = mixedDebtPositionTypeOrgRetrieverService.getMixedDebtPositionTypeOrg(organizationId);

    // verify
    Assertions.assertEquals(debtPositionTypeOrg, response);
    Mockito.verify(debtPositionTypeRepositoryMock, Mockito.times(1)).findById(DEBT_POSITION_TYPE_MIXED);
    Mockito.verify(debtPositionTypeOrgRepositoryMock, Mockito.times(1))
      .findByOrganizationIdAndCode(organizationId, debtPositionType.getCode());
    Mockito.verify(debtPositionTypeOrgMapperMock, Mockito.times(1))
      .mapFromDebtPositionType(debtPositionType, organizationId);
    Mockito.verify(debtPositionTypeOrgRepositoryMock, Mockito.times(1)).save(debtPositionTypeOrg);
  }

}
