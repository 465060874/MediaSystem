CREATE TABLE items IF NOT EXISTS
(
  id serial4,
  provider character varying(20) NOT NULL,
  providerid character varying(20) NOT NULL,
  "type" character varying(10) NOT NULL,
  localname character varying(1000) NOT NULL,
  title character varying(100) NOT NULL,
  season integer,
  episode integer,
  subtitle character varying(100),
  releasedate date,
  plot character varying(2000),
  imdbid character varying(20),
  rating numeric(4,1),
  runtime integer,
  lastupdated timestamp without time zone NOT NULL,
  lasthit timestamp without time zone NOT NULL,
  lastchecked timestamp without time zone NOT NULL,
  "version" integer NOT NULL,
  poster bytea,
  background bytea,
  banner bytea,
  CONSTRAINT id PRIMARY KEY (id),
  CONSTRAINT localname UNIQUE (localname)
);

CREATE UNIQUE INDEX localname_idx ON items (localname); 