package org.epam.learn.song_service.controller;

import java.util.List;
import java.util.Map;

import org.epam.learn.song_service.model.SongDto;
import org.epam.learn.song_service.service.SongService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/songs")
public class SongController {

    private final SongService songService;

    public SongController(SongService songService) {
        this.songService = songService;
    }

    @PostMapping
    public ResponseEntity<Map<String, Long>> createSongMetadata(@RequestBody @Valid SongDto songDto) {
        return ResponseEntity.ok(songService.createSong(songDto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SongDto> getSongMetadata(@PathVariable("id") Long id) {
        return ResponseEntity.ok(songService.getSongByResourceId(id));
    }

    @DeleteMapping
    public ResponseEntity<Map<String, List<Long>>> deleteSongsMetadata(@RequestParam("id") List<String> id) {
        return ResponseEntity.ok(songService.deleteSongsByResourceId(id));
    }
}
