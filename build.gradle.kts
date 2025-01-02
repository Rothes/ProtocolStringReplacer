plugins {
    kotlin("jvm") version "1.9.22"
    id("java")
    id("io.github.goooler.shadow") version "8.1.8"
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.11" apply false
}

repositories {
    mavenCentral()
}

group = "io.github.rothes"
version = rootProject.property("versionName").toString()

tasks.register<Copy>("createJars") {
    from(project(":bukkit").tasks.named("reobfJar"))
    into("$buildDir/allJars")
}


allprojects {
    apply(plugin = "java")
    apply(plugin = "kotlin")
    apply(plugin = "io.github.goooler.shadow")

    group = "io.github.rothes.protocolstringreplacer"
    version = rootProject.property("versionName").toString()

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

    tasks.compileKotlin {
        kotlinOptions {
            jvmTarget = javaVer.toString()
        }
    }

    tasks.javadoc {
        options.encoding = "UTF-8"
    }
}