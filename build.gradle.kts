import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "2.0.20"
    kotlin("plugin.serialization") version "2.0.20"
    id("net.neoforged.moddev") version "2.0.42-beta"
    id("maven-publish")
}

version = "${project.property("mod_version")}+${project.property("minecraft_version")}"
group = project.property("maven_group") as String

base {
    archivesName.set(project.property("archives_base_name") as String)
}

val targetJavaVersion = 21
java {
    toolchain.languageVersion = JavaLanguageVersion.of(targetJavaVersion)
    withSourcesJar()
}

// NeoForge configuration
neoForge {
    version = project.property("neoforge_version") as String
    
    // Access Transformer
    accessTransformers.from(file("src/main/resources/META-INF/accesstransformer.cfg"))
    
    runs {
        register("client") {
            client()
        }
        register("server") {
            server()
        }
    }
    
    mods {
        register("trueadaptivemusic") {
            sourceSet(sourceSets.main.get())
        }
    }
}

repositories {
    mavenCentral()
    
    // Forgified Fabric API
    maven("https://maven.su5ed.dev/releases")
    
    // Kotlin for Forge
    maven("https://thedarkcolour.github.io/KotlinForForge/")
}

dependencies {
    // Kotlin for Forge
    implementation("thedarkcolour:kotlinforforge-neoforge:${project.property("kotlin_forge_version")}")
    
    // Forgified Fabric API - provides Fabric API on NeoForge
    implementation("org.sinytra.forgified-fabric-api:forgified-fabric-api:${project.property("ffapi_version")}")
    
    // Kotlinx Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:${project.property("serialization_version")}")
}

tasks.processResources {
    inputs.property("version", project.version)
    inputs.property("minecraft_version", project.property("minecraft_version"))
    inputs.property("neoforge_version", project.property("neoforge_version"))
    filteringCharset = "UTF-8"

    filesMatching("META-INF/neoforge.mods.toml") {
        expand(
            "version" to project.version,
            "minecraft_version" to project.property("minecraft_version"),
            "neoforge_version" to project.property("neoforge_version"),
            "kotlin_forge_version" to project.property("kotlin_forge_version")
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
    from("LICENSE") {
        rename { "${it}_${project.base.archivesName}" }
    }
}

// Maven publication
publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifactId = project.property("archives_base_name") as String
            from(components["java"])
        }
    }

    repositories {
        // Add repositories to publish to here.
    }
}
