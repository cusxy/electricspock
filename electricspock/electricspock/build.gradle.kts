plugins {
    `java-library`
    `maven-publish`
    groovy
    id("org.jetbrains.kotlin.jvm") version "1.8.22"
}

dependencies {
    implementation("junit:junit:4.13.2")
    implementation("org.robolectric:robolectric:4.10.3")
    implementation("org.codehaus.groovy:groovy:3.0.18")
    implementation("org.spockframework:spock-core:2.3-groovy-3.0")
    implementation("org.spockframework:spock-junit4:2.3-groovy-3.0")
    implementation("org.junit.platform:junit-platform-runner:1.9.3")
    implementation("org.junit.vintage:junit-vintage-engine:5.9.3")
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("default") {
                from(components["java"])
                groupId = "ru.cusxy.mgga"
                artifactId = "electricspock"
                version = "1.0.0"
            }
        }
    }
}
