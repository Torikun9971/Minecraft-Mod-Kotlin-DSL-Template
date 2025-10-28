import java.text.SimpleDateFormat
import java.util.Date

plugins {
    eclipse
    idea
    `maven-publish`
    alias(libs.plugins.forgegradle)
    alias(libs.plugins.parchment.forgegradle)
    alias(libs.plugins.mixin)
}

val mcVersion = libs.versions.minecraft.get()

version = "${prop("mod_version")}+$mcVersion-${prop("mod_loader").lowercase()}"
group = prop("mod_group_id")

base {
    archivesName = prop("mod_file_name")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(prop("java_version"))
    }
}

minecraft {
    mappings(
        prop("mapping_channel"),
        prop("mapping_version")
    )
//    accessTransformer(file("src/main/resources/META-INF/accesstransformer.cfg"))

    copyIdeResources = true

    runs {
        configureEach {
            workingDirectory = project.file("run").path

            property("forge.logging.markers", "REGISTRIES")
            property("forge.logging.console.level", "debug")

            mods {
                create(prop("mod_id")) {
                    source(sourceSets.main.get())
                }
            }
        }

        create("client") {
            property("forge.enabledGameTestNamespaces", prop("mod_id"))

            if (canSpecifyUser()) {
                args(
                    "--username", prop("mc_username"),
                    "--uuid", prop("mc_uuid")
                )
            }
        }

        create("server") {
            property("forge.enabledGameTestNamespaces", prop("mod_id"))
            args("--nogui")
        }

        create("gameTestServer") {
            property("forge.enabledGameTestNamespaces", prop("mod_id"))
        }

        create("data") {
            workingDirectory = project.file("run-data").path

            args(
                "--mod", prop("mod_id"),
                "--all",
                "--output", file("src/generated/resources/"),
                "--existing", file("src/main/resources/")
            )
        }
    }
}

/**
mixin {
    add(sourceSets.main.get(), "${prop("mod_id")}.refmap.json")

    config("${prop("mod_id")}.mixins.json")
}
**/

sourceSets {
    main {
        resources {
            srcDir("src/generated/resources")
        }
    }
}

repositories {

}

dependencies {
    minecraft(libs.forge)
    annotationProcessor(libs.mixin.classifier("processor"))


}

tasks.named<ProcessResources>("processResources") {
    val replaceProperties: Map<String, String> = mapOf(
        "minecraft_version" to mcVersion,
        "minecraft_version_range" to prop("minecraft_version_range"),
        "forge_version" to libs.versions.forge.asProvider().get(),
        "forge_version_range" to prop("forge_version_range"),
        "loader_version_range" to prop("loader_version_range"),
        "mod_id" to prop("mod_id"),
        "mod_name" to prop("mod_name"),
        "mod_version" to prop("mod_version"),
        "mod_license" to prop("mod_license"),
        "mod_issue" to prop("mod_issue"),
        "mod_update_json" to prop("mod_update_json"),
        "mod_homepage" to prop("mod_homepage"),
        "mod_icon" to prop("mod_icon"),
        "mod_credits" to prop("mod_credits"),
        "mod_authors" to prop("mod_authors"),
        "mod_description" to prop("mod_description"),
    )

    inputs.properties(replaceProperties)

    filesMatching(listOf("META-INF/mods.toml", "pack.mcmeta")) {
        expand(replaceProperties + mapOf("project" to project))
    }
}

tasks.named<Jar>("jar") {
    manifest {
        attributes(
            "Specification-Title" to prop("mod_name"),
            "Specification-Vendor" to prop("mod_authors"),
            "Specification-Version" to "1",
            "Implementation-Title" to project.name,
            "Implementation-Version" to project.version,
            "Implementation-Vendor" to prop("mod_authors"),
            "Implementation-Timestamp" to SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(Date())
        )
    }

    finalizedBy("reobfJar")
}

publishing {
    publications {
        register<MavenPublication>("mavenJava") {
            artifact(tasks.jar)
        }
    }
    repositories {
        maven {
            url = uri("file://${project.projectDir}/mcmodsrepo")
        }
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}

fun prop(key: String): String {
    return properties[key].toString()
}

fun extra(key: String): String {
    return extra[key].toString()
}

fun Provider<MinimalExternalModuleDependency>.classifier(name: String): String {
    return "${this.get()}:$name"
}

fun canSpecifyUser(): Boolean {
    return hasProperty("mc_username") && hasProperty("mc_uuid")
}