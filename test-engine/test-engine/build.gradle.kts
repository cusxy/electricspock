plugins {
    `java-library`
    `maven-publish`
    id("org.jetbrains.kotlin.jvm") version "1.8.22"
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("default") {
                from(components["java"])
                groupId = "ru.cusxy.mgga"
                artifactId = "test-engine"
                version = "1.0.0"
            }
        }
    }
}
