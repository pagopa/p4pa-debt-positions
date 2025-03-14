package it.gov.pagopa.pu.debtpositions.controller;

import it.gov.pagopa.pu.debtpositions.controller.generated.DebtPositionTypeApi;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionTypeDetailDTO;
import it.gov.pagopa.pu.debtpositions.service.DebtPositionTypeService;
import it.gov.pagopa.pu.debtpositions.util.SecurityUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DebtPositionTypeControllerImpl implements DebtPositionTypeApi {
  private final DebtPositionTypeService debtPositionTypeService;

  public DebtPositionTypeControllerImpl(
    DebtPositionTypeService debtPositionTypeService) {
    this.debtPositionTypeService = debtPositionTypeService;
  }

  @Override
  public ResponseEntity<DebtPositionTypeDetailDTO> getDebtPositionTypeDetail(Long debtPositionTypeId, Long brokerId) {
    return ResponseEntity.ofNullable(debtPositionTypeService.getDebtPositionTypeDetail(debtPositionTypeId, brokerId, SecurityUtils.getAccessToken()));
  }
}
