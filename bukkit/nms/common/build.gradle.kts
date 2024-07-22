val serverVer = rootProject.property("targetMinecraftVersion").toString()

dependencies {
    paperweight.paperDevBundle("$serverVer-R0.1-SNAPSHOT")
}