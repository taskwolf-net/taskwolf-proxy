plugins {
  id("java")
  id("maven-publish")
  id("io.freefair.lombok") version "9.8.0"
  id("org.springframework.boot") version "4.1.1"
}

group = "net.taskwolf"
version = "1.0.0-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_21
java.targetCompatibility = JavaVersion.VERSION_21

publishing {
  publications {
    create<MavenPublication>("library") {
      from(components["java"])
    }
  }
}

repositories {
  mavenCentral()
  mavenLocal()
}

dependencies {
  testImplementation(platform("org.junit:junit-bom:6.1.3"))
  testImplementation("org.junit.jupiter:junit-jupiter:6.1.3")

  implementation("net.taskwolf:core:1.0.0-SNAPSHOT")

  implementation("com.google.inject:guice:7.0.0")

  implementation("com.google.guava:guava:33.7.2-jre")

  implementation("org.projectlombok:lombok:1.18.48")
  annotationProcessor("org.projectlombok:lombok:1.18.48")
  testImplementation("org.projectlombok:lombok:1.18.48")
  testAnnotationProcessor("org.projectlombok:lombok:1.18.48")

  implementation("com.datastax.oss:java-driver-core:4.17.0")

  implementation("org.json:json:20260814")
  implementation("commons-io:commons-io:2.22.0")

  implementation("io.netty:netty-all:4.2.18.Final")

  implementation("org.springframework.boot:spring-boot-starter-web:4.1.1")

  implementation("io.jsonwebtoken:jjwt:0.13.0")

  implementation("io.kubernetes:client-java:27.0.0")
}

tasks.test {
  useJUnitPlatform()
}

tasks.bootJar {
  mainClass = "net.taskwolf.proxy.ProxyApplication"
}