plugins {
    eclipse
    idea
    `java-library`
    `maven-publish`
    alias(libs.plugins.neogradle)
}

val mcVersion = libs.versions.minecraft.get()

version = "${property("mod_version")}+$mcVersion-${property("mod_loader").lowercase()}"
group = property("mod_group_id")

repositories {
    mavenLocal()
}

base {
    archivesName = "${properties["mod_file_name"]}"
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(property("java_version"))
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

            if (canSpecifyUser()) {
                arguments(
                    "--username", property("mc_username"),
                    "--uuid", property("mc_uuid")
                )
            }

            modSource(project.sourceSets.main.get())
        }

        create("client") {
            systemProperty("forge.enabledGameTestNamespaces", property("mod_id"))
        }

        create("server") {
            systemProperty("forge.enabledGameTestNamespaces", property("mod_id"))
            argument("--nogui")
        }

        create("gameTestServer") {
            systemProperty("forge.enabledGameTestNamespaces", property("mod_id"))
        }

        create("data") {
            arguments(
                "--mod", property("mod_id"),
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

dependencies {
    implementation(libs.neoforge)
}

tasks.named<Wrapper>("wrapper").configure {
    distributionType = Wrapper.DistributionType.BIN
}

tasks.named<ProcessResources>("processResources") {
    val replaceProperties = mapOf(
            "minecraft_version" to mcVersion,
            "minecraft_version_range" to properties["minecraft_version_range"],
            "neoforge_version" to libs.versions.neoforge.get(),
            "neoforge_version_range" to properties["neoforge_version_range"],
            "loader_version_range" to properties["loader_version_range"],
            "mod_id" to properties["mod_id"],
            "mod_name" to properties["mod_name"],
            "mod_version" to properties["mod_version"],
            "mod_license" to properties["mod_license"],
//            "mod_issue" to properties["mod_issue"],
//            "mod_update_json" to properties["mod_update_json"],
//            "mod_homepage" to properties["mod_homepage"],
//            "mod_icon" to properties["mod_icon"],
//            "mod_credits" to properties["mod_credits"],
            "mod_authors" to properties["mod_authors"],
            "mod_description" to properties["mod_description"],
    )

    inputs.properties(replaceProperties)

    filesMatching(listOf("META-INF/neoforge.mods.toml", "pack.mcmeta")) {
        expand(replaceProperties)
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

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}

idea {
    module {
        isDownloadSources = true
        isDownloadJavadoc = true
    }
}

fun property(key: String): String {
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
