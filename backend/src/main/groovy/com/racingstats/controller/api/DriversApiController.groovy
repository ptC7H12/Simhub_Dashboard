package com.racingstats.controller.api

import com.racingstats.domain.Driver
import com.racingstats.repository.DriverRepository
import groovy.util.logging.Slf4j
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping('/api/drivers')
@CrossOrigin(origins = ['http://localhost:5173'])
@Slf4j
class DriversApiController {

    private final DriverRepository driverRepository

    DriversApiController(DriverRepository driverRepository) {
        this.driverRepository = driverRepository
    }

    @GetMapping
    ResponseEntity<List<Driver>> getAllDrivers() {
        return ResponseEntity.ok(driverRepository.findAll())
    }

    @GetMapping('/{id}')
    ResponseEntity<?> getDriver(@PathVariable UUID id) {
        return driverRepository.findById(id)
                .map { ResponseEntity.ok(it) }
                .orElse(ResponseEntity.notFound().build())
    }
}