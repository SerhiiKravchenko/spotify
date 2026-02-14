package org.epam.learn.song_service.repository;

import java.util.Optional;

import org.epam.learn.song_service.model.Song;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SongRepository extends CrudRepository<Song, Long> {

    Optional<Song> findByResourceId(Long resourceId);

    void deleteByResourceId(Long resourceId);

    boolean existsByResourceId(Long resourceId);
}
