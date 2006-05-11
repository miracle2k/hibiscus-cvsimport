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


-- Verlorengegangene Constraints
-- Vorher sicherheitshalber loeschen (falls einige schon existieren)
--ALTER TABLE ueberweisung DROP CONSTRAINT fk_konto_usb;
--ALTER TABLE slastschrift DROP CONSTRAINT fk_konto_slast;
--ALTER TABLE umsatz DROP CONSTRAINT fk_konto_ums;
--ALTER TABLE lastschrift DROP CONSTRAINT fk_konto_last;

--ALTER TABLE ueberweisung ADD CONSTRAINT fk_konto_usb FOREIGN KEY (konto_id) REFERENCES konto (id) DEFERRABLE;
--ALTER TABLE slastschrift ADD CONSTRAINT fk_konto_slast FOREIGN KEY (konto_id) REFERENCES konto (id) DEFERRABLE;
--ALTER TABLE umsatz ADD CONSTRAINT fk_konto_ums FOREIGN KEY (konto_id) REFERENCES konto (id) DEFERRABLE;
--ALTER TABLE lastschrift ADD CONSTRAINT fk_konto_last FOREIGN KEY (konto_id) REFERENCES konto (id) DEFERRABLE;

------------------------------------------------------------------------
-- $Log$
-- Revision 1.2  2006-05-11 20:34:16  willuhn
-- @B fehleranfaellige SQL-Updates entfernt
--
-- Revision 1.1  2006/04/27 22:26:16  willuhn
-- *** empty log message ***
--
------------------------------------------------------------------------
