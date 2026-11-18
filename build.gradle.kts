plugins {
    java
    id("org.springframework.boot") version "4.1.2"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "com"
version = "0.0.1-SNAPSHOT"
description = "POSTpractice3"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-webmvc")          // 웹(API) 서버
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")        // [002 추가] JPA: 자바 객체 ↔ DB 테이블
    implementation("org.springframework.boot:spring-boot-h2console")               // [002 추가] H2 웹 콘솔 (브라우저로 DB 보기)
    implementation("org.springframework.boot:spring-boot-starter-validation")      // [010 추가] 입력값 검증 (@NotBlank, @Size, @Valid)
    compileOnly("org.projectlombok:lombok")                                        // @Getter 등 반복 코드 자동 생성
    developmentOnly("org.springframework.boot:spring-boot-devtools")               // 코드 수정 시 자동 재시작
    runtimeOnly("com.h2database:h2")                                               // [002 추가] H2 데이터베이스 본체
    annotationProcessor("org.projectlombok:lombok")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test") // JUnit5, AssertJ, MockMvc
    testCompileOnly("org.projectlombok:lombok")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testAnnotationProcessor("org.projectlombok:lombok")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
