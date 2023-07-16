plugins {
    `java-gradle-plugin`
    id("org.jetbrains.kotlin.jvm") version "1.8.22"
}

dependencies {
    implementation("com.android.tools.build:gradle:7.4.2")
}

gradlePlugin {
    plugins {
        create("agp") {
            id = "ru.cusxy.groovy-android"
            implementationClass = "plugin.GroovyAndroidPlugin"
        }
    }
}
