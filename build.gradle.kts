// TOP-LEVEL build file
plugins {
    // Estas líneas le dicen a Gradle qué plugins existen y qué versión usar
    id("com.android.application") version "9.0.0" apply false
    id("com.android.library") version "9.0.0" apply false
    id("org.jetbrains.kotlin.android") version "2.2.10" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.2.10" apply false

    // ESTA ES LA LÍNEA QUE TE FALTA:
    id("com.google.gms.google-services") version "4.4.2" apply false
}