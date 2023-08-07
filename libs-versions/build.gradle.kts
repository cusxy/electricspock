plugins {
    `version-catalog`
    `maven-publish`
}

copy {
    from("gradle/libs.versions.toml")
    into(layout.buildDirectory.file("version-catalog"))
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
