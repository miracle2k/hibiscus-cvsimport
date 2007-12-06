-- Neue Versionstabelle
CREATE TABLE version (
       id int(10) AUTO_INCREMENT
     , name VARCHAR(15) NOT NULL
     , version int(10) NOT NULL
     , UNIQUE (id)
     , PRIMARY KEY (id)
)TYPE=InnoDB;

------------------------------------------------------------------------
-- $Log$
-- Revision 1.1  2007-12-06 17:57:20  willuhn
-- @N Erster Code fuer das neue Versionierungs-System
--
------------------------------------------------------------------------
