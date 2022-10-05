import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

repositories {
    maven {
        name = "spigot-repo"
        url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    }
    maven {
        name = "papermc"
        url = uri("https://papermc.io/repo/repository/maven-public/")
    }
    maven {
        name = "dmulloy2-repo"
        url = uri("https://repo.dmulloy2.net/repository/public/")
    }
    maven {
        name = "placeholderapi"
        url = uri("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    }
    maven {
        name = "minecraft-libraries"
        url = uri("https://libraries.minecraft.net/")
    }
    maven {
        name = "jitpack.io"
        url = uri("https://jitpack.io/")
    }
    maven {
        name = "CodeMC"
        url = uri("https://repo.codemc.org/repository/maven-public/")
    }
}

dependencies {
    implementation("commons-collections:commons-collections:3.2.2")
    implementation("org.neosearch.stringsearcher:multiple-string-searcher:0.1.1")
    implementation("de.tr7zw:item-nbt-api:2.10.0")
    implementation("org.bstats:bstats-bukkit:3.0.0")

    compileOnly("io.papermc.paper:paper-api:1.19.2-R0.1-SNAPSHOT")
    compileOnly("org.spigotmc:spigot-api:1.19.2-R0.1-SNAPSHOT")
    compileOnly("commons-lang:commons-lang:2.6")

    compileOnly("com.comphenix.protocol:ProtocolLib:5.0.0-SNAPSHOT")
    compileOnly("me.clip:placeholderapi:2.11.1")

    compileOnly("org.apache.logging.log4j:log4j-core:2.18.0")
    compileOnly("org.apache.logging.log4j:log4j-api:2.18.0")
    compileOnly("org.jline:jline-reader:3.21.0")
    compileOnly("net.minecrell:terminalconsoleappender:1.3.0")
    compileOnly("com.mojang:brigadier:1.0.18")
}

tasks.named("shadowJar", ShadowJar::class) {
    archiveFileName.set("ProtocolStringReplacer-Bukkit-${project.version}.jar")

    minimize()
    relocate("org.apache.commons.collections", "io.github.rothes.protocolstringreplacer.lib.org.apache.commons.collections")
    relocate("org.neosearch.stringsearcher", "io.github.rothes.protocolstringreplacer.lib.org.neosearch.stringsearcher")
    relocate("org.bstats", "io.github.rothes.protocolstringreplacer.lib.org.bstats")
    relocate("de.tr7zw", "io.github.rothes.protocolstringreplacer.lib.de.tr7zw")
}

tasks.named("processResources", ProcessResources::class) {
    filesMatching("plugin.yml") {
        expand("projectVersionString" to project.version)
    }
}