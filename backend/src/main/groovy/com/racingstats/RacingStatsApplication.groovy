package com.racingstats

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class RacingStatsApplication {

    static void main(String[] args) {
        SpringApplication.run(RacingStatsApplication, args)
    }
}