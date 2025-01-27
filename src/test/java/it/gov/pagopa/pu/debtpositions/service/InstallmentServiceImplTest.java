package it.gov.pagopa.pu.debtpositions.service;

import it.gov.pagopa.pu.debtpositions.dto.Installment;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.debtpositions.mapper.InstallmentMapper;
import it.gov.pagopa.pu.debtpositions.repository.InstallmentPIIRepository;
import it.gov.pagopa.pu.debtpositions.util.TestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.jemos.podam.api.PodamFactory;

import java.util.ArrayList;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class InstallmentServiceImplTest {

  @Mock
  private InstallmentPIIRepository installmentPIIRepositoryMock;
  @Mock
  private InstallmentMapper installmentMapperMock;

  @InjectMocks
  private InstallmentServiceImpl installmentService;

  private final PodamFactory podamFactory = TestUtils.getPodamFactory();

  @Test
  void givenValidOrganizationAndNavWhGetInstallmentsByOrganizationIdAndNavThenOk() {
    //given
    List<Installment> installmentList = podamFactory.manufacturePojo(List.class, Installment.class);
    List<InstallmentDTO> installmentDTOList = new ArrayList<>();

    Mockito.when(installmentPIIRepositoryMock.getByOrganizationIdAndNav(1L, "NAV")).thenReturn(installmentList);
    installmentList.forEach(installment -> {
      InstallmentDTO installmentDTO = podamFactory.manufacturePojo(InstallmentDTO.class);
      installmentDTOList.add(installmentDTO);
      Mockito.when(installmentMapperMock.mapToDto(installment)).thenReturn(installmentDTO);
    });

    //when
    List<InstallmentDTO> response = installmentService.getInstallmentsByOrganizationIdAndNav(1L, "NAV");

    //verify
    Assertions.assertNotNull(response);
    Assertions.assertIterableEquals(installmentDTOList, response);
    Mockito.verify(installmentPIIRepositoryMock, Mockito.times(1)).getByOrganizationIdAndNav(1L, "NAV");
    installmentList.forEach(installment -> Mockito.verify(installmentMapperMock, Mockito.times(1)).mapToDto(installment));
  }
}
