import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
}

java {
    targetCompatibility = JavaVersion.VERSION_11
    sourceCompatibility = JavaVersion.VERSION_11
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_11
    }
}

dependencies {
    implementation(libs.androidBuildTools)
}

group = "ru.cusxy.mgga"
version = "1.0.0"

gradlePlugin {
    plugins {
        create("groovyAndroid") {
            id = "ru.cusxy.mgga.groovy-android-gradle-plugin"
            displayName = "groovy-android-gradle-plugin"
            implementationClass = "plugin.GroovyAndroidPlugin"
        }
    }
}
