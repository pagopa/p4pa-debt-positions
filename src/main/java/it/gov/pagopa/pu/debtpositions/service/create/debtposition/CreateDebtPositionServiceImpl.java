package it.gov.pagopa.pu.debtpositions.service.create.debtposition;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.exception.custom.ConflictErrorException;
import it.gov.pagopa.pu.debtpositions.mapper.DebtPositionMapper;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrg;
import it.gov.pagopa.pu.debtpositions.repository.InstallmentPIIRepository;
import it.gov.pagopa.pu.debtpositions.service.AuthorizeOperatorOnDebtPositionTypeService;
import it.gov.pagopa.pu.debtpositions.service.DebtPositionService;
import it.gov.pagopa.pu.debtpositions.service.create.GenerateIuvService;
import it.gov.pagopa.pu.debtpositions.service.create.ValidateDebtPositionService;
import it.gov.pagopa.pu.debtpositions.util.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CreateDebtPositionServiceImpl implements CreateDebtPositionService {

  private final AuthorizeOperatorOnDebtPositionTypeService authorizeOperatorOnDebtPositionTypeService;
  private final DebtPositionMapper debtPositionMapper;
  private final ValidateDebtPositionService validateDebtPositionService;
  private final InstallmentPIIRepository installmentPIIRepository;
  private final DebtPositionService debtPositionService;
  private final GenerateIuvService generateIuvService;

  public CreateDebtPositionServiceImpl(AuthorizeOperatorOnDebtPositionTypeService authorizeOperatorOnDebtPositionTypeService,
                                       DebtPositionMapper debtPositionMapper, ValidateDebtPositionService validateDebtPositionService,
                                       InstallmentPIIRepository installmentPIIRepository, DebtPositionService debtPositionService, GenerateIuvService generateIuvService) {
    this.authorizeOperatorOnDebtPositionTypeService = authorizeOperatorOnDebtPositionTypeService;
    this.debtPositionMapper = debtPositionMapper;
    this.validateDebtPositionService = validateDebtPositionService;
    this.installmentPIIRepository = installmentPIIRepository;
    this.debtPositionService = debtPositionService;
    this.generateIuvService = generateIuvService;
  }

  @Override
  public ResponseEntity<DebtPositionDTO> createDebtPosition(DebtPositionDTO debtPositionDTO, Boolean massive) {
    log.info("START Create DebtPosition...");
    String accessToken = SecurityUtils.getAccessToken();

    verifyAuthorization(debtPositionDTO);
    validateInstallments(debtPositionDTO, accessToken);

    DebtPosition debtPosition = debtPositionMapper.mapToModel(debtPositionDTO).getFirst();
    long countDuplicates = installmentPIIRepository.countExistingDebtPosition(debtPosition);
    if (countDuplicates > 0) {
      log.error("Duplicate record found for DebtPosition with id {}", debtPositionDTO.getDebtPositionId());
      throw new ConflictErrorException("Duplicate records found: the provided data conflicts with existing records.");
    }

    DebtPositionDTO savedDebtPosition = debtPositionService.saveDebtPosition(debtPositionDTO);

    //IS PAGOPA PAYMENT
    debtPositionDTO.getPaymentOptions().stream()
      .flatMap(po -> po.getInstallments().stream())
      .forEach(installment -> {
        String orgFiscalCode = installment.getDebtor().getFiscalCode();
          String generatedIuv = generateIuvService.generateIuv(orgFiscalCode, accessToken);
          //SAVE IUV
      });

    log.info("END Create DebtPosition...");
    return null;
  }

  private void verifyAuthorization(DebtPositionDTO debtPositionDTO) {
    log.info("START Verify Authorization...");

    String operatorExternalUserId = SecurityUtils.getCurrentUserExternalId();
    DebtPositionTypeOrg debtPositionTypeOrg = authorizeOperatorOnDebtPositionTypeService.authorize(debtPositionDTO.getOrganizationId(), debtPositionDTO.getDebtPositionTypeOrgId(), operatorExternalUserId);

    log.info("END Verify Authorization, found DebtPositionTypeOrg id: {}", debtPositionTypeOrg.getDebtPositionTypeOrgId());
  }

  private void validateInstallments(DebtPositionDTO debtPositionDTO, String accessToken) {
    log.info("START Validating installments...");

    validateDebtPositionService.validate(debtPositionDTO, accessToken);

    log.info("END validating installments");
  }
}
