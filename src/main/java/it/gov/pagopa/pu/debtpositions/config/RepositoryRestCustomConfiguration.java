package it.gov.pagopa.pu.debtpositions.config;

import io.swagger.v3.oas.models.PathItem;
import jakarta.persistence.EntityManager;
import jakarta.persistence.metamodel.EntityType;
import org.apache.commons.lang3.StringUtils;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurer;

import java.util.Set;

@Configuration
public class RepositoryRestCustomConfiguration {

  private final EntityManager entityManager;

  public RepositoryRestCustomConfiguration(EntityManager entityManager) {
    this.entityManager = entityManager;
  }

  @Bean
  public RepositoryRestConfigurer repositoryRestConfigurer() {
    return RepositoryRestConfigurer.withConfig(config -> {
      Set<EntityType<?>> entities = entityManager.getMetamodel().getEntities();
      config.exposeIdsFor(entities.stream()
        .map(EntityType::getJavaType)
        .toArray(Class[]::new));
    });
  }

  @Bean
  public OpenApiCustomizer operationIdCustomizer() {
    return openApi -> openApi.getPaths().entrySet().stream()
      .filter(e -> e.getKey().startsWith("/crud/"))
      .forEach(entry -> {
        String[] paths = entry.getKey().split("/");
        entry.getValue().readOperationsMap().forEach((httpMethod, operation) -> operation.setOperationId(
          "crud-" +
            StringUtils.firstNonEmpty(
              operation.getDescription(),
              paths[2] + "-" + paths[paths.length - 1]
            )
            + (PathItem.HttpMethod.GET.equals(httpMethod) && paths.length == 3 ? "s" : "")
        ));
      });
  }

}
