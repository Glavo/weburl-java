import org.gradle.kotlin.dsl.sourceSets

plugins {
    id("java-library")
    id("jacoco")
    id("maven-publish")
    id("signing")
    id("io.github.gradle-nexus.publish-plugin") version "2.0.0"
    id("org.glavo.load-maven-publish-properties") version "0.1.0"
    id("de.undercouch.download") version "5.7.0"
}

group = "org.glavo"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

val mainSourceSet = sourceSets["main"]
val benchmarkSourceSet = sourceSets.create("benchmark") {
    java.srcDir("src/benchmark/java")
    resources.srcDir("src/benchmark/resources")

    compileClasspath += mainSourceSet.output
    runtimeClasspath += output + compileClasspath
}

configurations {
    testImplementation {
        extendsFrom(configurations.compileOnly.get())
    }
}

configurations.named(benchmarkSourceSet.implementationConfigurationName) {
    extendsFrom(configurations["implementation"])
}
configurations.named(benchmarkSourceSet.compileOnlyConfigurationName) {
    extendsFrom(configurations["compileOnly"])
}
configurations.named(benchmarkSourceSet.runtimeOnlyConfigurationName) {
    extendsFrom(configurations["runtimeOnly"])
}

dependencies {
    compileOnly("org.jetbrains:annotations:26.1.0")
    compileOnly("com.ibm.icu:icu4j:78.3")

    testImplementation("com.google.code.gson:gson:2.11.0")
    testImplementation(platform("org.junit:junit-bom:6.0.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    fun benchmarkImplementation(notation: Any) = add(benchmarkSourceSet.implementationConfigurationName, notation)
    fun benchmarkAnnotationProcessor(notation: Any) =
        add(benchmarkSourceSet.annotationProcessorConfigurationName, notation)

    val jmhVersion = "1.37"
    benchmarkImplementation("org.openjdk.jmh:jmh-core:$jmhVersion")
    benchmarkAnnotationProcessor("org.openjdk.jmh:jmh-generator-annprocess:$jmhVersion")
}

tasks.withType(JavaCompile::class) {
    options.release.set(17)
}

tasks.named<JavaCompile>(benchmarkSourceSet.compileJavaTaskName) {
    modularity.inferModulePath.set(false)
}

tasks.test {
    useJUnitPlatform()
    testLogging.showStandardStreams = true
}

tasks.register<JavaExec>("benchmark") {
    group = "benchmark"
    description = "Runs JMH benchmarks from src/benchmark/java."
    dependsOn(tasks.named(benchmarkSourceSet.classesTaskName))
    classpath = benchmarkSourceSet.runtimeClasspath

    JavaVersion.current().majorVersion.toIntOrNull()?.let {
        if (it in 24..27) {
            jvmArgs("--sun-misc-unsafe-memory-access=allow")
        }
    }

    mainClass.set("org.openjdk.jmh.Main")
}

val downloadDir = layout.buildDirectory.dir("downloads")

val unicodeVersion = "17.0.0"
val idnaResources = listOf(
    "IdnaMappingTable",
    "IdnaTestV2"
)

val idnaDownloadTasks = idnaResources.map {
    tasks.register<de.undercouch.gradle.tasks.download.Download>("downloadIdna-$it") {
        src("https://www.unicode.org/Public/$unicodeVersion/idna/$it.txt")
        dest(downloadDir.map { dir -> dir.file("idna/$it.txt") })
        overwrite(false)
    }
}

tasks.register("downloadIdnaResources") {
    dependsOn(idnaDownloadTasks)
}

val wptCommit = "ebf8e3069ec4ac6498826bf9066419e46b0f4ac5"
val wptResources = listOf(
    "urltestdata"
)

val wptDownloadTasks = wptResources.map {
    tasks.register<de.undercouch.gradle.tasks.download.Download>("downloadWpt-$it") {
        src("https://raw.githubusercontent.com/web-platform-tests/wpt/$wptCommit/url/resources/$it.json")
        dest(downloadDir.map { dir -> dir.file("wpt/$it.json") })
        overwrite(false)
    }
}

tasks.register("downloadWptResources") {
    dependsOn(wptDownloadTasks)
}

tasks.test {
    dependsOn("downloadWptResources")
    inputs.dir(downloadDir.map { it.dir("wpt") })
    systemProperty("org.glavo.url.wpt.resources", downloadDir.map { it.dir("wpt") }.get().asFile.absolutePath)
}


tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        csv.required.set(true)
        html.required.set(true)
    }
}

tasks.withType<Javadoc> {
    (options as StandardJavadocDocletOptions).also {
        it.jFlags!!.addAll(listOf("-Duser.language=en", "-Duser.country=", "-Duser.variant="))

        it.encoding("UTF-8")
        it.addStringOption("link", "https://docs.oracle.com/en/java/javase/25/docs/api/")
        it.addBooleanOption("html5", true)
        it.addStringOption("Xdoclint:none", "-quiet")

        it.tags!!.addAll(
            listOf(
                "apiNote:a:API Note:",
                "implNote:a:Implementation Note:",
                "implSpec:a:Implementation Specification:",
            )
        )
    }
}

tasks.withType<GenerateModuleMetadata> {
    enabled = false
}

publishing.publications.create<MavenPublication>("maven") {
    groupId = project.group.toString()
    version = project.version.toString()
    artifactId = project.name

    from(components["java"])

    pom {
        name.set(project.name)
        description.set(project.description)
        url.set("https://github.com/Glavo/weburl-java")

        licenses {
            license {
                name.set("Apache-2.0")
                url.set("https://www.apache.org/licenses/LICENSE-2.0")
            }
        }

        developers {
            developer {
                id.set("Glavo")
                name.set("Glavo")
                email.set("zjx001202@gmail.com")
            }
        }

        scm {
            url.set("https://github.com/Glavo/weburl-java")
        }
    }
}
