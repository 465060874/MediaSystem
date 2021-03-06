CREATE TABLE identifiers (
  id ${SerialType},
  
  mediadata_id integer NOT NULL REFERENCES mediadata(id) ON DELETE CASCADE,
  lastupdated timestamp NOT NULL,

  mediatype varchar(50) NOT NULL,
  provider varchar(20) NOT NULL,
  providerid varchar(20),
    
  matchtype varchar(20),
  matchaccuracy real,
  
  CONSTRAINT identifiers_pk PRIMARY KEY (id),
  CONSTRAINT identifiers_mediadata_id UNIQUE (mediadata_id),
  CONSTRAINT identifiers_check_cache CHECK ((providerid IS NULL AND matchtype IS NULL AND matchaccuracy IS NULL) OR (providerid IS NOT NULL AND matchtype IS NOT NULL AND matchaccuracy IS NOT NULL))
);

ALTER TABLE mediadata DROP CONSTRAINT mediadata_check_identifier;
ALTER TABLE mediadata DROP COLUMN type;
ALTER TABLE mediadata DROP COLUMN provider;
ALTER TABLE mediadata DROP COLUMN providerid;
ALTER TABLE mediadata DROP COLUMN matchtype;
ALTER TABLE mediadata DROP COLUMN matchaccuracy;
