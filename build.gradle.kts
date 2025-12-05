plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.1.0"
    id("org.jetbrains.intellij.platform") version "2.7.1"
}

group = "no.ltj.intelligpt"
version = "1.0.0"

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

intellijPlatform {

    // Disse skrur du av for å gjøre pluginet raskere og lettere
    buildSearchableOptions.set(false)
    instrumentCode.set(false)

    pluginConfiguration {
        ideaVersion {
            sinceBuild = "251"   // IntelliJ 2025.1 baseline
        }

        version.set("1.0.0")

        changeNotes = """
            Initial version of LTJ IntelliGPT.
        """.trimIndent()
    }
}

dependencies {
    intellijPlatform {

        // IntelliJ Community Edition 2025.1
        intellijIdeaCommunity("2025.1")

        // Bundled Java support
        bundledPlugin("com.intellij.java")

        // Test framework (kan droppes men anbefales)
        testFramework(org.jetbrains.intellij.platform.gradle.TestFrameworkType.Platform)
    }
}

tasks.withType<JavaCompile> {
    sourceCompatibility = "21"
    targetCompatibility = "21"
    options.encoding = "UTF-8"
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
    }
}
