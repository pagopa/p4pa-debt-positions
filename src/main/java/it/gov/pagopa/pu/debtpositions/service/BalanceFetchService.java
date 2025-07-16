package it.gov.pagopa.pu.debtpositions.service;

import it.gov.pagopa.pu.debtpositions.connector.classification.service.BalanceService;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrg;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionTypeOrgRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class BalanceFetchService {

    private final DebtPositionTypeOrgRepository debtPositionTypeOrgRepository;
    private final BalanceService balanceService;

    public BalanceFetchService(DebtPositionTypeOrgRepository debtPositionTypeOrgRepository, BalanceService balanceService) {
        this.debtPositionTypeOrgRepository = debtPositionTypeOrgRepository;
        this.balanceService = balanceService;
    }

    public String getBalanceDefault(Long organizationId, DebtPositionTypeOrg debtPositionTypeOrg, String accessToken) {
        log.info("Retrieving balance from DebtPositionTypeOrg with orgId[{}] and debtPositionTypeCode[{}]", organizationId, debtPositionTypeOrg.getCode());

        if (StringUtils.isNotBlank(debtPositionTypeOrg.getBalance())) {
            return debtPositionTypeOrg.getBalance();
        }

        return balanceService.getBalanceByAssessmentRegistry(organizationId, debtPositionTypeOrg.getCode(), accessToken);
    }
}
