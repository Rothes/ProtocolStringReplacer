plugins {
    id("io.papermc.paperweight.userdev") version "1.7.1"
}

val serverVer = rootProject.property("targetMinecraftVersion").toString()

dependencies {
    paperweight.paperDevBundle("$serverVer-R0.1-SNAPSHOT")
    subprojects.filter {
        it.parent == project
    }.forEach {
        api(project(it.path, configuration = "reobf"))
    }
}

subprojects {
    apply(plugin = "io.papermc.paperweight.userdev")
    if (this.name != "packetreader") {
        dependencies {
            compileOnly(project(":bukkit:nms:packetreader"))
        }
    }

    tasks {
        shadowJar {
            relocate(
                "io.github.rothes.protocolstringreplacer.nms.generic",
                "io.github.rothes.protocolstringreplacer.nms.${project.name.lowercase()}"
            )
        }
    }
}