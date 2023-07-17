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
}

rootProject.name = "make-groovy-great-again"
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
