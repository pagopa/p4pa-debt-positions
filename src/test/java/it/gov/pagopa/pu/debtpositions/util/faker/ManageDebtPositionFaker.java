package it.gov.pagopa.pu.debtpositions.util.faker;

import it.gov.pagopa.pu.debtpositions.dto.generated.ManageDebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.ManageInstallmentDTO;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static it.gov.pagopa.pu.debtpositions.util.faker.InstallmentFaker.buildInstallmentDTO;

public class ManageDebtPositionFaker {

  private static final LocalDate DATE = LocalDate.of(2099, 1, 1);

  public static ManageDebtPositionDTO buildManageDebtPositionDTO(){
    return ManageDebtPositionDTO.builder()
      .debtPositionDescription("debtPositionDescription")
      .validityDate(DATE)
      .paymentOptionDescription("paymentOptionDescription")
      .paymentOptionId(1L)
      .installments(new ArrayList<>(List.of(buildManageInstallmentDTO())))
      .build();
  }

  public static ManageInstallmentDTO buildManageInstallmentDTO() {
    return ManageInstallmentDTO.builder()
      .action(ManageInstallmentDTO.ActionEnum.I)
      .installment(buildInstallmentDTO())
      .build();
  }
}
