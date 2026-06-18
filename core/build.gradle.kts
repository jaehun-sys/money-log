plugins {
    // 부모가 정의한 코틀린 플러그인을 그대로 상속받아 '적용'만 한다. (버전 생략)
    kotlin("jvm")
}

dependencies {
    // 순수 코틀린 테스트를 위한 JUnit 5 장착
    testImplementation(kotlin("test"))

    testImplementation("io.mockk:mockk:1.13.10")     // 👈 가짜 문(Mock Port)을 만들기 위해 필수
    testImplementation("org.assertj:assertj-core:3.25.3") // 👈 assertThat 문법을 쓰기 위해 필수

    // 🔥 [런타임 발사대 장착]
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.2")
}

tasks.test {
    useJUnitPlatform()
}