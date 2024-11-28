import org.apache.tools.ant.filters.ReplaceTokens

plugins {
    kotlin("jvm") version "1.9.22"
    id("java")
    id("io.github.goooler.shadow") version "8.1.8"
    id("io.papermc.paperweight.userdev") version "1.7.5"
}

val serverVer = rootProject.property("targetMinecraftVersion").toString()

dependencies {
    implementation(project(":bukkit:nms"))
    paperweight.paperDevBundle("$serverVer-R0.1-SNAPSHOT")
    implementation("commons-collections:commons-collections:3.2.2")
    implementation("org.neosearch.stringsearcher:multiple-string-searcher:0.1.1")
    implementation("de.tr7zw:item-nbt-api:2.13.2")
    implementation("org.bstats:bstats-bukkit:3.0.2")
    compileOnly("com.comphenix.protocol:ProtocolLib:5.3.0-SNAPSHOT")
    compileOnly("me.clip:placeholderapi:2.11.6")
    compileOnly("org.apache.logging.log4j:log4j-api:2.23.1")
    compileOnly("org.apache.logging.log4j:log4j-core:2.23.1")
    compileOnly("org.jline:jline-reader:3.26.2")
    compileOnly("net.minecrell:terminalconsoleappender:1.3.0")
    compileOnly("com.mojang:brigadier:1.0.18")
    compileOnly("commons-lang:commons-lang:2.6")
}

val fileName = "${rootProject.name}-${project.name}"
base.archivesName = fileName

tasks {
    shadowJar {
        archiveFileName = "${fileName}-${project.version}-mojmap.jar"

        relocate("org.bstats", "io.github.rothes.protocolstringreplacer.lib.org.bstats")
        relocate("org.apache.commons.collections", "io.github.rothes.protocolstringreplacer.lib.org.apache.commons.collections")
        relocate("org.neosearch.stringsearcher", "io.github.rothes.protocolstringreplacer.lib.org.neosearch.stringsearcher")
        relocate("de.tr7zw.changeme.nbtapi", "io.github.rothes.protocolstringreplacer.lib.de.tr7zw.changeme.nbtapi")
        relocate("kotlin", "io.github.rothes.protocolstringreplacer.lib.kotlin")
    }

    processResources {
        filter<ReplaceTokens>(
            "tokens" to mapOf(
                "versionName" to project.property("versionName"),
                "versionChannel" to project.property("versionChannel"),
                "versionId" to project.property("versionId"),
            ))
        outputs.doNotCacheIf("MakeReplacementsWork") { true }
    }
}

tasks {
    build {
        dependsOn(reobfJar)
    }

    reobfJar {
        mustRunAfter(shadowJar)
    }
}

paperweight.reobfArtifactConfiguration = io.papermc.paperweight.userdev.ReobfArtifactConfiguration.REOBF_PRODUCTION

allprojects {
    repositories {
        mavenLocal()
        mavenCentral()

        maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://repo.dmulloy2.net/repository/public/")
        maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
        maven("https://repo.codemc.org/repository/maven-public/")
        maven("https://jitpack.io/")
        maven("https://libraries.minecraft.net/")
    }

    tasks.shadowJar {
        dependencies {
            exclude(dependency("org.jetbrains:annotations"))
        }
    }
}