pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
    versionCatalogs {
        create("sharedLibs") {
            from("ru.cusxy.mgga.internal:libs-versions")
        }
    }
}

rootProject.name = "sample"
include(":app")
include(":lib")
