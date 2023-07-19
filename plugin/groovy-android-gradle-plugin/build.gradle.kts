plugins {
    `java-gradle-plugin`
    `maven-publish`
    alias(sharedLibs.plugins.gradlePublish)
    alias(sharedLibs.plugins.kotlinJvm)
}

dependencies {
    implementation(sharedLibs.androidBuildTools)
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
