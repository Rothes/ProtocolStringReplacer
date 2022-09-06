plugins {
    kotlin("jvm") version "1.7.10"
    java
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

allprojects {
    apply(plugin = "java-library")
    apply(plugin = "kotlin-platform-jvm")
    apply(plugin = "com.github.johnrengelman.shadow")

    group = "io.github.rothes"
    version = "3.0.0-SNAPSHOT"

    repositories {
        mavenLocal()
        mavenCentral()
    }

    dependencies {
        testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.0")
        testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.0")
    }

    tasks.getByName<JavaCompile>("compileJava") {
        options.encoding = "UTF-8"
        sourceCompatibility = JavaVersion.VERSION_1_8.toString()
        targetCompatibility = JavaVersion.VERSION_1_8.toString()
    }

    tasks.getByName<Test>("test") {
        useJUnitPlatform()
    }

    tasks.named("shadowJar", com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar::class) {
        archiveBaseName.set("ProtocolStringReplacer")
        minimize()

        relocate("kotlin", "io.github.rothes.protocolstringreplacer.lib.kotlin")
        relocate("org.jetbrains", "io.github.rothes.protocolstringreplacer.lib.org.jetbrains")
        relocate("org.intellij", "io.github.rothes.protocolstringreplacer.lib.org.intellij")
    }

}