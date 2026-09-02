package it.gov.pagopa.pu.debtpositions.service;

import it.gov.pagopa.pu.debtpositions.dto.generated.ErrorFieldDTO;
import it.gov.pagopa.pu.debtpositions.dto.spontaneous.SpontaneousFormStructure;
import it.gov.pagopa.pu.debtpositions.exception.common.ConflictException;
import it.gov.pagopa.pu.debtpositions.exception.common.InvalidValueException;
import it.gov.pagopa.pu.debtpositions.exception.common.NotFoundException;
import it.gov.pagopa.pu.debtpositions.model.SpontaneousForm;
import it.gov.pagopa.pu.debtpositions.repository.DebtPositionTypeOrgRepository;
import it.gov.pagopa.pu.debtpositions.repository.SpontaneousFormRepository;
import it.gov.pagopa.pu.debtpositions.util.ErrorCodeConstants;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Objects;

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
      throw new InvalidValueException(ErrorCodeConstants.ERROR_CODE_INVALID_SPONTANEOUS_FORM, "SpontaneousFormId must be null");
    }
    Optional<SpontaneousForm> optSpontaneousForm = spontaneousFormRepository.findByOrganizationIdAndCode(spontaneousForm.getOrganizationId(), spontaneousForm.getCode());
    if(optSpontaneousForm.isPresent()){
      throw new ConflictException(ErrorCodeConstants.ERROR_CODE_SPONTANEOUS_FORM_ALREADY_EXISTS, "There is another SpontaneousForm with organizationId "+spontaneousForm.getOrganizationId()+" and code "+spontaneousForm.getCode());
    }
    return spontaneousFormRepository.save(spontaneousForm);
  }

  @Override
  public void deleteSpontaneousForm(Long spontaneousFormId) {
    SpontaneousForm spontaneousForm = spontaneousFormRepository.findById(
        spontaneousFormId).orElseThrow(
        () -> new NotFoundException(ErrorCodeConstants.ERROR_CODE_SPONTANEOUS_FORM_NOT_FOUND, "SpontaneousForm having id "+spontaneousFormId+" not found"));

    long dptoCount = debtPositionTypeOrgRepository.countBySpontaneousFormId(spontaneousFormId);
    if(dptoCount > 0L){
      throw new ConflictException(ErrorCodeConstants.ERROR_CODE_INVALID_SPONTANEOUS_FORM, "The SpontaneousForm having id "+spontaneousFormId+" is referenced by "+dptoCount+" DebtPositionTypeOrgs");
    }
    spontaneousFormRepository.delete(spontaneousForm);
  }

  @Transactional
  @Override
  public SpontaneousForm updateSpontaneousForm(SpontaneousForm spontaneousForm) {
    if(spontaneousForm.getSpontaneousFormId()==null){
      throw new InvalidValueException(ErrorCodeConstants.ERROR_CODE_INVALID_SPONTANEOUS_FORM, "SpontaneousFormId must not be null");
    }
    validateSpontaneousForm(spontaneousForm);
    return spontaneousFormRepository.save(spontaneousForm);
  }

  @Transactional
  @Override
  public SpontaneousForm resolveOrCreateSpontaneousForm(SpontaneousForm spontaneousForm) {
    return spontaneousFormRepository.findByOrganizationIdAndCode(spontaneousForm.getOrganizationId(), spontaneousForm.getCode())
      .map(existingSpontaneousForm -> {
        checkMatchingStructure(existingSpontaneousForm, spontaneousForm.getStructure());
        return existingSpontaneousForm;
      })
      .orElseGet(() -> spontaneousFormRepository.save(spontaneousForm));
  }

  private void validateSpontaneousForm(SpontaneousForm spontaneousForm) {
    SpontaneousForm existingSpontaneousForm = spontaneousFormRepository.findById(spontaneousForm.getSpontaneousFormId())
      .orElseThrow(()->new NotFoundException(ErrorCodeConstants.ERROR_CODE_SPONTANEOUS_FORM_NOT_FOUND, "SpontaneousForm having id %s not found".formatted(spontaneousForm.getSpontaneousFormId())));
    checkReadOnlyFields(existingSpontaneousForm, spontaneousForm);
  }

  private void checkMatchingStructure(SpontaneousForm existingSpontaneousForm, SpontaneousFormStructure requestedStructure) {
    if (!Objects.equals(existingSpontaneousForm.getStructure(), requestedStructure)) {
      throw new ConflictException(
        ErrorCodeConstants.ERROR_CODE_SPONTANEOUS_FORM_STRUCTURE_MISMATCH,
        "A Spontaneous Form with the same code already exists but its structure differs from the requested structure.");
    }
  }

  private void checkReadOnlyFields(SpontaneousForm existingSpontaneousForm, SpontaneousForm updatedSpontaneousForm) {
    List<ErrorFieldDTO> modifiedFields = new ArrayList<>();
    checkImmutableField("organizationId", existingSpontaneousForm.getOrganizationId(), updatedSpontaneousForm.getOrganizationId(), modifiedFields);
    checkImmutableField("code", existingSpontaneousForm.getCode(), updatedSpontaneousForm.getCode(), modifiedFields);
    if(!CollectionUtils.isEmpty(modifiedFields)){
      throw new InvalidValueException(
        ErrorCodeConstants.ERROR_CODE_IMMUTABLE_FIELD,
        "The following SpontaneousForm fields are readOnly. "+modifiedFields.stream().map(ErrorFieldDTO::getField).toList(),
        modifiedFields);
    }
  }
}
