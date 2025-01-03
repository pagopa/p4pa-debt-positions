package it.gov.pagopa.pu.debtpositions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc()
@TestPropertySource(properties = {
  "spring.datasource.debt-position.driver-class-name=org.h2.Driver",
  "spring.datasource.debt-position.jdbc-url=jdbc:h2:mem:db;DB_CLOSE_DELAY=-1",
  "spring.datasource.debt-position.username=sa",
  "spring.datasource.debt-position.password=sa",

  "spring.datasource.citizen.driver-class-name=org.h2.Driver",
  "spring.datasource.citizen.jdbc-url=jdbc:h2:mem:db;DB_CLOSE_DELAY=-1",
  "spring.datasource.citizen.username=sa",
  "spring.datasource.citizen.password=sa",

  "logging.level.org.springdoc.core.utils.SpringDocAnnotationsUtils=OFF"
})
class OpenApiGeneratorTest {

  @Autowired
  private MockMvc mockMvc;

  @Test
  void generateAndVerifyCommit() throws Exception {
    MvcResult result = mockMvc.perform(
      get("/v3/api-docs")
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)
    ).andExpect(status().isOk()).andReturn();

    String openApi = result.getResponse().getContentAsString();
    Assertions.assertTrue(openApi.startsWith("{\"openapi\":\"3.0."));

    Files.writeString(Path.of("openapi/generated.openapi.json"), prettyPrint(openApi), StandardOpenOption.TRUNCATE_EXISTING);

    String gitStatus = execCmd("git", "status");
    Assertions.assertFalse(gitStatus.contains("openapi/generated.openapi.json"), "Generated OpenApi not committed");
  }

  public String prettyPrint(String json) throws JsonProcessingException {
    ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    JsonNode jsonObject = mapper.readTree(json);
    return mapper.writeValueAsString(jsonObject);
  }

  public static String execCmd(String... cmd) throws java.io.IOException {
    java.util.Scanner s = new java.util.Scanner(Runtime.getRuntime().exec(cmd).getInputStream()).useDelimiter("\\A");
    return s.hasNext() ? s.next() : "";
  }
}
