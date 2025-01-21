package it.gov.pagopa.pu.debtpositions.connector.auth.client;

import it.gov.pagopa.pu.auth.controller.generated.AuthzApi;
import it.gov.pagopa.pu.auth.dto.generated.UserInfo;
import it.gov.pagopa.pu.debtpositions.connector.auth.config.AuthApisHolder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuthzClientTest {

    @Mock
    private AuthApisHolder authApisHolderMock;
    @Mock
    private AuthzApi authzApiMock;

    private AuthzClient authzClient;

    @BeforeEach
    void setUp() {
        authzClient = new AuthzClient(authApisHolderMock);
    }

    @AfterEach
    void verifyNoMoreInteractions(){
        Mockito.verifyNoMoreInteractions(
                authApisHolderMock
        );
    }

    @Test
    void whenGetOperatorInfoThenInvokeWithAccessToken(){
        // Given
        UserInfo expectedResult = new UserInfo();
        String accessToken = "accessToken";
        String externalUserId = "externalUserId";

        Mockito.when(authApisHolderMock.getAuthzApi(accessToken))
                .thenReturn(authzApiMock);
        Mockito.when(authzApiMock.getUserInfoFromMappedExternaUserId(externalUserId))
                .thenReturn(expectedResult);

        // When
        UserInfo result = authzClient.getOperatorInfo(externalUserId, accessToken);

        // Then
        Assertions.assertSame(expectedResult, result);
    }
}
