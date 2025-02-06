package it.gov.pagopa.pu.debtpositions.repository;

import it.gov.pagopa.pu.debtpositions.citizen.enums.PersonalDataType;
import it.gov.pagopa.pu.debtpositions.citizen.service.PersonalDataService;
import it.gov.pagopa.pu.debtpositions.dto.Receipt;
import it.gov.pagopa.pu.debtpositions.dto.ReceiptPIIDTO;
import it.gov.pagopa.pu.debtpositions.mapper.ReceiptPIIMapper;
import it.gov.pagopa.pu.debtpositions.model.ReceiptNoPII;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

@Service
public class ReceiptPIIRepositoryImpl implements ReceiptPIIRepository {

  private final ReceiptPIIMapper receiptPIIMapper;
  private final PersonalDataService personalDataService;
  private final ReceiptNoPIIRepository receiptNoPIIRepository;

  public ReceiptPIIRepositoryImpl(ReceiptPIIMapper receiptPIIMapper, PersonalDataService personalDataService, ReceiptNoPIIRepository receiptNoPIIRepository) {
    this.receiptPIIMapper = receiptPIIMapper;
    this.personalDataService = personalDataService;
    this.receiptNoPIIRepository = receiptNoPIIRepository;
  }

  @Override
  public long save(Receipt receipt) {
    Pair<ReceiptNoPII, ReceiptPIIDTO> p = receiptPIIMapper.map(receipt);
    long personalDataId = personalDataService.insert(p.getSecond(), PersonalDataType.RECEIPT);
    p.getFirst().setPersonalDataId(personalDataId);
    receipt.setNoPII(p.getFirst());
    long newId = receiptNoPIIRepository.save(p.getFirst()).getInstallmentId();
    receipt.setReceiptId(newId);
    receipt.getNoPII().setReceiptId(newId);
    return newId;
  }
}
