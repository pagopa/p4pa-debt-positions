package it.gov.pagopa.pu.debtpositions.util.faker;

import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionOrigin;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionStatus;
import it.gov.pagopa.pu.debtpositions.dto.generated.MixedDebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import static it.gov.pagopa.pu.debtpositions.util.faker.PaymentOptionFaker.*;

public class DebtPositionFaker {

  private static final LocalDate DATE = LocalDate.of(2099, 1, 1);
  private static final OffsetDateTime DATETIME = OffsetDateTime.of(DATE, LocalTime.MIDNIGHT, ZoneOffset.UTC);

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
    debtPosition.setFlagPuPagoPaPayment(false);
    debtPosition.setCreationDate(DATETIME.toLocalDateTime());
    debtPosition.setUpdateDate(DATETIME.toLocalDateTime());
    debtPosition.setUpdateOperatorExternalId("OPERATOREXTERNALUSERID");
    debtPosition.setUpdateTraceId("TRACEID");
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
    debtPositionDTO.setFlagPuPagoPaPayment(false);
    debtPositionDTO.setCreationDate(DATETIME);
    debtPositionDTO.setUpdateDate(DATETIME);
    debtPositionDTO.setUpdateOperatorExternalId("OPERATOREXTERNALUSERID");
    debtPositionDTO.setUpdateTraceId("TRACEID");
    debtPositionDTO.setPaymentOptions(new ArrayList<>(List.of(buildPaymentOptionDTO())));
    return debtPositionDTO;
  }

  public static DebtPositionDTO buildGeneratedIuvDebtPositionDTO() {
    DebtPositionDTO debtPositionDTO = new DebtPositionDTO();
    debtPositionDTO.setDebtPositionId(1L);
    debtPositionDTO.setDebtPositionTypeOrgId(2L);
    debtPositionDTO.setIupdOrg("randomIUPD");
    debtPositionDTO.setDescription("Test Description");
    debtPositionDTO.setStatus(DebtPositionStatus.UNPAID);
    debtPositionDTO.setDebtPositionOrigin(DebtPositionOrigin.ORDINARY);
    debtPositionDTO.setOrganizationId(500L);
    debtPositionDTO.setValidityDate(DATE);
    debtPositionDTO.setFlagIuvVolatile(true);
    debtPositionDTO.setMultiDebtor(false);
    debtPositionDTO.setFlagPuPagoPaPayment(false);
    debtPositionDTO.setCreationDate(DATETIME);
    debtPositionDTO.setUpdateDate(DATETIME);
    debtPositionDTO.setPaymentOptions(new ArrayList<>(List.of(buildGeneratedIuvPaymentOptionDTO())));
    return debtPositionDTO;
  }

  public static DebtPositionDTO buildSyncDebtPositionDTO(){
    DebtPositionDTO debtPositionDTO = new DebtPositionDTO();
    debtPositionDTO.setDebtPositionTypeOrgId(1L);
    debtPositionDTO.setIupdOrg("IUPD_ORG");
    debtPositionDTO.setDescription("Test Description");
    debtPositionDTO.setDebtPositionOrigin(DebtPositionOrigin.ORDINARY_SIL);
    debtPositionDTO.setStatus(DebtPositionStatus.UNPAID);
    debtPositionDTO.setOrganizationId(1L);
    debtPositionDTO.setValidityDate(DATE);
    debtPositionDTO.setMultiDebtor(true);
    debtPositionDTO.setFlagPuPagoPaPayment(true);
    debtPositionDTO.setFlagIuvVolatile(false);
    debtPositionDTO.setPaymentOptions(new ArrayList<>(List.of(buildSyncPaymentOptionDTO())));
    return debtPositionDTO;
  }

  public static MixedDebtPositionDTO buildMixedDebtPositionDTO() {
    MixedDebtPositionDTO debtPositionDTO = new MixedDebtPositionDTO();
    debtPositionDTO.setOrganizationId(500L);
    debtPositionDTO.setDebtPositionOrigin(DebtPositionOrigin.ORDINARY);
    debtPositionDTO.setSourceFlowName("sourceFlowName");
    debtPositionDTO.setFlagIuvVolatile(true);
    debtPositionDTO.setDescription("Test Description");
    debtPositionDTO.setDueDate(DATE);
    debtPositionDTO.setDebtor(PersonFaker.buildPerson());
    debtPositionDTO.setTransfers(List.of(TransferFaker.buildMixedTransferDTO()));
    return debtPositionDTO;
  }

  public static DebtPosition buildMixedDebtPosition() {
    DebtPosition debtPosition = new DebtPosition();
    debtPosition.setDebtPositionId(1L);
    debtPosition.setDebtPositionTypeOrgId(1L);
    debtPosition.setIupdOrg("IUPD_ORG");
    debtPosition.setDescription("Test Description");
    debtPosition.setStatus(DebtPositionStatus.UNPAID);
    debtPosition.setDebtPositionOrigin(DebtPositionOrigin.ORDINARY);
    debtPosition.setOrganizationId(500L);
    debtPosition.setValidityDate(DATE);
    debtPosition.setFlagIuvVolatile(true);
    debtPosition.setFlagPuPagoPaPayment(true);
    debtPosition.setMultiDebtor(false);
    debtPosition.setCreationDate(DATETIME.toLocalDateTime());
    debtPosition.setUpdateDate(DATETIME.toLocalDateTime());
    debtPosition.setUpdateOperatorExternalId("OPERATOREXTERNALUSERID");
    debtPosition.setUpdateTraceId("TRACEID");
    debtPosition.setPaymentOptions(new TreeSet<>(new ArrayList<>(List.of(buildMixedPaymentOption()))));
    return debtPosition;
  }
}
