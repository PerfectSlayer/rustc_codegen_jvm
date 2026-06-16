public class Main {
    public static void main(String[] args) {
        // Bound by the pinned name, not the Rust name `compute_checksum`.
        int result = jvm_export_name.computeChecksum(2, 5);
        if (result == 67) {
            System.out.println("Export name test passed: " + result);
        } else {
            throw new AssertionError("expected 67 but got " + result);
        }
    }
}
