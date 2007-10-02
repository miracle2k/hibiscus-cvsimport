ALTER TABLE umsatz ALTER COLUMN kommentar varchar(1000) NULL;
ALTER TABLE systemnachricht ALTER COLUMN nachricht varchar(1000) NULL;

------------------------------------------------------------------------
-- $Log$
-- Revision 1.1  2007-10-02 16:08:55  willuhn
-- @C Bugfix mit dem falschen Spaltentyp nochmal ueberarbeitet
--
------------------------------------------------------------------------
