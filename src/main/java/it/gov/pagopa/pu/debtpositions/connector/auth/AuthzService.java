package it.gov.pagopa.pu.debtpositions.connector.auth;

import it.gov.pagopa.pu.auth.dto.generated.UserInfo;

public interface AuthzService {
    UserInfo getOperatorInfo(String mappedExternalUserId);
}
