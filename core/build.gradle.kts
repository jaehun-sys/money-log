plugins {
    // 부모가 정의한 코틀린 플러그인을 그대로 상속받아 '적용'만 한다. (버전 생략)
    kotlin("jvm")
}

dependencies {
    // 순수 코틀린 테스트를 위한 JUnit 5 장착
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}