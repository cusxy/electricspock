plugins {
    alias(sharedLibs.plugins.androidLibrary)
    alias(sharedLibs.plugins.kotlinAndroid)
    id("ru.cusxy.mgga.groovy-android-gradle-plugin")
}

android {
    namespace = "ru.cusxy.sample.lib"
    compileSdk = 33

    defaultConfig {
        minSdk = 26
        targetSdk = 33

        testInstrumentationRunner = "android.support.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
}

dependencies {
    implementation(kotlin("reflect"))

    implementation(sharedLibs.appcompat)

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