import jdk.internal.jshell.tool.resources.version

plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.spring) apply false
    alias(libs.plugins.spring.boot) apply false
    alias(libs.plugins.spring.dependency.management) apply false
    alias(libs.plugins.kotlin.jpa) apply false
}

group = "com.asimorphic"
version = "0.0.1-MILESTONE"

subprojects {
    group = rootProject.group
    version = rootProject.version
}