package it.gov.pagopa.pu.debtpositions.service.create.receipt.techdp;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.PaymentOptionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.ReceiptWithAdditionalNodeDataDTO;
import it.gov.pagopa.pu.debtpositions.mapper.ReceiptWithAdditionalInfoMapper;
import it.gov.pagopa.pu.debtpositions.model.*;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionTypeOrgRepository;
import it.gov.pagopa.pu.debtpositions.service.DebtPositionService;
import it.gov.pagopa.pu.debtpositions.service.dptypeorg.UnknownDebtPositionTypeOrgRetrieverService;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class TechnicalDpUpdateService {

  private final ReceiptWithAdditionalInfoMapper receiptMapper;
  private final DebtPositionService debtPositionService;
  private final UnknownDebtPositionTypeOrgRetrieverService unknownRetrieverService;
  private final DebtPositionTypeOrgRepository typeOrgRepository;

  public TechnicalDpUpdateService(ReceiptWithAdditionalInfoMapper receiptMapper,
                                  DebtPositionService debtPositionService,
                                  UnknownDebtPositionTypeOrgRetrieverService unknownRetrieverService,
                                  DebtPositionTypeOrgRepository typeOrgRepository) {
    this.receiptMapper = receiptMapper;
    this.debtPositionService = debtPositionService;
    this.unknownRetrieverService = unknownRetrieverService;
    this.typeOrgRepository = typeOrgRepository;
  }

  public DebtPositionDTO getDebtPositionDTO(ReceiptWithAdditionalNodeDataDTO receiptDTO, Organization organization) {
    Long targetTypeOrgId = resolveDpTypeOrgId(organization.getOrganizationId(), receiptDTO.getDebtPositionTypeOrgCode(), null, false, null);
    return receiptMapper.mapToDebtPosition(receiptDTO, organization, targetTypeOrgId);
  }

  public DebtPositionDTO updateDp(DebtPosition dp, ReceiptWithAdditionalNodeDataDTO receiptDTO, Organization organization, boolean preserveTypeOrg, DebtPositionTypeOrg unknownTypeOrg) {
    Long targetTypeOrgId = resolveDpTypeOrgId(
      organization.getOrganizationId(),
      receiptDTO.getDebtPositionTypeOrgCode(),
      dp.getDebtPositionTypeOrgId(),
      preserveTypeOrg,
      unknownTypeOrg
    );

    DebtPositionDTO updatedTechDp = receiptMapper.mapToDebtPosition(receiptDTO, organization, targetTypeOrgId);
    updateDp(dp, updatedTechDp);
    return updatedTechDp;
  }

  public DebtPositionDTO updateDp(DebtPosition dp, ReceiptWithAdditionalNodeDataDTO receiptDTO, Organization organization) {
    return updateDp(dp, receiptDTO, organization, false, null);
  }

  public void updateDp(DebtPosition dp, DebtPositionDTO dpDTO) {
    propagateIdsForDebtPosition(dp, dpDTO);
    debtPositionService.saveDebtPosition(dpDTO);
  }

  private Long resolveDpTypeOrgId(Long orgId, String code, Long dpTypeOrgId, boolean preserveTypeOrg, DebtPositionTypeOrg unknownTypeOrg) {
    if (preserveTypeOrg) {
      return dpTypeOrgId;
    }

    return typeOrgRepository.findByOrganizationIdAndCode(orgId, code)
      .map(DebtPositionTypeOrg::getDebtPositionTypeOrgId)
      .orElseGet(() -> {
        if (unknownTypeOrg != null) {
          return unknownTypeOrg.getDebtPositionTypeOrgId();
        }
        return unknownRetrieverService.getUnknownDebtPositionTypeOrg(orgId).getDebtPositionTypeOrgId();
      });
  }

  private void propagateIdsForDebtPosition(DebtPosition entity, DebtPositionDTO dto) {
    dto.setDebtPositionId(entity.getDebtPositionId());

    Map<Integer, PaymentOption> poByIndex = indexBy(
      entity.getPaymentOptions(),
      PaymentOption::getPaymentOptionIndex
    );

    dto.getPaymentOptions()
      .forEach(poDTO -> {
        PaymentOption poEntity = poByIndex.get(poDTO.getPaymentOptionIndex());
        poDTO.setDebtPositionId(dto.getDebtPositionId());
        if (poEntity != null) {
          propagateIdsForPaymentOption(poEntity, poDTO);
        }
      });
  }

  private void propagateIdsForPaymentOption(PaymentOption poEntity, PaymentOptionDTO poDTO) {
    poDTO.setPaymentOptionId(poEntity.getPaymentOptionId());

    Map<String, InstallmentNoPII> instByIud = indexBy(
      poEntity.getInstallments(),
      InstallmentNoPII::getIud
    );

    poDTO.getInstallments()
      .forEach(instDTO -> {
        InstallmentNoPII instEntity = instByIud.get(instDTO.getIud());
        instDTO.setPaymentOptionId(poDTO.getPaymentOptionId());
        if (instEntity != null) {
          propagateIdsForInstallment(instEntity, instDTO);
        }
      });
  }

  private void propagateIdsForInstallment(InstallmentNoPII instEntity, InstallmentDTO instDTO) {
    instDTO.setInstallmentId(instEntity.getInstallmentId());

    Map<Integer, Transfer> trByIndex = indexBy(
      instEntity.getTransfers(),
      Transfer::getTransferIndex
    );

    instDTO.getTransfers()
      .forEach(trDTO -> {
        Transfer trEntity = trByIndex.get(trDTO.getTransferIndex());
        trDTO.setInstallmentId(instDTO.getInstallmentId());
        if (trEntity != null) {
          trDTO.setTransferId(trEntity.getTransferId());
        }
      });
  }

  private static <T, K> Map<K, T> indexBy(Collection<T> items, Function<T, K> keyFn) {
    return items.stream()
      .collect(Collectors.toMap(
        keyFn,
        Function.identity()
      ));
  }
}
