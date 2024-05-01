plugins {
  id("java")
  id("maven-publish")
  id("org.springframework.boot") version "3.2.4"
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
  repositories {
    maven {
      url = uri("https://git.taskwolf.net/api/v4/projects/13/packages/maven")
      credentials(HttpHeaderCredentials::class) {
        name = "Private-Token"
        value = System.getenv("TASKWOLF_GITLAB_PRIVATE_TOKEN") ?:
          findProperty("taskwolfGitlabPrivateToken") as String?
      }
      authentication {
        create("header", HttpHeaderAuthentication::class)
      }
    }
  }
}

repositories {
  mavenCentral()
  maven {
    url = uri("https://git.taskwolf.net/api/v4/projects/8/packages/maven")
    credentials(HttpHeaderCredentials::class) {
      name = "Private-Token"
      value = System.getenv("TASKWOLF_GITLAB_PRIVATE_TOKEN") ?:
        findProperty("taskwolfGitlabPrivateToken") as String?
    }
    authentication {
      create("header", HttpHeaderAuthentication::class)
    }
  }
}

dependencies {
  testImplementation(platform("org.junit:junit-bom:5.10.2"))
  testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")

  implementation("net.taskwolf:core:1.0.0-SNAPSHOT")

  implementation("com.google.inject:guice:7.0.0")

  implementation("com.google.guava:guava:33.1.0-jre")

  implementation("org.projectlombok:lombok:1.18.32")
  annotationProcessor("org.projectlombok:lombok:1.18.32")
  testImplementation("org.projectlombok:lombok:1.18.32")
  testAnnotationProcessor("org.projectlombok:lombok:1.18.32")

  implementation("com.datastax.oss:java-driver-core:4.17.0")

  implementation("org.json:json:20240303")
  implementation("commons-io:commons-io:2.16.1")

  implementation("io.netty:netty-all:4.1.108.Final")

  implementation("org.springframework.boot:spring-boot-starter-web:3.2.4")

  implementation("io.jsonwebtoken:jjwt:0.12.5")
}

tasks.test {
  useJUnitPlatform()
}

tasks.bootJar {
  mainClass = "net.taskwolf.proxy.ProxyApplication"
}