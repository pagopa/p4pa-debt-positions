package it.gov.pagopa.pu.debtpositions.dto.spontaneous;

import it.gov.pagopa.pu.debtpositions.enums.RenderType;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SpontaneousFormField implements Serializable {
  @NotNull
  private String name;
  private boolean required;
  private String regex;
  @NotNull
  private RenderType htmlRender;
  private String htmlClass;
  private String htmlLabel;
  private String htmlPlaceholder;
  private String bindCms;
  private String defaultValue;
  @NotNull
  private Integer insertableOrder;
  @NotNull
  private Boolean indexable;
  @NotNull
  private Integer renderableOrder;
  @NotNull
  private Integer searchableOrder;
  @NotNull
  private Integer listableOrder;
  @NotNull
  private Boolean insertable;
  @NotNull
  private Boolean renderable;
  @NotNull
  private Boolean searchable;
  @NotNull
  private Boolean listable;
  @NotNull
  private Boolean association;
  @NotNull
  private Boolean detailLink;
  private String associationField;
  @NotNull
  private Integer minOccurences;
  @NotNull
  private Integer maxOccurences;
  private String groupBy;
  private Map<String, String> extraAttr;
  private List<String> enumerationList;
  private List<SpontaneousFormField> subfields;
  private String validDependsOn;
  private String validDependsOnUids;
  private String valueDependsOn;
  private String valueDependsOnUids;
  private String hiddenDependsOn;
  private String hiddenDependsOnUids;
  private String mandatoryDependsOn;
  private String mandatoryDependsOnUids;
  private String enabledDependsOn;
  private String enabledDependsOnUids;
  private String errorMessage;
  private String helpMessage;
  private String source;
  private List<String> sourceParams;
}
