package org.epam.learn.resource_service.service.impl;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.epam.learn.resource_service.client.SongServiceClient;
import org.epam.learn.resource_service.client.StorageServiceClient;
import org.epam.learn.resource_service.exception.CsvLengthException;
import org.epam.learn.resource_service.exception.IdNotValidException;
import org.epam.learn.resource_service.exception.ResourceNotFoundException;
import org.epam.learn.resource_service.messaging.ResourceMessagePublisher;
import org.epam.learn.resource_service.model.Mp3FileUrl;
import org.epam.learn.resource_service.model.StorageDto;
import org.epam.learn.resource_service.repository.ResourceRepository;
import org.epam.learn.resource_service.service.S3Service;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResourceServiceImplTest {

    private static final String STAGING_BUCKET = "staging-bucket";
    private static final String PERMANENT_BUCKET = "permanent-bucket";
    private static final StorageDto STAGING = new StorageDto(1L, "STAGING", STAGING_BUCKET, "/files");
    private static final StorageDto PERMANENT = new StorageDto(2L, "PERMANENT", PERMANENT_BUCKET, "/files");

    @Mock
    private ResourceRepository resourceRepository;
    @Mock
    private S3Service s3Service;
    @Mock
    private ResourceMessagePublisher messagePublisher;
    @Mock
    private SongServiceClient songServiceClient;
    @Mock
    private StorageServiceClient storageServiceClient;

    @InjectMocks
    private ResourceServiceImpl service;

    @Test
    void upload_savesEntityInStagingAndPublishesEvent() {
        byte[] mp3 = {1, 2, 3};
        UUID key = UUID.randomUUID();
        Mp3FileUrl unsaved = new Mp3FileUrl(key, "s3://bucket/" + key);
        Mp3FileUrl saved = new Mp3FileUrl(key, "s3://bucket/" + key);
        saved.setId(1L);

        when(storageServiceClient.getStaging()).thenReturn(STAGING);
        when(s3Service.uploadFile(any(byte[].class), eq(STAGING_BUCKET))).thenReturn(unsaved);
        when(resourceRepository.save(any(Mp3FileUrl.class))).thenReturn(saved);

        Map<String, Long> result = service.upload(mp3);

        assertThat(result).containsEntry("id", 1L);
        assertThat(unsaved.getState()).isEqualTo("STAGING");
        assertThat(unsaved.getBucket()).isEqualTo(STAGING_BUCKET);
        assertThat(unsaved.getPath()).isEqualTo("/files");
        verify(messagePublisher).publishResourceUploaded(1L);
    }

    @Test
    void download_returnsBytes_whenEntityExists() {
        UUID key = UUID.randomUUID();
        Mp3FileUrl entity = new Mp3FileUrl(key, "s3://bucket/" + key);
        entity.setId(1L);
        entity.setBucket(STAGING_BUCKET);
        byte[] expected = {10, 20, 30};

        when(resourceRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(s3Service.downloadFile(key.toString(), STAGING_BUCKET)).thenReturn(expected);

        assertThat(service.download(1L)).isEqualTo(expected);
    }

    @Test
    void download_throwsResourceNotFoundException_whenEntityNotFound() {
        when(resourceRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.download(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void download_throwsIdNotValidException_whenIdIsNull() {
        assertThatThrownBy(() -> service.download(null))
                .isInstanceOf(IdNotValidException.class);
    }

    @Test
    void download_throwsIdNotValidException_whenIdIsZero() {
        assertThatThrownBy(() -> service.download(0L))
                .isInstanceOf(IdNotValidException.class);
    }

    @Test
    void download_throwsIdNotValidException_whenIdIsNegative() {
        assertThatThrownBy(() -> service.download(-1L))
                .isInstanceOf(IdNotValidException.class);
    }

    @Test
    void markProcessed_movesFileToPermanentAndUpdatesState() {
        UUID key = UUID.randomUUID();
        Mp3FileUrl entity = new Mp3FileUrl(key, "s3://bucket/" + key);
        entity.setId(1L);
        entity.setState("STAGING");
        entity.setBucket(STAGING_BUCKET);
        entity.setPath("/files");

        when(resourceRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(storageServiceClient.getPermanent()).thenReturn(PERMANENT);

        service.markProcessed(1L);

        verify(s3Service).moveFile(key.toString(), STAGING_BUCKET, PERMANENT_BUCKET);
        assertThat(entity.getState()).isEqualTo("PERMANENT");
        assertThat(entity.getBucket()).isEqualTo(PERMANENT_BUCKET);
        verify(resourceRepository).save(entity);
    }

    @Test
    void markProcessed_throwsResourceNotFoundException_whenEntityNotFound() {
        when(resourceRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.markProcessed(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void delete_removesS3AndDbRecordsAndNotifiesSongService() {
        UUID key1 = UUID.randomUUID();
        UUID key2 = UUID.randomUUID();
        Mp3FileUrl entity1 = new Mp3FileUrl(key1, "s3://bucket/" + key1);
        entity1.setId(1L);
        entity1.setBucket(STAGING_BUCKET);
        Mp3FileUrl entity2 = new Mp3FileUrl(key2, "s3://bucket/" + key2);
        entity2.setId(2L);
        entity2.setBucket(PERMANENT_BUCKET);

        when(resourceRepository.existsById(1L)).thenReturn(true);
        when(resourceRepository.existsById(2L)).thenReturn(true);
        when(resourceRepository.findById(1L)).thenReturn(Optional.of(entity1));
        when(resourceRepository.findById(2L)).thenReturn(Optional.of(entity2));

        Map<String, List<Long>> result = service.delete(List.of("1", "2"));

        assertThat(result.get("ids")).containsExactlyInAnyOrder(1L, 2L);
        verify(resourceRepository).deleteById(1L);
        verify(resourceRepository).deleteById(2L);
        verify(s3Service).deleteFile(key1.toString(), STAGING_BUCKET);
        verify(s3Service).deleteFile(key2.toString(), PERMANENT_BUCKET);
        verify(songServiceClient).deleteSongs(List.of(1L, 2L));
    }

    @Test
    void delete_returnsOnlyExistingIds_whenSomeIdsNotFound() {
        UUID key1 = UUID.randomUUID();
        Mp3FileUrl entity1 = new Mp3FileUrl(key1, "s3://bucket/" + key1);
        entity1.setId(1L);
        entity1.setBucket(STAGING_BUCKET);

        when(resourceRepository.existsById(1L)).thenReturn(true);
        when(resourceRepository.existsById(99L)).thenReturn(false);
        when(resourceRepository.findById(1L)).thenReturn(Optional.of(entity1));

        Map<String, List<Long>> result = service.delete(List.of("1", "99"));

        assertThat(result.get("ids")).containsExactly(1L);
        verify(resourceRepository, times(1)).deleteById(any());
        verify(s3Service, times(1)).deleteFile(any(), any());
        verify(songServiceClient).deleteSongs(List.of(1L));
    }

    @Test
    void delete_doesNotNotifySongService_whenNoResourcesExist() {
        when(resourceRepository.existsById(1L)).thenReturn(false);
        when(resourceRepository.existsById(2L)).thenReturn(false);

        Map<String, List<Long>> result = service.delete(List.of("1", "2"));

        assertThat(result.get("ids")).isEmpty();
        verifyNoInteractions(s3Service);
        verifyNoInteractions(songServiceClient);
    }

    @Test
    void delete_throwsIllegalArgumentException_whenIdsListIsNull() {
        assertThatThrownBy(() -> service.delete(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void delete_throwsIllegalArgumentException_whenIdsListIsEmpty() {
        assertThatThrownBy(() -> service.delete(Collections.emptyList()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void delete_throwsCsvLengthException_whenCsvExceedsLimit() {
        List<String> ids = Collections.nCopies(20, "1000000000");

        assertThatThrownBy(() -> service.delete(ids))
                .isInstanceOf(CsvLengthException.class);
    }
}
