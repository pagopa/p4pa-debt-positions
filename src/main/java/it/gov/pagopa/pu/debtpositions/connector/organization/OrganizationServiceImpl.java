package it.gov.pagopa.pu.debtpositions.connector.organization;

import it.gov.pagopa.pu.debtpositions.connector.auth.AuthnService;
import it.gov.pagopa.pu.debtpositions.connector.organization.client.OrganizationSearchClient;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Lazy
@Service
public class OrganizationServiceImpl implements OrganizationService {

    private final AuthnService authnService;
    private final OrganizationSearchClient organizationSearchClient;

    public OrganizationServiceImpl(AuthnService authnService, OrganizationSearchClient organizationSearchClient) {
        this.authnService = authnService;
        this.organizationSearchClient = organizationSearchClient;
    }

    @Override
    public Optional<Organization> getOrganizationByFiscalCode(String orgFiscalCode) {
        return Optional.ofNullable(
                organizationSearchClient.findByOrgFiscalCode(orgFiscalCode, authnService.getAccessToken())
        );
    }

    @Override
    public Optional<Organization> getOrganizationByIpaCode(String ipaCode) {
        return Optional.ofNullable(
                organizationSearchClient.findByIpaCode(ipaCode, authnService.getAccessToken())
        );
    }
}
