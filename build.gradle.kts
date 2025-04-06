import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.FileInputStream
import java.util.Properties

// env 디렉토리에서 환경 파일 로드
val envProperties = Properties().apply {
    val envProfile = System.getenv("SPRING_PROFILES_ACTIVE") ?: "dev"
    val envFile = rootProject.file("env/${envProfile}.env")
    if (envFile.exists()) {
        load(FileInputStream(envFile))
    } else {
        // 기본 dev.env 파일을 로드 시도
        val defaultEnvFile = rootProject.file("env/dev.env")
        if (defaultEnvFile.exists()) {
            load(FileInputStream(defaultEnvFile))
        }
    }
}

// 환경 변수에서 값을 가져오는 도우미 함수
// 시스템 환경 변수 > env 파일 > 기본값 순으로 확인
fun getConfigValue(key: String, defaultValue: String): String {
    return System.getenv(key) ?: envProperties.getProperty(key) ?: defaultValue
}

// 레지스트리 관련 속성
val registryUrl = getConfigValue("DOCKER_REGISTRY_URL", "localhost:5000")
val dbUrl = getConfigValue("DB_URL", "jdbc:postgresql://localhost:5432/postgres")
val dbUsername = getConfigValue("DB_USERNAME", "postgres")
val dbPassword = getConfigValue("DB_PASSWORD", "postgres")
val jvmXms = getConfigValue("JVM_XMS", "512m")
val jvmXmx = getConfigValue("JVM_XMX", "1g")
val jvmMaxRamPercentage = getConfigValue("JVM_MAX_RAM_PERCENTAGE", "75")

plugins {
    id("org.springframework.boot") version "3.4.3"
    id("io.spring.dependency-management") version "1.1.4"
    kotlin("jvm") version "1.9.23"
    kotlin("plugin.spring") version "1.9.23"
    kotlin("plugin.jpa") version "1.9.23"
    id("com.google.cloud.tools.jib") version "3.4.1"
}
    
group = "yousang"
version = "0.0.1-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_21
}

repositories {
    mavenCentral()
}

val exposedVersion = "0.60.0"

dependencies {
    // Spring Boot
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    
    // Kotlin
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.5.0")
    
    // Environment Configuration
    implementation("io.github.cdimascio:dotenv-kotlin:6.5.1")
    
    // Database
    implementation("org.postgresql:postgresql")
    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-kotlin-datetime:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-spring-boot-starter:$exposedVersion")
    implementation("com.zaxxer:HikariCP")

    // env
    implementation("io.github.cdimascio:dotenv-java:3.2.0")
    
    // SpringDoc OpenAPI (Swagger)
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.4.0")
    
    // Logging
    implementation("io.github.microutils:kotlin-logging-jvm:3.0.5")
    
    // HTTP Client
    implementation("org.springframework:spring-webflux")
    implementation("io.projectreactor.netty:reactor-netty")
    implementation("org.jsoup:jsoup:1.17.2")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
    implementation("org.projectlombok:lombok")
    implementation("org.springframework.boot:spring-boot-starter-webflux")

    // Testing
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.2")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.10.2")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.10.2")
    testImplementation("io.mockk:mockk:1.13.10")
    testImplementation("com.ninja-squad:springmockk:4.0.2")
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(module = "mockito-core")
    }
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.springframework.restdocs:spring-restdocs-mockmvc")
    testImplementation("io.projectreactor:reactor-test")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    runtimeOnly("com.h2database:h2")

    // AOP
    implementation("org.springframework.boot:spring-boot-starter-aop")
    implementation("org.aspectj:aspectjweaver:1.9.22")
    implementation("org.aspectj:aspectjrt:1.9.22")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs += "-Xjsr305=strict"
        jvmTarget = "21"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

// 환경 변수 정보 출력
tasks.register("printEnv") {
    doLast {
        println("========== 환경 변수 정보 ==========")
        println("Registry URL: $registryUrl")
        println("Database URL: $dbUrl")
        println("Database Username: $dbUsername")
        println("JVM Memory Settings: $jvmXms - $jvmXmx (Max RAM: $jvmMaxRamPercentage%)")
        println("===================================")
    }
}

// Jib 설정
jib {
    from {
        image = "eclipse-temurin:21-jre-alpine"
    }
    to {
        // 환경 변수에서 가져온 레지스트리 주소 사용
        image = "${registryUrl}/rest-server:${project.version}"
    }
    container {
        ports = listOf("8080")
        jvmFlags = listOf(
            "-Xms${jvmXms}", 
            "-Xmx${jvmXmx}", 
            "-XX:+UseContainerSupport", 
            "-XX:MaxRAMPercentage=${jvmMaxRamPercentage}"
        )
        creationTime = "USE_CURRENT_TIMESTAMP"
        
        // Spring 설정 환경 변수
        environment = mapOf(
            "SPRING_PROFILES_ACTIVE" to getConfigValue("SPRING_PROFILES_ACTIVE", "prod"),
            "SPRING_CONFIG_LOCATION" to "classpath:/,file:/app/config/",
            "DB_URL" to dbUrl,
            "DB_USERNAME" to dbUsername,
            "DB_PASSWORD" to dbPassword,
            "CORS_ALLOWED_ORIGINS" to getConfigValue("CORS_ALLOWED_ORIGINS", "https://example.com"),
            "LOG_LEVEL" to getConfigValue("LOG_LEVEL", "INFO"),
            "LOG_FILE_PATH" to getConfigValue("LOG_FILE_PATH", "/var/log/rest-server/application.log")
        )
    }
    
    // 리소스 복사 설정
    extraDirectories {
        paths {
            path {
                setFrom(file("src/main/resources"))
                into = "/app/config"
            }
        }
    }
}

