// `compute_checksum` is exposed to Java as `computeChecksum` via #[jvm::export_name].
#![feature(register_tool)]
#![register_tool(jvm)]

#[jvm::export_name = "computeChecksum"]
pub fn compute_checksum(a: i32, b: i32) -> i32 {
    a * 31 + b
}

pub fn main() {}
