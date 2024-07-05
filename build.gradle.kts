val ktorVersion = "2.3.11"
val logbackVersion = "1.4.12"
plugins {
    kotlin("jvm") version "1.9.23"
    id("io.ktor.plugin") version "2.3.11"
    kotlin("plugin.serialization") version "1.8.0"
}


group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    api(platform("net.mamoe:mirai-bom:2.16.0"))
    api("net.mamoe:mirai-core-api")     // 编译代码使用
    runtimeOnly("net.mamoe:mirai-core") // 运行时使用
    implementation("com.google.code.gson:gson:2.8.9")
    implementation("com.jayway.jsonpath:json-path:2.9.0")
    implementation("org.ktorm:ktorm-core:4.0.0")
    runtimeOnly("mysql:mysql-connector-java:8.0.28")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.13.3")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.13.3")

    implementation("io.ktor:ktor-client-core")
    implementation("io.ktor:ktor-client-cio")
    implementation("io.ktor:ktor-client-content-negotiation")
    implementation("io.ktor:ktor-serialization-gson")
    implementation("io.ktor:ktor-server-content-negotiation")
    implementation("io.ktor:ktor-serialization-jackson")
    implementation("io.ktor:ktor-server-status-pages")
    implementation("io.ktor:ktor-server-core")
    implementation("io.ktor:ktor-server-netty")
    implementation("ch.qos.logback:logback-classic:$logbackVersion")
    implementation("io.ktor:ktor-server-config-yaml")
}

tasks.test {
    useJUnitPlatform()
}
tasks.jar {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    manifest {
        attributes(mapOf("Main-Class" to "Main"))
    }
    from(configurations.runtimeClasspath.get().map {
        if (it.isDirectory) it else zipTree(it)
    })
    val sourcesMain = sourceSets.main.get()
    sourcesMain.allSource.forEach { println("add from sources: ${it.name}") }
    from(sourcesMain.output)
    exclude("META-INF/*.RSA", "META-INF/*.S", "META-INF/*.DSA")
}

kotlin {
    jvmToolchain(21)
}