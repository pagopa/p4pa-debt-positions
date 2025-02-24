package it.gov.pagopa.pu.debtpositions.service;

import io.micrometer.common.util.StringUtils;
import it.gov.pagopa.pu.debtpositions.dto.Installment;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.mapper.DebtPositionMapper;
import it.gov.pagopa.pu.debtpositions.model.*;
import it.gov.pagopa.pu.debtpositions.repository.*;
import it.gov.pagopa.pu.debtpositions.util.Utilities;
import it.gov.pagopa.pu.organization.dto.generated.Organization;
import jakarta.transaction.Transactional;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.util.Map;

import static it.gov.pagopa.pu.debtpositions.util.Utilities.getRandomicUUID;

@Service
public class DebtPositionServiceImpl implements DebtPositionService {

  private final DebtPositionRepository debtPositionRepository;
  private final DebtPositionTypeOrgRepository debtPositionTypeOrgRepository;
  private final PaymentOptionRepository paymentOptionRepository;
  private final InstallmentPIIRepository installmentRepository;
  private final TransferRepository transferRepository;
  private final DebtPositionMapper debtPositionMapper;

  public DebtPositionServiceImpl(DebtPositionRepository debtPositionRepository, PaymentOptionRepository paymentOptionRepository,
                                 InstallmentPIIRepository installmentRepository, TransferRepository transferRepository,
                                 DebtPositionMapper debtPositionMapper, DebtPositionTypeOrgRepository debtPositionTypeOrgRepository) {
    this.debtPositionRepository = debtPositionRepository;
    this.paymentOptionRepository = paymentOptionRepository;
    this.installmentRepository = installmentRepository;
    this.transferRepository = transferRepository;
    this.debtPositionMapper = debtPositionMapper;
    this.debtPositionTypeOrgRepository = debtPositionTypeOrgRepository;
  }

  @Transactional
  @Override
  public DebtPositionDTO saveDebtPosition(DebtPositionDTO debtPositionDTO, Organization org) {
    Pair<DebtPosition, Map<InstallmentNoPII, Installment>> mappedDebtPosition = debtPositionMapper.mapToModel(debtPositionDTO);
    DebtPositionTypeOrg debtPositionTypeOrg = debtPositionTypeOrgRepository.findById(debtPositionDTO.getDebtPositionTypeOrgId()).orElse(null);
    setDebtPositionParams(debtPositionDTO, mappedDebtPosition, org);

    DebtPosition savedDebtPosition = debtPositionRepository.save(mappedDebtPosition.getFirst());

    savedDebtPosition.getPaymentOptions().forEach(paymentOption -> {
      paymentOption.setDebtPositionId(savedDebtPosition.getDebtPositionId());
      PaymentOption savedPaymentOption = paymentOptionRepository.save(paymentOption);

      savedPaymentOption.getInstallments().forEach(installmentNoPII -> {
        Installment mappedInstallment = mappedDebtPosition.getSecond().get(installmentNoPII);
        mappedInstallment.setPaymentOptionId(savedPaymentOption.getPaymentOptionId());
        setInstallmentParam(debtPositionTypeOrg, mappedInstallment, installmentNoPII, org);

        InstallmentNoPII savedInstallment = installmentRepository.save(mappedInstallment).getNoPII();
        installmentNoPII.setPersonalDataId(savedInstallment.getPersonalDataId());
        installmentNoPII.setInstallmentId(savedInstallment.getInstallmentId());
        installmentNoPII.setPaymentOptionId(savedPaymentOption.getPaymentOptionId());

        mappedInstallment.getTransfers().forEach(transfer -> {
          transfer.setInstallmentId(savedInstallment.getInstallmentId());
          setTransferParam(debtPositionTypeOrg, transfer, org);
          transferRepository.save(transfer);
        });
      });
    });

    return debtPositionMapper.mapToDto(savedDebtPosition);
  }

  private void setDebtPositionParams(DebtPositionDTO debtPositionDTO, Pair<DebtPosition, Map<InstallmentNoPII, Installment>> mappedDebtPosition, Organization org) {
    if (StringUtils.isBlank(debtPositionDTO.getIupdOrg())) {
      mappedDebtPosition.getFirst().setIupdOrg(Utilities.generateRandomIupd(org.getOrgFiscalCode()));
    }
  }

  private void setInstallmentParam(DebtPositionTypeOrg debtPositionTypeOrg, Installment mappedInstallment, InstallmentNoPII installmentNoPII, Organization org) {
    String iupdPagopa = org.getOrgFiscalCode() + "_" + getRandomicUUID();
    mappedInstallment.setIupdPagopa(iupdPagopa);
    installmentNoPII.setIupdPagopa(iupdPagopa);

    if (StringUtils.isBlank(mappedInstallment.getIud())) {
      String iud = Utilities.getRandomIUD();
      mappedInstallment.setIud(iud);
      installmentNoPII.setIud(iud);
    }

    if (StringUtils.isBlank(mappedInstallment.getBalance())) {
      mappedInstallment.setBalance(debtPositionTypeOrg.getBalance());
      installmentNoPII.setBalance(debtPositionTypeOrg.getBalance());
    }
  }

  private void setTransferParam(DebtPositionTypeOrg debtPositionTypeOrg, Transfer transfer, Organization org) {
    if (transfer.getTransferIndex() == 1) {
      transfer.setIban(StringUtils.isBlank(debtPositionTypeOrg.getIban()) ? org.getIban() : debtPositionTypeOrg.getIban());
    }
  }
}

