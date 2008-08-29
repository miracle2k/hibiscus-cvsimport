-- ----------------------------------------------------------------------
-- Aendert den Namen der Spalte "iseinnahme" in "umsatztyp"
-- ----------------------------------------------------------------------

alter table umsatztyp add umsatztyp int(1) NULL;
update umsatztyp set umsatztyp=iseinnahme;
alter table umsatztyp drop iseinnahme;

-- ----------------------------------------------------------------------
-- $Log$
-- Revision 1.1  2008-08-29 16:46:24  willuhn
-- @N BUGZILLA 616
--
-- ----------------------------------------------------------------------
