package com.api.serverLaS;

import com.api.serverLaS.services.DatabaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootApplication
public class ServerLaSApplication implements CommandLineRunner {

	@Autowired
	private DatabaseService databaseService;

	public static void main(String[] args) {
		SpringApplication.run(ServerLaSApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		databaseService.createTables();
//		databaseService.fillDatabase();
	}
}
