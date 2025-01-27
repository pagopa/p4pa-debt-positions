package it.gov.pagopa.pu.debtpositions.config;

import io.swagger.v3.oas.models.parameters.Parameter;
import java.util.List;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.CollectionUtils;

@Configuration
public class OpenApiConfig {
  @Bean
  public OpenApiCustomizer addSpringPaginatedHeader() {
    return openApi -> openApi.getPaths().forEach((path, pathItem) -> {
      if(pathItem.getGet()!=null
          && !CollectionUtils.isEmpty(pathItem.getGet().getParameters())
          && pathItem.getGet().getParameters().stream().anyMatch(
        OpenApiConfig::isPaginationParameter)){
        List<Parameter> parameters = pathItem.getGet().getParameters();
        parameters.forEach(p->{
          if(isPaginationParameter(p)){
            p.addExtension("x-ignore", true);
          }
        });
        pathItem.getGet().addExtension("x-spring-paginated",true);
      }
    });
  }

  private static boolean isPaginationParameter(Parameter p) {
    return p.getName().equals("page") || p.getName().equals("size")
      || p.getName().equals("sort");
  }
}
