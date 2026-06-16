import org.gradle.api.JavaVersion.VERSION_1_8

plugins {
    java
    id("me.champeau.jmh") version "0.7.3"
}

// Must equal [package].name in rust-lib/Cargo.toml. cargo emits the jar as <crateName>-<hash>.jar.
val crateName = "bench_rust"

repositories {
    mavenCentral()
}

java {
    sourceCompatibility = VERSION_1_8
    targetCompatibility = VERSION_1_8
}

jmh {
    jmhVersion = "1.37"
}

// If you rebuild the backend itself (./build.py all), pass --rerun-tasks
val rustJar = layout.buildDirectory.file("rust-libs/$crateName.jar")

val buildRustLib by tasks.registering(Exec::class) {
    group = "build"
    description = "Compiles the Rust benchmark library to a JVM jar via cargo."
    workingDir = file("rust-lib")
    commandLine("cargo", "build", "--release")

    inputs.dir("rust-lib/src")
    inputs.file("rust-lib/Cargo.toml")
    inputs.file("rust-lib/.cargo/config.toml")
    outputs.file(rustJar)

    // cargo emits <crateName>-<hash>.jar; copy the freshest one to the stable staged path.
    doLast {
        val deps = file("rust-lib/target/release/deps")
        val produced = deps.listFiles { _, name -> name.startsWith(crateName) && name.endsWith(".jar") }
            ?.maxByOrNull { it.lastModified() }
            ?: throw GradleException("cargo build produced no $crateName-*.jar in $deps")
        val dest = rustJar.get().asFile
        dest.parentFile.mkdirs()
        produced.copyTo(dest, overwrite = true)
    }
}

dependencies {
    // The staged jar flows into the jmh compile and runtime classpaths
    jmhImplementation(files(rustJar).builtBy(buildRustLib))
}
