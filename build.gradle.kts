import org.apache.tools.ant.filters.ReplaceTokens

plugins {
    id("java")
    id("io.github.goooler.shadow") version "8.1.8"
    id("io.papermc.paperweight.userdev") version "1.7.1"
}

group = "io.github.rothes"
version = rootProject.property("versionName").toString()

val serverVer = "1.21"

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

dependencies {
    paperweight.paperDevBundle("$serverVer-R0.1-SNAPSHOT")
    implementation("commons-collections:commons-collections:3.2.2")
    implementation("org.neosearch.stringsearcher:multiple-string-searcher:0.1.1")
    implementation("de.tr7zw:item-nbt-api:2.13.1")
    implementation("org.bstats:bstats-bukkit:3.0.2")
    compileOnly("com.comphenix.protocol:ProtocolLib:5.3.0-SNAPSHOT")
    compileOnly("me.clip:placeholderapi:2.11.4")
    compileOnly("org.apache.logging.log4j:log4j-api:2.23.1")
    compileOnly("org.apache.logging.log4j:log4j-core:2.23.1")
    compileOnly("org.jline:jline-reader:3.26.2")
    compileOnly("net.minecrell:terminalconsoleappender:1.3.0")
    compileOnly("com.mojang:brigadier:1.0.18")
    compileOnly("commons-lang:commons-lang:2.6")
}

val javaVer = JavaVersion.VERSION_1_8

java {
    disableAutoTargetJvm()
    sourceCompatibility = javaVer
    targetCompatibility = javaVer
    withSourcesJar()
    withJavadocJar()
}

tasks.compileJava {
    options.encoding = "UTF-8"
    sourceCompatibility = javaVer.toString()
    targetCompatibility = javaVer.toString()
}

tasks.javadoc {
    options.encoding = "UTF-8"
}

tasks.processResources {
    filter<ReplaceTokens>(
        "tokens" to mapOf(
            "versionName" to project.property("versionName"),
            "versionChannel" to project.property("versionChannel"),
            "versionId" to project.property("versionId"),
        ))
    outputs.cacheIf { false } // Disable cache as it breaks replacements
}

base.archivesName = project.name
tasks.shadowJar {
    archiveFileName = "${project.name}-${project.version}-mojmap.jar"

    relocate("org.bstats", "me.rothes.protocolstringreplacer.lib.org.bstats")
    relocate("org.apache.commons.collections", "me.rothes.protocolstringreplacer.lib.org.apache.commons.collections")
    relocate("org.neosearch.stringsearcher", "me.rothes.protocolstringreplacer.lib.org.neosearch.stringsearcher")
    relocate("de.tr7zw.changeme.nbtapi", "me.rothes.protocolstringreplacer.lib.de.tr7zw.changeme.nbtapi")
}