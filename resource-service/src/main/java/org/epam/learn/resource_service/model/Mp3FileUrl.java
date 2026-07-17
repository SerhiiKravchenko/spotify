package org.epam.learn.resource_service.model;

import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "urls")
public class Mp3FileUrl {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private UUID key;
    private String url;

    public Mp3FileUrl() {
    }

    public Mp3FileUrl(UUID key, String url) {
        this.key = key;
        this.url = url;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public UUID getKey() {
        return key;
    }

    public void setKey(UUID key) {
        this.key = key;
    }

    @Override
    public String toString() {
        return "Mp3FileUrl{" +
                "id=" + id +
                ", key=" + key +
                ", url='" + url + '\'' +
                '}';
    }
}
