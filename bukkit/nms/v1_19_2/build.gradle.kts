val serverVer = "1.19.1"

dependencies {
    paperweight.paperDevBundle("$serverVer-R0.1-SNAPSHOT")
    pluginRemapper("net.fabricmc:tiny-remapper:0.10.3:fat")
    implementation(project(":bukkit:nms:common:generic_v1_19"))
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}
