package it.gov.pagopa.pu.debtpositions.service;

import it.gov.pagopa.pu.debtpositions.dto.Installment;
import it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.InstallmentDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.PaymentOptionDTO;
import it.gov.pagopa.pu.debtpositions.dto.generated.TransferDTO;
import it.gov.pagopa.pu.debtpositions.mapper.DebtPositionMapper;
import it.gov.pagopa.pu.debtpositions.mapper.InstallmentMapper;
import it.gov.pagopa.pu.debtpositions.mapper.PaymentOptionMapper;
import it.gov.pagopa.pu.debtpositions.mapper.TransferMapper;
import it.gov.pagopa.pu.debtpositions.model.DebtPosition;
import it.gov.pagopa.pu.debtpositions.model.PaymentOption;
import it.gov.pagopa.pu.debtpositions.model.Transfer;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionRepository;
import it.gov.pagopa.pu.debtpositions.repository.InstallmentPIIRepository;
import it.gov.pagopa.pu.debtpositions.repository.PaymentOptionRepository;
import it.gov.pagopa.pu.debtpositions.repository.TransferRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class DebtPositionServiceImplTest {

  @Mock
  private DebtPositionRepository debtPositionRepository;

  @Mock
  private PaymentOptionRepository paymentOptionRepository;

  @Mock
  private InstallmentPIIRepository installmentRepository;

  @Mock
  private TransferRepository transferRepository;

  @Mock
  private DebtPositionMapper debtPositionMapper;

  @Mock
  private PaymentOptionMapper paymentOptionMapper;

  @Mock
  private InstallmentMapper installmentMapper;

  @Mock
  private TransferMapper transferMapper;

  @InjectMocks
  private DebtPositionServiceImpl debtPositionService;

  @Test
  void givenValidDebtPositionDTO_WhenSaveDebtPosition_ThenSaveAllEntities() {
    DebtPositionDTO inputDto = new DebtPositionDTO();
    inputDto.setDebtPositionId(1L);

    PaymentOptionDTO paymentOptionDTO = new PaymentOptionDTO();
    InstallmentDTO installmentDTO = new InstallmentDTO();
    TransferDTO transferDTO = new TransferDTO();
    installmentDTO.setTransfers(List.of(transferDTO));
    paymentOptionDTO.setInstallments(List.of(installmentDTO));
    inputDto.setPaymentOptions(List.of(paymentOptionDTO));

    DebtPosition mappedDebtPosition = new DebtPosition();
    mappedDebtPosition.setDebtPositionId(1L);

    PaymentOption mappedPaymentOption = new PaymentOption();
    mappedPaymentOption.setDebtPositionId(1L);

    Installment mappedInstallment = new Installment();
    mappedInstallment.setPaymentOptionId(1L);

    Transfer mappedTransfer = new Transfer();
    mappedTransfer.setInstallmentId(1L);

    Mockito.when(debtPositionMapper.mapToModel(inputDto)).thenReturn(mappedDebtPosition);
    Mockito.when(paymentOptionMapper.mapToModel(paymentOptionDTO)).thenReturn(mappedPaymentOption);
    Mockito.when(installmentMapper.mapToModel(installmentDTO)).thenReturn(mappedInstallment);
    Mockito.when(transferMapper.mapToModel(transferDTO)).thenReturn(mappedTransfer);

    Mockito.when(debtPositionRepository.save(mappedDebtPosition)).thenReturn(mappedDebtPosition);
    Mockito.when(paymentOptionRepository.save(mappedPaymentOption)).thenReturn(mappedPaymentOption);
    Mockito.when(transferRepository.save(mappedTransfer)).thenReturn(mappedTransfer);

    debtPositionService.saveDebtPosition(inputDto);

    Mockito.verify(debtPositionRepository).save(mappedDebtPosition);
    Mockito.verify(paymentOptionRepository, Mockito.times(1)).save(mappedPaymentOption);
    Mockito.verify(installmentRepository, Mockito.times(1)).save(mappedInstallment);
    Mockito.verify(transferRepository, Mockito.times(1)).save(mappedTransfer);

    Mockito.verify(debtPositionMapper).mapToModel(inputDto);
    Mockito.verify(paymentOptionMapper).mapToModel(paymentOptionDTO);
    Mockito.verify(installmentMapper).mapToModel(installmentDTO);
    Mockito.verify(transferMapper).mapToModel(transferDTO);
  }


  @Test
  void givenRepositoryFails_WhenSaveDebtPosition_ThenThrowRuntimeException() {
    DebtPositionDTO inputDto = new DebtPositionDTO();
    DebtPosition mappedEntity = new DebtPosition();
    Mockito.when(debtPositionMapper.mapToModel(inputDto)).thenReturn(mappedEntity);
    Mockito.when(debtPositionRepository.save(mappedEntity)).thenThrow(new RuntimeException("Database error"));

    assertThrows(RuntimeException.class, () -> debtPositionService.saveDebtPosition(inputDto));
    Mockito.verify(debtPositionMapper).mapToModel(inputDto);
    Mockito.verify(debtPositionRepository).save(mappedEntity);
  }
}

