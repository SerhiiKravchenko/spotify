DROP TABLE IF EXISTS songs;

CREATE TABLE IF NOT EXISTS songs (
    id BIGSERIAL PRIMARY KEY,
    resource_id BIGINT NOT NULL,
    name VARCHAR(255),
    artist VARCHAR(255),
    album VARCHAR(255),
    duration VARCHAR(255),
    year VARCHAR(255)
);