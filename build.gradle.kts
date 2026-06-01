plugins {
    // 1. 여기서 코틀린 JVM 플러그인의 버전을 전역으로 명시한다.
    // apply false는 루트 자체에는 적용 안 하고, 자식들에게 물려주겠다는 뜻이다.
    kotlin("jvm") version "1.9.22" apply false
}

allprojects {
    group = "io.github.jaehun-sys"
    version = "0.0.1-SNAPSHOT"

    repositories {
        mavenCentral() // 2. 플러그인과 라이브러리를 다운받을 중앙 저장소 선언
    }
}