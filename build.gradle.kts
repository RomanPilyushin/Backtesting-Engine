plugins {
    id("java")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    // https://mvnrepository.com/artifact/org.apache.commons/commons-math3
    implementation("org.apache.commons:commons-math3:3.6.1")
    // https://mvnrepository.com/artifact/org.la4j/la4j
    implementation("org.la4j:la4j:0.6.0")
    // https://mvnrepository.com/artifact/io.reactivex.rxjava3/rxjava
    // https://mvnrepository.com/artifact/io.reactivex/rxjava
    implementation("io.reactivex:rxjava:1.3.8")
    // https://mvnrepository.com/artifact/io.reactivex/rxjava-async-util
    implementation("io.reactivex:rxjava-async-util:0.21.0")
    // https://mvnrepository.com/artifact/org.apache.httpcomponents/httpclient
    implementation("org.apache.httpcomponents:httpclient:4.5.14")
    // https://mvnrepository.com/artifact/org.slf4j/slf4j-api
    implementation("org.slf4j:slf4j-api:2.0.9")
    // https://mvnrepository.com/artifact/ch.qos.logback/logback-classic
    testImplementation("ch.qos.logback:logback-classic:1.4.11")
}

tasks.test {
    useJUnitPlatform()
}