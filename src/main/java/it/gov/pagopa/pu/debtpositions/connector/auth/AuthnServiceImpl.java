package it.gov.pagopa.pu.debtpositions.connector.auth;

import it.gov.pagopa.pu.debtpositions.connector.auth.service.AuthAccessTokenRetriever;
import org.springframework.stereotype.Service;

@Service
public class AuthnServiceImpl implements AuthnService {

    private final AuthAccessTokenRetriever accessTokenRetriever;

    public AuthnServiceImpl(AuthAccessTokenRetriever accessTokenRetriever) {
        this.accessTokenRetriever = accessTokenRetriever;
    }

  @Override
  public String getAccessToken() {
    return accessTokenRetriever.getAccessToken(null).getAccessToken();
  }

    @Override
    public String getAccessToken(String orgIpaCode) {
        return accessTokenRetriever.getAccessToken(orgIpaCode).getAccessToken();
    }
}
