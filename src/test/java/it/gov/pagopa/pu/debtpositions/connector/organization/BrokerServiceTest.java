package it.gov.pagopa.pu.debtpositions.connector.organization;

import it.gov.pagopa.pu.debtpositions.connector.organization.client.BrokerEntityClient;
import it.gov.pagopa.pu.organization.dto.generated.Broker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BrokerServiceTest {

  @Mock
  private BrokerEntityClient brokerEntityClientMock;

  private BrokerService brokerService;

  @BeforeEach
  void init() {
    brokerService = new BrokerServiceImpl(brokerEntityClientMock);
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(
      brokerEntityClientMock
    );
  }

  @Test
  void givenValidBrokerIdWhenFindByIdThenOk() {
    // Given
    Long brokerId = 1L;
    Broker expectedResult = new Broker();
    String accessToken = "ACCESS_TOKEN";
    Mockito.when(brokerEntityClientMock.findById(brokerId, accessToken))
      .thenReturn(expectedResult);

    // When
    Broker result = brokerService.findById(brokerId, accessToken);

    // Then
    Assertions.assertNotNull(result);
    Assertions.assertSame(expectedResult, result);
  }
}
