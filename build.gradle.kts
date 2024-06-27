plugins {
    kotlin("jvm") version "1.9.23"
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
    implementation("io.ktor:ktor-client-core:2.0.3")
    implementation("io.ktor:ktor-client-cio:2.0.3")
    implementation("io.ktor:ktor-client-content-negotiation:2.0.3")
    implementation("io.ktor:ktor-serialization-gson:2.0.3")
    implementation("com.google.code.gson:gson:2.8.9")
    implementation("com.jayway.jsonpath:json-path:2.6.0")
    implementation("com.jayway.jsonpath:json-path:2.6.0")
    implementation("org.ktorm:ktorm-core:4.0.0")
    runtimeOnly("mysql:mysql-connector-java:8.0.28")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}