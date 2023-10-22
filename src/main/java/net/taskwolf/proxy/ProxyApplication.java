package net.taskwolf.proxy;

import net.taskwolf.core.intro.Intro;
import net.taskwolf.core.log.Log;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ProxyApplication {
  public static void main(String[] args) throws Exception {
    System.setProperty("jdk.httpclient.allowRestrictedHeaders",
      "host,connection,content-length");
    Intro.create("1.0.0").print();
    var log = Log.create("Proxy", "/logs/");
    SpringApplication.run(ProxyApplication.class);
  }
}
