plugins {
    java
}

group = "com.example.aioeconomy"
version = "0.1.0-prototype"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    // The source uses Paper's public API; it does not need server internals or remapping.
    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

tasks.jar {
    archiveBaseName.set("AIOEconomy")
}
