plugins {
    kotlin("jvm") version "1.9.22"
    id("java")
    id("io.github.goooler.shadow") version "8.1.8"
    id("io.papermc.paperweight.userdev")
}

val serverVer = rootProject.property("targetMinecraftVersion").toString()

dependencies {
    paperweight.paperDevBundle("$serverVer-R0.1-SNAPSHOT")
    subprojects.filter {
        it.parent == project
    }.forEach {
        api(project(it.path, configuration = if (it.hasProperty("no-reobf")) "shadow" else "reobf"))
    }
}

subprojects {
    apply(plugin = "io.papermc.paperweight.userdev")
    if (this.name != "common") {
        dependencies {
            compileOnly(project(":bukkit:nms:common"))
        }
    }

    tasks.shadowJar {
        relocate(
            "io.github.rothes.protocolstringreplacer.nms.generic",
            "io.github.rothes.protocolstringreplacer.nms.${project.name}"
        )
    }
}

allprojects {
    tasks {
        build {
            dependsOn(reobfJar)
        }

        reobfJar {
            mustRunAfter(shadowJar)
        }
    }

    paperweight.reobfArtifactConfiguration = io.papermc.paperweight.userdev.ReobfArtifactConfiguration.REOBF_PRODUCTION
}