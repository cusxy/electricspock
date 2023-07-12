plugins {
    `java-gradle-plugin`
    id("org.jetbrains.kotlin.jvm") version "1.8.22"
}

gradlePlugin {
    plugins {
        create("agp") {
            id = "ru.cusxy.gap"
            implementationClass = "plugin.GroovyAndroidPlugin"
        }
    }
}
