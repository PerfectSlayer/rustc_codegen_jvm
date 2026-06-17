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
        // The Rust item name is `echo`; the pin renames it to `echoValue` so that any
        // call-site that falls back to the Rust item name produces NoSuchMethodError.
        int i = Tag_i32.echoValue(7);
        long l = Tag_i64.echoValue(9L);
        if (i != 7) {
            throw new AssertionError("echoValue(int): expected 7 but got " + i);
        }
        if (l != 9L) {
            throw new AssertionError("echoValue(long): expected 9 but got " + l);
        }

        // Rust→Rust call through force(): exercises call-site name resolution for pinned
        // generic-impl methods. If the naming bug is present this throws NoSuchMethodError.
        long sum = jvm_export_name.force();
        if (sum != 16L) {
            throw new AssertionError("force: expected 16 but got " + sum);
        }

        System.out.println("export_name test passed: " + checksum + ", " + v + ", " + i + ", " + l + ", " + sum);
    }
}
