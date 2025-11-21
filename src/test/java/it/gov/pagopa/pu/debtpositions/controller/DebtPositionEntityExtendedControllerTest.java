package it.gov.pagopa.pu.debtpositions.controller;

import it.gov.pagopa.pu.debtpositions.controller.generated.DebtPositionEntityExtendedControllerApi;
import it.gov.pagopa.pu.debtpositions.dto.BaseDebtPosition;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionOrigin;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

@ExtendWith(MockitoExtension.class)
class DebtPositionEntityExtendedControllerTest {

  @Mock
  private DebtPositionRepository repositoryMock;

  private DebtPositionEntityExtendedControllerApi api;

  @BeforeEach
  void init(){
    api = new DebtPositionEntityExtendedControllerImpl(repositoryMock);
  }

  @AfterEach
  void verifyNoMoreInteractions(){
    Mockito.verifyNoMoreInteractions(repositoryMock);
  }

  @Test
  void whenGetDebtPositionNoPiiByIdThenInvokeRepositoryAndReturnResult(){
    // Given
    Long dpId = -1L;
    DebtPosition expectedBody = new DebtPosition();

    Mockito.when(repositoryMock.findEntityGraphByDebtPositionId(dpId))
      .thenReturn(expectedBody);

    // When
    ResponseEntity<BaseDebtPosition> result = api.getDebtPositionNoPiiById(dpId);

    // Then
    Assertions.assertNotNull(result);
    Assertions.assertEquals(HttpStatus.OK, result.getStatusCode());
    Assertions.assertSame(expectedBody, result.getBody());
  }

  @Test
  void whenGetDebtPositionsNoPiiByOrganizationIdAndInstallmentNav() {
    // Given
    Long orgId = -1L;
    String iuv = "IUV";
    List<DebtPositionOrigin> dpOrigins = List.of(DebtPositionOrigin.ORDINARY);
    List<DebtPosition> expectedBody = List.of(new DebtPosition());

    Mockito.when(repositoryMock.findEntityGraphByOrganizationIdAndInstallmentIuv(
      Mockito.same(orgId),
      Mockito.same(iuv),
      Mockito.same(dpOrigins)
      ))
      .thenReturn(expectedBody);

    // When
    ResponseEntity<List<BaseDebtPosition>> result = api.getDebtPositionsNoPiiByOrganizationIdAndInstallmentIuv(orgId, iuv, dpOrigins);

    // Then
    Assertions.assertNotNull(result);
    Assertions.assertEquals(HttpStatus.OK, result.getStatusCode());
    Assertions.assertNotNull(result.getBody());
    Assertions.assertEquals(expectedBody.size(), result.getBody().size());
    Assertions.assertSame(expectedBody.getFirst(), result.getBody().getFirst());

  }
}
