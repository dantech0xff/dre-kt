import com.vanniktech.maven.publish.AndroidSingleVariantLibrary
import com.vanniktech.maven.publish.SonatypeHost

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.maven.publish)
}

android {
    namespace = "dev.drekt.android"
    compileSdk = 36

    defaultConfig {
        minSdk = 26
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        jvmToolchain(17)
    }
}

dependencies {
    api(project(":dre-core"))

    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.kotlinx.coroutines.android)

    testImplementation(libs.junit)
    testImplementation(libs.truth)
    testImplementation(libs.turbine)
    testImplementation(libs.kotlinx.coroutines.test)
}

mavenPublishing {
    configure(AndroidSingleVariantLibrary(variant = "release", sourcesJar = true, publishJavadocJar = true))
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
    signAllPublications()

    coordinates("io.github.dantech0xff", "dre-android", "0.1.2")

    pom {
        name.set("dre-android")
        description.set("Dispatch → Reduce → Effects — Android ViewModel integration")
        url.set("https://github.com/dantech0xff/dre-kt")
        licenses {
            license {
                name.set("MIT License")
                url.set("https://opensource.org/licenses/MIT")
            }
        }
        developers {
            developer {
                id.set("dantech0xff")
                name.set("Dan")
                url.set("https://github.com/dantech0xff")
            }
        }
        scm {
            url.set("https://github.com/dantech0xff/dre-kt")
            connection.set("scm:git:git://github.com/dantech0xff/dre-kt.git")
            developerConnection.set("scm:git:ssh://github.com/dantech0xff/dre-kt.git")
        }
    }
}
