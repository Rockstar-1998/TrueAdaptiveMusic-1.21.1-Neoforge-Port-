import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "2.2.20"
    kotlin("plugin.serialization") version "2.2.20"
    id("net.neoforged.moddev") version "2.0.126"
    id("maven-publish")
}

version = "${project.property("mod_version") as String}+${project.property("minecraft_version")}"
group = project.property("mod_group_id") as String

base {
    archivesName.set(project.property("archives_base_name") as String)
}

val targetJavaVersion = 21
java {
    toolchain.languageVersion = JavaLanguageVersion.of(targetJavaVersion)
    withSourcesJar()
}

val client by sourceSets.creating {
    compileClasspath += sourceSets["main"].output + sourceSets["main"].compileClasspath
    runtimeClasspath += sourceSets["main"].output + sourceSets["main"].runtimeClasspath
}

sourceSets {
    named("main") {
        java {
            exclude("liltojustice/trueadaptivemusic/client/**")
            exclude("trueadaptivemusic/client/**")
        }
        allSource.exclude("liltojustice/trueadaptivemusic/client/**")
        allSource.exclude("trueadaptivemusic/client/**")
    }
}

kotlin {
    sourceSets {
        named("main") {
            kotlin.exclude("liltojustice/trueadaptivemusic/client/**")
        }
    }
}

configurations {
    val clientImplementation by getting {
        extendsFrom(implementation.get())
    }
    val clientCompileOnly by getting {
        extendsFrom(compileOnly.get())
    }
    val clientRuntimeOnly by getting {
        extendsFrom(runtimeOnly.get())
    }
}

neoForge {
    version = project.property("neo_version") as String

    runs {
        create("client") {
            client()
        }
    }

    mods {
        create(project.property("mod_id") as String) {
            sourceSet(sourceSets["main"])
            sourceSet(client)
        }
    }
}

repositories {
    maven("https://maven.neoforged.net/releases")
    maven {
        name = "Kotlin for Forge"
        setUrl("https://thedarkcolour.github.io/KotlinForForge/")
    }
}

dependencies {
    implementation("thedarkcolour:kotlinforforge-neoforge:${project.property("kotlinforforge_version")}")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:${project.property("serialization_version")}")
}

tasks.processResources {
    inputs.property("version", project.version)
    inputs.property("minecraft_version", project.property("minecraft_version"))
    inputs.property("minecraft_version_range", project.property("minecraft_version_range"))
    inputs.property("neo_version_range", project.property("neo_version_range"))
    inputs.property("loader_version_range", project.property("loader_version_range"))
    inputs.property("mod_id", project.property("mod_id"))
    inputs.property("mod_name", project.property("mod_name"))
    inputs.property("mod_license", project.property("mod_license"))
    inputs.property("mod_authors", project.property("mod_authors"))
    inputs.property("mod_description", project.property("mod_description"))
    filteringCharset = "UTF-8"

    filesMatching("META-INF/neoforge.mods.toml") {
        expand(
            "version" to project.version,
            "minecraft_version" to project.property("minecraft_version"),
            "minecraft_version_range" to project.property("minecraft_version_range"),
            "neo_version_range" to project.property("neo_version_range"),
            "loader_version_range" to project.property("loader_version_range"),
            "mod_id" to project.property("mod_id"),
            "mod_name" to project.property("mod_name"),
            "mod_license" to project.property("mod_license"),
            "mod_authors" to project.property("mod_authors"),
            "mod_description" to project.property("mod_description")
        )
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(targetJavaVersion)
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions.jvmTarget.set(JvmTarget.fromTarget(targetJavaVersion.toString()))
}

tasks.jar {
    dependsOn("clientClasses")
    from(client.output)
    from("LICENSE") {
        rename { "${it}_${project.base.archivesName}" }
    }
}

tasks.named<Jar>("sourcesJar") {
    from(client.allSource)
}


publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifactId = project.property("archives_base_name") as String
            from(components["java"])
        }
    }
}
