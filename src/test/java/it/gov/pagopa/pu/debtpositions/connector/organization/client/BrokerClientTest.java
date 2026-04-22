package it.gov.pagopa.pu.debtpositions.connector.organization.client;

import it.gov.pagopa.pu.debtpositions.connector.organization.config.OrganizationApisHolder;
import it.gov.pagopa.pu.organization.client.generated.BrokerConfigurationEntityControllerApi;
import it.gov.pagopa.pu.organization.client.generated.BrokerEntityControllerApi;
import it.gov.pagopa.pu.organization.dto.generated.Broker;
import it.gov.pagopa.pu.organization.dto.generated.BrokerConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BrokerClientTest {
  @Mock
  private OrganizationApisHolder organizationApisHolderMock;
  @Mock
  private BrokerEntityControllerApi brokerEntityControllerApiMock;

  private BrokerClient brokerClient;

  @BeforeEach
  void setUp() {
    brokerClient = new BrokerClient(organizationApisHolderMock);
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
    Broker result = brokerClient.findById(brokerId, accessToken);

    // Then
    Assertions.assertSame(expectedResult, result);
  }

  @Test
  void testGetBrokerConfigurationById() {
    String brokerId = "1";
    String accessToken = "accessToken";
    BrokerConfiguration expectedBrokerConfiguration = mock(BrokerConfiguration.class);
    var brokerConfigurationEntityControllerApi = mock(BrokerConfigurationEntityControllerApi.class);

    when(organizationApisHolderMock.getBrokerConfigurationEntityControllerApi(accessToken))
      .thenReturn(brokerConfigurationEntityControllerApi);
    when(brokerConfigurationEntityControllerApi.crudGetBrokerconfiguration(brokerId))
      .thenReturn(expectedBrokerConfiguration);

    BrokerConfiguration result = brokerClient.getBrokerConfigurationById(Long.valueOf(brokerId), accessToken);

    assertEquals(expectedBrokerConfiguration, result);
    Mockito.verify(organizationApisHolderMock).getBrokerConfigurationEntityControllerApi(accessToken);
    Mockito.verify(brokerConfigurationEntityControllerApi).crudGetBrokerconfiguration(brokerId);
  }
}
