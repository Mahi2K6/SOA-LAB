package com.smarttourism.tourist;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Tourist Service. Run several copies on different ports; each one
 * registers with Eureka under the same name, TOURIST-SERVICE.
 */
@SpringBootApplication
public class TouristServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(TouristServiceApplication.class, args);
    }
}
