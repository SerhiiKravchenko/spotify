package org.epam.learn.resourceprocessor.service.impl;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.mp3.Mp3Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.epam.learn.resourceprocessor.model.SongMetadata;
import org.epam.learn.resourceprocessor.service.MetadataExtractorService;
import org.springframework.stereotype.Service;

@Service
public class MetadataExtractorServiceImpl implements MetadataExtractorService {

    private static final String TITLE_TAG = "dc:title";
    private static final String ARTIST_TAG = "xmpDM:artist";
    private static final String ALBUM_TAG = "xmpDM:album";
    private static final String DURATION_TAG = "xmpDM:duration";
    private static final String RELEASE_DATE_TAG = "xmpDM:releaseDate";
    private static final String DURATION_FORMAT = "%02d:%02d";
    private static final String DEFAULT_DURATION = "00:00";

    @Override
    public SongMetadata extract(byte[] mp3File) {
        try {
            BodyContentHandler handler = new BodyContentHandler();
            Metadata metadata = new Metadata();
            InputStream inputStream = new ByteArrayInputStream(mp3File);
            ParseContext pContext = new ParseContext();

            new Mp3Parser().parse(inputStream, handler, metadata, pContext);

            return toSongMetadata(metadata);
        } catch (Exception e) {
            throw new RuntimeException("Failed to extract MP3 metadata", e);
        }
    }

    private SongMetadata toSongMetadata(Metadata metadata) {
        SongMetadata song = new SongMetadata();
        song.setName(metadata.get(TITLE_TAG));
        song.setArtist(metadata.get(ARTIST_TAG));
        song.setAlbum(metadata.get(ALBUM_TAG));
        song.setDuration(formatDuration(metadata.get(DURATION_TAG)));
        song.setYear(metadata.get(RELEASE_DATE_TAG));
        return song;
    }

    private String formatDuration(String duration) {
        try {
            double seconds = Double.parseDouble(duration);
            int minutes = (int) seconds / 60;
            int remainingSeconds = (int) seconds % 60;
            return String.format(DURATION_FORMAT, minutes, remainingSeconds);
        } catch (NumberFormatException e) {
            return DEFAULT_DURATION;
        }
    }
}
