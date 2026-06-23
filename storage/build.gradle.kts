plugins {
    // 1. 부모의 코틀린 플러그인 상속
    kotlin("jvm")

    // 2. 스프링/JPA용 코틀린 플러그인은 부모 버전에 맞춰 명시해 줌
    kotlin("plugin.spring") version "1.9.22"
    kotlin("plugin.jpa") version "1.9.22"

    // 3. 💥 [독자 노선] 스프링 부트와 의존성 관리 버전을 여기에 직접 박아버린다!
    id("org.springframework.boot") version "3.2.4"
    id("io.spring.dependency-management") version "1.1.5"
}

dependencies {
    // 💡 가장 중요! 바깥 세상(storage)은 무균실(core)을 반드시 의존해야 한다! (그래야 Port를 구현함)
    implementation(project(":core"))

    // [Spring Boot & JPA] DB와 소통하기 위한 기술들
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")

    // [Database Driver] 일단 가볍게 메모리 DB(H2)로 시작해서 나중에 DB로 갈아끼운다
    runtimeOnly("com.h2database:h2")

    // 테스트 도구
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

// 💡 Storage 모듈은 혼자 톰캣 띄우고 실행되는 놈(Application)이 아니라 '부품'이다.
// 따라서 실행 가능한 BootJar를 만들지 말고, 일반 순수 Jar로 빌드하라고 명시해야 한다. (멀티모듈 필수 설정)
tasks.bootJar { enabled = false }
tasks.jar { enabled = true }