package it.gov.pagopa.pu.debtpositions.util.faker;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionOrigin;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionStatus;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import static it.gov.pagopa.pu.debtpositions.util.faker.PaymentOptionFaker.*;

public class DebtPositionFaker {

  private static final OffsetDateTime DATE = OffsetDateTime.of(2025, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);

  public static DebtPosition buildDebtPosition() {
    DebtPosition debtPosition = new DebtPosition();
    debtPosition.setDebtPositionId(1L);
    debtPosition.setDebtPositionTypeOrgId(2L);
    debtPosition.setIupdOrg("IUPD_ORG");
    debtPosition.setDescription("Test Description");
    debtPosition.setStatus(DebtPositionStatus.TO_SYNC);
    debtPosition.setDebtPositionOrigin(DebtPositionOrigin.ORDINARY);
    debtPosition.setOrganizationId(500L);
    debtPosition.setValidityDate(DATE);
    debtPosition.setFlagIuvVolatile(true);
    debtPosition.setMultiDebtor(false);
    debtPosition.setFlagPagoPaPayment(false);
    debtPosition.setCreationDate(DATE.toLocalDateTime());
    debtPosition.setUpdateDate(DATE.toLocalDateTime());
    debtPosition.setPaymentOptions(new TreeSet<>(new ArrayList<>(List.of(buildPaymentOption()))));
    return debtPosition;
  }

  public static DebtPositionDTO buildDebtPositionDTO() {
    DebtPositionDTO debtPositionDTO = new DebtPositionDTO();
    debtPositionDTO.setDebtPositionId(1L);
    debtPositionDTO.setDebtPositionTypeOrgId(2L);
    debtPositionDTO.setIupdOrg("IUPD_ORG");
    debtPositionDTO.setDescription("Test Description");
    debtPositionDTO.setStatus(DebtPositionStatus.UNPAID);
    debtPositionDTO.setDebtPositionOrigin(DebtPositionOrigin.ORDINARY);
    debtPositionDTO.setOrganizationId(500L);
    debtPositionDTO.setValidityDate(DATE);
    debtPositionDTO.setFlagIuvVolatile(true);
    debtPositionDTO.setMultiDebtor(false);
    debtPositionDTO.setFlagPagoPaPayment(false);
    debtPositionDTO.setCreationDate(DATE);
    debtPositionDTO.setUpdateDate(DATE);
    debtPositionDTO.setPaymentOptions(new ArrayList<>(List.of(buildPaymentOptionDTO())));
    return debtPositionDTO;
  }

  public static DebtPositionDTO buildGeneratedIuvDebtPositionDTO() {
    DebtPositionDTO debtPositionDTO = new DebtPositionDTO();
    debtPositionDTO.setDebtPositionId(1L);
    debtPositionDTO.setDebtPositionTypeOrgId(2L);
    debtPositionDTO.setIupdOrg("IUPD_ORG");
    debtPositionDTO.setDescription("Test Description");
    debtPositionDTO.setStatus(DebtPositionStatus.UNPAID);
    debtPositionDTO.setDebtPositionOrigin(DebtPositionOrigin.ORDINARY);
    debtPositionDTO.setOrganizationId(500L);
    debtPositionDTO.setValidityDate(DATE);
    debtPositionDTO.setFlagIuvVolatile(true);
    debtPositionDTO.setMultiDebtor(false);
    debtPositionDTO.setFlagPagoPaPayment(false);
    debtPositionDTO.setCreationDate(DATE);
    debtPositionDTO.setUpdateDate(DATE);
    debtPositionDTO.setPaymentOptions(new ArrayList<>(List.of(buildGeneratedIuvPaymentOptionDTO())));
    return debtPositionDTO;
  }
}
