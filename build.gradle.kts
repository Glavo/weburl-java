import org.gradle.kotlin.dsl.sourceSets
import org.gradle.language.jvm.tasks.ProcessResources
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.OutputStream
import java.nio.charset.StandardCharsets

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

    testImplementation("com.google.code.gson:gson:2.11.0")
    testImplementation("com.ibm.icu:icu4j:78.3")
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

val idnaAuxiliaryResources = listOf(
    "DerivedBidiClass",
    "DerivedCombiningClass",
    "DerivedGeneralCategory",
    "DerivedJoiningType"
)

val idnaAuxiliaryDownloadTasks = idnaAuxiliaryResources.map {
    tasks.register<de.undercouch.gradle.tasks.download.Download>("downloadUnicode-$it") {
        src("https://www.unicode.org/Public/$unicodeVersion/ucd/extracted/$it.txt")
        dest(downloadDir.map { dir -> dir.file("unicode/$it.txt") })
        overwrite(false)
    }
}

val generatedIdnaResourcesDir = layout.buildDirectory.dir("generated/resources/idna/main")
val generatedIdnaDataFile =
    generatedIdnaResourcesDir.map { it.file("org/glavo/url/internal/idna/IdnaData.bin") }

val generateIdnaData = tasks.register("generateIdnaData") {
    dependsOn(tasks.named("downloadIdna-IdnaMappingTable"))
    dependsOn(idnaAuxiliaryDownloadTasks)

    val mappingFile = downloadDir.map { it.file("idna/IdnaMappingTable.txt") }
    val bidiClassFile = downloadDir.map { it.file("unicode/DerivedBidiClass.txt") }
    val combiningClassFile = downloadDir.map { it.file("unicode/DerivedCombiningClass.txt") }
    val generalCategoryFile = downloadDir.map { it.file("unicode/DerivedGeneralCategory.txt") }
    val joiningTypeFile = downloadDir.map { it.file("unicode/DerivedJoiningType.txt") }

    inputs.file(mappingFile)
    inputs.file(bidiClassFile)
    inputs.file(combiningClassFile)
    inputs.file(generalCategoryFile)
    inputs.file(joiningTypeFile)
    outputs.file(generatedIdnaDataFile)

    doLast {
        generateIdnaBinary(
            mappingFile.get().asFile,
            bidiClassFile.get().asFile,
            combiningClassFile.get().asFile,
            generalCategoryFile.get().asFile,
            joiningTypeFile.get().asFile,
            generatedIdnaDataFile.get().asFile
        )
    }
}

mainSourceSet.resources.srcDir(generatedIdnaResourcesDir)

tasks.named<ProcessResources>(mainSourceSet.processResourcesTaskName) {
    dependsOn(generateIdnaData)
}

tasks.named<ProcessResources>("processTestResources") {
    dependsOn(tasks.named("downloadIdna-IdnaTestV2"))
    from(downloadDir.map { it.file("idna/IdnaTestV2.txt") }) {
        into("org/glavo/url/internal/idna")
    }
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

private data class CodePointRange(val start: Int, val end: Int)

private data class IdnaMappingRange(
    val start: Int,
    val end: Int,
    val status: Int,
    val mapping: String
)

private data class BinaryIdnaMappingRange(
    val start: Int,
    val end: Int,
    val status: Int,
    val mappingOffset: Int,
    val mappingLength: Int
)

private data class JoiningTypeRange(
    val start: Int,
    val end: Int,
    val joiningType: Int
)

private data class BidiClassRange(
    val start: Int,
    val end: Int,
    val bidiClass: Int
)

private val IDNA_DATA_MAGIC = 0x49444E41
private val IDNA_DATA_VERSION = 2

private val IDNA_STATUS_DISALLOWED = 0
private val IDNA_STATUS_VALID = 1
private val IDNA_STATUS_IGNORED = 2
private val IDNA_STATUS_MAPPED = 3
private val IDNA_STATUS_DEVIATION = 4

private val JOINING_TYPE_LEFT = 1
private val JOINING_TYPE_RIGHT = 2
private val JOINING_TYPE_DUAL = 3
private val JOINING_TYPE_TRANSPARENT = 4

private val BIDI_CLASS_LEFT_TO_RIGHT = 1
private val BIDI_CLASS_RIGHT_TO_LEFT = 2
private val BIDI_CLASS_ARABIC_LETTER = 3
private val BIDI_CLASS_EUROPEAN_NUMBER = 4
private val BIDI_CLASS_EUROPEAN_SEPARATOR = 5
private val BIDI_CLASS_EUROPEAN_TERMINATOR = 6
private val BIDI_CLASS_ARABIC_NUMBER = 7
private val BIDI_CLASS_COMMON_SEPARATOR = 8
private val BIDI_CLASS_BOUNDARY_NEUTRAL = 9
private val BIDI_CLASS_OTHER_NEUTRAL = 10
private val BIDI_CLASS_NONSPACING_MARK = 11

private fun generateIdnaBinary(
    mappingFile: File,
    bidiClassFile: File,
    combiningClassFile: File,
    generalCategoryFile: File,
    joiningTypeFile: File,
    outputFile: File
) {
    val mappingRanges = parseIdnaMappingRanges(mappingFile)
    val viramaRanges = parseViramaRanges(combiningClassFile)
    val markRanges = parseMarkRanges(generalCategoryFile)
    val bidiClassRanges = parseBidiClassRanges(bidiClassFile)
    val joiningTypeRanges = parseJoiningTypeRanges(joiningTypeFile)

    val mappingPool = ByteArrayOutputStream()
    val mappingOffsets = LinkedHashMap<String, Int>()
    val binaryMappingRanges = mappingRanges.map { range ->
        if (range.mapping.isEmpty()) {
            BinaryIdnaMappingRange(range.start, range.end, range.status, -1, 0)
        } else {
            val offset = mappingOffsets.getOrPut(range.mapping) {
                val currentOffset = mappingPool.size()
                mappingPool.write(range.mapping.toByteArray(StandardCharsets.UTF_8))
                currentOffset
            }
            val length = range.mapping.toByteArray(StandardCharsets.UTF_8).size
            BinaryIdnaMappingRange(range.start, range.end, range.status, offset, length)
        }
    }

    outputFile.parentFile.mkdirs()
    outputFile.outputStream().buffered().use { output ->
        output.writeIntLittleEndian(IDNA_DATA_MAGIC)
        output.writeIntLittleEndian(IDNA_DATA_VERSION)

        output.writeIntLittleEndian(binaryMappingRanges.size)
        for (range in binaryMappingRanges) {
            output.writeIntLittleEndian(range.start)
            output.writeIntLittleEndian(range.end)
            output.write(range.status)
            output.writeIntLittleEndian(range.mappingOffset)
            output.writeShortLittleEndian(range.mappingLength)
        }

        val mappingPoolBytes = mappingPool.toByteArray()
        output.writeIntLittleEndian(mappingPoolBytes.size)
        output.write(mappingPoolBytes)

        output.writeIntLittleEndian(viramaRanges.size)
        for (range in viramaRanges) {
            output.writeIntLittleEndian(range.start)
            output.writeIntLittleEndian(range.end)
        }

        output.writeIntLittleEndian(markRanges.size)
        for (range in markRanges) {
            output.writeIntLittleEndian(range.start)
            output.writeIntLittleEndian(range.end)
        }

        output.writeIntLittleEndian(bidiClassRanges.size)
        for (range in bidiClassRanges) {
            output.writeIntLittleEndian(range.start)
            output.writeIntLittleEndian(range.end)
            output.write(range.bidiClass)
        }

        output.writeIntLittleEndian(joiningTypeRanges.size)
        for (range in joiningTypeRanges) {
            output.writeIntLittleEndian(range.start)
            output.writeIntLittleEndian(range.end)
            output.write(range.joiningType)
        }
    }
}

private fun OutputStream.writeIntLittleEndian(value: Int) {
    write(value and 0xff)
    write(value ushr 8 and 0xff)
    write(value ushr 16 and 0xff)
    write(value ushr 24 and 0xff)
}

private fun OutputStream.writeShortLittleEndian(value: Int) {
    write(value and 0xff)
    write(value ushr 8 and 0xff)
}

private fun parseIdnaMappingRanges(file: File): List<IdnaMappingRange> {
    val ranges = ArrayList<IdnaMappingRange>()
    file.forEachLine(StandardCharsets.UTF_8) { line ->
        val data = line.substringBefore('#').trim()
        if (data.isEmpty()) {
            return@forEachLine
        }

        val fields = data.split(';').map { it.trim() }
        if (fields.size < 2) {
            return@forEachLine
        }

        val range = parseCodePointRange(fields[0])
        val status = when (fields[1]) {
            "valid" -> IDNA_STATUS_VALID
            "ignored" -> IDNA_STATUS_IGNORED
            "mapped" -> IDNA_STATUS_MAPPED
            "deviation" -> IDNA_STATUS_DEVIATION
            "disallowed" -> IDNA_STATUS_DISALLOWED
            else -> error("Unknown IDNA status '${fields[1]}' in $file")
        }
        val mapping = if (fields.size >= 3) parseCodePointSequence(fields[2]) else ""
        ranges += IdnaMappingRange(range.start, range.end, status, mapping)
    }
    return ranges
}

private fun parseViramaRanges(file: File): List<CodePointRange> {
    val ranges = ArrayList<CodePointRange>()
    file.forEachLine(StandardCharsets.UTF_8) { line ->
        val data = line.substringBefore('#').trim()
        if (data.isEmpty()) {
            return@forEachLine
        }

        val fields = data.split(';').map { it.trim() }
        if (fields.size >= 2 && fields[1] == "9") {
            ranges += parseCodePointRange(fields[0])
        }
    }
    return mergeRanges(ranges)
}

private fun parseMarkRanges(file: File): List<CodePointRange> {
    val ranges = ArrayList<CodePointRange>()
    file.forEachLine(StandardCharsets.UTF_8) { line ->
        val data = line.substringBefore('#').trim()
        if (data.isEmpty()) {
            return@forEachLine
        }

        val fields = data.split(';').map { it.trim() }
        if (fields.size >= 2 && fields[1] in setOf("Mn", "Mc", "Me")) {
            ranges += parseCodePointRange(fields[0])
        }
    }
    return mergeRanges(ranges)
}

private fun parseBidiClassRanges(file: File): List<BidiClassRange> {
    val ranges = ArrayList<BidiClassRange>()
    file.forEachLine(StandardCharsets.UTF_8) { line ->
        val data = line.substringBefore('#').trim()
        if (data.isEmpty()) {
            return@forEachLine
        }

        val fields = data.split(';').map { it.trim() }
        if (fields.size < 2) {
            return@forEachLine
        }

        val bidiClass = when (fields[1]) {
            "L" -> BIDI_CLASS_LEFT_TO_RIGHT
            "R" -> BIDI_CLASS_RIGHT_TO_LEFT
            "AL" -> BIDI_CLASS_ARABIC_LETTER
            "EN" -> BIDI_CLASS_EUROPEAN_NUMBER
            "ES" -> BIDI_CLASS_EUROPEAN_SEPARATOR
            "ET" -> BIDI_CLASS_EUROPEAN_TERMINATOR
            "AN" -> BIDI_CLASS_ARABIC_NUMBER
            "CS" -> BIDI_CLASS_COMMON_SEPARATOR
            "BN" -> BIDI_CLASS_BOUNDARY_NEUTRAL
            "ON" -> BIDI_CLASS_OTHER_NEUTRAL
            "NSM" -> BIDI_CLASS_NONSPACING_MARK
            else -> return@forEachLine
        }
        val range = parseCodePointRange(fields[0])
        ranges += BidiClassRange(range.start, range.end, bidiClass)
    }
    return mergeBidiClassRanges(ranges)
}

private fun parseJoiningTypeRanges(file: File): List<JoiningTypeRange> {
    val ranges = ArrayList<JoiningTypeRange>()
    file.forEachLine(StandardCharsets.UTF_8) { line ->
        val data = line.substringBefore('#').trim()
        if (data.isEmpty()) {
            return@forEachLine
        }

        val fields = data.split(';').map { it.trim() }
        if (fields.size < 2) {
            return@forEachLine
        }

        val type = when (fields[1]) {
            "L" -> JOINING_TYPE_LEFT
            "R" -> JOINING_TYPE_RIGHT
            "D" -> JOINING_TYPE_DUAL
            "T" -> JOINING_TYPE_TRANSPARENT
            else -> return@forEachLine
        }
        val range = parseCodePointRange(fields[0])
        ranges += JoiningTypeRange(range.start, range.end, type)
    }
    return mergeJoiningTypeRanges(ranges)
}

private fun parseCodePointRange(value: String): CodePointRange {
    val delimiter = value.indexOf("..")
    if (delimiter < 0) {
        val codePoint = value.toInt(16)
        return CodePointRange(codePoint, codePoint)
    }
    val start = value.substring(0, delimiter).toInt(16)
    val end = value.substring(delimiter + 2).toInt(16)
    return CodePointRange(start, end)
}

private fun parseCodePointSequence(value: String): String {
    if (value.isBlank()) {
        return ""
    }

    val output = StringBuilder()
    for (item in value.trim().split(Regex("\\s+"))) {
        if (item.isNotEmpty()) {
            output.appendCodePoint(item.toInt(16))
        }
    }
    return output.toString()
}

private fun mergeRanges(input: List<CodePointRange>): List<CodePointRange> {
    if (input.isEmpty()) {
        return input
    }

    val output = ArrayList<CodePointRange>()
    val sorted = input.sortedWith(compareBy<CodePointRange> { it.start }.thenBy { it.end })
    var current = sorted.first()
    for (range in sorted.drop(1)) {
        if (current.end + 1 == range.start) {
            current = CodePointRange(current.start, range.end)
        } else {
            output += current
            current = range
        }
    }
    output += current
    return output
}

private fun mergeJoiningTypeRanges(input: List<JoiningTypeRange>): List<JoiningTypeRange> {
    if (input.isEmpty()) {
        return input
    }

    val output = ArrayList<JoiningTypeRange>()
    val sorted = input.sortedWith(compareBy<JoiningTypeRange> { it.start }.thenBy { it.end })
    var current = sorted.first()
    for (range in sorted.drop(1)) {
        if (current.end + 1 == range.start && current.joiningType == range.joiningType) {
            current = JoiningTypeRange(current.start, range.end, current.joiningType)
        } else {
            output += current
            current = range
        }
    }
    output += current
    return output
}

private fun mergeBidiClassRanges(input: List<BidiClassRange>): List<BidiClassRange> {
    if (input.isEmpty()) {
        return input
    }

    val output = ArrayList<BidiClassRange>()
    val sorted = input.sortedWith(compareBy<BidiClassRange> { it.start }.thenBy { it.end })
    var current = sorted.first()
    for (range in sorted.drop(1)) {
        if (current.end + 1 == range.start && current.bidiClass == range.bidiClass) {
            current = BidiClassRange(current.start, range.end, current.bidiClass)
        } else {
            output += current
            current = range
        }
    }
    output += current
    return output
}
