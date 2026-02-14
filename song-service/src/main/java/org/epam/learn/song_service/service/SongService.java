package org.epam.learn.song_service.service;

import java.util.List;
import java.util.Map;

import org.epam.learn.song_service.model.SongDto;

public interface SongService {

    Map<String, Long> createSong(SongDto songDto);

    SongDto getSongByResourceId(Long resourceId);

    Map<String, List<Long>> deleteSongsByResourceId(List<String> resourceIds);
}
