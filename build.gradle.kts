plugins {
    id("java")
}

group = "hw.okit"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.mockito:mockito-core:5.16.1")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.12.1")
}

tasks.test {
    useJUnitPlatform()
}