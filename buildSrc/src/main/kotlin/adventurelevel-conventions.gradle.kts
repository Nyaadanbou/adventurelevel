plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    kotlin("plugin.atomicfu")
    id("com.gradleup.shadow")
}

val local = the<org.gradle.accessors.dm.LibrariesForLocal>()

group = "cc.mewcraft.adventurelevel"

repositories {
    mavenCentral()
    maven("https://oss.sonatype.org/content/groups/public/") {
        name = "sonatype"
    }
    maven("https://repo.papermc.io/repository/maven-public/") {
        name = "papermc-repo"
    }
    maven("https://repo.mewcraft.cc/releases")
    maven("https://repo.mewcraft.cc/private") {
        credentials {
            username = providers.gradleProperty("nyaadanbou.mavenUsername").orNull
            password = providers.gradleProperty("nyaadanbou.mavenPassword").orNull
        }
    }
}

java {
    withSourcesJar()
}

kotlin {
    jvmToolchain(21)
    sourceSets {
        val main by getting {
            dependencies {
                compileOnly(kotlin("stdlib"))
                compileOnly(kotlin("reflect"))
                compileOnly(local.kotlinx.coroutines.core)
            }
        }
        val test by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(local.kotlinx.coroutines.core)
            }
        }
    }
}