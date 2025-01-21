package it.gov.pagopa.pu.debtpositions.connector.organization.client;

import it.gov.pagopa.pu.debtpositions.connector.organization.config.OrganizationApisHolder;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Lazy
@Service
public class OrganizationSearchClient {

    private final OrganizationApisHolder organizationApisHolder;

    public OrganizationSearchClient(OrganizationApisHolder organizationApisHolder) {
        this.organizationApisHolder = organizationApisHolder;
    }

    public Organization findByIpaCode(String ipaCode, String accessToken) {
        return organizationApisHolder.getOrganizationSearchControllerApi(accessToken)
                .crudOrganizationsFindByIpaCode(ipaCode);
    }

    public Organization findByOrgFiscalCode(String orgFiscalCode, String accessToken) {
        return organizationApisHolder.getOrganizationSearchControllerApi(accessToken)
                .crudOrganizationsFindByOrgFiscalCode(orgFiscalCode);
    }

}
