plugins {
    id("java")
    id("jacoco")
}

group = "hw.okit"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}



//dependencies {
////    testImplementation(platform("org.junit:junit-bom:5.10.0"))
////    testImplementation("org.junit.jupiter:junit-jupiter"
//    testImplementation("org.junit.jupiter:junit-jupiter-api:5.12.1")
//}
dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.mockito:mockito-core:5.16.1")
    testImplementation("org.mockito:mockito-junit-jupiter:5.16.1")
}

tasks.test {
    useJUnitPlatform()
}

jacoco {
    toolVersion = "0.8.12"
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        html.required.set(true)
        xml.required.set(false)
        csv.required.set(false)
    }
}