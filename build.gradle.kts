// ── build.gradle.kts (raíz) ─────────────────────────────────────────────────
// Archivo de configuración principal de Gradle para el repositorio de plugins.
// Define la versión de Kotlin, el plugin de CloudStream y los sub-proyectos.

buildscript {
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.1.2")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.10")
        // Plugin oficial que compila los .kt → archivos .cs3
        classpath("com.github.recloudstream:gradle:-SNAPSHOT")
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
    }
}

// ── Sub-proyectos (un bloque por plugin) ────────────────────────────────────
// Para agregar un nuevo plugin: crea su carpeta y añade una línea aquí.
tasks.register("listPlugins") {
    doLast {
        println("Plugins disponibles: PelisplusHD, Latanime")
    }
}
