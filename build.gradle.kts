plugins {
    kotlin("jvm") version "2.3.0"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(25)
}

tasks.test {
    useJUnitPlatform()
}

tasks.register<JavaExec>("runBufferUiDemo") {
    group = "application"
    description = "Runs the Swing UI demo for TerminalBuffer"
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set("terminalbuffer.demo.BufferUiDemoKt")
}