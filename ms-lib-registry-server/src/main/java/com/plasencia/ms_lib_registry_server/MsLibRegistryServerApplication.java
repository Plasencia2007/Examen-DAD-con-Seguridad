package com.plasencia.ms_lib_registry_server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer
public class MsLibRegistryServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(MsLibRegistryServerApplication.class, args);
	}

}
