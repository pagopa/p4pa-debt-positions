package it.gov.pagopa.pu.debtpositions.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

class SecurityUtilsTest {

  @AfterEach
  void clear(){
    SecurityContextHolder.clearContext();
  }

//region getAccessToken
  @Test
  void givenNoSecurityContextWhenGetAccessTokenThenNull(){
    Assertions.assertNull(SecurityUtils.getAccessToken());
  }

  @Test
  void givenNoAuthenticationWhenGetAccessTokenThenNull(){
    SecurityContextHolder.setContext(new SecurityContextImpl());
    Assertions.assertNull(SecurityUtils.getAccessToken());
  }

  @Test
  void givenJwtWhenGetAccessTokenThenReturnToken(){
    // Given
    String jwt = "TOKENHEADER.TOKENPAYLOAD.TOKENDIGEST";
    SecurityContextHolder.setContext(new SecurityContextImpl(new JwtAuthenticationToken(Jwt
      .withTokenValue(jwt)
      .header("", "")
      .claim("", "")
      .build())));

    // When
    String result = SecurityUtils.getAccessToken();

    // Then
    Assertions.assertSame(jwt, result);
  }
//endregion

  @Test
  void givenJwtWhenGetCurrentUserExternalIdThenReturnPrincipalName(){
    // Given
    String principalName = "PRINCIPALNAME";
    SecurityContextHolder.setContext(new SecurityContextImpl(new JwtAuthenticationToken(Mockito.mock(Jwt.class), null, principalName)));

    // When
    String result = SecurityUtils.getCurrentUserExternalId();

    // Then
    Assertions.assertSame(principalName, result);
  }
}
