# JVM Benchmarks: Java vs Rust-compiled-to-JVM

A [JMH](https://github.com/openjdk/jmh) micro-benchmark project that measures the *same*
operations implemented two ways and runs them side by side:

1. **Java** — hand-written, in `src/main/java/.../JavaArithmetic.java`.
2. **Rust** — the same logic in `rust-lib/`, compiled to JVM bytecode by this repository's
   `rustc_codegen_jvm` backend.

Both are invoked as ordinary `public static` calls (`invokestatic`) from the same benchmark, so
the JIT can inline either one and the comparison is fair. The starter set covers a few arithmetic
operations; add your own cases as described below.

Everything targets and runs on **Java 8** (the Rust-compiled bytecode is class file version 52).

---

## Layout

```
benchmarks/
├── build.gradle.kts          # java + me.champeau.jmh plugin, Java 8, locates the Rust jar
├── settings.gradle
├── gradlew / gradlew.bat     # Gradle wrapper (pinned to Gradle 8.14.5)
├── rust-lib/                 # Rust crate compiled to a JVM jar
│   ├── Cargo.toml            # crate name: bench_rust
│   ├── .cargo/config.toml    # points cargo at the rustc_codegen_jvm backend
│   └── src/main.rs           # pub fns in module rustbench::arithmetic
└── src/
    ├── main/java/io/github/integralpilot/bench/JavaArithmetic.java     # Java reference impl
    └── jmh/java/io/github/integralpilot/bench/ArithmeticBenchmark.java # the JMH benchmarks
```

The Rust functions live in a doubly-nested module `rustbench::arithmetic`, which the backend
compiles to the JVM class `rustbench.arithmetic` (package `rustbench`, class `arithmetic`). A named
package is required because JMH benchmarks must live in a named package and Java cannot import a
default-package class. The crate is named `bench_rust` (not `rustbench`) so its crate-root class
does not collide with the `rustbench` package.

---

## Prerequisites

- **The parent project must be built first.** From the repository root:
  ```bash
  ./build.py all
  ```
  This produces the artifacts `rust-lib/.cargo/config.toml` refers to: the codegen backend
  (`target/release/librustc_codegen_jvm.dylib`), `java-linker`, `library/build/libs/library-0.1.0.jar`,
  `vendor/r8.jar`, and `proguard/default.pro`.
- **Rust nightly** (the backend requires it; see the repo's `rust-toolchain.toml`).
- **A JDK for Gradle.** Gradle 8.14.5 runs on JDK 8–21 (it does **not** support JDK 22+). Run the
  benchmarks on **JDK 8** to honour the "runs on Java 8" goal:
  ```bash
  export JAVA_HOME=/path/to/jdk8
  ```
- **Not on macOS?** Edit the `codegen-backend` line in `rust-lib/.cargo/config.toml` to use the
  right dynamic-library suffix: `.so` on Linux, `.dll` on Windows (`.dylib` is the default).

---

## Running the benchmarks

```bash
./gradlew jmh
```

If you rebuild the backend itself with `./build.py all`, force a fresh compile with `./gradlew jmh --rerun-tasks`.

`./gradlew jmh` runs the full suite with JMH's default iteration counts (slow but rigorous).
Results are also written to `build/results/jmh/results.txt`.

### Quick smoke run

To iterate quickly, build the benchmark jar and run it directly with reduced settings and a filter:

```bash
./gradlew jmhJar
java -jar build/libs/rustc-jvm-benchmarks-jmh.jar "Add|SumTo" -f 1 -wi 1 -i 1
```

`-f` forks, `-wi` warmup iterations, `-i` measurement iterations; the first argument is a regex
selecting benchmarks. Example output (1 fork / 1 iteration on JDK 8):

```
Benchmark                        Mode  Cnt     Score   Units
ArithmeticBenchmark.javaAdd      avgt          1.001   ns/op
ArithmeticBenchmark.rustAdd      avgt          1.004   ns/op
ArithmeticBenchmark.javaSumTo    avgt       2255.652   ns/op
ArithmeticBenchmark.rustSumTo    avgt       2246.773   ns/op
```

---

## Adding a benchmark case

1. **Rust** — add a `pub fn` to the `arithmetic` module in `rust-lib/src/main.rs`.
2. **Java** — add a matching `static` method to `JavaArithmetic`.
3. **Benchmark** — add a `java<Name>` / `rust<Name>` pair of `@Benchmark` methods to
   `ArithmeticBenchmark`. Read inputs from the `@State` fields (not constants) so the JIT cannot
   fold the work away, and return the result (JMH consumes it via a `Blackhole`).

### Type guidance

- Stick to `i32` ↔ `int` and `i64` ↔ `long`. The backend widens unsigned types (`u32` → `long`,
  `u64` → `java.math.BigInteger`), which would make the Java/Rust comparison apples-to-oranges.
- **Signed integer division is currently omitted.** The backend lowers division's overflow-check
  path to `org.rustlang.primitives.I32`, a runtime helper the `library` shim does not yet provide,
  so R8 fails to link a jar that divides non-constant operands. Re-add a `div` benchmark once that
  class ships.

---

## How it fits together

`rust-lib/.cargo/config.toml` makes `cargo build` use `rustc_codegen_jvm` as the codegen backend
and `java-linker` as the linker, producing a self-contained jar (R8 bundles the `org/rustlang/*`
runtime). The `buildRustLib` task in `build.gradle.kts` runs that cargo build, stages the jar, and
adds it to the `jmhImplementation` classpath, so the benchmarks compile and link against the
Rust-compiled classes directly.
