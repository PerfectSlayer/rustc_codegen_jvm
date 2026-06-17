public class Main {
    public static void main(String[] args) {
        // All calls bind the pinned names, not the Rust names; if any override were dropped
        // this would fail to compile (or, for the generic case, drop a monomorphization).

        // Free function on the crate class.
        int checksum = jvm_export_name.computeChecksum(2, 5);
        if (checksum != 67) {
            throw new AssertionError("computeChecksum: expected 67 but got " + checksum);
        }

        // Concrete-impl methods: static factory + instance method.
        Greeter g = Greeter.makeGreeter(42);
        int v = g.getValue();
        if (v != 42) {
            throw new AssertionError("getValue: expected 42 but got " + v);
        }

        // Generic-impl method: each monomorphization on its own class, under the pinned name.
        int i = Tag_i32.echo(7);
        long l = Tag_i64.echo(9L);
        if (i != 7) {
            throw new AssertionError("echo(int): expected 7 but got " + i);
        }
        if (l != 9L) {
            throw new AssertionError("echo(long): expected 9 but got " + l);
        }

        System.out.println("export_name test passed: " + checksum + ", " + v + ", " + i + ", " + l);
    }
}
