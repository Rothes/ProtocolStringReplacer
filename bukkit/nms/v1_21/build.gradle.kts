val serverVer = "1.21"

dependencies {
    paperweight.paperDevBundle("$serverVer-R0.1-SNAPSHOT")
    implementation(project(":bukkit:nms:common:generic_v1_19_3"))
}