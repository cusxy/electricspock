plugins {
    `java-gradle-plugin`
    alias(sharedLibs.plugins.kotlinJvm)
}

dependencies {
    implementation(sharedLibs.androidBuildTools)
}

group = "ru.cusxy.mgga"
version = "1.0.0"

gradlePlugin {
    plugins {
        create("agp") {
            id = "ru.cusxy.mgga.groovy-android"
            implementationClass = "plugin.GroovyAndroidPlugin"
        }
    }
}
