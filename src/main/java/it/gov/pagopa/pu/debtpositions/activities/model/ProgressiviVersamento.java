package it.gov.pagopa.pu.debtpositions.activities.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.*;

@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class ProgressiviVersamento extends BaseEntity {

  public static final String ALIAS = "ProgressiviVersamento";
  public static final String FIELDS = ""+ALIAS+".id as ProgressiviVersamento_id,"+ALIAS+".version as ProgressiviVersamento_version"+
      ","+ALIAS+".cod_ipa_ente as ProgressiviVersamento_codIpaEnte"+
      ","+ALIAS+".tipo_generatore as ProgressiviVersamento_tipoGeneratore"+
      ","+ALIAS+".tipo_versamento as ProgressiviVersamento_tipoVersamento"+
      ","+ALIAS+".progressivo_versamento as ProgressiviVersamento_progressivoVersamento";

  private Long id;
  private int version;
  private String codIpaEnte;
  private String tipoGeneratore;
  private String tipoVersamento;
  private Long progressivoVersamento;
}
