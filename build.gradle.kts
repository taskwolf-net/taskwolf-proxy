plugins {
  id("java")
  id("io.freefair.lombok") version "8.13"
  id("org.springframework.boot") version "3.4.3"
}

group = "net.taskwolf"
version = "1.0.0-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_21
java.targetCompatibility = JavaVersion.VERSION_21

repositories {
  mavenCentral()
  mavenLocal()
}

dependencies {
  testImplementation(platform("org.junit:junit-bom:5.12.0"))
  testImplementation("org.junit.jupiter:junit-jupiter:5.12.0")

  implementation("net.taskwolf:core:1.0.0-SNAPSHOT")

  implementation("com.google.inject:guice:7.0.0")

  implementation("com.google.guava:guava:33.4.0-jre")

  implementation("org.projectlombok:lombok:1.18.36")
  annotationProcessor("org.projectlombok:lombok:1.18.36")
  testImplementation("org.projectlombok:lombok:1.18.36")
  testAnnotationProcessor("org.projectlombok:lombok:1.18.36")

  implementation("com.datastax.oss:java-driver-core:4.17.0")

  implementation("org.json:json:20250107")
  implementation("commons-io:commons-io:2.18.0")

  implementation("io.netty:netty-all:4.1.119.Final")

  implementation("org.springframework.boot:spring-boot-starter-web:3.4.3")

  implementation("io.jsonwebtoken:jjwt:0.12.6")

  implementation("io.kubernetes:client-java:23.0.0")
}

tasks.test {
  useJUnitPlatform()
}

tasks.bootJar {
  mainClass = "net.taskwolf.proxy.ProxyApplication"
}