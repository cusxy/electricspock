plugins {
    alias(sharedLibs.plugins.androidApplication)
    alias(sharedLibs.plugins.kotlinAndroid)
    id("ru.cusxy.mgga.groovy-android")
}

android {
    namespace = "ru.cusxy.sample.app"
    compileSdk = 33

    defaultConfig {
        applicationId = "ru.cusxy.sample.app"
        minSdk = 26
        targetSdk = 33
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
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

    implementation(sharedLibs.core.ktx)
    implementation(sharedLibs.appcompat)
    implementation(sharedLibs.material)
    implementation(sharedLibs.constraintlayout)

    testImplementation("ru.cusxy.mgga:electricspock")
    testImplementation(sharedLibs.groovy)
    testImplementation(sharedLibs.junit)
    testImplementation(sharedLibs.robolectric)
    testImplementation(sharedLibs.spockframework.spockCore)

    testImplementation(sharedLibs.hamcrest)
    testRuntimeOnly(sharedLibs.bytebuddy)
    testRuntimeOnly(sharedLibs.objenesis)

    androidTestImplementation(sharedLibs.androidx.test.ext.junit)
    androidTestImplementation(sharedLibs.espresso.core)
}