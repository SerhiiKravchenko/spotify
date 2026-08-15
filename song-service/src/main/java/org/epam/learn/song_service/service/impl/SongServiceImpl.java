package org.epam.learn.song_service.service.impl;

import java.util.List;
import java.util.Map;

import org.epam.learn.song_service.exception.MetadataAlreadyExistsException;
import org.epam.learn.song_service.exception.SongNotFoundException;
import org.epam.learn.song_service.mapper.SongMapper;
import org.epam.learn.song_service.model.Song;
import org.epam.learn.song_service.model.SongDto;
import org.epam.learn.song_service.repository.SongRepository;
import org.epam.learn.song_service.service.SongService;
import org.epam.learn.song_service.utility.Utility;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static java.util.Objects.nonNull;
import static org.epam.learn.song_service.utility.Utility.validateIdIsPositive;

@Service
public class SongServiceImpl implements SongService {

    private static final String METADATA_ALREADY_EXISTS_MESSAGE = "Metadata for resource ID=%d already exists";
    private static final String SAVED_ID_KEY = "id";
    private static final String SONG_METADATA_NOT_FOUND_MESSAGE = "Song metadata for ID=%d not found";
    private static final String DELETED_ID_KEY = "ids";
    private final SongRepository songRepository;
    private final SongMapper songMapper;

    public SongServiceImpl(SongRepository songRepository, SongMapper songMapper) {
        this.songRepository = songRepository;
        this.songMapper = songMapper;
    }

    @Override
    public Map<String, Long> createSong(SongDto songDto) {
        if (songRepository.existsByResourceId(songDto.getId())) {
            throw new MetadataAlreadyExistsException(String.format(METADATA_ALREADY_EXISTS_MESSAGE, songDto.getId()));
        }
        Song saved = songRepository.save(songMapper.toEntity(songDto));
        return nonNull(saved.getId()) ? Map.of(SAVED_ID_KEY, songDto.getId()) : Map.of();
    }

    @Override
    public SongDto getSongByResourceId(Long resourceId) {
        validateIdIsPositive(resourceId);

        return songRepository.findByResourceId(resourceId)
                .map(songMapper::toDto)
                .orElseThrow(() -> new SongNotFoundException(String.format(SONG_METADATA_NOT_FOUND_MESSAGE, resourceId)));
    }


    @Override
    @Transactional
    public Map<String, List<Long>> deleteSongsByResourceId(List<String> resourceIds) {
        Utility.validateStringIdsForDeletion(resourceIds);

        List<Long> deleted = resourceIds.stream()
                .map(Long::parseLong)
                .filter(songRepository::existsByResourceId)
                .toList();

        deleted.forEach(songRepository::deleteByResourceId);

        return Map.of(DELETED_ID_KEY, deleted);
    }
}
