val serverVer = "1.21.5"

dependencies {
    paperweight.paperDevBundle("$serverVer-no-moonrise-SNAPSHOT")
    implementation(project(":bukkit:nms:common:generic_v1_21_3"))
}