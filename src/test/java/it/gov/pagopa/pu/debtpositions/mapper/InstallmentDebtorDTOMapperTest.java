package it.gov.pagopa.pu.debtpositions.mapper;

import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDebtorDTO;
import it.gov.pagopa.pu.debtpositions.exception.custom.IllegalStateBusinessException;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrg;
import it.gov.pagopa.pu.debtpositions.util.TestUtils;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import uk.co.jemos.podam.api.PodamFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class InstallmentDebtorDTOMapperTest {

  private final InstallmentDebtorDTOMapper mapper =  Mappers.getMapper(InstallmentDebtorDTOMapper.class);

  private final PodamFactory podam = TestUtils.getPodamFactory();

  @Test
  void whenMapThenOk() {
    List<InstallmentDTO> installments = podam.manufacturePojo(List.class, InstallmentDTO.class);
    Map<Long, InstallmentDTO> installmentMap = installments.stream().collect(Collectors.toMap(InstallmentDTO::getInstallmentId, Function.identity()));
    Map<Long,DebtPositionTypeOrg> dptoMap = new HashMap<>();
    for (InstallmentDTO installment : installments) {
      dptoMap.put(installment.getInstallmentId(),podam.manufacturePojo(DebtPositionTypeOrg.class));
    }

    List<InstallmentDebtorDTO> result = mapper.map(installments,dptoMap);

    assertNotNull(result);
    assertEquals(installments.size(),result.size());
    for (InstallmentDebtorDTO installmentDebtorDTO : result) {
      TestUtils.checkNotNullFields(installmentDebtorDTO);
      Long installmentId = installmentDebtorDTO.getInstallmentId();
      assertTrue(installmentMap.containsKey(installmentId));
      TestUtils.reflectionEqualsByName(installmentMap.get(installmentId),installmentDebtorDTO);
      assertTrue(dptoMap.containsKey(installmentId));
      assertEquals(dptoMap.get(installmentId).getDescription(),installmentDebtorDTO.getDebtPositionTypeOrgDescription());
      assertEquals(dptoMap.get(installmentId).getOrganizationId(),installmentDebtorDTO.getOrganizationId());
    }
  }

  @Test
  void givenNoMatchingDebtPositionTypeOrgWhenMapThenIllegalStateBusinessException() {
    InstallmentDTO installment = podam.manufacturePojo(InstallmentDTO.class);
    installment.setInstallmentId(1L);
    List<InstallmentDTO> installments = List.of(installment);
    Map<Long,DebtPositionTypeOrg> dptoMap = new HashMap<>();
    dptoMap.put(2L,podam.manufacturePojo(DebtPositionTypeOrg.class));

    assertThrows(IllegalStateBusinessException.class,()-> mapper.map(installments,dptoMap));
  }

  @Test
  void givenNoDebtPositionTypeOrgMapWhenMapThenIllegalStateBusinessException() {
    InstallmentDTO installment = podam.manufacturePojo(InstallmentDTO.class);
    installment.setInstallmentId(1L);
    List<InstallmentDTO> installments = List.of(installment);

    assertThrows(IllegalStateBusinessException.class,()-> mapper.map(installments,null));
  }
}
