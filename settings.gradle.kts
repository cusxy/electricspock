rootProject.name = "make-groovy-great-again"

pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
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
}

includeBuild("libs-versions") {
    dependencySubstitution {
        substitute(module("ru.cusxy.mgga.internal:libs-versions")).using(project(":"))
    }
}
includeBuild("plugin")
includeBuild("sample")
includeBuild("electricspock") {
    dependencySubstitution {
        substitute(module("ru.cusxy.mgga:electricspock")).using(project(":electricspock"))
    }
}
