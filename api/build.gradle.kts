plugins {
    kotlin("jvm")
    kotlin("plugin.spring") version "1.9.22" // 스프링 빈 클래스에 자동으로 open을 열어주는 코틀린-스프링 플러그인
    id("org.springframework.boot") version "3.2.3"
    id("io.spring.dependency-management") version "1.1.6"
}

dependencies {
    // 🏛️ 헥사고날 의존성: Inbound Web Adapter는 Core(UseCase, Command)에 의존한다.
    implementation(project(":core"))
    implementation(project(":storage"))

    // 🌐 Web & Validation
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    // 🧪 테스트 도구
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation(kotlin("test"))
    testImplementation("io.mockk:mockk:1.13.10")
    testImplementation("com.ninja-squad:springmockk:4.0.2") // Spring 컨텍스트에서 @MockkBean을 사용하기 위한 라이브러리

    // 런타임 테스트 엔진
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}