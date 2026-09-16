package it.gov.pagopa.pu.debtpositions.connector.auth;

import it.gov.pagopa.pu.auth.dto.generated.AccessToken;
import it.gov.pagopa.pu.debtpositions.connector.auth.service.AuthAccessTokenRetriever;
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

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthnServiceTest {

    @Mock
    private AuthAccessTokenRetriever accessTokenRetrieverMock;

    private AuthnService authnService;

    @BeforeEach
    void init(){
        authnService = new AuthnServiceImpl(accessTokenRetrieverMock);
    }

    @AfterEach
    void verifyNoMoreInteractions(){
        Mockito.verifyNoMoreInteractions(
                accessTokenRetrieverMock
        );
    }

    @ParameterizedTest
    @ValueSource(strings = "COD_IPA_ORG")
    @NullSource
    void whenGetAccessTokenThenInvokeAccessTokenRetriever(String orgIpaCode){
        // Given
        String expectedResult = "TOKEN";
        when(accessTokenRetrieverMock.getAccessToken(orgIpaCode))
                .thenReturn(AccessToken.builder().accessToken(expectedResult).tokenType("TOKENTYPE").expiresIn(0).build());

        // When
        String result = orgIpaCode == null ? authnService.getAccessToken() : authnService.getAccessToken(orgIpaCode);

        // Then
        Assertions.assertSame(expectedResult, result);
    }
}
