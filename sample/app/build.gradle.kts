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
                    excludeEngines("spock")
                }
                test.jvmArgs(
//                    "--illegal-access=debug",
//                    "--add-exports", "java.base/java.lang=ALL-UNNAMED",
                    "--add-opens", "java.base/java.lang=ALL-UNNAMED",
//                    "--add-opens", "java.base/java.time=ALL-UNNAMED",
//                    "--add-opens", "java.base/java.lang.reflect=ALL-UNNAMED"
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

    testImplementation(libs.junit)
//    testImplementation("ru.cusxy.mgga:electricspock")
    implementation("org.robolectric:robolectric:4.10.3")
    testImplementation("org.codehaus.groovy:groovy:3.0.18")
    testImplementation("org.spockframework:spock-core:2.3-groovy-3.0")
//    testImplementation("org.spockframework:spock-junit4:2.3-groovy-3.0")
//    testImplementation("org.junit.platform:junit-platform-runner:1.9.3")
//    testImplementation("org.junit.vintage:junit-vintage-engine:5.9.3")

//    testImplementation("org.robolectric:android-all:10-robolectric-5803371")
//    testImplementation("org.robolectric:android-all-instrumented:10-robolectric-5803371-i1")

//    testImplementation("io.kotest:kotest-framework-api:5.6.2")
//    testImplementation("io.kotest:kotest-runner-junit5:5.6.2")

    testImplementation("org.hamcrest:hamcrest-core:2.2")
    testRuntimeOnly("net.bytebuddy:byte-buddy:1.12.17")
    testRuntimeOnly("org.objenesis:objenesis:3.3")

    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.espresso.core)
}