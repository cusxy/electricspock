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
includeBuild("plugin")
includeBuild("sample")
includeBuild("electricspock") {
    dependencySubstitution {
        substitute(module("ru.cusxy.mgga:electricspock")).using(project(":electricspock"))
    }
}
includeBuild("test-engine") {
    dependencySubstitution {
        substitute(module("ru.cusxy.mgga:test-engine")).using(project(":test-engine"))
    }
}
