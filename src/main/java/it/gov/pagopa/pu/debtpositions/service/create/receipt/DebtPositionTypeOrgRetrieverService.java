package it.gov.pagopa.pu.debtpositions.service.create.receipt;

import it.gov.pagopa.pu.debtpositions.exception.custom.NotFoundException;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrg;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionTypeOrgRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class DebtPositionTypeOrgRetrieverService {

  private final DebtPositionTypeOrgRepository debtPositionTypeOrgRepository;
  private final String pagopaReceiptDebtPositionTypeOrgCode;

  private final Map<Long, DebtPositionTypeOrg> pagopaReceiptDebtPositionTypeOrgMap = new ConcurrentHashMap<>();

  public DebtPositionTypeOrgRetrieverService(
    DebtPositionTypeOrgRepository debtPositionTypeOrgRepository,
    @Value("${debt-position.technical.pagopa-receipt.debt-position-type-org-code}") String pagopaReceiptDebtPositionTypeOrgCode
  ) {
    this.debtPositionTypeOrgRepository = debtPositionTypeOrgRepository;
    this.pagopaReceiptDebtPositionTypeOrgCode = pagopaReceiptDebtPositionTypeOrgCode;
  }

  public DebtPositionTypeOrg getPagopaReceiptDebtPositionTypeOrg(Long organizationId) {
    return pagopaReceiptDebtPositionTypeOrgMap.computeIfAbsent(organizationId,
      orgId -> retrieveDebtPositionTypeOrg(orgId, pagopaReceiptDebtPositionTypeOrgCode));
  }

  private DebtPositionTypeOrg retrieveDebtPositionTypeOrg(Long organizationId, String debtPositionTypeOrgCode) {
    return debtPositionTypeOrgRepository.findByOrganizationIdAndCode(organizationId, debtPositionTypeOrgCode)
      .orElseThrow(() -> new NotFoundException("Debt position type["+debtPositionTypeOrgCode+"] not found for orgId["+organizationId+"]"));
  }

}
