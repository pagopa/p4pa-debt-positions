import org.openapitools.generator.gradle.plugin.tasks.GenerateTask

plugins {
	java
	id("org.springframework.boot") version "3.4.1"
	id("io.spring.dependency-management") version "1.1.7"
	jacoco
	id("org.sonarqube") version "6.0.1.5171"
	id("com.github.ben-manes.versions") version "0.51.0"
	id("org.openapi.generator") version "7.10.0"
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
}

repositories {
	mavenCentral()
}

val springDocOpenApiVersion = "2.7.0"
val openApiToolsVersion = "0.2.6"
val micrometerVersion = "1.4.1"
val postgresJdbcVersion = "42.7.4"

val bouncycastleVersion = "1.79"

dependencies {
	implementation("org.springframework.boot:spring-boot-starter")
	implementation("org.springframework.boot:spring-boot-starter-web")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
  implementation("org.springframework.boot:spring-boot-starter-actuator")
  implementation("org.springframework.boot:spring-boot-starter-data-rest")
  implementation("org.springframework.boot:spring-boot-starter-data-jpa")
  implementation("io.micrometer:micrometer-tracing-bridge-otel:$micrometerVersion")
  implementation("io.micrometer:micrometer-registry-prometheus")
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:$springDocOpenApiVersion")
	implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
	implementation("org.openapitools:jackson-databind-nullable:$openApiToolsVersion")

  //security
  implementation("org.bouncycastle:bcprov-jdk18on:$bouncycastleVersion")


  //postgres jdbc
  implementation("org.postgresql:postgresql:$postgresJdbcVersion")

	compileOnly("org.projectlombok:lombok")
	annotationProcessor("org.projectlombok:lombok")

	//	Testing
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.mockito:mockito-core")
	testImplementation ("org.projectlombok:lombok")
  testImplementation("com.h2database:h2")
}

tasks.withType<Test> {
	useJUnitPlatform()
	finalizedBy(tasks.jacocoTestReport)
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

configurations {
	compileClasspath {
		resolutionStrategy.activateDependencyLocking()
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
    "openApiGenerateP4PAAUTH"
  )
}

configure<SourceSetContainer> {
  named("main") {
    java.srcDir("$projectDir/build/generated/src/main/java")
  }
}

springBoot {
	mainClass.value("it.gov.pagopa.pu.debtpositions.DebtPositionsApplication")
}

openApiGenerate {
  generatorName.set("spring")
  inputSpec.set("$rootDir/openapi/p4pa-debt-position.openapi.yaml")
  outputDir.set("$projectDir/build/generated")
  apiPackage.set("it.gov.pagopa.pu.debtpositions.controller.generated")
  modelPackage.set("it.gov.pagopa.pu.debtpositions.dto.generated")
  configOptions.set(mapOf(
    "dateLibrary" to "java8",
    "requestMappingMode" to "api_interface",
    "useSpringBoot3" to "true",
    "interfaceOnly" to "true",
    "useTags" to "true",
    "useBeanValidation" to "true",
    "generateConstructorWithAllArgs" to "true",
    "generatedConstructorWithRequiredArgs" to "true",
    "additionalModelTypeAnnotations" to "@lombok.Builder"
  ))
}

tasks.register<GenerateTask>("openApiGenerateORGANIZATION") {
  group = "AutomaticallyGeneratedCode"
  description = "openapi"

  generatorName.set("java")
  remoteInputSpec.set("https://raw.githubusercontent.com/pagopa/p4pa-organization/refs/heads/develop/openapi/generated.openapi.json")
  outputDir.set("$projectDir/build/generated")
  invokerPackage.set("it.gov.pagopa.pu.organization.generated")
  apiPackage.set("it.gov.pagopa.pu.organization.client.generated")
  modelPackage.set("it.gov.pagopa.pu.organization.dto.generated")
  configOptions.set(mapOf(
    "swaggerAnnotations" to "false",
    "openApiNullable" to "false",
    "dateLibrary" to "java8",
    "serializableModel" to "true",
    "useSpringBoot3" to "true",
    "useJakartaEe" to "true",
    "serializationLibrary" to "jackson",
    "generateSupportingFiles" to "true",
    "generateConstructorWithAllArgs" to "false",
    "generatedConstructorWithRequiredArgs" to "true",
    "additionalModelTypeAnnotations" to "@lombok.Data @lombok.Builder @lombok.AllArgsConstructor"
  ))
  library.set("resttemplate")
}

tasks.register<GenerateTask>("openApiGenerateP4PAAUTH") {
  group = "AutomaticallyGeneratedCode"
  description = "openapi"

  generatorName.set("java")
  remoteInputSpec.set("https://raw.githubusercontent.com/pagopa/p4pa-auth/refs/heads/develop/openapi/p4pa-auth.openapi.yaml")
  outputDir.set("$projectDir/build/generated")
  invokerPackage.set("it.gov.pagopa.pu.auth.generated")
  apiPackage.set("it.gov.pagopa.pu.auth.controller.generated")
  modelPackage.set("it.gov.pagopa.pu.auth.dto.generated")
  configOptions.set(mapOf(
    "swaggerAnnotations" to "false",
    "openApiNullable" to "false",
    "dateLibrary" to "java8",
    "serializableModel" to "true",
    "useSpringBoot3" to "true",
    "useJakartaEe" to "true",
    "serializationLibrary" to "jackson",
    "generateSupportingFiles" to "true",
    "generateConstructorWithAllArgs" to "false",
    "generatedConstructorWithRequiredArgs" to "true",
    "additionalModelTypeAnnotations" to "@lombok.Data @lombok.Builder @lombok.AllArgsConstructor"
  )
  )
  library.set("resttemplate")
}
