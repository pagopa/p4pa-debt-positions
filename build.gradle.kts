import org.openapitools.generator.gradle.plugin.tasks.GenerateTask
import java.util.*
import com.github.jk1.license.render.*
import com.github.jk1.license.filter.*
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
  java
  id("org.springframework.boot") version "4.0.0"
  id("io.spring.dependency-management") version "1.1.7"
  jacoco
  id("org.sonarqube") version "7.2.1.6560"
  id("com.github.ben-manes.versions") version "0.53.0"
  id("org.openapi.generator") version "7.17.0"
  id("org.ajoberstar.grgit") version "5.3.2"
  id("com.gorylenko.gradle-git-properties") version "2.5.4"
  id("com.github.jk1.dependency-license-report") version "3.0.1"
}

group = "it.gov.pagopa.payhub"
version = "0.0.1"
description = "p4pa-debt-positions"

java {
  toolchain {
    languageVersion = JavaLanguageVersion.of(21)
  }
}

configurations {
  compileOnly {
    extendsFrom(configurations.annotationProcessor.get())
  }
  compileClasspath {
    resolutionStrategy.activateDependencyLocking()
  }
}

licenseReport {
  renderers =
    arrayOf(XmlReportRenderer("third-party-libs.xml", "Back-End Libraries"))
  outputDir = "$projectDir/dependency-licenses"
  filters = arrayOf(SpdxLicenseBundleNormalizer())
}
tasks.classes {
  finalizedBy(tasks.generateLicenseReport)
}

repositories {
  mavenCentral()
}

val springDocOpenApiVersion = "3.0.0"
val janinoVersion = "3.1.12"
val openApiToolsVersion = "0.2.8"
val springWolfAsyncApiVersion = "1.20.0"
val micrometerVersion = "1.6.1"
val postgresJdbcVersion = "42.7.8"
val bouncycastleVersion = "1.83"
val mapStructVersion = "1.6.3"
val podamVersion = "8.0.2.RELEASE"
val caffeineVersion = "3.2.3"
val httpClientVersion = "5.5.1"
val openHtmlToPdfVersion = "1.0.10"
val springCloudDepsVersion = "2025.1.0"
val commonsLang3Version = "3.20.0"
val lz4JavaVersion = "1.10.1"

dependencyManagement {
  imports {
    mavenBom("org.springframework.cloud:spring-cloud-dependencies:$springCloudDepsVersion")
  }
}

dependencies {
  implementation("org.springframework.boot:spring-boot-starter-webmvc")
  implementation("org.springframework.boot:spring-boot-starter-opentelemetry")
  implementation("org.springframework.boot:spring-boot-starter-restclient")
  implementation("org.springframework.boot:spring-boot-starter-validation")
  implementation("org.springframework.boot:spring-boot-starter-security-oauth2-resource-server")
  implementation("org.springframework.boot:spring-boot-starter-actuator")
  implementation("org.springframework.boot:spring-boot-starter-hateoas")
  implementation("org.springframework.boot:spring-boot-starter-data-rest")
  implementation("org.springframework.boot:spring-boot-starter-data-jpa")
  implementation("org.springframework.boot:spring-boot-starter-cache")
  implementation("com.github.ben-manes.caffeine:caffeine:$caffeineVersion")
  implementation("org.springframework.cloud:spring-cloud-starter-stream-kafka") {
    exclude(group = "org.lz4", module = "lz4-java")
  }
  implementation("at.yawk.lz4:lz4-java:$lz4JavaVersion")
  implementation("io.micrometer:micrometer-tracing-bridge-otel:$micrometerVersion")
  implementation("io.micrometer:micrometer-registry-prometheus")
  implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:$springDocOpenApiVersion") {
    exclude(group = "org.apache.commons", module = "commons-lang3")
  }
  implementation("org.apache.commons:commons-lang3:$commonsLang3Version")
  implementation("org.codehaus.janino:janino:$janinoVersion")
  implementation("io.github.springwolf:springwolf-kafka:${springWolfAsyncApiVersion}") {
    exclude(group = "org.lz4", module = "lz4-java")
  }
  implementation("io.github.springwolf:springwolf-ui:${springWolfAsyncApiVersion}")
  implementation("io.github.springwolf:springwolf-cloud-stream:${springWolfAsyncApiVersion}")
  implementation("org.openapitools:jackson-databind-nullable:$openApiToolsVersion")
  implementation("org.mapstruct:mapstruct:$mapStructVersion")
  implementation("org.bouncycastle:bcprov-jdk18on:$bouncycastleVersion")
  implementation("org.postgresql:postgresql:$postgresJdbcVersion")
  implementation("org.apache.httpcomponents.client5:httpclient5:$httpClientVersion")
  implementation("org.springframework.boot:spring-boot-starter-freemarker")
  implementation("com.openhtmltopdf:openhtmltopdf-pdfbox:${openHtmlToPdfVersion}")

  compileOnly("org.projectlombok:lombok")

  //  Testing
  testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
  testImplementation("org.springframework.boot:spring-boot-starter-security-test")
  testImplementation("org.mockito:mockito-core")
  testImplementation("org.projectlombok:lombok")
  testImplementation("com.h2database:h2")
  testImplementation("uk.co.jemos.podam:podam:$podamVersion")

  testAnnotationProcessor("org.projectlombok:lombok")

  annotationProcessor("org.projectlombok:lombok")
  annotationProcessor("org.mapstruct:mapstruct-processor:$mapStructVersion")
}

tasks.withType<Test> {
  useJUnitPlatform()
  finalizedBy(tasks.jacocoTestReport)
}

val mockitoAgent = configurations.create("mockitoAgent")
dependencies {
  mockitoAgent("org.mockito:mockito-core") { isTransitive = false }
}
tasks {
  test {
    jvmArgs("-javaagent:${mockitoAgent.asPath}")
    testLogging.events = setOf(TestLogEvent.FAILED)
    testLogging.exceptionFormat = TestExceptionFormat.FULL
  }
}

tasks.jacocoTestReport {
  dependsOn(tasks.test)
  reports {
    xml.required = true
  }
}

val projectInfo = mapOf(
  "artifactId" to project.name,
  "version" to project.version
)

tasks {
  val processResources by getting(ProcessResources::class) {
    filesMatching("**/application.yml") {
      expand(projectInfo)
    }
  }
}

tasks.compileJava {
  dependsOn("dependenciesBuild")
}

tasks.register("dependenciesBuild") {
  group = "AutomaticallyGeneratedCode"
  description = "grouping all together automatically generate code tasks"

  dependsOn(
    "openApiGenerate",
    "openApiGenerateORGANIZATION",
    "openApiGenerateP4PAAUTH",
    "openApiGenerateWORKFLOWHUB",
    "openApiGenerateCLASSIFICATION"
  )
}

configure<SourceSetContainer> {
  named("main") {
    java.srcDir("$projectDir/build/generated/src/main/java")
  }
}

springBoot {
  buildInfo()
  mainClass.value("it.gov.pagopa.pu.debtpositions.DebtPositionsApplication")
}

openApiGenerate {
  generatorName.set("spring")
  inputSpec.set("$rootDir/openapi/p4pa-debt-position.openapi.yaml")
  outputDir.set("$projectDir/build/generated")
  apiPackage.set("it.gov.pagopa.pu.debtpositions.controller.generated")
  modelPackage.set("it.gov.pagopa.pu.debtpositions.dto.generated")
  openapiNormalizer.set(mapOf("REF_AS_PARENT_IN_ALLOF" to "true"))
  typeMappings.set(
    mapOf(
      "ReceiptOrigin" to "it.gov.pagopa.pu.debtpositions.enums.ReceiptOriginType",
      "InstallmentPaidView" to "it.gov.pagopa.pu.debtpositions.dto.InstallmentPaidViewDTO",
      "PaymentEventType" to "it.gov.pagopa.pu.workflowhub.dto.generated.PaymentEventType",
      "object" to "tools.jackson.databind.JsonNode",
      "InstallmentSyncStatus" to "it.gov.pagopa.pu.debtpositions.model.InstallmentSyncStatus",
      "DebtPositionTypeOrg" to "it.gov.pagopa.pu.debtpositions.model.DebtPositionTypeOrg",
      "InstallmentNoPII" to "it.gov.pagopa.pu.debtpositions.model.InstallmentNoPII",
      "ReceiptNoPII" to "it.gov.pagopa.pu.debtpositions.model.ReceiptNoPII",
      "SpontaneousForm" to "it.gov.pagopa.pu.debtpositions.model.SpontaneousForm",
      "BaseDebtPosition" to "it.gov.pagopa.pu.debtpositions.dto.BaseDebtPosition",
      "DebtorDebtPositionDTO" to "it.gov.pagopa.pu.debtpositions.dto.DebtorDebtPositionDTO",
      "PaymentOptionType" to "it.gov.pagopa.pu.debtpositions.enums.PaymentOptionType",
      "InstallmentViewDTO" to "it.gov.pagopa.pu.debtpositions.dto.InstallmentViewDTO",
    )
  )
  configOptions.set(
    mapOf(
      "dateLibrary" to "java8",
      "requestMappingMode" to "api_interface",
      "useSpringBoot3" to "true",
      "interfaceOnly" to "true",
      "useTags" to "true",
      "useBeanValidation" to "true",
      "generateConstructorWithAllArgs" to "true",
      "generatedConstructorWithRequiredArgs" to "true",
      "enumPropertyNaming" to "original",
      "additionalModelTypeAnnotations" to "@lombok.experimental.SuperBuilder(toBuilder = true)"
    )
  )
}

var targetEnv = when (Objects.requireNonNullElse(
  System.getProperty("targetBranch"),
  grgit.branch.current().name
)) {
  "uat" -> "uat"
  "main" -> "main"
  else -> "develop"
}

tasks.register<GenerateTask>("openApiGenerateORGANIZATION") {
  group = "AutomaticallyGeneratedCode"
  description = "openapi"

  generatorName.set("java")
  remoteInputSpec.set("https://raw.githubusercontent.com/pagopa/p4pa-organization/refs/heads/$targetEnv/openapi/generated.openapi.json")
  outputDir.set("$projectDir/build/generated")
  invokerPackage.set("it.gov.pagopa.pu.organization.generated")
  apiPackage.set("it.gov.pagopa.pu.organization.client.generated")
  modelPackage.set("it.gov.pagopa.pu.organization.dto.generated")
  configOptions.set(
    mapOf(
      "swaggerAnnotations" to "false",
      "openApiNullable" to "false",
      "dateLibrary" to "java8",
      "serializableModel" to "true",
      "useSpringBoot3" to "true",
      "useJakartaEe" to "true",
      "useOneOfInterfaces" to "true",
      "useBeanValidation" to "true",
      "serializationLibrary" to "jackson",
      "generateSupportingFiles" to "true",
      "generateConstructorWithAllArgs" to "true",
      "generatedConstructorWithRequiredArgs" to "true",
      "enumPropertyNaming" to "original",
      "additionalModelTypeAnnotations" to "@lombok.experimental.SuperBuilder(toBuilder = true)"
    )
  )
  library.set("resttemplate")
}

tasks.register<GenerateTask>("openApiGenerateP4PAAUTH") {
  group = "AutomaticallyGeneratedCode"
  description = "openapi"

  generatorName.set("java")
  remoteInputSpec.set("https://raw.githubusercontent.com/pagopa/p4pa-auth/refs/heads/$targetEnv/openapi/p4pa-auth.openapi.yaml")
  outputDir.set("$projectDir/build/generated")
  invokerPackage.set("it.gov.pagopa.pu.auth.generated")
  apiPackage.set("it.gov.pagopa.pu.auth.controller.generated")
  modelPackage.set("it.gov.pagopa.pu.auth.dto.generated")
  configOptions.set(
    mapOf(
      "swaggerAnnotations" to "false",
      "openApiNullable" to "false",
      "dateLibrary" to "java8",
      "serializableModel" to "true",
      "useSpringBoot3" to "true",
      "useJakartaEe" to "true",
      "useOneOfInterfaces" to "true",
      "useBeanValidation" to "true",
      "serializationLibrary" to "jackson",
      "generateSupportingFiles" to "true",
      "generateConstructorWithAllArgs" to "true",
      "generatedConstructorWithRequiredArgs" to "true",
      "enumPropertyNaming" to "original",
      "additionalModelTypeAnnotations" to "@lombok.experimental.SuperBuilder(toBuilder = true)"
    )
  )
  library.set("resttemplate")
}

tasks.register<GenerateTask>("openApiGenerateWORKFLOWHUB") {
  group = "AutomaticallyGeneratedCode"
  description = "openapi"

  generatorName.set("java")
  remoteInputSpec.set("https://raw.githubusercontent.com/pagopa/p4pa-workflow-hub/refs/heads/$targetEnv/openapi/generated.openapi.json")
  outputDir.set("$projectDir/build/generated")
  invokerPackage.set("it.gov.pagopa.pu.workflowhub.generated")
  apiPackage.set("it.gov.pagopa.pu.workflowhub.controller.generated")
  modelPackage.set("it.gov.pagopa.pu.workflowhub.dto.generated")
  typeMappings.set(
    mapOf(
      "DebtPositionDTO" to "it.gov.pagopa.pu.debtpositions.dto.generated.DebtPositionDTO",
      "IngestionFlowFileType" to "String",
      "WfExecutionConfig" to "tools.jackson.databind.JsonNode",
      "FineWfExecutionConfig" to "tools.jackson.databind.JsonNode",
      "ExportFileType" to "String",
      "WorkflowExecutionStatus" to "String"
    )
  )
  configOptions.set(
    mapOf(
      "swaggerAnnotations" to "false",
      "openApiNullable" to "false",
      "dateLibrary" to "java8",
      "serializableModel" to "true",
      "useSpringBoot3" to "true",
      "useJakartaEe" to "true",
      "useOneOfInterfaces" to "true",
      "useBeanValidation" to "true",
      "serializationLibrary" to "jackson",
      "generateSupportingFiles" to "true",
      "generateConstructorWithAllArgs" to "true",
      "generatedConstructorWithRequiredArgs" to "true",
      "enumPropertyNaming" to "original",
      "additionalModelTypeAnnotations" to "@lombok.experimental.SuperBuilder(toBuilder = true)"
    )
  )
  library.set("resttemplate")
}

tasks.register<GenerateTask>("openApiGenerateCLASSIFICATION") {
  group = "AutomaticallyGeneratedCode"
  description = "openapi"

  generatorName.set("java")
  remoteInputSpec.set("https://raw.githubusercontent.com/pagopa/p4pa-classification/refs/heads/$targetEnv/openapi/generated.openapi.json")
  outputDir.set("$projectDir/build/generated")
  invokerPackage.set("it.gov.pagopa.pu.classification.generated")
  apiPackage.set("it.gov.pagopa.pu.classification.controller.generated")
  modelPackage.set("it.gov.pagopa.pu.classification.dto.generated")
  configOptions.set(
    mapOf(
      "swaggerAnnotations" to "false",
      "openApiNullable" to "false",
      "dateLibrary" to "java8",
      "serializableModel" to "true",
      "useSpringBoot3" to "true",
      "useJakartaEe" to "true",
      "useOneOfInterfaces" to "true",
      "useBeanValidation" to "true",
      "serializationLibrary" to "jackson",
      "generateSupportingFiles" to "true",
      "generateConstructorWithAllArgs" to "true",
      "generatedConstructorWithRequiredArgs" to "true",
      "enumPropertyNaming" to "original",
      "additionalModelTypeAnnotations" to "@lombok.experimental.SuperBuilder(toBuilder = true)"
    )
  )
  library.set("resttemplate")
  typeMappings.set(
    mapOf(
      "LocalDateTime" to "java.time.LocalDateTime"
    )
  )
}
