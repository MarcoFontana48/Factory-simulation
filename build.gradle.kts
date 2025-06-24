import org.gradle.configurationcache.extensions.capitalized

plugins {
    java
}

allprojects {
    apply<JavaPlugin>()

    java {
        toolchain {
            languageVersion = JavaLanguageVersion.of(17)
        }
    }

    repositories {
        mavenCentral()
    }

    group = "it.unibo.ise"
}

subprojects {
    sourceSets {
        main {
            resources {
                srcDir("src/main/asl")
            }
        }
    }

    dependencies {
        implementation("io.github.jason-lang:jason-interpreter:3.2.1")
        testImplementation("org.junit.platform:junit-platform-launcher:1.9.3")
        testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.3")
        testImplementation("org.junit.jupiter:junit-jupiter-params:5.9.3")
        testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.3")
    }

    file(projectDir).listFiles()
        .filter { it.extension == "mas2j" }
        .forEach { mas2jFile ->
            tasks.register<JavaExec>("run${mas2jFile.nameWithoutExtension.capitalized()}Mas") {
                group = "run"
                classpath = sourceSets.getByName("main").runtimeClasspath
                mainClass.set("jason.infra.centralised.RunCentralisedMAS")
                args(mas2jFile.path)
                standardInput = System.`in`
                javaLauncher.set(javaToolchains.launcherFor(java.toolchain))
            }
        }

    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
    }
}
