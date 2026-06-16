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

val repoRoot: File? = rootDir.parentFile
val rustJar = layout.buildDirectory.file("rust-libs/$crateName.jar")

val buildRustLib by tasks.registering(Exec::class) {
    group = "build"
    description = "Compiles the Rust benchmark library to a JVM jar via cargo."
    workingDir = file("rust-lib")
    commandLine("cargo", "build", "--release")

    inputs.dir("rust-lib/src")
    inputs.file("rust-lib/Cargo.toml")
    inputs.file("rust-lib/.cargo/config.toml")
    // ".dylib" matches rust-lib/.cargo/config.toml, .so/.dll on Linux/Windows.
    inputs.files(
        repoRoot?.resolve("target/release/librustc_codegen_jvm.dylib"),
        repoRoot?.resolve("java-linker/target/release/java-linker"),
        repoRoot?.resolve("library/build/libs/library-0.1.0.jar"),
        repoRoot?.resolve("vendor/r8.jar"),
        repoRoot?.resolve("proguard/default.pro"),
    ).withPropertyName("backend")
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
