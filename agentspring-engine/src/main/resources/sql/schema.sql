create schema agentspring_face AUTHORIZATION DBA;
set schema agentspring_face;

drop table sources if exists;

CREATE TABLE sources (
  id identity NOT NULL PRIMARY KEY,
  title varchar(45) DEFAULT NULL,
  start varchar(45) DEFAULT NULL,
  script longvarchar NOT NULL
);

drop table visuals if exists;

CREATE TABLE visuals (
  id identity NOT NULL PRIMARY KEY,
  title varchar(45) DEFAULT NULL,
  class varchar(45) DEFAULT NULL,
  /* Chart class visual parameters */
  type varchar(45) DEFAULT NULL,
  yaxis varchar(45) DEFAULT NULL
);

drop table visuals_sources if exists;

CREATE TABLE visuals_sources (
  visual integer NOT NULL,
  source integer NOT NULL,
  FOREIGN KEY (visual) REFERENCES visuals (id) ON DELETE CASCADE,
  FOREIGN KEY (source) REFERENCES sources (id) ON DELETE RESTRICT,
  PRIMARY KEY (visual, source)
);