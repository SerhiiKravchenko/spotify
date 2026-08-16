DROP TABLE IF EXISTS storages;

CREATE TABLE IF NOT EXISTS storages (
    id BIGSERIAL PRIMARY KEY,
    storage_type VARCHAR(255) NOT NULL,
    bucket VARCHAR(255) NOT NULL,
    path VARCHAR(255) NOT NULL,
    CONSTRAINT uq_storages_bucket_path UNIQUE (bucket, path)
);
