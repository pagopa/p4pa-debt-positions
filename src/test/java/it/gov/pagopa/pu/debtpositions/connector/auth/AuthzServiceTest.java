package it.gov.pagopa.pu.debtpositions.connector.auth;

import it.gov.pagopa.pu.auth.dto.generated.UserInfo;
import it.gov.pagopa.pu.debtpositions.connector.auth.client.AuthzClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuthzServiceTest {

    @Mock
    private AuthzClient authzClientMock;
    @Mock
    private AuthnService authnServiceMock;

    private AuthzService authzService;

    @BeforeEach
    void init(){
        authzService = new AuthzServiceImpl(authzClientMock, authnServiceMock);
    }

    @AfterEach
    void verifyNoMoreInteractions(){
        Mockito.verifyNoMoreInteractions(
                authzClientMock,
                authnServiceMock
        );
    }

    @Test
    void whenGetOperatorInfoThenObtainTokenAndCallAuthzClient(){
        String externalUserId = "externalUserId";
        String token = "TOKEN";
        UserInfo expectedResult = new UserInfo();

        Mockito.when(authnServiceMock.getAccessToken()).thenReturn(token);
        Mockito.when(authzClientMock.getOperatorInfo(externalUserId, token))
                .thenReturn(expectedResult);

        // When
        UserInfo result = authzService.getOperatorInfo(externalUserId);

        // Then
        Assertions.assertSame(expectedResult, result);
    }
}
