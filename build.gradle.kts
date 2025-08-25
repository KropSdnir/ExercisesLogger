// File: build.gradle.kts
// Timestamp: Updated on 2025-08-20 21:14:14
// Scope: This is the top-level build file where you can add configuration options common to all sub-projects/modules. It defines project-wide settings and dependencies, such as the plugins for Hilt (dependency injection) and KSP (code generation).

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.jetbrains.kotlin.android) apply false
    alias(libs.plugins.hilt.android) apply false
    alias(libs.plugins.ksp) apply false
}
