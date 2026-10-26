// Standalone build, reconstructed 2026-10-26 (finalize track B).
// The previous root file used a legacy buildscript block and the app
// module had no real build script at all, so the repo could not compile.
// Plugin versions come from gradle/libs.versions.toml.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.google.devtools.ksp) apply false
    alias(libs.plugins.secrets) apply false
    alias(libs.plugins.google.services) apply false
}
