/*************************************************************************
 *                                                                       *
 *  EJBCA: The OpenSource Certificate Authority                          *
 *                                                                       *
 *  This software is free software; you can redistribute it and/or       *
 *  modify it under the terms of the GNU Lesser General Public           *
 *  License as published by the Free Software Foundation; either         *
 *  version 2.1 of the License, or any later version.                    *
 *                                                                       *
 *  See terms of license at gnu.org.                                     *
 *                                                                       *
 *************************************************************************/
package org.ejbca.core.protocol.crlstore;

import javax.ejb.EJBException;

import org.ejbca.core.ejb.ServiceLocator;
import org.ejbca.core.ejb.ca.crl.IOnlyDataCRLSessionLocal;
import org.ejbca.core.ejb.ca.crl.IOnlyDataCRLSessionLocalHome;
import org.ejbca.core.model.ca.store.CRLInfo;
import org.ejbca.core.model.log.Admin;
/**
 * DB store of data to be used by the VA
 *
 * @author primelars
 * @version $Id$
 *
 */
public class CRLStoreStandAlone implements ICRLStore {
	private static IOnlyDataCRLSessionLocal crlStore = null;
	synchronized IOnlyDataCRLSessionLocal getCRLSession(){
		if(crlStore == null){
			try {
				IOnlyDataCRLSessionLocalHome storehome = (IOnlyDataCRLSessionLocalHome)ServiceLocator.getInstance().getLocalHome(IOnlyDataCRLSessionLocalHome.COMP_NAME);
				crlStore = storehome.create();
			}catch(Exception e){
				throw new EJBException(e);
			}
		}
		return crlStore;
	}
	/* (non-Javadoc)
	 * @see org.ejbca.core.protocol.crlstore.ICRLStore#getCRLInfo(org.ejbca.core.model.log.Admin, java.lang.String)
	 */
	public CRLInfo getCRLInfo(Admin admin, String fingerprint) {
		return getCRLSession().getCRLInfo(admin, fingerprint);
	}
	/* (non-Javadoc)
	 * @see org.ejbca.core.protocol.crlstore.ICRLStore#getLastCRL(org.ejbca.core.model.log.Admin, java.lang.String, boolean)
	 */
	public byte[] getLastCRL(Admin admin, String issuerdn, boolean deltaCRL) {
		return getCRLSession().getLastCRL(admin, issuerdn, deltaCRL);
	}
	/* (non-Javadoc)
	 * @see org.ejbca.core.protocol.crlstore.ICRLStore#getLastCRLInfo(org.ejbca.core.model.log.Admin, java.lang.String, boolean)
	 */
	public CRLInfo getLastCRLInfo(Admin admin, String issuerdn, boolean deltaCRL) {
		return getCRLSession().getLastCRLInfo(admin, issuerdn, deltaCRL);
	}
	/* (non-Javadoc)
	 * @see org.ejbca.core.protocol.crlstore.ICRLStore#getLastCRLNumber(org.ejbca.core.model.log.Admin, java.lang.String, boolean)
	 */
	public int getLastCRLNumber(Admin admin, String issuerdn, boolean deltaCRL) {
		return getCRLSession().getLastCRLNumber(admin, issuerdn, deltaCRL);
	}
}
