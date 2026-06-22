// `#[jvm::export_name]` end-to-end: a free function, concrete-impl methods (static + instance),
// and a generic-impl method (each monomorphization on its own class). The generic case folds in
// the reproduction from PerfectSlayer/rustc_codegen_jvm PR #5 by @ban.
//
// Pins deliberately differ from the Rust item names (e.g. `echo` -> `echoValue`) so that any
// call site falling back to the Rust name is caught: the Rust->Rust calls in `force` and
// `greeter_value` would otherwise resolve to a method that doesn't exist.
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

// Generic-impl method: emitted on each monomorphized class (`Tag_i32`, `Tag_i64`) as `echoValue`.
pub struct Tag<T>(PhantomData<T>);

impl<T> Tag<T> {
    #[jvm::export_name = "echoValue"]
    pub fn echo(x: T) -> T {
        x
    }
}

// Rust->Rust call to a pinned generic-impl static fn (exercises call-site name resolution).
pub fn force() -> i64 {
    Tag::<i32>::echo(7) as i64 + Tag::<i64>::echo(9)
}

// Rust->Rust calls to a pinned static assoc fn and a pinned instance method.
pub fn greeter_value(value: i32) -> i32 {
    let g = Greeter::new_greeter(value);
    g.get_value()
}

pub fn main() {
    let _ = force();
    let _ = greeter_value(1);
}
