val serverVer = "1.21.1"

dependencies {
    paperweight.paperDevBundle("$serverVer-R0.1-SNAPSHOT")
    implementation(project(":bukkit:nms:common:generic_v1_21"))
}