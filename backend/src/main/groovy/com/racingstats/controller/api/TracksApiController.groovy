package com.racingstats.controller.api

import com.racingstats.domain.Track
import com.racingstats.repository.TrackRepository
import groovy.util.logging.Slf4j
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping('/api/tracks')
@CrossOrigin(origins = ['http://localhost:5173'])
@Slf4j
class TracksApiController {

    private final TrackRepository trackRepository

    TracksApiController(TrackRepository trackRepository) {
        this.trackRepository = trackRepository
    }

    @GetMapping
    ResponseEntity<List<Track>> getAllTracks() {
        return ResponseEntity.ok(trackRepository.findAll())
    }

    @GetMapping('/{id}')
    ResponseEntity<?> getTrack(@PathVariable UUID id) {
        return trackRepository.findById(id)
                .map { ResponseEntity.ok(it) }
                .orElse(ResponseEntity.notFound().build())
    }
}