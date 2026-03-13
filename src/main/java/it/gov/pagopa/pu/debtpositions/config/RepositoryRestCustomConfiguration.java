package it.gov.pagopa.pu.debtpositions.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.Schema;
import org.apache.commons.lang3.StringUtils;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mapping.PersistentEntity;
import org.springframework.data.mapping.context.PersistentEntities;
import org.springframework.data.rest.webmvc.alps.AlpsJacksonJsonHttpMessageConverter;
import org.springframework.data.rest.webmvc.alps.RootResourceInformationToAlpsDescriptorConverter;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurer;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver;
import tools.jackson.databind.json.JsonMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@Configuration
public class RepositoryRestCustomConfiguration implements RepositoryRestConfigurer {

  public static final String SPRING_DATA_REST_MODEL_PREFIX = "EntityModel";

  private final PersistentEntities persistentEntities;
  private final JsonMapper jsonMapper;

  public RepositoryRestCustomConfiguration(PersistentEntities persistentEntities, JsonMapper jsonMapper) {
    this.persistentEntities = persistentEntities;
    this.jsonMapper = jsonMapper;
  }

  @Bean
  public RepositoryRestConfigurer repositoryRestConfigurer() {
    return RepositoryRestConfigurer.withConfig(config ->
      config.exposeIdsFor(persistentEntities.get()
        .map(PersistentEntity::getType)
        .toArray(Class[]::new)));
  }

  @Bean
  public OpenApiCustomizer operationIdCustomizer() {
    return openApi -> {
      renameSpringDataRestModels(openApi);
      renameSpringDataRestOperationIds(openApi);
    };
  }


  // overriding because the default implementation is not using the right JsonMapper
  @Bean
  @Primary
  public AlpsJacksonJsonHttpMessageConverter alpsJsonHttpMessageCustomConverter(
    RootResourceInformationToAlpsDescriptorConverter alpsConverter) {
    return new AlpsJacksonJsonHttpMessageConverter(jsonMapper, alpsConverter);
  }

  @Override
  public void configureExceptionHandlerExceptionResolver(ExceptionHandlerExceptionResolver exceptionResolver) {
    List<HttpMessageConverter<?>> messageConverters = new ArrayList<>(exceptionResolver.getMessageConverters());
    // ErrorDTO is not an Entity, thus we have to register a generic converter
    messageConverters.add(new JacksonJsonHttpMessageConverter(jsonMapper));
    exceptionResolver.setMessageConverters(messageConverters);
  }

  private void renameSpringDataRestModels(OpenAPI openApi) {
    renameModelsInComponentsSchemas(openApi);
    renameModelsInOperations(openApi);
  }

  private void renameModelsInComponentsSchemas(OpenAPI openApi) {
    List<String> entityModels = openApi.getComponents().getSchemas().keySet().stream()
      .filter(s -> s.contains(SPRING_DATA_REST_MODEL_PREFIX))
      .toList();

    entityModels.forEach(m -> {
      Schema<?> schema = openApi.getComponents().getSchemas().remove(m);
      String newName = m.replace(SPRING_DATA_REST_MODEL_PREFIX, "");
      schema.setName(newName);
      openApi.getComponents().getSchemas().put(newName, schema);
    });

    openApi.getComponents().getSchemas().values()
      .forEach(this::renameModelsInNestedSchemas);
  }

  private void renameModelsInNestedSchemas(Schema<?> schema) {
    if (schema.get$ref() != null) {
      if (schema.get$ref().contains(SPRING_DATA_REST_MODEL_PREFIX)) {
        schema.set$ref(schema.get$ref().replace(SPRING_DATA_REST_MODEL_PREFIX, ""));
      }
    } else {
      if (schema.getItems() != null) {
        renameModelsInNestedSchemas(schema.getItems());
      } else if (schema.getProperties() != null) {
        schema.getProperties().values().forEach(this::renameModelsInNestedSchemas);
      }
    }
  }

  private void renameModelsInOperations(OpenAPI openApi) {
    openApi.getPaths().values().stream()
      .flatMap(p -> Stream.of(
        p.getGet(),
        p.getPost(),
        p.getPut(),
        p.getDelete(),
        p.getPatch()
      ))
      .filter(Objects::nonNull)
      .forEach(op -> {
        if (op.getRequestBody() != null) {
          Schema<?> schema = op.getRequestBody().getContent().get(MediaType.APPLICATION_JSON_VALUE).getSchema();
          String ref = schema.get$ref();
          if (ref != null && ref.contains(SPRING_DATA_REST_MODEL_PREFIX)) {
            schema.set$ref(ref.replace(SPRING_DATA_REST_MODEL_PREFIX, ""));
          }
        }
        if (op.getResponses() != null) {
          op.getResponses().values()
            .stream().filter(r -> r.getContent() != null)
            .flatMap(r -> r.getContent().values().stream())
            .forEach(t -> {
              Schema<?> schema = t.getSchema();
              String ref = schema.get$ref();
              if (ref != null && ref.contains(SPRING_DATA_REST_MODEL_PREFIX)) {
                schema.set$ref(ref.replace(SPRING_DATA_REST_MODEL_PREFIX, ""));
              }
            });
        }
      });
  }

  private void renameSpringDataRestOperationIds(OpenAPI openApi) {
    openApi.getPaths().entrySet().stream()
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
