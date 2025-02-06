import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `java-library`
    alias(libs.plugins.kotlinJvm)
    groovy
    `maven-publish`
}

group = "ru.cusxy.mgga"
version = "1.0.0"

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_1_8
    }
}

dependencies {
    implementation(libs.groovy)
    implementation(libs.junit)
    implementation(libs.robolectric)
    implementation(libs.spockframework.spockCore)
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("default") {
                from(components["java"])
            }
        }
    }
}
