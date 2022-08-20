import org.jetbrains.compose.compose

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.6.10"
    id("org.jetbrains.intellij") version "1.8.0"
    id("org.jetbrains.compose") version "1.1.0"
}

group = "com.gelonggld"
version = "1.0-SNAPSHOT"


repositories {
    maven(url ="https://maven.aliyun.com/nexus/content/groups/public/")
    mavenCentral()
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation("mysql:mysql-connector-java:8.0.11")
    implementation("org.mariadb.jdbc:mariadb-java-client:2.2.5")
    implementation(compose.desktop.currentOs)
}

intellij {
    version.set("222.3345.118")
    type.set("IC") // Target IDE Platform
    plugins.set(listOf("com.intellij.java","org.jetbrains.kotlin"))
}

tasks {

    withType<JavaCompile> {
        sourceCompatibility = "11"
        targetCompatibility = "11"
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "11"
    }

    patchPluginXml {
        sinceBuild.set("222.3345.118")
        untilBuild.set("2022.*")
    }

    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        token.set(System.getenv("PUBLISH_TOKEN"))
    }

}