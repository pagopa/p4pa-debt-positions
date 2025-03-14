package it.gov.pagopa.pu.debtpositions.mapper;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionTypeDetailDTO;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionType;
import it.gov.pagopa.pu.organization.dto.generated.Taxonomy;
import org.springframework.stereotype.Service;

@Service
public class DebtPositionTypeMapper {

  public DebtPositionTypeDetailDTO mapToDebtPositionTypeDetailDTO(DebtPositionType debtPositionType, Taxonomy taxonomy) {
    if (debtPositionType == null || taxonomy == null) {
      return null;
    }

    return DebtPositionTypeDetailDTO.builder()
      .debtPositionTypeId(debtPositionType.getDebtPositionTypeId())
      .code(debtPositionType.getCode())
      .description(debtPositionType.getDescription())
      .organizationTypeDescription(taxonomy.getOrganizationTypeDescription())
      .macroAreaName(taxonomy.getMacroAreaName())
      .serviceType(taxonomy.getServiceType())
      .collectionReason(taxonomy.getCollectionReason())
      .taxonomyCode(debtPositionType.getTaxonomyCode())
      .flagAnonymousFiscalCode(debtPositionType.isFlagAnonymousFiscalCode())
      .flagMandatoryDueDate(debtPositionType.isFlagMandatoryDueDate())
      .flagNotifyIo(debtPositionType.isFlagNotifyIo())
      .ioTemplateMessage(debtPositionType.getIoTemplateMessage())
      .build();
  }

}
