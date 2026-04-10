package it.gov.pagopa.pu.debtpositions.mapper;

import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDebtorDTO;
import it.gov.pagopa.pu.debtpositions.exception.custom.IllegalStateBusinessException;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrg;
import it.gov.pagopa.pu.debtpositions.util.ErrorCodeConstants;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.Map;

@Mapper(componentModel = "spring")
public interface InstallmentDebtorDTOMapper {

  List<InstallmentDebtorDTO> map(List<InstallmentDTO> installments, @Context Map<Long,DebtPositionTypeOrg> debtPositionTypeOrgMap);

  @Mapping(target = "debtPositionTypeOrgDescription", expression = "java(resolveDebtPositionTypeOrgDescription(installment,debtPositionTypeOrgMap))")
  @Mapping(target = "organizationId", expression = "java(resolveOrganizationId(installment,debtPositionTypeOrgMap))")
  InstallmentDebtorDTO map(InstallmentDTO installment, @Context Map<Long,DebtPositionTypeOrg> debtPositionTypeOrgMap);

  default Long resolveOrganizationId(InstallmentDTO installment, @Context Map<Long,DebtPositionTypeOrg> debtPositionTypeOrgMap){
    return getDebtPositionTypeOrg(installment.getInstallmentId(),debtPositionTypeOrgMap).getOrganizationId();
  }

  default String resolveDebtPositionTypeOrgDescription(InstallmentDTO installment, @Context Map<Long,DebtPositionTypeOrg> debtPositionTypeOrgMap){
    return getDebtPositionTypeOrg(installment.getInstallmentId(),debtPositionTypeOrgMap).getDescription();
  }

  private DebtPositionTypeOrg getDebtPositionTypeOrg(Long installmentId, Map<Long, DebtPositionTypeOrg> debtPositionTypeOrgMap) {
    if (debtPositionTypeOrgMap == null) {
      throw new IllegalStateBusinessException(ErrorCodeConstants.ERROR_CODE_INSTALLMENT_MAPPING_ERROR, "debtPositionTypeOrgMap must not be null");
    }
    DebtPositionTypeOrg dpto = debtPositionTypeOrgMap.get(installmentId);
    if (dpto == null) {
      throw new IllegalStateBusinessException(ErrorCodeConstants.ERROR_CODE_INSTALLMENT_MAPPING_ERROR, "Missing DebtPositionTypeOrg for installmentId " + installmentId);
    }
    return dpto;
  }
}
