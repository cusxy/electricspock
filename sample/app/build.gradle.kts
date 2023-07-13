plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
    id("ru.cusxy.groovy-android")
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
//            all { test ->
//                test.useJUnitPlatform()
//            }
        }
    }
}

dependencies {
    implementation(libs.core.ktx)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)

    testImplementation(libs.junit)
    testImplementation("ru.cusxy.mgga:electricspock")
    testImplementation("org.robolectric:robolectric:4.10.3")
    testImplementation("org.codehaus.groovy:groovy:3.0.18")
    testImplementation("org.spockframework:spock-core:2.3-groovy-3.0")
    testImplementation("org.spockframework:spock-junit4:2.3-groovy-3.0")
//    testImplementation("org.junit.vintage:junit-vintage-engine:5.9.3")
    implementation("org.junit.platform:junit-platform-runner:1.9.3")

    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.espresso.core)
}