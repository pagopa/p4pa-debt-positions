package it.gov.pagopa.pu.debtpositions.connector.migration.client;

import it.gov.pagopa.pu.debtpositions.connector.migration.config.MigrationApisHolder;
import it.gov.pagopa.pu.migration.dto.generated.ConsumeDebtPositionTypeOrgOperatorsDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class DebtPositionTypeOrgOperatorsClient {
  private final MigrationApisHolder migrationApisHolder;

  public DebtPositionTypeOrgOperatorsClient(MigrationApisHolder migrationApisHolder) {
    this.migrationApisHolder = migrationApisHolder;
  }

  public List<Long> getUnconsumedDebtPositionTypeOrgIds(Long organizationId, String fiscalCode, String accessToken) {
    return migrationApisHolder.getDebtPositionTypeOrgOperatorsApi(accessToken)
      .getUnconsumedDebtPositionTypeOrgIds(organizationId, fiscalCode);
  }

  public void consumeDebtPositionTypeOrgOperators(Long organizationId, List<Long> debtPositionTypeOrgIds, String fiscalCode, String accessToken) {
    ConsumeDebtPositionTypeOrgOperatorsDTO consumeDebtPositionTypeOrgOperatorsDTO = ConsumeDebtPositionTypeOrgOperatorsDTO.builder()
        .debtPositionTypeOrgIds(debtPositionTypeOrgIds)
        .fiscalCode(fiscalCode)
        .build();
    migrationApisHolder.getDebtPositionTypeOrgOperatorsApi(accessToken)
      .consumeDebtPositionTypeOrgOperators(organizationId, consumeDebtPositionTypeOrgOperatorsDTO);
  }
}
