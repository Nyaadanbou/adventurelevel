plugins {
    id("nyaadanbou-conventions.repositories")
    id("adventurelevel-conventions")
    `maven-publish`
}

description = "The API of AdventureLevel"

dependencies {
    compileOnly(local.guice)
    compileOnly(local.paper)
    compileOnly(local.helper)
}

publishing {
    repositories {
        maven("https://repo.mewcraft.cc/private") {
            credentials {
                username = providers.gradleProperty("nyaadanbou.mavenUsername").orNull
                password = providers.gradleProperty("nyaadanbou.mavenPassword").orNull
            }
        }
    }
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}