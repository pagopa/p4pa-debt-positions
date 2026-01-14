package it.gov.pagopa.pu.debtpositions.service;

import it.gov.pagopa.pu.debtpositions.exception.custom.ConflictErrorException;
import it.gov.pagopa.pu.debtpositions.exception.custom.NotFoundException;
import it.gov.pagopa.pu.debtpositions.model.SpontaneousForm;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionTypeOrgRepository;
import it.gov.pagopa.pu.debtpositions.repository.SpontaneousFormRepository;
import jakarta.validation.ValidationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static it.gov.pagopa.pu.debtpositions.util.Utilities.checkImmutableField;

@Service
public class SpontaneousFormServiceImpl implements SpontaneousFormService {
  private final SpontaneousFormRepository spontaneousFormRepository;
  private final DebtPositionTypeOrgRepository debtPositionTypeOrgRepository;

  public SpontaneousFormServiceImpl(SpontaneousFormRepository spontaneousFormRepository, DebtPositionTypeOrgRepository debtPositionTypeOrgRepository) {
    this.spontaneousFormRepository = spontaneousFormRepository;
    this.debtPositionTypeOrgRepository = debtPositionTypeOrgRepository;
  }

  @Transactional
  @Override
  public SpontaneousForm createSpontaneousForm(SpontaneousForm spontaneousForm) {
    if(spontaneousForm.getSpontaneousFormId()!=null){
      throw new ValidationException("[INVALID_SPONTANEOUS_FORM] SpontaneousFormId must be null");
    }
    Optional<SpontaneousForm> optSpontaneousForm = spontaneousFormRepository.findByOrganizationIdAndCode(spontaneousForm.getOrganizationId(), spontaneousForm.getCode());
    if(optSpontaneousForm.isPresent()){
      throw new ConflictErrorException("[SPONTANEOUS_FORM_ALREADY_EXISTS] There is another SpontaneousForm with organizationId "+spontaneousForm.getOrganizationId()+" and code "+spontaneousForm.getCode());
    }
    return spontaneousFormRepository.save(spontaneousForm);
  }

  @Override
  public void deleteSpontaneousForm(Long spontaneousFormId) {
    SpontaneousForm spontaneousForm = spontaneousFormRepository.findById(
        spontaneousFormId).orElseThrow(
        () -> new NotFoundException("[SPONTANEOUS_FORM_NOT_FOUND] SpontaneousForm having id "+spontaneousFormId+" not found"));

    long dptoCount = debtPositionTypeOrgRepository.countBySpontaneousFormId(spontaneousFormId);
    if(dptoCount > 0L){
      throw new ConflictErrorException("[INVALID_SPONTANEOUS_FORM] The SpontaneousForm having id "+spontaneousFormId+" is referenced by "+dptoCount+" DebtPositionTypeOrgs");
    }
    spontaneousFormRepository.delete(spontaneousForm);
  }

  @Transactional
  @Override
  public SpontaneousForm updateSpontaneousForm(SpontaneousForm spontaneousForm) {
    if(spontaneousForm.getSpontaneousFormId()==null){
      throw new ValidationException("[INVALID_SPONTANEOUS_FORM] SpontaneousFormId must not be null");
    }
    validateSpontaneousForm(spontaneousForm);
    return spontaneousFormRepository.save(spontaneousForm);
  }

  private void validateSpontaneousForm(SpontaneousForm spontaneousForm) {
    SpontaneousForm existingSpontaneousForm = spontaneousFormRepository.findById(spontaneousForm.getSpontaneousFormId())
      .orElseThrow(()->new NotFoundException("[SPONTANEOUS_FORM_NOT_FOUND] SpontaneousForm having id %s not found".formatted(spontaneousForm.getSpontaneousFormId())));
    checkReadOnlyFields(existingSpontaneousForm, spontaneousForm);
  }

  private void checkReadOnlyFields(SpontaneousForm existingSpontaneousForm, SpontaneousForm updatedSpontaneousForm) {
    List<String> modifiedFields = new ArrayList<>();
    checkImmutableField("organizationId", existingSpontaneousForm.getOrganizationId(), updatedSpontaneousForm.getOrganizationId(), modifiedFields);
    checkImmutableField("code", existingSpontaneousForm.getCode(), updatedSpontaneousForm.getCode(), modifiedFields);
    if(!CollectionUtils.isEmpty(modifiedFields)){
      throw new ValidationException("[IMMUTABLE_FIELD] The following SpontaneousForm fields are readOnly. "+modifiedFields);
    }
  }
}
