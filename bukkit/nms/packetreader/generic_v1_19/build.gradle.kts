val serverVer = "1.19"

dependencies {
    paperweight.paperDevBundle("$serverVer-R0.1-SNAPSHOT")
    pluginRemapper("net.fabricmc:tiny-remapper:0.10.3:fat")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}