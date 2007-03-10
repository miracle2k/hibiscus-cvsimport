------------------------------------------------------------------------
-- $Source$
-- $Revision$
-- $Date$
-- $Author$
-- $Locker$
-- $State$
--
-- Copyright (c) by willuhn.webdesign
-- All rights reserved
--
------------------------------------------------------------------------

-- Neue Spalte "umsatztyp_id"
ALTER CREATE TABLE umsatz (
  id NUMERIC default UNIQUEKEY('umsatz'),
  konto_id int(4) NOT NULL,
  empfaenger_konto varchar(15),
  empfaenger_blz varchar(15),
  empfaenger_name varchar(255),
  betrag double NOT NULL,
  zweck varchar(35),
  zweck2 varchar(35),
  datum date NOT NULL,
  valuta date NOT NULL,
  saldo double,
  primanota varchar(100),
  art varchar(100),
  customerref varchar(100),
  kommentar text NULL,
  checksum numeric NULL,
  umsatztyp_id int(5) NULL,
  UNIQUE (id),
  PRIMARY KEY (id)
);

-- Neue Spalte "parent_id"
-- Neue Spalte "nummer" - Heiner
-- Unique-Index auf die Spalte "name"  - Heiner
ALTER CREATE TABLE umsatztyp (
  id NUMERIC default UNIQUEKEY('umsatztyp'),
  name varchar(255) NOT NULL,
  nummer varchar(5) NULL,
  pattern varchar(255) NOT NULL,
  isregex int(1) NULL,
  iseinnahme int(1) NULL,
  parent_id int(5) NULL,
  UNIQUE (id),
  UNIQUE (name),
  PRIMARY KEY (id)
);

ALTER TABLE umsatz ADD CONSTRAINT fk_umsatztyp1 FOREIGN KEY (umsatztyp_id) REFERENCES umsatztyp (id) DEFERRABLE;
ALTER TABLE umsatztyp ADD CONSTRAINT fk_umsatztyp2 FOREIGN KEY (parent_id) REFERENCES umsatztyp (id) DEFERRABLE;

-- Statements deaktiviert. Das f�hrt bei mehrfacher Ausf�hrung zu Fehlern: Uniqe Index!  Heiner
-- Mu� zum Release wieder aktiviert werden.
-- Sinnvolle vordefinierte Werte fuer Suchfilter
-- insert into umsatztyp (name,pattern,isregex,iseinnahme) values ('Gehalt','(Lohn.*?)|(Gehalt.*?)',1,1);
-- insert into umsatztyp (name,pattern,isregex,iseinnahme) values ('Miete','Miete',0,0);
-- insert into umsatztyp (name,pattern,isregex,iseinnahme) values ('Kreditkarte','(Visa.*?)|(Mastercard.*?)|(American Express.*?)',1,0);
-- insert into umsatztyp (name,pattern,isregex,iseinnahme) values ('GEZ','RUNDFUNKANST.',0,0);
-- insert into umsatztyp (name,pattern,isregex,iseinnahme) values ('Telefon','(O2.*?)|(Telekom.*?)|(telecom.*?)|(Vodafone.*?)|(eplus.*?)|(t-mobile.*?)|(Arcor.*?)',1,0);
-- insert into umsatztyp (name,pattern,isregex,iseinnahme) values ('EC-Kartenzahlung','EC.*?',1,0);

------------------------------------------------------------------------
-- $Log$
-- Revision 1.2  2007-03-10 07:19:12  jost
-- Neu: Nummer für die Sortierung der Umsatz-Kategorien
-- Umsatzkategorien editierbar gemacht (Verlagerung vom Code -> DB)
--
-- Revision 1.1  2006/11/23 23:24:17  willuhn
-- @N Umsatz-Kategorien: DB-Update, Edit
--
------------------------------------------------------------------------