# Keep all members from the Rust runtime shim since some are reachable only reflectively.
-keep class org.rustlang.** { *; }

-keep public class *
-keep class * {
    <fields>;
}
-keepclassmembers class * implements * {
    <methods>;
}

-keepattributes MethodParameters
-keepattributes InnerClasses
-keepattributes EnclosingMethod