import org.gradle.kotlin.dsl.sourceSets
import org.gradle.language.jvm.tasks.ProcessResources
import org.glavo.url.build.IdnaDataGenerator
import org.glavo.url.build.WebsiteServer
import java.security.MessageDigest

plugins {
    id("java-library")
    id("jacoco")
    id("maven-publish")
    id("signing")
    id("io.github.gradle-nexus.publish-plugin") version "2.0.0"
    id("org.glavo.load-maven-publish-properties") version "0.1.0"
    id("de.undercouch.download") version "5.7.0"
    id("org.teavm") version "0.14.0"
}

group = "org.glavo"
version = "0.3.0" // + "-SNAPSHOT"
description = "A modern Java URL library that brings browser-grade WHATWG URL parsing to URI-style APIs."

repositories {
    mavenCentral()
}

val mainSourceSet = sourceSets["main"]
val teavmSourceSet = sourceSets.getByName("teavm").apply {
    java.setSrcDirs(listOf("src/website/java"))
    resources.setSrcDirs(listOf("src/website/teavm-resources"))

    compileClasspath += mainSourceSet.output + mainSourceSet.compileClasspath
    runtimeClasspath += mainSourceSet.output + mainSourceSet.runtimeClasspath
}

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

    teavm("org.teavm:teavm-jso:0.14.0")
    teavm("org.teavm:teavm-jso-impl:0.14.0")
    teavm("org.teavm:teavm-classlib:0.14.0")
    add(teavmSourceSet.compileOnlyConfigurationName, "org.teavm:teavm-core:0.14.0")

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

java {
    withSourcesJar()
    withJavadocJar()
}

tasks.withType(JavaCompile::class) {
    options.encoding = "UTF-8"
    options.release.set(17)
    options.javaModuleVersion = project.version.toString()
}

tasks.named<JavaCompile>(benchmarkSourceSet.compileJavaTaskName) {
    modularity.inferModulePath.set(false)
}

tasks.named<JavaCompile>(teavmSourceSet.compileJavaTaskName) {
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

teavm {
    all {
        mainClass.set("org.glavo.url.website.WebURLViewer")
        outputDir.set(layout.buildDirectory.dir("generated/teavm"))
        fastGlobalAnalysis.set(true)
        processMemory.set(1024)
    }

    wasmGC {
        targetFileName.set("weburl-viewer.wasm")
        relativePathInOutputDir.set("wasm-gc")
        copyRuntime.set(true)
        obfuscated.set(false)
        sourceMap.set(true)
        strict.set(true)
    }
}

val websiteOutputDir = layout.buildDirectory.dir("website")
val teavmWasmOutputDir = layout.buildDirectory.dir("generated/teavm/wasm-gc")
val websiteJavaScriptDownloadDir = layout.buildDirectory.dir("downloads/website-js")
val websiteJavaScriptOutputDir = layout.buildDirectory.dir("generated/website-js")
val websiteDeploymentDir = layout.buildDirectory.dir("website-deployment")

val whatwgUrlVersion = "16.0.1"

fun websiteJavaScriptDownloadFile(name: String) =
    websiteJavaScriptDownloadDir.map { directory -> directory.file(name) }

fun sha256(file: File): String {
    val digest = MessageDigest.getInstance("SHA-256")
    file.inputStream().use { input ->
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        while (true) {
            val read = input.read(buffer)
            if (read < 0) {
                break
            }
            digest.update(buffer, 0, read)
        }
    }
    return digest.digest().joinToString("") { "%02x".format(it.toInt() and 0xff) }
}

data class WebsiteJavaScriptResource(
    val taskName: String,
    val moduleSpecifier: String,
    val outputPath: String,
    val checksum: String,
)

val whatwgUrlJavaScriptResources = listOf(
    WebsiteJavaScriptResource(
        "Bundle",
        "/whatwg-url@$whatwgUrlVersion/es2022/whatwg-url.bundle.mjs",
        "whatwg-url.mjs",
        "7e4fcfa37575eef1e835046e5548d8b0b1d880bafb6cf6be64bf89c7623300d1"
    ),
    WebsiteJavaScriptResource(
        "Tr46MappingTable",
        "/tr46@6.0.0/lib/mappingTable.json?module",
        "tr46@6.0.0/lib/mappingTable.mjs",
        "4698fb5225bf38e7b3cd7b86d34f6ab197bcd34717da3d85bdb9a4973d281255"
    ),
    WebsiteJavaScriptResource(
        "Tr46Regexes",
        "/tr46@6.0.0/es2022/lib/regexes.mjs",
        "tr46@6.0.0/es2022/lib/regexes.mjs",
        "f4c9072a51f34bebd240250cb1ae8a63a2489ddad5bbff043923dbe7f5c0cfba"
    ),
    WebsiteJavaScriptResource(
        "Tr46StatusMapping",
        "/tr46@6.0.0/es2022/lib/statusMapping.mjs",
        "tr46@6.0.0/es2022/lib/statusMapping.mjs",
        "b8bf24c0ad6389c7e879e65bce4b0b9f87338b45f942186eac5871b4a120948e"
    ),
    WebsiteJavaScriptResource(
        "ExodusBytesUtils",
        "/@exodus/bytes@1.15.0/es2022/fallback/_utils.mjs",
        "@exodus/bytes@1.15.0/es2022/fallback/_utils.mjs",
        "617d9c42d9ac91374a75d989eefc30cd1dc4469491a473c515355c4125060f34"
    ),
    WebsiteJavaScriptResource(
        "ExodusBytesAssert",
        "/@exodus/bytes@1.15.0/es2022/assert.mjs",
        "@exodus/bytes@1.15.0/es2022/assert.mjs",
        "45dcb917479fd759c9422a930bcf58793558286143458e7147ff5eea8a0af22d"
    ),
    WebsiteJavaScriptResource(
        "ExodusBytesBase64",
        "/@exodus/bytes@1.15.0/es2022/base64.mjs",
        "@exodus/bytes@1.15.0/es2022/base64.mjs",
        "0725ec849080cfe1201ecb1eb91c49e4b4cf4cad3b01414ae2fd5aed4c1ac5e5"
    ),
    WebsiteJavaScriptResource(
        "ExodusBytesBase64Fallback",
        "/@exodus/bytes@1.15.0/es2022/fallback/base64.mjs",
        "@exodus/bytes@1.15.0/es2022/fallback/base64.mjs",
        "fde0af3b702f370241ff3ff4b1b98b2cfa226ef8caa87fed9c3963a521ecf5a7"
    ),
    WebsiteJavaScriptResource(
        "ExodusBytesEncodingApi",
        "/@exodus/bytes@1.15.0/es2022/fallback/encoding.api.mjs",
        "@exodus/bytes@1.15.0/es2022/fallback/encoding.api.mjs",
        "319803a1c9b6b34a8ba38c6fc85678cce9e1b90ebe866f80c93f02f0f219ef33"
    ),
    WebsiteJavaScriptResource(
        "ExodusBytesEncodingLabels",
        "/@exodus/bytes@1.15.0/es2022/fallback/encoding.labels.mjs",
        "@exodus/bytes@1.15.0/es2022/fallback/encoding.labels.mjs",
        "d54dae61bdeac6657d8591f47525ec79825c330338f7d689394e01f64c8c7a4c"
    ),
    WebsiteJavaScriptResource(
        "ExodusBytesEncoding",
        "/@exodus/bytes@1.15.0/es2022/fallback/encoding.mjs",
        "@exodus/bytes@1.15.0/es2022/fallback/encoding.mjs",
        "e9327e30754ef9a5ea122164d9b70686296c3552c242cd9e1f235f1deafbe265"
    ),
    WebsiteJavaScriptResource(
        "ExodusBytesEncodingUtil",
        "/@exodus/bytes@1.15.0/es2022/fallback/encoding.util.mjs",
        "@exodus/bytes@1.15.0/es2022/fallback/encoding.util.mjs",
        "3363fe5f3d894a8451e83521c9b7b6a12e2f6820d018b3dbe1100f08db613bec"
    ),
    WebsiteJavaScriptResource(
        "ExodusBytesLatin1",
        "/@exodus/bytes@1.15.0/es2022/fallback/latin1.mjs",
        "@exodus/bytes@1.15.0/es2022/fallback/latin1.mjs",
        "a1bcede3e80bc2f5736b501466b3c8f6484810824f144e9f99a9d269278fd307"
    ),
    WebsiteJavaScriptResource(
        "ExodusBytesMultiByte",
        "/@exodus/bytes@1.15.0/es2022/fallback/multi-byte.mjs",
        "@exodus/bytes@1.15.0/es2022/fallback/multi-byte.mjs",
        "d2346bd7d38a2145007ea31f9cfa78cefc00efdc780f05059392510a50ef69af"
    ),
    WebsiteJavaScriptResource(
        "ExodusBytesMultiByteTable",
        "/@exodus/bytes@1.15.0/es2022/fallback/multi-byte.table.mjs",
        "@exodus/bytes@1.15.0/es2022/fallback/multi-byte.table.mjs",
        "df32ffc446b581152531187cc4be0c7c3105dae3420ded33159a51997aea6ee8"
    ),
    WebsiteJavaScriptResource(
        "ExodusBytesMultiByteEncodings",
        "/@exodus/bytes@1.15.0/es2022/fallback/multi-byte.encodings.mjs",
        "@exodus/bytes@1.15.0/es2022/fallback/multi-byte.encodings.mjs",
        "4070668cfdbdcbd209359468d56f26b351675d727d986d7ed98eb2b47f63b4f0"
    ),
    WebsiteJavaScriptResource(
        "ExodusBytesMultiByteEncodingsData",
        "/@exodus/bytes@1.15.0/fallback/multi-byte.encodings.json?module",
        "@exodus/bytes@1.15.0/fallback/multi-byte.encodings-json.mjs",
        "f384de59c68a4e801082dd6efac47afc60b1897acb4f354eaecb7e43b278a97a"
    ),
    WebsiteJavaScriptResource(
        "ExodusBytesPercent",
        "/@exodus/bytes@1.15.0/es2022/fallback/percent.mjs",
        "@exodus/bytes@1.15.0/es2022/fallback/percent.mjs",
        "3e7b752b08300cffed3c33c40ad66593e407aab2f8230f12bfb7bf900277d999"
    ),
    WebsiteJavaScriptResource(
        "ExodusBytesPlatform",
        "/@exodus/bytes@1.15.0/es2022/fallback/platform.mjs",
        "@exodus/bytes@1.15.0/es2022/fallback/platform.mjs",
        "d0816f7257914a0f4e159442be2601856371aa66dd0369aecc3e8226c808f9c3"
    ),
    WebsiteJavaScriptResource(
        "ExodusBytesPlatformNative",
        "/@exodus/bytes@1.15.0/es2022/fallback/platform.native.mjs",
        "@exodus/bytes@1.15.0/es2022/fallback/platform.native.mjs",
        "091be1e1650b645d88fe363446971a7f3201ca55bd2695daadc949b7a94558bd"
    ),
    WebsiteJavaScriptResource(
        "ExodusBytesSingleByteEncodings",
        "/@exodus/bytes@1.15.0/es2022/fallback/single-byte.encodings.mjs",
        "@exodus/bytes@1.15.0/es2022/fallback/single-byte.encodings.mjs",
        "123dfc6b2bae50c3a095fe9b55ff32f9a299f6a1b51e37e918d6bebff9427e90"
    ),
    WebsiteJavaScriptResource(
        "ExodusBytesSingleByteFallback",
        "/@exodus/bytes@1.15.0/es2022/fallback/single-byte.mjs",
        "@exodus/bytes@1.15.0/es2022/fallback/single-byte.mjs",
        "80b2b3c7e7456ef066d3b4ec0c9b76c06f787da13a58ab53eef4f1a305a3e450"
    ),
    WebsiteJavaScriptResource(
        "ExodusBytesUtf8Auto",
        "/@exodus/bytes@1.15.0/es2022/fallback/utf8.auto.mjs",
        "@exodus/bytes@1.15.0/es2022/fallback/utf8.auto.mjs",
        "a62764d0a5ddb025ee667081c60e41f37ec94748f73e5a151a2d33c939a92c28"
    ),
    WebsiteJavaScriptResource(
        "ExodusBytesUtf16Fallback",
        "/@exodus/bytes@1.15.0/es2022/fallback/utf16.mjs",
        "@exodus/bytes@1.15.0/es2022/fallback/utf16.mjs",
        "838ad2499fb5fca3791ac7f0fe220fc3795eba7d3a192fae4dd80301075ab650"
    ),
    WebsiteJavaScriptResource(
        "ExodusBytesSingleByte",
        "/@exodus/bytes@1.15.0/es2022/single-byte.mjs",
        "@exodus/bytes@1.15.0/es2022/single-byte.mjs",
        "b07b7f44f9f09e08393427d9eec99581a3cca7233b26bbb8ebc9c82d1ecdf5e0"
    ),
    WebsiteJavaScriptResource(
        "ExodusBytesUtf16",
        "/@exodus/bytes@1.15.0/es2022/utf16.mjs",
        "@exodus/bytes@1.15.0/es2022/utf16.mjs",
        "76ba52dcd43b56165182525303c4dff516a23340d004d62336e242840b788db9"
    ),
    WebsiteJavaScriptResource(
        "ExodusBytesUtf8",
        "/@exodus/bytes@1.15.0/es2022/utf8.mjs",
        "@exodus/bytes@1.15.0/es2022/utf8.mjs",
        "964dc4088e28504bc82497be236e7a489d40672ba56f544bba34a916ee697dc4"
    ),
    WebsiteJavaScriptResource(
        "NodeBuffer",
        "/node/buffer.mjs",
        "node/buffer.mjs",
        "64fb61aa5f48644d685f9ceabedba60ea6b5d6ce03dac1943e863d00d9e574f3"
    ),
)

val whatwgUrlJavaScriptModuleOutputs = buildMap {
    for (resource in whatwgUrlJavaScriptResources) {
        put(resource.moduleSpecifier, resource.outputPath)
    }
    put("/tr46@^6.0.0/lib/mappingTable.json?module", "tr46@6.0.0/lib/mappingTable.mjs")
    put("/tr46@^6.0.0/lib/regexes?target=es2022", "tr46@6.0.0/es2022/lib/regexes.mjs")
    put("/tr46@^6.0.0/lib/statusMapping?target=es2022", "tr46@6.0.0/es2022/lib/statusMapping.mjs")
    put("/@exodus/bytes@^1.11.0/fallback/_utils?target=es2022", "@exodus/bytes@1.15.0/es2022/fallback/_utils.mjs")
    put("/@exodus/bytes@^1.11.0/fallback/encoding?target=es2022", "@exodus/bytes@1.15.0/es2022/fallback/encoding.mjs")
    put("/@exodus/bytes@^1.11.0/fallback/latin1?target=es2022", "@exodus/bytes@1.15.0/es2022/fallback/latin1.mjs")
    put("/@exodus/bytes@^1.11.0/fallback/multi-byte?target=es2022", "@exodus/bytes@1.15.0/es2022/fallback/multi-byte.mjs")
    put("/@exodus/bytes@^1.11.0/fallback/percent?target=es2022", "@exodus/bytes@1.15.0/es2022/fallback/percent.mjs")
    put("/@exodus/bytes@^1.11.0/fallback/platform?target=es2022", "@exodus/bytes@1.15.0/es2022/fallback/platform.mjs")
    put("/@exodus/bytes@^1.11.0/fallback/single-byte?target=es2022", "@exodus/bytes@1.15.0/es2022/fallback/single-byte.mjs")
    put("/@exodus/bytes@^1.11.0/fallback/utf8.auto?target=es2022", "@exodus/bytes@1.15.0/es2022/fallback/utf8.auto.mjs")
    put("../../fallback/multi-byte.encodings.json?module", "@exodus/bytes@1.15.0/fallback/multi-byte.encodings-json.mjs")
}

fun relativeModuleSpecifier(fromPath: String, toPath: String): String {
    val fromSegments = fromPath.split('/').dropLast(1)
    val toSegments = toPath.split('/')
    var common = 0
    while (
        common < fromSegments.size &&
        common < toSegments.size &&
        fromSegments[common] == toSegments[common]
    ) {
        common++
    }

    val segments = List(fromSegments.size - common) { ".." } + toSegments.drop(common)
    val path = segments.joinToString("/")
    return if (path.startsWith(".")) path else "./$path"
}

fun rewriteWebsiteModuleImports(line: String, fromPath: String): String {
    var updated = line
    for ((moduleSpecifier, outputPath) in whatwgUrlJavaScriptModuleOutputs) {
        val replacement = relativeModuleSpecifier(fromPath, outputPath)
        updated = updated
            .replace("\"$moduleSpecifier\"", "\"$replacement\"")
            .replace("'$moduleSpecifier'", "'$replacement'")
    }
    return updated
}

val downloadWhatwgUrlJavaScriptResources = whatwgUrlJavaScriptResources.map { resource ->
    tasks.register<de.undercouch.gradle.tasks.download.Download>("downloadWhatwgUrl${resource.taskName}") {
        src("https://esm.sh${resource.moduleSpecifier}")
        dest(websiteJavaScriptDownloadFile(resource.outputPath))
        overwrite(false)
        connectTimeout(30_000)
        readTimeout(180_000)
        retries(3)
    }
}

val verifyWhatwgUrlBundle = tasks.register("verifyWhatwgUrlBundle") {
    group = "website"
    description = "Verifies downloaded whatwg-url browser modules."
    dependsOn(downloadWhatwgUrlJavaScriptResources)
    inputs.files(whatwgUrlJavaScriptResources.map { websiteJavaScriptDownloadFile(it.outputPath) })

    doLast {
        for (resource in whatwgUrlJavaScriptResources) {
            val file = websiteJavaScriptDownloadFile(resource.outputPath).get().asFile
            val actualChecksum = sha256(file)
            if (actualChecksum != resource.checksum) {
                throw GradleException(
                    "SHA-256 mismatch for ${resource.outputPath}: expected ${resource.checksum} but got $actualChecksum"
                )
            }
        }
    }
}

val prepareWebsiteJavaScript = tasks.register<Sync>("prepareWebsiteJavaScript") {
    group = "website"
    description = "Prepares downloaded website JavaScript dependencies for local serving."
    dependsOn(verifyWhatwgUrlBundle)

    for (resource in whatwgUrlJavaScriptResources) {
        from(websiteJavaScriptDownloadFile(resource.outputPath)) {
            val outputDirectory = resource.outputPath.substringBeforeLast('/', "")
            if (outputDirectory.isNotEmpty()) {
                into(outputDirectory)
            }
            rename { resource.outputPath.substringAfterLast('/') }
            filter { line: String ->
                rewriteWebsiteModuleImports(line, resource.outputPath)
            }
        }
    }
    into(websiteJavaScriptOutputDir.map { directory -> directory.dir("vendor") })
}

tasks.register<Delete>("cleanWebsite") {
    group = "website"
    description = "Deletes generated website outputs."
    delete(websiteOutputDir, websiteJavaScriptOutputDir, websiteDeploymentDir)
}

tasks.register<Sync>("buildWebsite") {
    group = "website"
    description = "Builds the static project website with the TeaVM WebAssembly URL viewer."
    dependsOn(tasks.named("buildWasmGC"))
    dependsOn(prepareWebsiteJavaScript)

    from("src/website/resources")
    from(websiteJavaScriptOutputDir)
    from(teavmWasmOutputDir) {
        into("wasm-gc")
    }
    into(websiteOutputDir)
}

tasks.register("serveWebsite") {
    group = "website"
    description = "Builds and serves the website locally over HTTP. Set -Pwebsite.port=8080 to choose a port."
    dependsOn(tasks.named("buildWebsite"))

    doLast {
        val port = providers.gradleProperty("website.port").orElse("8080").get().toInt()
        WebsiteServer.serve(websiteOutputDir.get().asFile, port)
    }
}

tasks.register<Sync>("stageWebsiteDeployment") {
    group = "website"
    description = "Copies the built website to build/website-deployment for CI deployment steps."
    dependsOn(tasks.named("buildWebsite"))

    from(websiteOutputDir)
    into(websiteDeploymentDir)
}

tasks.register<Sync>("deployWebsite") {
    group = "website"
    description = "Copies the built website to -Pwebsite.deployDir=<directory>; publishing is left to CI."
    dependsOn(tasks.named("buildWebsite"))

    val deployDir = providers.gradleProperty("website.deployDir")
    doFirst {
        if (!deployDir.isPresent) {
            throw GradleException("Set -Pwebsite.deployDir=<directory> to choose the website deployment target.")
        }
    }

    from(websiteOutputDir)
    into(deployDir.map { file(it) })
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
        IdnaDataGenerator.generate(
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

tasks.named<Jar>("sourcesJar") {
    dependsOn(generateIdnaData)
}

tasks.named<ProcessResources>("processTestResources") {
    dependsOn(tasks.named("downloadIdna-IdnaTestV2"))
    from(downloadDir.map { it.file("idna/IdnaTestV2.txt") }) {
        into("org/glavo/url/internal/idna")
    }
}

val wptCommit = "ebf8e3069ec4ac6498826bf9066419e46b0f4ac5"
data class WptResource(
    val name: String,
    val path: String,
)

val wptResources = listOf(
    WptResource("IdnaTestV2", "url/resources/IdnaTestV2.json"),
    WptResource("toascii", "url/resources/toascii.json"),
    WptResource("urltestdata", "url/resources/urltestdata.json"),
    WptResource("urlpatterntestdata", "urlpattern/resources/urlpatterntestdata.json"),
)

val wptDownloadTasks = wptResources.map { resource ->
    tasks.register<de.undercouch.gradle.tasks.download.Download>("downloadWpt-${resource.name}") {
        src("https://raw.githubusercontent.com/web-platform-tests/wpt/$wptCommit/${resource.path}")
        dest(downloadDir.map { dir -> dir.file("wpt/${resource.name}.json") })
        overwrite(false)
        connectTimeout(30_000)
        readTimeout(180_000)
        retries(3)
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


if (System.getenv("JITPACK").isNullOrBlank() && rootProject.ext.has("signing.key")) {
    signing {
        useInMemoryPgpKeys(
            rootProject.ext["signing.keyId"].toString(),
            rootProject.ext["signing.key"].toString(),
            rootProject.ext["signing.password"].toString(),
        )
        sign(publishing.publications["maven"])
    }
}

// ./gradlew publishToSonatype closeAndReleaseSonatypeStagingRepository
nexusPublishing {
    repositories {
        sonatype {
            nexusUrl.set(uri("https://ossrh-staging-api.central.sonatype.com/service/local/"))
            snapshotRepositoryUrl.set(uri("https://central.sonatype.com/repository/maven-snapshots/"))

            username.set(rootProject.ext["sonatypeUsername"].toString())
            password.set(rootProject.ext["sonatypePassword"].toString())
        }
    }
}
