package it.gov.pagopa.pu.debtpositions.service.installmentsync;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionOrigin;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentSynchronizeDTO;
import it.gov.pagopa.pu.debtpositions.exception.custom.ConflictErrorException;
import it.gov.pagopa.pu.debtpositions.mapper.DebtPositionMapper;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionRepository;
import it.gov.pagopa.pu.debtpositions.service.installmentsync.operation.InstallmentSynchronizeCancelServiceImpl;
import it.gov.pagopa.pu.debtpositions.service.installmentsync.operation.InstallmentSynchronizeInsertServiceImpl;
import it.gov.pagopa.pu.debtpositions.service.installmentsync.operation.InstallmentSynchronizeUpdateServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class InstallmentSynchronizeServiceImpl implements InstallmentSynchronizeService {

  private final DebtPositionRepository debtPositionRepository;
  private final DebtPositionMapper debtPositionMapper;
  private final InstallmentSynchronizeInsertServiceImpl installmentSynchronizeInsertService;
  private final InstallmentSynchronizeUpdateServiceImpl installmentSynchronizeUpdateService;
  private final InstallmentSynchronizeCancelServiceImpl installmentSynchronizeCancelService;

  public InstallmentSynchronizeServiceImpl(DebtPositionRepository debtPositionRepository, DebtPositionMapper debtPositionMapper, InstallmentSynchronizeInsertServiceImpl installmentSynchronizeInsertService, InstallmentSynchronizeUpdateServiceImpl installmentSynchronizeUpdateService, InstallmentSynchronizeCancelServiceImpl installmentSynchronizeCancelService) {
    this.debtPositionRepository = debtPositionRepository;
    this.debtPositionMapper = debtPositionMapper;
    this.installmentSynchronizeInsertService = installmentSynchronizeInsertService;
    this.installmentSynchronizeUpdateService = installmentSynchronizeUpdateService;
    this.installmentSynchronizeCancelService = installmentSynchronizeCancelService;
  }

  @Override
  public String installmentSynchronize(InstallmentSynchronizeDTO installmentSynchronizeDTO, Boolean massive, DebtPositionOrigin debtPositionOrigin, String accessToken, String operatorExternalUserId) {
    DebtPositionDTO debtPositionDTO = retrieveAndVerifyOrigin(installmentSynchronizeDTO.getIupdOrg(), debtPositionOrigin);

    InstallmentSynchronizeDTO.ActionEnum action = installmentSynchronizeDTO.getAction();
    return switch (action) {
      case InstallmentSynchronizeDTO.ActionEnum.I ->
        installmentSynchronizeInsertService.syncInstallment(installmentSynchronizeDTO, debtPositionDTO, debtPositionOrigin, massive, accessToken, operatorExternalUserId);
      case InstallmentSynchronizeDTO.ActionEnum.M ->
        installmentSynchronizeUpdateService.syncInstallment(installmentSynchronizeDTO, debtPositionDTO, massive, accessToken, operatorExternalUserId);
      case InstallmentSynchronizeDTO.ActionEnum.A ->
        installmentSynchronizeCancelService.syncInstallment(installmentSynchronizeDTO, debtPositionDTO, massive, accessToken, operatorExternalUserId);
    };
  }

  private DebtPositionDTO retrieveAndVerifyOrigin(String iupdOrg, DebtPositionOrigin debtPositionOrigin) {
    DebtPosition debtPosition = debtPositionRepository.findByIupdOrg(iupdOrg);

    if (debtPosition == null){
      return null;
    }

    if (!debtPositionOrigin.equals(debtPosition.getDebtPositionOrigin())) {
      throw new ConflictErrorException(String.format("There is another debt position with iupd %s requested but different origin", iupdOrg));
    }
    return debtPositionMapper.mapToDto(debtPosition);
  }
}
