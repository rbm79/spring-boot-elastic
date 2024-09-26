package com.rbm.logs.examples.demo.application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import co.elastic.apm.attach.ElasticApmAttacher;

@SpringBootApplication
@ComponentScan(basePackages = "com.rbm.logs.examples.demo")
public class DemoApplication {

	public static void main(String[] args) {
		ElasticApmAttacher.attach();
		SpringApplication.run(DemoApplication.class, args);
	}

}
