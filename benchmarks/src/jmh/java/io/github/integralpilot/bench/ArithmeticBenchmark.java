package io.github.integralpilot.bench;

import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

// The Rust functions, compiled to JVM bytecode by rustc_codegen_jvm, live in the class
// `rustbench.arithmetic` (Java package `rustbench`, class `arithmetic`). The jar is put on the
// JMH classpath by build.gradle. Calls below are plain `invokestatic`, exactly like the Java
// side, so the JIT can inline both and the comparison is fair.
import rustbench.arithmetic;

/**
 * Compares the hand-written Java implementation ({@link JavaArithmetic}) against the equivalent
 * Rust implementation compiled to JVM bytecode by {@code rustc_codegen_jvm}.
 *
 * <p>Each workload has a paired {@code java*} / {@code rust*} benchmark so the two columns sit
 * next to each other in the JMH report. Inputs are held in non-{@code final} {@link State} fields
 * so the JIT cannot constant-fold the operations away. Returned values are implicitly consumed by
 * JMH (it feeds them to a {@code Blackhole}), so no result can be dead-code-eliminated.
 *
 * <p>To add a case later: add a {@code pub fn} to {@code rust-lib}'s {@code arithmetic} module, a
 * matching {@code static} method in {@link JavaArithmetic}, and a {@code java*}/{@code rust*}
 * pair here.
 */
@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class ArithmeticBenchmark {

    private int a;
    private int b;
    private long la;
    private long lb;
    private long x;
    private int n;

    @Setup
    public void setUp() {
        a = 1_234_567;
        b = 89_012;
        la = 9_876_543_210L;
        lb = 1_234_567L;
        x = 1_009L;
        n = 10_000;
    }

    // --- add (int) ---
    @Benchmark
    public int javaAdd() {
        return JavaArithmetic.add(a, b);
    }

    @Benchmark
    public int rustAdd() {
        return arithmetic.add(a, b);
    }

    // --- sub (int) ---
    @Benchmark
    public int javaSub() {
        return JavaArithmetic.sub(a, b);
    }

    @Benchmark
    public int rustSub() {
        return arithmetic.sub(a, b);
    }

    // --- mul (int) ---
    @Benchmark
    public int javaMul() {
        return JavaArithmetic.mul(a, b);
    }

    @Benchmark
    public int rustMul() {
        return arithmetic.mul(a, b);
    }

    // --- add (long) ---
    @Benchmark
    public long javaAddLong() {
        return JavaArithmetic.addLong(la, lb);
    }

    @Benchmark
    public long rustAddLong() {
        return arithmetic.add_long(la, lb);
    }

    // --- mul (long) ---
    @Benchmark
    public long javaMulLong() {
        return JavaArithmetic.mulLong(la, lb);
    }

    @Benchmark
    public long rustMulLong() {
        return arithmetic.mul_long(la, lb);
    }

    // --- sum_to (loop workload) ---
    @Benchmark
    public long javaSumTo() {
        return JavaArithmetic.sumTo(n);
    }

    @Benchmark
    public long rustSumTo() {
        return arithmetic.sum_to(n);
    }

    // --- poly (Horner, long) ---
    @Benchmark
    public long javaPoly() {
        return JavaArithmetic.poly(x);
    }

    @Benchmark
    public long rustPoly() {
        return arithmetic.poly(x);
    }
}
