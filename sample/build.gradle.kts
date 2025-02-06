import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
    id("ru.cusxy.mgga.groovy-android-gradle-plugin")
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_1_8
    }
}

android {
    namespace = "ru.cusxy.mgga.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "ru.cusxy.mgga.app"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            all { test ->
                test.useJUnitPlatform {
                    includeEngines("electric-spock")
                }
                test.jvmArgs(
                    "--add-opens", "java.base/java.lang=ALL-UNNAMED",
                )
            }
        }
    }
}

dependencies {
    implementation(kotlin("reflect"))

    implementation(libs.core.ktx)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)

    testImplementation("ru.cusxy.mgga:electricspock")
    testImplementation(libs.groovy)
    testImplementation(libs.junit)
    testImplementation(libs.robolectric)
    testImplementation(libs.spockframework.spockCore)

    testImplementation(libs.hamcrest)
    testRuntimeOnly(libs.bytebuddy)
    testRuntimeOnly(libs.objenesis)

    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.espresso.core)
}