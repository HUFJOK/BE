package com.likelion.hufjok;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@OpenAPIDefinition(
		servers = {
				@Server(url = "https://hufjok.lion.it.kr", description = "배포 서버")
		}
)
@SpringBootApplication
public class HufjokApplication {

	public static void main(String[] args) {
		SpringApplication.run(HufjokApplication.class, args);
	}

}
