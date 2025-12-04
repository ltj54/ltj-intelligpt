plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.1.0"
    id("org.jetbrains.intellij.platform") version "2.7.1"
}

group = "no.ltj.intelligpt"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()

    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        // IntelliJ Community Edition – riktig måte
        create("IC", "2025.1")

        // Kun Java-plugin (bundled)
        bundledPlugin("com.intellij.java")

        // Test-framework
        testFramework(org.jetbrains.intellij.platform.gradle.TestFrameworkType.Platform)
    }
}

intellijPlatform {
    pluginConfiguration {
        ideaVersion {
            // since-build for 2025.1
            // 251 = 2025.1 baseline
            sinceBuild = "251"
        }

        version.set("1.0.0")

        changeNotes = """
            Initial LTJ IntelliGPT version.
        """.trimIndent()
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
