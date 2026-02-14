package org.epam.learn.resource_service.service.impl;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.mp3.Mp3Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.epam.learn.resource_service.model.MetadataInfo;
import org.epam.learn.resource_service.service.MetadataService;
import org.springframework.stereotype.Service;

@Service
public class MetadataServiceImpl implements MetadataService {

    private static final String TITLE_TAG = "dc:title";
    private static final String ARTIST_TAG = "xmpDM:artist";
    private static final String ALBUM_TAG = "xmpDM:album";
    private static final String DURATION_TAG = "xmpDM:duration";
    private static final String RELEASE_DATE_TAG = "xmpDM:releaseDate";
    private static final String DURATION_FORMAT = "%02d:%02d";
    private static final String DEFAULT_DURATION = "00:00";

    @Override
    public MetadataInfo getMetadataFromMp3File(byte[] file) {
        try {
            BodyContentHandler handler = new BodyContentHandler();
            Metadata metadata = new Metadata();
            InputStream inputStream = new ByteArrayInputStream(file);
            ParseContext pContext = new ParseContext();

            Mp3Parser Mp3Parser = new Mp3Parser();
            Mp3Parser.parse(inputStream, handler, metadata, pContext);

            return getMetadataInfo(metadata);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private MetadataInfo getMetadataInfo(Metadata metadata) {
        MetadataInfo metadataInfo = new MetadataInfo();
        metadataInfo.setName(metadata.get(TITLE_TAG));
        metadataInfo.setArtist(metadata.get(ARTIST_TAG));
        metadataInfo.setAlbum(metadata.get(ALBUM_TAG));
        metadataInfo.setDuration(formatDuration(metadata.get(DURATION_TAG)));
        metadataInfo.setYear(metadata.get(RELEASE_DATE_TAG));
        return metadataInfo;
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
