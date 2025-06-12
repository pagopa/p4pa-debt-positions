package it.gov.pagopa.pu.debtpositions.util.faker;

import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentStatus;
import it.gov.pagopa.pu.debtpositions.dto.generated.ManageDebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.ManageInstallmentDTO;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static it.gov.pagopa.pu.debtpositions.dto.generated.Action.*;
import static it.gov.pagopa.pu.debtpositions.util.faker.InstallmentFaker.buildInstallmentDTO;

public class ManageDebtPositionFaker {

  private static final LocalDate DATE = LocalDate.of(2099, 1, 1);

  public static ManageDebtPositionDTO buildManageDebtPositionDTO(){
    return ManageDebtPositionDTO.builder()
      .debtPositionDescription("debtPositionDescription")
      .validityDate(DATE)
      .paymentOptionDescription("paymentOptionDescription")
      .paymentOptionId(10L)
      .installments(new ArrayList<>(List.of(buildManageInsertInstallmentDTO(), buildManageUpdateInstallmentDTO(), buildManageCancelInstallmentDTO())))
      .build();
  }

  public static ManageInstallmentDTO buildManageInsertInstallmentDTO() {
    return ManageInstallmentDTO.builder()
      .action(I)
      .installment(buildInstallmentDTO().installmentId(1L).iud("iud1").status(InstallmentStatus.UNPAID).syncStatus(null))
      .build();
  }

  public static ManageInstallmentDTO buildManageUpdateInstallmentDTO() {
    return ManageInstallmentDTO.builder()
      .action(M)
      .installment(buildInstallmentDTO().installmentId(2L).status(InstallmentStatus.UNPAID).syncStatus(null))
      .build();
  }

  public static ManageInstallmentDTO buildManageCancelInstallmentDTO() {
    return ManageInstallmentDTO.builder()
      .action(A)
      .installment(buildInstallmentDTO().installmentId(3L).status(InstallmentStatus.UNPAID).syncStatus(null))
      .build();
  }
}
