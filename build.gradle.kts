plugins {
    java
    id("org.springframework.boot") version "3.3.5"
    id("io.spring.dependency-management") version "1.1.6"
}

group = "com.example.booking"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {

    /* =========================
     * Spring Boot Starters
     * ========================= */
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    /* =========================
     * Database
     * ========================= */
    implementation("org.postgresql:postgresql")
    implementation("org.liquibase:liquibase-core")

    /* =========================
     * Cache (Redis)
     * ========================= */
    implementation("org.springframework.boot:spring-boot-starter-data-redis")

    /* =========================
     * OpenAPI / Swagger
     * ========================= */
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.5.0")

    /* =========================
     * Utilities
     * ========================= */
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    /* =========================
     * Testing
     * ========================= */
    testImplementation("org.springframework.boot:spring-boot-starter-test")

    // Testcontainers
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:postgresql")
    testImplementation("org.testcontainers:redis")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

springBoot {
    buildInfo()
}
