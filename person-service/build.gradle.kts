import org.gradle.api.internal.lambdas.SerializableLambdas.spec
import org.openapitools.generator.gradle.plugin.tasks.GenerateTask

val versions = mapOf(
    "mapstruct" to "1.5.5.Final",
    "springDocOpenApiStarterWebMvcUi" to "2.5.0",
    "javaxAnnotationApi" to "1.3.2",
    "javaxValidationApi" to "2.0.0.Final",
    "comGoogleCodeFindBugs" to "3.0.2",
    "springCloudOpenfeignStarter" to "4.1.1",
    "logbackClassic" to "1.5.18",
    "hibernateEnvers" to "6.4.4.Final",
    "testContainers" to "1.19.3",
    "junitJupiter" to "5.12.2",
    "feignMicrometer" to "13.6",
    "springCloud" to "2025.0.0",
    "opentelemetryInstrumentation" to "2.15.0"
)

plugins {
    idea
    java
    id("org.springframework.boot") version "3.5.6"
    id("io.spring.dependency-management") version "1.1.7"
    id("maven-publish")
    id("org.openapi.generator") version "7.13.0"
}

group = "com.evilcorp"
version = "1.0.0-SNAPSHOT"
description = "Person domain service for microservices demo project"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(24)
    }
}

repositories {
    mavenCentral()
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:${versions["springCloud"]}")
        mavenBom("io.opentelemetry.instrumentation:opentelemetry-instrumentation-bom:${versions["opentelemetryInstrumentation"]}")
    }
}

// Отключает кэш для changing-зависимостей.
// Это удобно для SNAPSHOT, но замедляет сборку.
// Использовать осознанно.
configurations.all {
    resolutionStrategy.cacheChangingModulesFor(0, "seconds")
}

dependencies {
    // Spring
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-aop")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.cloud:spring-cloud-starter-openfeign:${versions["springCloudOpenfeignStarter"]}")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:${versions["springDocOpenApiStarterWebMvcUi"]}")

    // Persistence
    implementation("org.postgresql:postgresql")
    implementation("org.hibernate.orm:hibernate-envers:${versions["hibernateEnvers"]}")
    implementation("org.flywaydb:flyway-database-postgresql")

    // Observability
    implementation("io.opentelemetry.instrumentation:opentelemetry-spring-boot-starter")
//    implementation("io.opentelemetry.instrumentation:opentelemetry-instrumentation-api:${versions["opentelemetryInstrumentation"]}")
    implementation("io.github.openfeign:feign-micrometer:${versions["feignMicrometer"]}")
    implementation("io.opentelemetry:opentelemetry-exporter-otlp")
    implementation("io.micrometer:micrometer-observation")
    implementation("io.micrometer:micrometer-tracing")
    implementation("io.micrometer:micrometer-tracing-bridge-otel")
    runtimeOnly("io.micrometer:micrometer-registry-prometheus")
    implementation("ch.qos.logback:logback-classic:${versions["logbackClassic"]}")

    // Helpers
    implementation("javax.validation:validation-api:${versions["javaxValidationApi"]}")
    implementation("javax.annotation:javax.annotation-api:${versions["javaxAnnotationApi"]}")
    compileOnly("com.google.code.findbugs:jsr305:${versions["comGoogleCodeFindBugs"]}")
    compileOnly("org.projectlombok:lombok")
    compileOnly("org.mapstruct:mapstruct:${versions["mapstruct"]}")
    annotationProcessor("org.projectlombok:lombok")
    annotationProcessor("org.mapstruct:mapstruct-processor:${versions["mapstruct"]}")

    // Tests
    testCompileOnly("org.projectlombok:lombok")
    testAnnotationProcessor("org.projectlombok:lombok")
    testImplementation("org.junit.jupiter:junit-jupiter-api:${versions["junitJupiter"]}")
    testImplementation("org.testcontainers:postgresql:${versions["testContainers"]}")
    testImplementation("org.testcontainers:testcontainers:${versions["testContainers"]}")
    testImplementation("org.testcontainers:junit-jupiter:${versions["testContainers"]}")


    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

/*
──────────────────────────────────────────────────────
============== Api generation ==============
──────────────────────────────────────────────────────
*/

// OpenAPI: автогенерация по всем спецификациям
// Поиск спецификаций в 'openapi/*.yaml |yml*, логируется список.
// Для каждой спецификации регистрируется задача 'GenerateTask*:
val openApiDir = file("$rootDir/openapi")
val foundSpecifications = openApiDir.listFiles { f -> f.extension in listOf("yaml", "yml") }
logger.lifecycle("Found ${foundSpecifications.size} specification" + (if (foundSpecifications.isEmpty()) "" else ": ${foundSpecifications.joinToString { it.name }}"))

foundSpecifications.forEach { spec ->

    val dir = getAbsolutePath(spec.nameWithoutExtension)
    val packageName = defineJavaPackageName(spec.nameWithoutExtension)

    val taskName = buildGenerateApiTaskName(spec.nameWithoutExtension)
    logger.lifecycle("Registering $taskName from dir: ${dir.get()}")

    val basePackage = "com.evilcorp.$packageName"

    tasks.register(taskName, GenerateTask::class.java) {
        // Генератор: '"spring" с библиотекой "spring-cloud" * -> Feign-клиенты + модели.
        // Пакеты: 'api, dto, config внутри org.evilcorp.<имя пакета>.
        generatorName.set("spring")
        inputSpec.set(spec.absolutePath)
        outputDir.set(dir)

        // generateAllOpenApi* зависит от всех generate** задач.
        // Компиляция Java зависит от generateAllOpenApi,
        // добавляются сгенерированные 'srcDir в 'sourceSets.main*.
        configOptions.set(
            mapOf(
                "library" to "spring-cloud",
                "skipDefaultInterface" to "true",
                "useBeanValidation" to "true",
                "openApiNullable" to "false",
                "useFeignClientUrl" to "true",
                "useTags" to "true",
                "apiPackage" to "$basePackage.api",
                "modelPackage" to "$basePackage.dto",
                "configPackage" to "$basePackage.config",
            )
        )

        doFirst {
            logger.lifecycle("$taskName starting generation from ${spec.name}")
        }
    }

}

fun getAbsolutePath(filenameWithoutExtension: String): Provider<String> {
    return layout.buildDirectory
        .dir("generated-sources/openapi/${filenameWithoutExtension}")
        .map { it -> it.asFile.absolutePath }
}

fun defineJavaPackageName(name: String): String {
    val beforeDash = name.substringBefore('-')
    val match = Regex("^[a-z]+]").find(beforeDash)
    return match?.value ?: beforeDash.lowercase()
}

fun buildGenerateApiTaskName(name: String): String {
    val res = buildTaskName("generate", name)
    return res
}

fun buildJarTaskName(name: String): String {
    return buildTaskName("jar", name)
}

fun buildTaskName(taskPrefix: String, name: String): String {
    val res = name.split(Regex("[^A-Za-z0-9]"))
        .filter { it.isNotBlank() }
        .joinToString("") { it.replaceFirstChar(Char::uppercaseChar) }

    return "${taskPrefix}-${res}"
}

val namesWithoutExtension = foundSpecifications.map { it.nameWithoutExtension }

sourceSets.named("main") {
    namesWithoutExtension.forEach { name ->
        java.srcDir(layout.buildDirectory.dir("generated-sources/openapi/${name}/src/main/java"))
    }
}

tasks.register("generateAllOpenApi") {
    foundSpecifications.forEach { spec ->
        dependsOn(buildGenerateApiTaskName(spec.nameWithoutExtension))
    }
    doLast {
        logger.lifecycle("generateAllOpenApi: all specifications have been generated")
    }
}

tasks.named("compileJava") {
    dependsOn(tasks.named("generateAllOpenApi"))
}

/*
──────────────────────────────────────────────────────
============== Building jars ==============
──────────────────────────────────────────────────────
*/

// Многоартефактная сборка (по спецификациям)
// Для каждой спецификации:
// Создаётся отдельный 'SourceSet' -> собственная компиляция ('compile<Spec>Java *).
// Создаётся отдельная 'Jar' задача -> артефакт <spec>.jar в 'build/libs
// Задумка: собрать отдельные JAR'ы сгенерированного клиента/моделей для каждой спецификации.