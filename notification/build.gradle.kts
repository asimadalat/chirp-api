plugins {
    id("java-library")
    id("chirp.spring-boot-service")
    kotlin("plugin.jpa")
}

group = "com.asimorphic"
version = "unspecified"

repositories {
    mavenCentral()
}

dependencies {
    implementation(projects.common)

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}