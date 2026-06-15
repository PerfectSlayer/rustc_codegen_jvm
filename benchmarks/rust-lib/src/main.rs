//! Rust side of the JVM benchmark suite.
//!
//! These functions are compiled to JVM bytecode by `rustc_codegen_jvm` and then benchmarked
//! against the equivalent hand-written Java in `src/main/java/.../JavaArithmetic.java`.
//!
//! The functions are placed inside a *doubly* nested module on purpose: the codegen backend
//! maps `mod outer { mod inner { fn f } }` to the JVM class `outer/inner` (Java package
//! `outer`, class `inner`). A named package is required so the JMH benchmarks — which must
//! live in a named package themselves — can `import rustbench.arithmetic;` and call these as
//! ordinary `public static` methods (a direct `invokestatic`, so the JIT can inline them).
//!
//! The crate is named `bench_rust` (not `rustbench`) so the crate-root class emitted for
//! `fn main` does not collide with the `rustbench` package (a top-level class and a package
//! sharing a fully-qualified name is illegal in Java, JLS §7.1).
//!
//! Only `i32`/`i64` are used: they map cleanly to JVM `int`/`long`. Unsigned types are avoided
//! because the backend widens them (`u32` -> `long`, `u64` -> `BigInteger`), which would make
//! the comparison against Java unfair. Wrapping arithmetic is used so the semantics match
//! Java's silent two's-complement overflow.

pub mod rustbench {
    pub mod arithmetic {
        pub fn add(a: i32, b: i32) -> i32 {
            a.wrapping_add(b)
        }

        pub fn sub(a: i32, b: i32) -> i32 {
            a.wrapping_sub(b)
        }

        pub fn mul(a: i32, b: i32) -> i32 {
            a.wrapping_mul(b)
        }

        // NOTE: signed integer division (`a / b`, `wrapping_div`) is intentionally omitted.
        // The codegen backend lowers its overflow-check path to `org.rustlang.primitives.I32`,
        // a runtime helper the `library` shim does not yet provide, so R8 fails to link a jar
        // that performs division on non-constant operands. Re-add a `div` benchmark once the
        // shim ships that class.

        pub fn add_long(a: i64, b: i64) -> i64 {
            a.wrapping_add(b)
        }

        pub fn mul_long(a: i64, b: i64) -> i64 {
            a.wrapping_mul(b)
        }

        /// Loop workload: sum of `1..=n`, accumulated in an `i64`.
        pub fn sum_to(n: i32) -> i64 {
            let mut acc: i64 = 0;
            let mut i: i32 = 1;
            while i <= n {
                acc = acc.wrapping_add(i as i64);
                i += 1;
            }
            acc
        }

        /// Evaluates the cubic `3x^3 + 2x^2 + x + 5` via Horner's method, wrapping on overflow.
        pub fn poly(x: i64) -> i64 {
            let mut r: i64 = 3;
            r = r.wrapping_mul(x).wrapping_add(2);
            r = r.wrapping_mul(x).wrapping_add(1);
            r.wrapping_mul(x).wrapping_add(5)
        }
    }
}

// A binary crate needs a `main`; the linker uses it as the jar's entry point. The benchmarks
// never call it.
fn main() {}
