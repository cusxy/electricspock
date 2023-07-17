plugins {
    `version-catalog`
    `maven-publish`
}

catalog {
    versionCatalog {
        from(files("gradle/libs.versions.toml"))
    }
}

publishing {
    publications {
        create<MavenPublication>("default") {
            from(components["versionCatalog"])
            groupId = "ru.cusxy.mgga.internal"
            artifactId = "libs-versions"
            version = "1.0.0"
        }
    }
}
