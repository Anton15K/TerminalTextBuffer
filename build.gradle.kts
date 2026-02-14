plugins {
    kotlin("jvm") version "2.3.0"
    jacoco
    id("me.champeau.jmh") version "0.7.3"
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
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}

jmh {
    fork.set(1)
    warmupIterations.set(3)
    iterations.set(5)
}

tasks.register<JavaExec>("runBufferUiDemo") {
    group = "application"
    description = "Runs the Swing UI demo for TerminalBuffer"
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set("terminalbuffer.demo.BufferUiDemoKt")
}