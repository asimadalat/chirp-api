plugins {
    id("java-library")
    id("chirp.kotlin-common")
    id("org.springframework.boot")
}

group = "com.asimorphic"
version = "unspecified"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}