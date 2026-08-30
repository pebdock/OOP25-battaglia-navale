plugins {
    // Apply the java plugin to add support for Java
    java

    // Apply the application plugin to add support for building a CLI application
    // You can run your app via task "run": ./gradlew run
    application

    /*
     * Adds tasks to export a runnable jar.
     * In order to create it, launch the "shadowJar" task.
     * The runnable jar will be found in build/libs/projectname-all.jar
     */
    id("com.gradleup.shadow") version "9.2.2"
    id("org.danilopianini.gradle-java-qa") version "1.159.0"
}

// Compile and test the project using Java 21
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories { // Where to search for dependencies
    mavenCentral()
}

dependencies {
    // Suppressions for SpotBugs
    compileOnly("com.github.spotbugs:spotbugs-annotations:4.9.8")

    // JUnit API and testing engine
    testImplementation(platform("org.junit:junit-bom:6.0.1"))
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

application {
    mainClass.set("it.unibo.battleship.BattleshipApp")
}


tasks.withType<Test>().configureEach {
    useJUnitPlatform() // Enables the engine of JUnit 5/6
    testLogging { // Additional Options
        // Display all events (test started, succeeded, failed...)
        events(*org.gradle.api.tasks.testing.logging.TestLogEvent.entries.toTypedArray())
        showStandardStreams = true // Show the standard output
    }
}