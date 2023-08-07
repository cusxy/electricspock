plugins {
    `java-library`
    `maven-publish`
    groovy
    alias(sharedLibs.plugins.kotlinJvm)
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of("11"))
    }
}

dependencies {
    implementation(sharedLibs.groovy)
    implementation(sharedLibs.junit)
    implementation(sharedLibs.robolectric)
    implementation(sharedLibs.spockframework.spockCore)
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
