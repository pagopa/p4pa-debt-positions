package it.gov.pagopa.pu.debtpositions.connector.auth.service;

import it.gov.pagopa.pu.auth.dto.generated.AccessToken;
import it.gov.pagopa.pu.debtpositions.connector.auth.client.AuthnClient;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthAccessTokenRetrieverTest {

    private static final String CLIENTSECRET = "clientsecret";

    @Mock
    private AuthnClient authnClientMock;

    private AuthAccessTokenRetriever accessTokenRetriever;

    @BeforeEach
    void init(){
        accessTokenRetriever = new AuthAccessTokenRetriever(CLIENTSECRET, authnClientMock);
    }

    @AfterEach
    void verifyNoMoreInteractions(){
        Mockito.verifyNoMoreInteractions(
                authnClientMock
        );
    }

    @ParameterizedTest
    @ValueSource(strings = "COD_IPA_ORG")
    @NullSource
    void givenEmptyCacheWhenGetAccessTokenThenInvokeAndCache(String orgIpaCode){
        // Given
        AccessToken expectedResult = AccessToken.builder()
                .expiresIn(10)
                .accessToken("ACCESSTOKEN")
                .tokenType("TOKENTYPE")
                .build();

        // When
        configureAndInvoke(orgIpaCode, expectedResult);

        // Then
        verify(authnClientMock)
                .postToken(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
    }

    @ParameterizedTest
    @ValueSource(strings = "COD_IPA_ORG")
    @NullSource
    void givenExpiredCacheWhenGetAccessTokenThenInvokeAndCache(String orgIpaCode){
        // Given
        AccessToken expectedResult = AccessToken.builder()
                .expiresIn(5)
                .accessToken("ACCESSTOKEN")
                .tokenType("TOKENTYPE")
                .build();

        // When
        configureAndInvoke(orgIpaCode, expectedResult);

        // Then
        verify(authnClientMock, times(2))
                .postToken(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
    }

    private void configureAndInvoke(String orgIpaCode, AccessToken expectedResult) {
        // Given
        when(authnClientMock.postToken("piattaforma-unitaria_"+ StringUtils.stripToEmpty(orgIpaCode),
            "client_credentials", "openid", null, null, null, CLIENTSECRET))
                .thenReturn(expectedResult);

        // When
        AccessToken result1 = accessTokenRetriever.getAccessToken(orgIpaCode);
        AccessToken result2 = accessTokenRetriever.getAccessToken(orgIpaCode);

        // Then
        Assertions.assertSame(expectedResult, result1);
        Assertions.assertSame(expectedResult, result2);
    }
}
