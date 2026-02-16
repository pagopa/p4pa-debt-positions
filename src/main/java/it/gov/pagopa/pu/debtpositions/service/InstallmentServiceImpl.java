package it.gov.pagopa.pu.debtpositions.service;

import io.micrometer.common.util.StringUtils;
import it.gov.pagopa.pu.debtpositions.dto.filters.ExportPaidInstallmentsFiltersDTO;
import it.gov.pagopa.pu.debtpositions.dto.WfExecutionParameters;
import it.gov.pagopa.pu.debtpositions.dto.generated.ActualizeAmountRequestDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionOrigin;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDebtorDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDetailDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentStatus;
import it.gov.pagopa.pu.debtpositions.dto.filters.InstallmentsSearchFiltersDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.PagedInstallmentsPaidView;
import it.gov.pagopa.pu.debtpositions.dto.generated.PagedInstallmentsView;
import it.gov.pagopa.pu.debtpositions.dto.generated.TransferDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.UpdateInstallmentNotificationDateRequest;
import it.gov.pagopa.pu.debtpositions.exception.custom.ConflictErrorException;
import it.gov.pagopa.pu.debtpositions.exception.custom.InvalidConditionException;
import it.gov.pagopa.pu.debtpositions.exception.custom.InvalidParamException;
import it.gov.pagopa.pu.debtpositions.exception.custom.NotFoundException;
import it.gov.pagopa.pu.debtpositions.mapper.DebtPositionMapper;
import it.gov.pagopa.pu.debtpositions.mapper.InstallmentDebtorDTOMapper;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrg;
import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionRepository;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionTypeOrgRepository;
import it.gov.pagopa.pu.debtpositions.repository.InstallmentNoPIIRepository;
import it.gov.pagopa.pu.debtpositions.repository.pii.InstallmentPIIRepository;
import it.gov.pagopa.pu.debtpositions.repository.view.installment.InstallmentDetailPIIViewRepository;
import it.gov.pagopa.pu.debtpositions.repository.view.installment.InstallmentViewPIIRepository;
import it.gov.pagopa.pu.debtpositions.repository.view.installment.InstallmentPaidViewPIIViewRepository;
import it.gov.pagopa.pu.debtpositions.service.update.DebtPositionUpdateInstallmentService;
import it.gov.pagopa.pu.debtpositions.util.InstallmentUtils;
import it.gov.pagopa.pu.workflowhub.dto.generated.WorkflowCreatedDTO;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class InstallmentServiceImpl implements InstallmentService {
  private final InstallmentPIIRepository installmentPIIRepository;
  private final InstallmentNoPIIRepository installmentNoPIIRepository;
  private final InstallmentDetailPIIViewRepository installmentDetailPIIViewRepository;
  private final InstallmentPaidViewPIIViewRepository installmentPaidViewPIIViewRepository;
  private final DebtPositionService debtPositionService;
  private final DebtPositionUpdateInstallmentService debtPositionUpdateInstallmentService;
  private final DebtPositionRepository debtPositionRepository;
  private final DebtPositionMapper debtPositionMapper;
  private final DebtPositionTypeOrgRepository debtPositionTypeOrgRepository;
  private final InstallmentDebtorDTOMapper installmentDebtorDTOMapper;
  private final InstallmentViewPIIRepository installmentViewPIIRepository;

  @Override
  public List<InstallmentDTO> getInstallmentsByOrganizationIdAndNav(Long organizationId, String nav, List<DebtPositionOrigin> debtPositionOrigin) {
    return installmentPIIRepository.getByOrganizationIdAndNav(organizationId, nav, debtPositionOrigin);
  }

  @Override
  public List<InstallmentDTO> getInstallmentsByOrganizationIdAndReceiptId(Long organizationId, Long receiptId, List<DebtPositionOrigin> debtPositionOrigin) {
    return installmentPIIRepository.getByOrganizationIdAndReceiptId(organizationId, receiptId, debtPositionOrigin);
  }

  @Override
  public InstallmentDetailDTO getInstallmentDetail(Long installmentId, String operatorExternalUserId) {
    return installmentDetailPIIViewRepository.getInstallmentDetail(installmentId, operatorExternalUserId);
  }

  @Override
  public PagedInstallmentsPaidView getPagedInstallmentPaidView(ExportPaidInstallmentsFiltersDTO exportPaidInstallmentsFiltersDTO, Pageable pageable) {
    return installmentPaidViewPIIViewRepository.getPagedInstallmentPaidView(exportPaidInstallmentsFiltersDTO, pageable);
  }

  @Override
  public WorkflowCreatedDTO updateInstallmentNotificationDate(UpdateInstallmentNotificationDateRequest request, WfExecutionParameters wfExecutionParameters, String operatorExternalUserId, String accessToken) {
    DebtPositionDTO debtPositionDTO = debtPositionService.getDebtPosition(request.getDebtPositionId());

    List<InstallmentDTO> updatedInstallments = new ArrayList<>();

    debtPositionDTO.getPaymentOptions()
      .forEach(paymentOptionDTO -> paymentOptionDTO.getInstallments().stream()
        .filter(installmentDTO -> !InstallmentStatus.CANCELLED.equals(installmentDTO.getStatus()) && request.getNav().contains(installmentDTO.getNav()) &&
          request.getNotificationDate() != null && installmentDTO.getNotificationDate() != request.getNotificationDate())
        .forEach(installmentDTO -> {
          log.info("Updating notificationDate {} for installment with id {} related to debt position {}", request.getNotificationDate(), installmentDTO.getInstallmentId(), request.getDebtPositionId());
          installmentDTO.setNotificationDate(request.getNotificationDate());
          if(installmentDTO.getDueDate() != null && installmentDTO.getDueDate().isBefore(LocalDate.now())) {
            log.info("Obtained a notificationDate on an already expired Installment: installmentId:{} dueDate:{} status:{}",
              installmentDTO.getInstallmentId(), installmentDTO.getDueDate(), installmentDTO.getStatus());
            installmentDTO.setDueDate(LocalDate.now());
          }
          updatedInstallments.add(installmentDTO);
        }));

    if (updatedInstallments.isEmpty()) {
      return null;
    }

    return debtPositionUpdateInstallmentService.updateInstallment(debtPositionDTO, updatedInstallments, wfExecutionParameters, accessToken, operatorExternalUserId);
  }

  @Override
  public InstallmentDTO updateInstallmentNotificationFee(ActualizeAmountRequestDTO actualizeAmountRequest,
    WfExecutionParameters wfExecutionParameters, String accessToken, String operatorExternalUserId) {
    String nav = actualizeAmountRequest.getNav();
    List<InstallmentDTO> installments = installmentPIIRepository.getByOrganizationIdAndNav(actualizeAmountRequest.getOrganizationId(),
        nav, InstallmentUtils.ORDINARY_DEBT_POSITION_ORIGINS).stream()
      .filter(installment -> InstallmentUtils.MODIFIABLE_STATUSES.contains(installment.getStatus()))
      .toList();
    if(installments.isEmpty())
      throw new NotFoundException("[INSTALLMENT_NOT_FOUND] The installment with NAV: "+nav+" was not found");
    if(installments.size() > 1)
      throw new ConflictErrorException("[TOO_MANY_INSTALLMENTS] Found more than one installment processable with NAV: "+nav);
    if(InstallmentStatus.EXPIRED.equals(installments.getFirst().getStatus()))
      throw new InvalidConditionException("[INVALID_INSTALLMENT_STATUS] The installment with NAV: " + nav + " is expired");

    long notificationFeeCents = calculateFeeAlreadyPaid(actualizeAmountRequest.getNewFeeCents(), installments.getFirst().getIun());

    InstallmentDTO installment = calculateNewAmount(installments.getFirst(), notificationFeeCents);

    if(actualizeAmountRequest.getActualizedFromPuSil()) {
      installment.setBalance(actualizeAmountRequest.getBalance());
      installment.setIun(actualizeAmountRequest.getIun());
      installment.setNotificationDate(actualizeAmountRequest.getNotificationDate());
    }

    DebtPosition debtPosition = debtPositionRepository.findEntityGraphByInstallmentId(installment.getInstallmentId());
    DebtPositionDTO debtPositionDTO = debtPositionMapper.mapToDto(debtPosition);
    debtPositionDTO.getPaymentOptions()
      .forEach(paymentOptionDTO -> {
        List<InstallmentDTO> updatedInstallmentsList = paymentOptionDTO.getInstallments().stream()
          .map(installmentDTO ->
            Objects.equals(installmentDTO.getInstallmentId(), installment.getInstallmentId()) ? installment : installmentDTO
          ).toList();
        paymentOptionDTO.setInstallments(updatedInstallmentsList);
      });

    debtPositionUpdateInstallmentService.updateInstallment(debtPositionDTO, Collections.singletonList(installment),
      wfExecutionParameters, accessToken, operatorExternalUserId);

    return installment;
  }

  private long calculateFeeAlreadyPaid(long notificationFeeCents, String iun) {
    List<InstallmentNoPII> installmentNoPIIS = installmentNoPIIRepository.findPaidByIun(iun);
    if(installmentNoPIIS==null)
      return notificationFeeCents;
    long feeAlreadyPaid = installmentNoPIIS.stream()
      .mapToLong(i -> Objects.requireNonNullElse(i.getNotificationFeeCents(),0L)).sum();
    return notificationFeeCents-feeAlreadyPaid;
  }

  private InstallmentDTO calculateNewAmount(InstallmentDTO installmentDTO, long notificationFeeCents) {
    long oldNotificationFeeCents = java.util.Objects.requireNonNullElse(installmentDTO.getNotificationFeeCents(), 0L);
    long notificationFeeDifference = notificationFeeCents - oldNotificationFeeCents;
    installmentDTO.setNotificationFeeCents(notificationFeeCents);

    boolean transferUpdated = false;
    List<TransferDTO> transfers = installmentDTO.getTransfers();
    for (TransferDTO transfer : transfers) {
      //exclude first item if it's a tax stamp
      if (transfer.getStampType()==null) {
        transfer.setAmountCents(transfer.getAmountCents()+notificationFeeDifference);
        transferUpdated = true;
        break;
      }
    }

    if (!transferUpdated)
      throw new IllegalStateException("[TRANSFER_NOT_FOUND] No eligible transfer found to update notification fee");

    long newInstallmentAmount = transfers.stream()
      .mapToLong(TransferDTO::getAmountCents)
      .sum();

    installmentDTO.setAmountCents(newInstallmentAmount);
    return installmentDTO;
  }

  @Override
  public List<InstallmentDebtorDTO> getInstallmentsByIuvOrNav(String iuvOrNav, String debtorFiscalCode, Long organizationId, List<InstallmentStatus> statuses) {
    if(organizationId==null && StringUtils.isBlank(debtorFiscalCode)){
      throw new InvalidParamException("[MISSING_FIELDS] Either debtorFiscalCode or organizationId must be provided");
    }
    List<InstallmentDTO> installments = installmentPIIRepository.findByIuvOrNav(iuvOrNav, debtorFiscalCode, organizationId, statuses);
    if(CollectionUtils.isEmpty(installments)){
      return Collections.emptyList();
    }
    return installmentDebtorDTOMapper.map(installments,buildDebtPositionTypeOrgMap(installments));
  }

  @Override
  public PagedInstallmentsView getPagedInstallmentsByFilters(InstallmentsSearchFiltersDTO installmentsSearchFiltersDTO, Pageable pageable) {
    return installmentViewPIIRepository.getPagedInstallmentsByFilters(installmentsSearchFiltersDTO, pageable);
  }

  private Map<Long, DebtPositionTypeOrg> buildDebtPositionTypeOrgMap(List<InstallmentDTO> installments) {
    Set<Long> installmentIdSet = installments.stream().map(InstallmentDTO::getInstallmentId).collect(Collectors.toSet());
    return installmentIdSet.stream().collect(
      Collectors.toMap(Function.identity(),debtPositionTypeOrgRepository::getDebtPositionTypeOrgByInstallmentId));
  }
}
