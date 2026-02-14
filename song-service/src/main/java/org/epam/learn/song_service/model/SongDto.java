package org.epam.learn.song_service.model;

import org.epam.learn.song_service.validator.annotation.Duration;
import org.epam.learn.song_service.validator.annotation.Year;

import jakarta.validation.GroupSequence;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

interface BasicChecks {
}

interface AdvancedChecks {
}

@GroupSequence({SongDto.class, BasicChecks.class, AdvancedChecks.class})
public class SongDto {
    @Min(value = 1, message = "ID must be a positive number", groups = BasicChecks.class)
    private Long id;

    @NotNull(message = "Song name is required", groups = BasicChecks.class)
    @Size(min = 1, max = 100, message = "Song name must be between 1 and 100 characters", groups = AdvancedChecks.class)
    private String name;

    @NotNull(message = "Artist name is required", groups = BasicChecks.class)
    @Size(min = 1, max = 100, message = "Artist name must be between 1 and 100 characters", groups = AdvancedChecks.class)
    private String artist;

    @NotNull(message = "Album name is required", groups = BasicChecks.class)
    @Size(min = 1, max = 100, message = "Album name must be between 1 and 100 characters", groups = AdvancedChecks.class)
    private String album;

    @NotNull(message = "Duration is required", groups = BasicChecks.class)
    @Duration(groups = AdvancedChecks.class)
    private String duration;

    @NotNull(message = "Year is required", groups = BasicChecks.class)
    @Year(groups = AdvancedChecks.class)
    private String year;

    public SongDto() {
    }

    public SongDto(Long id, String name, String artist, String album, String duration, String year) {
        this.id = id;
        this.name = name;
        this.artist = artist;
        this.album = album;
        this.duration = duration;
        this.year = year;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }
}
