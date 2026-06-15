package io.github.integralpilot.bench;

/**
 * Hand-written Java implementation of the benchmark workloads.
 *
 * <p>Each method mirrors a {@code pub fn} in {@code rust-lib/src/main.rs} (compiled to the JVM
 * class {@code rustbench.arithmetic} by {@code rustc_codegen_jvm}). Java {@code int}/{@code long}
 * arithmetic overflows silently with two's-complement wraparound, which matches the Rust side's
 * {@code wrapping_*} operations, so the two implementations are behaviourally identical.
 *
 * <p>Java 8 only — no APIs newer than 1.8 are used here.
 */
public final class JavaArithmetic {

    private JavaArithmetic() {
    }

    public static int add(int a, int b) {
        return a + b;
    }

    public static int sub(int a, int b) {
        return a - b;
    }

    public static int mul(int a, int b) {
        return a * b;
    }

    public static long addLong(long a, long b) {
        return a + b;
    }

    public static long mulLong(long a, long b) {
        return a * b;
    }

    /** Sum of {@code 1..=n}, accumulated in a {@code long}. */
    public static long sumTo(int n) {
        long acc = 0;
        for (int i = 1; i <= n; i++) {
            acc += i;
        }
        return acc;
    }

    /** Evaluates the cubic {@code 3x^3 + 2x^2 + x + 5} via Horner's method, wrapping on overflow. */
    public static long poly(long x) {
        long r = 3;
        r = r * x + 2;
        r = r * x + 1;
        return r * x + 5;
    }
}
