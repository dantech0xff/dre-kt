import com.vanniktech.maven.publish.SonatypeHost

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.maven.publish)
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)

    testImplementation(libs.junit)
    testImplementation(libs.truth)
    testImplementation(libs.turbine)
    testImplementation(libs.kotlinx.coroutines.test)
}

mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
    signAllPublications()

    coordinates("io.github.dantech0xff", "dre-core", "0.1.3")

    pom {
        name.set("dre-core")
        description.set("Dispatch → Reduce → Effects — platform-agnostic state management core")
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
