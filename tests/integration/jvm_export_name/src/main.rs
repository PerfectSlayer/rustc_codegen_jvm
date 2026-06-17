// `#[jvm::export_name]` end-to-end: a free function, concrete-impl methods (static + instance),
// and a generic-impl method (each monomorphization on its own class). The generic case folds in
// the reproduction from PerfectSlayer/rustc_codegen_jvm PR #5 by @ban.
#![feature(register_tool)]
#![register_tool(jvm)]

use core::marker::PhantomData;

// Free function.
#[jvm::export_name = "computeChecksum"]
pub fn compute_checksum(a: i32, b: i32) -> i32 {
    a * 31 + b
}

// Concrete-impl methods: a static associated fn and an instance method.
pub struct Greeter {
    pub value: i32,
}

impl Greeter {
    #[jvm::export_name = "makeGreeter"]
    pub fn new_greeter(value: i32) -> Greeter {
        Greeter { value }
    }

    #[jvm::export_name = "getValue"]
    pub fn get_value(&self) -> i32 {
        self.value
    }
}

// Generic-impl method: emitted on each monomorphized class (`Tag_i32`, `Tag_i64`) as `echo`.
pub struct Tag<T>(PhantomData<T>);

impl<T> Tag<T> {
    #[jvm::export_name = "echoValue"]
    pub fn echo(x: T) -> T {
        x
    }
}

// Force both monomorphizations to be collected from a reachable root.
pub fn force() -> i64 {
    Tag::<i32>::echo(7) as i64 + Tag::<i64>::echo(9)
}

pub fn main() {
    let _ = force();
}
