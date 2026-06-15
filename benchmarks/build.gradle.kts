import org.gradle.api.JavaVersion.VERSION_1_8
import java.io.File
import java.util.concurrent.Callable

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

// --- Manual two-step build -------------------------------------------------------------------
// This project does NOT run cargo. Build the Rust library first:
//     cd rust-lib && cargo build --release
// then run the benchmarks with ./gradlew jmh. The function below locates the freshest
// cargo-produced jar and adds it to the JMH classpath. It is evaluated lazily (only when the
// classpath is resolved), so `./gradlew tasks` works even before cargo has run; `./gradlew jmh`
// fails with a clear message if the jar is missing.
fun locateRustJar(): File {
    val depsDir = file("rust-lib/target/release/deps")
    val jars = depsDir.listFiles { _, name ->
        name.startsWith(crateName) && name.endsWith(".jar")
    }?.toList().orEmpty()
    if (jars.isEmpty()) {
        throw GradleException(
            "Rust benchmark jar not found in $depsDir.\n" +
                "Build the Rust library first:  (cd rust-lib && cargo build --release)"
        )
    }
    return jars.maxByOrNull { it.lastModified() }!!
}

dependencies {
    // 'jmhImplementation' flows into both the jmh compile and runtime classpaths.
    jmhImplementation(files(Callable { locateRustJar() }))
}
