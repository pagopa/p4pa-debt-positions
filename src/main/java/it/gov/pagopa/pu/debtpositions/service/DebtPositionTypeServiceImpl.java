package it.gov.pagopa.pu.debtpositions.service;

import it.gov.pagopa.pu.debtpositions.connector.organization.service.TaxonomyService;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionTypeDetailDTO;
import it.gov.pagopa.pu.debtpositions.exception.custom.NotFoundException;
import it.gov.pagopa.pu.debtpositions.mapper.DebtPositionTypeMapper;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionType;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionTypeRepository;
import it.gov.pagopa.pu.organization.dto.generated.Taxonomy;
import org.springframework.stereotype.Service;

@Service
public class DebtPositionTypeServiceImpl implements DebtPositionTypeService {
  private final DebtPositionTypeRepository debtPositionTypeRepository;
  private final TaxonomyService taxonomyService;
  private final DebtPositionTypeMapper mapper;

  public DebtPositionTypeServiceImpl(DebtPositionTypeRepository debtPositionTypeRepository, TaxonomyService taxonomyService, DebtPositionTypeMapper mapper) {
    this.debtPositionTypeRepository = debtPositionTypeRepository;
    this.taxonomyService = taxonomyService;
    this.mapper = mapper;
  }

  @Override
  public DebtPositionTypeDetailDTO getDebtPositionTypeDetail(Long debtPositionTypeId, Long brokerId, String accessToken) {
    DebtPositionType debtPositionType = debtPositionTypeRepository.findById(debtPositionTypeId)
      .orElseThrow(() -> new NotFoundException("Debt position type having id %s not found".formatted(debtPositionTypeId)));

    if (!brokerId.equals(debtPositionType.getBrokerId())) {
      return null;
    }

    Taxonomy taxonomy = taxonomyService.getTaxonomyByTaxonomyCode(debtPositionType.getTaxonomyCode(), accessToken)
      .orElseThrow(() -> new NotFoundException("Taxonomy having code %s not found".formatted(debtPositionType.getTaxonomyCode())));

    return mapper.mapToDebtPositionTypeDetailDTO(debtPositionType, taxonomy);
  }

}
