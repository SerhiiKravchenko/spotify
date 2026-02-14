package org.epam.learn.song_service.mapper;

import org.epam.learn.song_service.model.Song;
import org.epam.learn.song_service.model.SongDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface SongMapper {

    @Mapping(target = "resourceId", source = "id")
    @Mapping(target = "id", ignore = true)
    Song toEntity(SongDto dto);

    @Mapping(target = "id", source = "resourceId")
    SongDto toDto(Song song);
}
