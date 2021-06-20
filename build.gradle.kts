plugins {
    java
    kotlin("jvm") version "1.4.32"
    kotlin("plugin.serialization") version "1.5.0"
}

group = "com.xuranus"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.2.1")
    implementation("org.ansj:ansj_seg:5.1.6")

}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}