/**********************************************************************
 * $Source$
 * $Revision$
 * $Date$
 * $Author$
 * $Locker$
 * $State$
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci;

import java.util.Date;

import org.kapott.hbci.GV.HBCIJob;
import org.kapott.hbci.callback.AbstractHBCICallback;
import org.kapott.hbci.exceptions.HBCI_Exception;
import org.kapott.hbci.manager.HBCIUtils;
import org.kapott.hbci.manager.HBCIUtilsInternal;
import org.kapott.hbci.passport.HBCIPassport;
import org.kapott.hbci.passport.INILetter;
import org.kapott.hbci.status.HBCIMsgStatus;

import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.hbci.gui.DialogFactory;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.I18N;
import de.willuhn.util.Logger;

/**
 * Dieser HBCICallbackSWT implementiert den HBCICallbackSWT des HBCI-Systems und
 * schreibt die Log-Ausgaben in das Jameica-Log.
 */
public class HBCICallbackSWT extends AbstractHBCICallback
{

	private I18N i18n;

  /**
   * ct.
   */
  public HBCICallbackSWT()
  {
    super();
    i18n = Application.getPluginLoader().getPlugin(HBCI.class).getResources().getI18N();
  }

  /**
   * @see org.kapott.hbci.callback.HBCICallback#log(java.lang.String, int, java.util.Date, java.lang.StackTraceElement)
   */
  public void log(String msg, int level, Date date, StackTraceElement trace)
  {
  	switch (level)
  	{
  		case HBCIUtils.LOG_DEBUG2:
			case HBCIUtils.LOG_DEBUG:
  			Logger.debug(msg);
  			break;

			case HBCIUtils.LOG_INFO:
				Logger.info(msg);
				break;

			case HBCIUtils.LOG_WARN:
				Logger.warn(msg);
				break;

  		case HBCIUtils.LOG_ERR:
				Logger.error(msg + " " + trace.toString());
				break;

			default:
				Logger.warn(msg);
  	}
  }

  /**
   * @see org.kapott.hbci.callback.HBCICallback#callback(org.kapott.hbci.passport.HBCIPassport, int, java.lang.String, int, java.lang.StringBuffer)
   */
  public void callback(HBCIPassport passport, int reason, String msg, int datatype, StringBuffer retData) {

		try {
				INILetter iniletter;
				Date      date;
            
			switch (reason) {
				case NEED_PASSPHRASE_LOAD:
				case NEED_PASSPHRASE_SAVE:
					retData.replace(0,retData.length(),Settings.getPassphrase());
					break;
	
				case NEED_CHIPCARD:
					GUI.getStatusBar().setErrorText(i18n.tr("Bitte legen Sie Ihre HBCI-Chipkarte in das Leseger�t."));
					break;

				case HAVE_CHIPCARD:
					GUI.getStatusBar().setSuccessText(i18n.tr("HBCI-Chipkarte wird ausgelesen."));
					break;
	
				case NEED_HARDPIN:
					GUI.getStatusBar().setSuccessText(i18n.tr("Bitte geben Sie die PIN in Ihren Chipkarten-Leser ein."));
					break;

				case NEED_SOFTPIN:
					retData.replace(0,retData.length(),DialogFactory.getPIN());
					break;
				case NEED_PT_PIN:
					retData.replace(0,retData.length(),DialogFactory.getPIN());
					break;
				case NEED_PT_TAN:
					retData.replace(0,retData.length(),DialogFactory.getTAN());
					break;

				case HAVE_HARDPIN:
					GUI.getStatusBar().setSuccessText(i18n.tr("PIN wurde eingegeben."));
					break;

				case NEED_REMOVE_CHIPCARD:
					GUI.getStatusBar().setErrorText(i18n.tr("Bitte entfernen Sie die Chipkarte aus dem Leseger�t."));
					break;

				case NEED_CONNECTION:
					if (!Settings.getOnlineMode())
						DialogFactory.openSimple("Internet-Verbindung","Bitte stellen Sie sicher, dass eine Internetverbindung verf�gbar ist.");
					break;
				case CLOSE_CONNECTION:
					if (!Settings.getOnlineMode())
						DialogFactory.openSimple("Internet-Verbindung","Sie k�nnen die Internetverbindung nun wieder trennen.");
					break;

				case NEED_COUNTRY:
				case NEED_BLZ:
				case NEED_HOST:
				case NEED_PORT:
				case NEED_FILTER:
				case NEED_USERID:
				case NEED_CUSTOMERID:
				case NEED_NEW_INST_KEYS_ACK:
				case HAVE_NEW_MY_KEYS:
				case HAVE_INST_MSG:
				case HAVE_CRC_ERROR:
				case HAVE_ERROR:
				case NEED_SIZENTRY_SELECT:
					Logger.error("NOT IMPLEMENTED: " + msg+ " ["+retData.toString()+"]: ");
					throw new HBCI_Exception("reason not implemented");

				default:
						throw new HBCI_Exception("no reason given");
	
			}

		}
		catch (Exception e)
		{
			throw new HBCI_Exception(e);
		}
  }

	private void status(String text)
	{
		Logger.debug(text);
	}
	
  /**
   * @see org.kapott.hbci.callback.HBCICallback#status(org.kapott.hbci.passport.HBCIPassport, int, java.lang.Object[])
   */
  public void status(HBCIPassport passport, int statusTag, Object[] o) {
		switch (statusTag) {

			case STATUS_INST_BPD_INIT: 
				status(HBCIUtilsInternal.getLocMsg("STATUS_REC_INST_DATA"));
				break;

			case STATUS_INST_BPD_INIT_DONE:
				status(HBCIUtilsInternal.getLocMsg("STATUS_REC_INST_DATA_DONE",passport.getBPDVersion()));
				break;

			case STATUS_INST_GET_KEYS:
				status(HBCIUtilsInternal.getLocMsg("STATUS_REC_INST_KEYS"));
				break;

			case STATUS_INST_GET_KEYS_DONE:
				status(HBCIUtilsInternal.getLocMsg("STATUS_REC_INST_KEYS_DONE"));
				break;

			case STATUS_SEND_KEYS:
				status(HBCIUtilsInternal.getLocMsg("STATUS_SEND_MY_KEYS"));
				break;

			case STATUS_SEND_KEYS_DONE:
				status(HBCIUtilsInternal.getLocMsg("STATUS_SEND_MY_KEYS_DONE") + ", Status: "+((HBCIMsgStatus)o[0]).toString());
				break;

			case STATUS_INIT_SYSID:
				status(HBCIUtilsInternal.getLocMsg("STATUS_REC_SYSID"));
				break;

			case STATUS_INIT_SYSID_DONE:
				status(HBCIUtilsInternal.getLocMsg("STATUS_REC_SYSID_DONE",o[1].toString()) + ", Status: "+((HBCIMsgStatus)o[0]).toString());
				break;

			case STATUS_INIT_SIGID:
				status(HBCIUtilsInternal.getLocMsg("STATUS_REC_SIGID"));
				break;

			case STATUS_INIT_SIGID_DONE:
				status(HBCIUtilsInternal.getLocMsg("STATUS_REC_SIGID_DONE",o[1].toString()) + ", Status: "+((HBCIMsgStatus)o[0]).toString());
				break;

			case STATUS_INIT_UPD:
				status(HBCIUtilsInternal.getLocMsg("STATUS_REC_USER_DATA"));
				break;

			case STATUS_INIT_UPD_DONE:
				status(HBCIUtilsInternal.getLocMsg("STATUS_REC_USER_DATA_DONE",passport.getUPDVersion()));
				break;

			case STATUS_LOCK_KEYS:
				status(HBCIUtilsInternal.getLocMsg("STATUS_USR_LOCK"));
				break;

			case STATUS_LOCK_KEYS_DONE:
				status(HBCIUtilsInternal.getLocMsg("STATUS_USR_LOCK_DONE") + ", Status: "+((HBCIMsgStatus)o[0]).toString());
				break;

			case STATUS_DIALOG_INIT:
				status(HBCIUtilsInternal.getLocMsg("STATUS_DIALOG_INIT"));
				break;

			case STATUS_DIALOG_INIT_DONE:
				status(HBCIUtilsInternal.getLocMsg("STATUS_DIALOG_INIT_DONE",o[1]) + ", Status: "+((HBCIMsgStatus)o[0]).toString());
				break;

			case STATUS_SEND_TASK:
				status(HBCIUtilsInternal.getLocMsg("STATUS_DIALOG_NEW_JOB",((HBCIJob)o[0]).getName()));
				break;

			case STATUS_SEND_TASK_DONE:
				status(HBCIUtilsInternal.getLocMsg("STATUS_DIALOG_JOB_DONE",((HBCIJob)o[0]).getName()));
				break;

			case STATUS_DIALOG_END:
				status(HBCIUtilsInternal.getLocMsg("STATUS_DIALOG_END"));
				break;

			case STATUS_DIALOG_END_DONE:
				status(HBCIUtilsInternal.getLocMsg("STATUS_DIALOG_END_DONE") + ", Status: "+((HBCIMsgStatus)o[0]).toString());
				break;

			case STATUS_MSG_CREATE:
				status(HBCIUtilsInternal.getLocMsg("STATUS_MSG_CREATE",o[0].toString()));
				break;

			case STATUS_MSG_SIGN:
				status(HBCIUtilsInternal.getLocMsg("STATUS_MSG_SIGN"));
				break;

			case STATUS_MSG_CRYPT:
				status(HBCIUtilsInternal.getLocMsg("STATUS_MSG_CRYPT"));
				break;

			case STATUS_MSG_SEND:
				status(HBCIUtilsInternal.getLocMsg("STATUS_MSG_SEND"));
				break;

			case STATUS_MSG_RECV:
				status(HBCIUtilsInternal.getLocMsg("STATUS_MSG_RECV"));
				break;

			case STATUS_MSG_PARSE:
				status(HBCIUtilsInternal.getLocMsg("STATUS_MSG_PARSE",o[0].toString()+")"));
				break;

			case STATUS_MSG_DECRYPT:
				status(HBCIUtilsInternal.getLocMsg("STATUS_MSG_DECRYPT"));
				break;

			case STATUS_MSG_VERIFY:
				status(HBCIUtilsInternal.getLocMsg("STATUS_MSG_VERIFY"));
				break;

			default:
				throw new HBCI_Exception(HBCIUtilsInternal.getLocMsg("STATUS_INVALID",Integer.toString(statusTag)));
		}
    
  }

}


/**********************************************************************
 * $Log$
 * Revision 1.15  2004-10-14 23:14:20  willuhn
 * @N new hbci4java (2.5pre)
 * @B fixed locales
 *
 * Revision 1.14  2004/07/25 17:15:06  willuhn
 * @C PluginLoader is no longer static
 *
 * Revision 1.13  2004/07/21 23:54:30  willuhn
 * *** empty log message ***
 *
 * Revision 1.12  2004/06/30 20:58:29  willuhn
 * *** empty log message ***
 *
 * Revision 1.11  2004/05/05 21:27:13  willuhn
 * @N added TAN-Dialog
 *
 * Revision 1.10  2004/03/30 22:07:49  willuhn
 * *** empty log message ***
 *
 * Revision 1.9  2004/03/06 18:25:10  willuhn
 * @D javadoc
 * @C removed empfaenger_id from umsatz
 *
 * Revision 1.8  2004/03/03 22:26:40  willuhn
 * @N help texts
 * @C refactoring
 *
 * Revision 1.7  2004/02/21 19:49:04  willuhn
 * @N PINDialog
 *
 * Revision 1.6  2004/02/17 00:53:22  willuhn
 * @N SaldoAbfrage
 * @N Ueberweisung
 * @N Empfaenger
 *
 * Revision 1.5  2004/02/13 00:41:56  willuhn
 * *** empty log message ***
 *
 * Revision 1.4  2004/02/12 23:46:46  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2004/02/12 00:47:21  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2004/02/11 00:11:20  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/02/09 22:09:40  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2004/02/09 13:06:03  willuhn
 * @C misc
 *
 **********************************************************************/