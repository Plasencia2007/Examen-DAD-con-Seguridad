package com.plasencia.ms_lib_config_server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

@SpringBootApplication
@EnableConfigServer
public class MsLibConfigServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(MsLibConfigServerApplication.class, args);
	}

}
