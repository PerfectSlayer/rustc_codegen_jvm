plugins {
    java
}

group = "org.rustlang"
version = "0.1.0"

repositories {
    mavenCentral()
}

tasks.withType<JavaCompile> {
    options.release.set(8)
}
