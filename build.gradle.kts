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

configurations {
    testImplementation {
        extendsFrom(configurations.compileOnly.get())
    }
}

dependencies {
    compileOnly("org.jetbrains:annotations:26.1.0")
    compileOnly("com.ibm.icu:icu4j:78.3")

    testImplementation("com.google.code.gson:gson:2.11.0")
    testImplementation(platform("org.junit:junit-bom:6.0.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType(JavaCompile::class) {
    options.release.set(17)
}

tasks.test {
    useJUnitPlatform()
    testLogging.showStandardStreams = true
}

val downloadDir = layout.buildDirectory.dir("downloads")

val wptCommit = "ebf8e3069ec4ac6498826bf9066419e46b0f4ac5"
val wptResources = listOf(
    "setters_tests",
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
