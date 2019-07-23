package org.github.tattoo;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.system.ApplicationPidFileWriter;

@SpringBootApplication
public class Application {

  public static void main(String[] args) {
    SpringApplication application = new SpringApplication(Application.class);
    ApplicationPidFileWriter applicationPidFileWriter = new ApplicationPidFileWriter("app.pid");
    application.addListeners(applicationPidFileWriter);
    application.run(args);
  }

}