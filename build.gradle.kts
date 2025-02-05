plugins {
    id("java")
}

group = "dev.subscripted"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    implementation("com.zaxxer:HikariCP:2.3.2")
    implementation ("mysql:mysql-connector-java:8.0.33")


}

tasks.test {
    useJUnitPlatform()
}