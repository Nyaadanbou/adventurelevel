@file:Suppress("UnstableApiUsage")

dependencyResolutionManagement {
    repositories {
        maven("https://repo.mewcraft.cc/releases")
        maven("https://repo.mewcraft.cc/private") {
            credentials {
                username = providers.gradleProperty("nyaadanbouPrivateUsername").orNull
                password = providers.gradleProperty("nyaadanbouPrivatePassword").orNull
            }
        }
    }
    versionCatalogs {
        create("local") {
            from(files("../gradle/local.versions.toml"))
        }
    }
}