package it.gov.pagopa.pu.debtpositions.connector.organization.client;

import it.gov.pagopa.pu.debtpositions.connector.organization.config.OrganizationApisHolder;
import it.gov.pagopa.pu.organization.client.generated.BrokerEntityControllerApi;
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
class BrokerEntityClientTest {
  @Mock
  private OrganizationApisHolder organizationApisHolderMock;
  @Mock
  private BrokerEntityControllerApi brokerEntityControllerApiMock;

  private BrokerEntityClient brokerEntityClient;

  @BeforeEach
  void setUp() {
    brokerEntityClient = new BrokerEntityClient(organizationApisHolderMock);
  }

  @AfterEach
  void verifyNoMoreInteractions() {
    Mockito.verifyNoMoreInteractions(
      organizationApisHolderMock
    );
  }

  @Test
  void givenValidBrokerIdWhenFindByIdThenBroker() {
    // Given
    String accessToken = "ACCESS_TOKEN";
    long brokerId = 1L;
    Broker expectedResult = new Broker();

    Mockito.when(organizationApisHolderMock.getBrokerEntityControllerApi(accessToken))
      .thenReturn(brokerEntityControllerApiMock);
    Mockito.when(brokerEntityControllerApiMock.crudGetBroker(Long.toString(brokerId)))
      .thenReturn(expectedResult);

    // When
    Broker result = brokerEntityClient.findById(brokerId, accessToken);

    // Then
    Assertions.assertSame(expectedResult, result);
  }
}
