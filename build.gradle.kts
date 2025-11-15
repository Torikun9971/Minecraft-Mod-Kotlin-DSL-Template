plugins {
    eclipse
    idea
    `java-library`
    `maven-publish`
    alias(libs.plugins.neogradle)
}

val mcVersion = libs.versions.minecraft.get()

version = "${prop("mod_version")}+$mcVersion-${prop("mod_loader").lowercase()}"
group = prop("mod_group")

base {
    archivesName = prop("mod_filename")
}

repositories {
    mavenLocal()
}

dependencies {
    implementation(libs.neoforge)
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(prop("java_version"))
    }
}

minecraft {
    /*
    accessTransformers {
        file("src/main/resources/META-INF/accesstransformer.cfg")
    }
     */

    runs {
        configureEach {
            systemProperty("forge.logging.markers", "REGISTRIES")
            systemProperty("forge.logging.console.level", "debug")

            modSource(project.sourceSets.main.get())
        }

        create("client") {
            systemProperty("forge.enabledGameTestNamespaces", prop("mod_id"))

            if (canSpecifyUser()) {
                arguments(
                    "--username", prop("mc_username"),
                    "--uuid", prop("mc_uuid")
                )
            }
        }

        create("server") {
            systemProperty("forge.enabledGameTestNamespaces", prop("mod_id"))
            argument("--nogui")
        }

        create("gameTestServer") {
            systemProperty("forge.enabledGameTestNamespaces", prop("mod_id"))
        }

        create("data") {
            arguments(
                "--mod", prop("mod_id"),
                "--all",
                "--output", file("src/generated/resources/").absolutePath,
                "--existing", file("src/main/resources/").absolutePath
            )
        }
    }
}

sourceSets {
    main {
        resources {
            srcDir("src/generated/resources")
        }
    }
}

configurations {
    runtimeClasspath {
        extendsFrom(localRuntime.get())
    }
}

tasks.named<Wrapper>("wrapper").configure {
    distributionType = Wrapper.DistributionType.BIN
}

tasks.named<ProcessResources>("processResources") {
    val replaceProperties = mapOf(
            "minecraft_version" to mcVersion,
            "minecraft_version_range" to prop("minecraft_version_range"),
            "neoforge_version" to libs.versions.neoforge.get(),
            "neoforge_version_range" to prop("neoforge_version_range"),
            "loader_version_range" to prop("loader_version_range"),
            "mod_id" to prop("mod_id"),
            "mod_name" to prop("mod_name"),
            "mod_version" to prop("mod_version"),
            "mod_license" to prop("mod_license"),
//            "mod_issue" to prop("mod_issue"),
//            "mod_update_json" to prop("mod_update_json"),
//            "mod_homepage" to prop("mod_homepage"),
//            "mod_credits" to prop("mod_credits"),
            "mod_authors" to prop("mod_authors"),
            "mod_description" to prop("mod_description"),
    )

    inputs.properties(replaceProperties)

    filesMatching(listOf("META-INF/neoforge.mods.toml", "pack.mcmeta")) {
        expand(replaceProperties)
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}

idea {
    module {
        isDownloadSources = true
        isDownloadJavadoc = true
    }
}

publishing {
    publications {
        register<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
    repositories {
        maven {
            url = project.projectDir.resolve("repo").toURI()
        }
    }
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
