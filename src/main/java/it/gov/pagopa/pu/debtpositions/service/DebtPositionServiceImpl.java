package it.gov.pagopa.pu.debtpositions.service;

import it.gov.pagopa.pu.debtpositions.dto.generated.*;
import it.gov.pagopa.pu.debtpositions.exception.custom.NotFoundException;
import it.gov.pagopa.pu.debtpositions.mapper.DebtPositionMapper;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII;
import it.gov.pagopa.pu.debtpositions.model.PaymentOption;
import it.gov.pagopa.pu.debtpositions.model.Transfer;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class DebtPositionServiceImpl implements DebtPositionService {

  private final DebtPositionRepository debtPositionRepository;
  private final DebtPositionSaveService debtPositionSaveService;
  private final DebtPositionMapper debtPositionMapper;
  private final DebtPositionDeleteService debtPositionDeleteService;

  public DebtPositionServiceImpl(DebtPositionRepository debtPositionRepository,
                                 DebtPositionSaveService debtPositionSaveService,
                                 DebtPositionMapper debtPositionMapper, DebtPositionDeleteService debtPositionDeleteService) {
    this.debtPositionRepository = debtPositionRepository;
    this.debtPositionSaveService = debtPositionSaveService;
    this.debtPositionMapper = debtPositionMapper;
    this.debtPositionDeleteService = debtPositionDeleteService;
  }

  @Override
  public void saveDebtPosition(DebtPositionDTO debtPositionDTO) {
    debtPositionSaveService.saveDebtPositionDTO(debtPositionDTO);
  }

  @Override
  public void saveDebtPosition(DebtPosition debtPosition) {
    debtPositionSaveService.saveDebtPosition(debtPosition);
  }

  @Override
  public DebtPositionDTO mapDebtPosition(DebtPosition debtPosition) {
    return debtPositionMapper.mapToDto(debtPosition);
  }

  @Override
  public DebtPositionDTO getDebtPosition(Long debtPositionId) {
    DebtPosition debtPosition = getDebtPositionNoPII(debtPositionId);
    return mapDebtPosition(debtPosition);
  }

  @Override
  public DebtPositionDTO getDebtPositionByInstallmentId(Long installmentId) {
    DebtPosition debtPosition = debtPositionRepository.findEntityGraphByInstallmentId(installmentId);
    if (debtPosition == null) {
      throw new NotFoundException("DebtPosition having installmentId %d not found".formatted(installmentId));
    }
    return mapDebtPosition(debtPosition);
  }

  @Override
  public List<DebtPositionDTO> getDebtPositionsByOrganizationIdAndIuv(Long organizationId, String iuv, List<DebtPositionOrigin> debtPositionOrigin) {
    List<DebtPosition> debtPositions = debtPositionRepository.findEntityGraphByOrganizationIdAndInstallmentIuv(organizationId, iuv, debtPositionOrigin);
    return debtPositions.stream().map(this::mapDebtPosition).toList();
  }

  @Override
  public List<DebtPositionDTO> getDebtPositionsByOrganizationIdAndIud(Long organizationId, String iud, List<DebtPositionOrigin> debtPositionOrigin) {
    List<DebtPosition> debtPositions = debtPositionRepository.findEntityGraphByOrganizationIdAndInstallmentIud(organizationId, iud, debtPositionOrigin);
    return debtPositions.stream().map(this::mapDebtPosition).toList();
  }

  @Override
  public DebtPosition getDebtPositionNoPII(Long debtPositionId) {
    DebtPosition debtPosition = debtPositionRepository.findEntityGraphByDebtPositionId(debtPositionId);
    if (debtPosition == null) {
      throw new NotFoundException("DebtPosition having debtPositionId %d not found".formatted(debtPositionId));
    }
    return debtPosition;
  }

  @Override
  public PagedDebtPositions getPagedDebtPositionsByIngestionFlowFileId(Long ingestionFlowFileId, List<InstallmentStatus> statusToExclude, Pageable pageable) {
    Page<DebtPosition> pagedDebtPositionsDTO = debtPositionRepository.findEntityGraphByIngestionFlowFileIdAndStatusToExclude(ingestionFlowFileId, statusToExclude, pageable);

    return debtPositionMapper.mapToPagedDebtPositions(pagedDebtPositionsDTO);
  }

  @Override
  public void delete(DebtPosition debtPosition) {
    debtPositionDeleteService.delete(debtPosition);
  }

  @Override
  public Optional<DebtPosition> getDebtPositionByIupdAndOrganizationId(String iupd, Long organizationId) {
    return debtPositionRepository.findDebtPositionByIupdOrgAndOrganizationId(iupd, organizationId);
  }

  @Override
  public void updateDebtPosition(Long debtPositionId, DebtPositionDTO debtPositionDTO) {
    DebtPosition debtPosition = debtPositionRepository.findEntityGraphByDebtPositionId(debtPositionId);
    if (debtPosition == null) {
      throw new EntityNotFoundException("DebtPosition with id %d not found".formatted(debtPositionId));
    }

    propagateIdsForDebtPosition(debtPosition, debtPositionDTO);

    debtPositionSaveService.saveDebtPositionDTO(debtPositionDTO);
  }

  private void propagateIdsForDebtPosition(DebtPosition entity, DebtPositionDTO dto) {
    if (entity == null || dto == null) return;

    if (dto.getDebtPositionId() == null && entity.getDebtPositionId() != null) {
      dto.setDebtPositionId(entity.getDebtPositionId());
    }

    Map<Integer, PaymentOption> poByIndex = indexBy(
      entity.getPaymentOptions(),
      PaymentOption::getPaymentOptionIndex
    );

    streamOf(dto.getPaymentOptions())
      .forEach(poDTO -> {
        PaymentOption poEntity = poByIndex.get(poDTO.getPaymentOptionIndex());
        if (poEntity != null) {
          propagateIdsForPaymentOption(poEntity, poDTO);
        }
      });
  }

  private void propagateIdsForPaymentOption(PaymentOption poEntity, PaymentOptionDTO poDTO) {
    if (poDTO.getPaymentOptionId() == null && poEntity.getPaymentOptionId() != null) {
      poDTO.setPaymentOptionId(poEntity.getPaymentOptionId());
    }

    Map<String, InstallmentNoPII> instByIud = indexBy(
      poEntity.getInstallments(),
      InstallmentNoPII::getIud
    );

    streamOf(poDTO.getInstallments())
      .forEach(instDTO -> {
        InstallmentNoPII instEntity = instByIud.get(instDTO.getIud());
        if (instEntity != null) {
          propagateIdsForInstallment(instEntity, instDTO);
        }
      });
  }

  private void propagateIdsForInstallment(InstallmentNoPII instEntity, InstallmentDTO instDTO) {
    if (instDTO.getInstallmentId() == null && instEntity.getInstallmentId() != null) {
      instDTO.setInstallmentId(instEntity.getInstallmentId());
    }

    Map<Integer, Transfer> trByIndex = indexBy(
      instEntity.getTransfers(),
      Transfer::getTransferIndex
    );

    streamOf(instDTO.getTransfers())
      .forEach(trDTO -> {
        Transfer trEntity = trByIndex.get(trDTO.getTransferIndex());
        if (trEntity != null && trDTO.getTransferId() == null) {
          trDTO.setTransferId(trEntity.getTransferId());
        }
      });
  }

  private static <T> Stream<T> streamOf(Collection<T> c) {
    return c == null ? Stream.empty() : c.stream().filter(Objects::nonNull);
  }

  private static <T, K> Map<K, T> indexBy(Collection<T> items, Function<T, K> keyFn) {
    return streamOf(items)
      .map(t -> new AbstractMap.SimpleEntry<>(keyFn.apply(t), t))
      .filter(e -> e.getKey() != null)
      .collect(Collectors.toMap(
        Map.Entry::getKey,
        Map.Entry::getValue,
        (a, b) -> a
      ));
  }
}

