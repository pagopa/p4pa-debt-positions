package it.gov.pagopa.pu.debtpositions.service;

import it.gov.pagopa.pu.debtpositions.dto.WfExecutionParameters;
import it.gov.pagopa.pu.debtpositions.dto.generated.*;
import it.gov.pagopa.pu.debtpositions.exception.custom.ConflictErrorException;
import it.gov.pagopa.pu.debtpositions.exception.custom.NotFoundException;
import it.gov.pagopa.pu.debtpositions.mapper.DebtPositionMapper;
import it.gov.pagopa.pu.debtpositions.mapper.InstallmentMapper;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionRepository;
import it.gov.pagopa.pu.debtpositions.repository.InstallmentNoPIIRepository;
import it.gov.pagopa.pu.debtpositions.repository.InstallmentPIIRepository;
import it.gov.pagopa.pu.debtpositions.repository.view.installment.InstallmentDetailPIIViewRepository;
import it.gov.pagopa.pu.debtpositions.repository.view.installment.InstallmentPaidViewPIIViewRepository;
import it.gov.pagopa.pu.debtpositions.service.update.DebtPositionUpdateInstallmentService;
import it.gov.pagopa.pu.debtpositions.util.InstallmentUtils;
import it.gov.pagopa.pu.workflowhub.dto.generated.WorkflowCreatedDTO;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
public class InstallmentServiceImpl implements InstallmentService {
  private final InstallmentPIIRepository installmentPIIRepository;
  private final InstallmentNoPIIRepository installmentNoPIIRepository;
  private final InstallmentMapper installmentMapper;
  private final InstallmentDetailPIIViewRepository installmentDetailPIIViewRepository;
  private final InstallmentPaidViewPIIViewRepository installmentPaidViewPIIViewRepository;
  private final DebtPositionService debtPositionService;
  private final DebtPositionUpdateInstallmentService debtPositionUpdateInstallmentService;
  private final DebtPositionRepository debtPositionRepository;
  private final DebtPositionMapper debtPositionMapper;

  public InstallmentServiceImpl(InstallmentPIIRepository installmentPIIRepository,
    InstallmentNoPIIRepository installmentNoPIIRepository, InstallmentMapper installmentMapper, InstallmentDetailPIIViewRepository installmentDetailPIIViewRepository, InstallmentPaidViewPIIViewRepository installmentPaidViewPIIViewRepository, DebtPositionService debtPositionService, DebtPositionUpdateInstallmentService debtPositionUpdateInstallmentService,
    DebtPositionRepository debtPositionRepository,
    DebtPositionMapper debtPositionMapper) {
    this.installmentPIIRepository = installmentPIIRepository;
    this.installmentNoPIIRepository = installmentNoPIIRepository;
    this.installmentMapper = installmentMapper;
    this.installmentDetailPIIViewRepository = installmentDetailPIIViewRepository;
    this.installmentPaidViewPIIViewRepository = installmentPaidViewPIIViewRepository;
    this.debtPositionService = debtPositionService;
    this.debtPositionUpdateInstallmentService = debtPositionUpdateInstallmentService;
    this.debtPositionRepository = debtPositionRepository;
    this.debtPositionMapper = debtPositionMapper;
  }

  @Override
  public List<InstallmentDTO> getInstallmentsByOrganizationIdAndNav(Long organizationId, String nav, List<DebtPositionOrigin> debtPositionOrigin) {
    return installmentPIIRepository.getByOrganizationIdAndNav(organizationId, nav, debtPositionOrigin).stream()
            .map(installmentMapper::mapToDto)
            .toList();
  }

  @Override
  public InstallmentDetailDTO getInstallmentDetail(Long installmentId, String operatorExternalUserId) {
    return installmentDetailPIIViewRepository.getInstallmentDetail(installmentId, operatorExternalUserId);
  }

  @Override
  public PagedInstallmentsPaidView getPagedInstallmentPaidView(Long organizationId, String operatorExternalUserId, OffsetDateTime paymentDateFrom, OffsetDateTime paymentDateTo, Long debtPositionTypeOrgId, Pageable pageable) {
    return installmentPaidViewPIIViewRepository.getPagedInstallmentPaidView(organizationId, operatorExternalUserId, paymentDateFrom, paymentDateTo, debtPositionTypeOrgId, pageable);
  }

  @Override
  public WorkflowCreatedDTO updateInstallmentNotificationDate(UpdateInstallmentNotificationDateRequest request, WfExecutionParameters wfExecutionParameters, String operatorExternalUserId, String accessToken) {
    DebtPositionDTO debtPositionDTO = debtPositionService.getDebtPosition(request.getDebtPositionId());

    List<InstallmentDTO> updatedInstallments = new ArrayList<>();

    debtPositionDTO.getPaymentOptions()
      .forEach(paymentOptionDTO -> paymentOptionDTO.getInstallments().stream()
        .filter(installmentDTO -> !InstallmentStatus.CANCELLED.equals(installmentDTO.getStatus()) && request.getNav().contains(installmentDTO.getNav()))
        .forEach(installmentDTO -> {
          log.info("Updating notificationDate {} for installment with id {} related to debt position {}", request.getNotificationDate(), installmentDTO.getInstallmentId(), request.getDebtPositionId());
          installmentDTO.setNotificationDate(request.getNotificationDate());
          updatedInstallments.add(installmentDTO);
        }));

    if (updatedInstallments.isEmpty()) {
      return null;
    }

    return debtPositionUpdateInstallmentService.updateInstallment(debtPositionDTO, updatedInstallments, wfExecutionParameters, accessToken, operatorExternalUserId);
  }

  @Override
  public InstallmentDTO updateInstallmentNotificationFee(Long organizationId, String nav, long notificationFeeCents,
    WfExecutionParameters wfExecutionParameters, String accessToken, String operatorExternalUserId) {
    List<InstallmentDTO> installments = installmentPIIRepository.getByOrganizationIdAndNav(organizationId, nav, null).stream()
      .filter(installment -> InstallmentUtils.MODIFIABLE_STATUSES.contains(installment.getStatus()))
      .map(installmentMapper::mapToDto)
      .toList();

    if(installments.isEmpty())
      throw new NotFoundException("The installment with NAV: "+nav+" was not found");
    if(installments.size() > 1)
      throw new ConflictErrorException("Found more than one installment processable with NAV: "+nav);

    notificationFeeCents = calculateFeeAlreadyPaid(notificationFeeCents, installments.getFirst().getIun());

    InstallmentDTO installment = calculateNewAmount(installments.getFirst(), notificationFeeCents);

    DebtPosition debtPosition = debtPositionRepository.findByInstallmentId(installment.getInstallmentId());
    DebtPositionDTO debtPositionDTO = debtPositionMapper.mapToDto(debtPosition);
    debtPositionDTO.getPaymentOptions()
      .forEach(paymentOptionDTO -> {
        List<InstallmentDTO> updatedInstallmentsList = paymentOptionDTO.getInstallments().stream()
          .map(installmentDTO ->
            installmentDTO.getInstallmentId().equals(installment.getInstallmentId()) ? installment : installmentDTO
          ).toList();
        paymentOptionDTO.setInstallments(updatedInstallmentsList);
      });

    debtPositionUpdateInstallmentService.updateInstallment(debtPositionDTO, Collections.singletonList(installment),
      wfExecutionParameters, accessToken, operatorExternalUserId);

    return installment;
  }

  private long calculateFeeAlreadyPaid(long notificationFeeCents, String iun) {
    List<InstallmentNoPII> installmentNoPIIS = installmentNoPIIRepository.findPaidByIun(iun);
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
      throw new IllegalStateException("No eligible transfer found to update notification fee");

    long newInstallmentAmount = transfers.stream()
      .mapToLong(TransferDTO::getAmountCents)
      .sum();

    installmentDTO.setAmountCents(newInstallmentAmount);
    return installmentDTO;
  }
}
