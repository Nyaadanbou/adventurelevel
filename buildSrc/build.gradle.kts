plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
    maven("https://repo.mewcraft.cc/releases")
    maven("https://repo.mewcraft.cc/private") {
        credentials {
            username = providers.gradleProperty("nyaadanbou.mavenUsername").orNull
            password = providers.gradleProperty("nyaadanbou.mavenPassword").orNull
        }
    }
}

dependencies {
    implementation(local.plugin.shadow)
    implementation(local.plugin.kotlin.jvm)
    implementation(local.plugin.kotlin.serialization)
    implementation(local.plugin.kotlin.atomicfu)
    implementation(local.plugin.nyaadanbou.conventions)
}

dependencies {
    implementation(files(local.javaClass.superclass.protectionDomain.codeSource.location))
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}