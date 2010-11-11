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

package org.ejbca.core.protocol.cmp;

import java.io.UnsupportedEncodingException;
import java.rmi.RemoteException;

import javax.ejb.CreateException;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.bouncycastle.asn1.DEROctetString;
import org.ejbca.config.CmpConfiguration;
import org.ejbca.core.ejb.ServiceLocator;
import org.ejbca.core.ejb.ServiceLocatorException;
import org.ejbca.core.ejb.ca.caadmin.ICAAdminSessionHome;
import org.ejbca.core.ejb.ca.caadmin.ICAAdminSessionRemote;
import org.ejbca.core.ejb.ca.store.ICertificateStoreSessionHome;
import org.ejbca.core.ejb.ca.store.ICertificateStoreSessionRemote;
import org.ejbca.core.ejb.ra.raadmin.IRaAdminSessionHome;
import org.ejbca.core.ejb.ra.raadmin.IRaAdminSessionRemote;
import org.ejbca.core.model.ca.caadmin.CAInfo;
import org.ejbca.core.model.log.Admin;
import org.ejbca.core.model.ra.NotFoundException;
import org.ejbca.core.model.ra.raadmin.EndEntityProfile;

import com.novosec.pkix.asn1.cmp.PKIHeader;

/**
 * Base class for CMP message handlers that require RA mode secret verification.
 * 
 * This class contains common methods for extracting the RA authentication secret.
 * 
 * @version $Id$
 */
public class BaseCmpMessageHandler {

	private static Logger LOG = Logger.getLogger(BaseCmpMessageHandler.class);

    /** strings for error messages defined in internal resources */
	protected static final String CMP_ERRORADDUSER = "cmp.erroradduser";
	protected static final String CMP_ERRORGENERAL = "cmp.errorgeneral";
	
	protected static final int CMP_GET_EEP_FROM_KEYID  = -1;
	protected static final int CMP_GET_CP_FROM_KEYID   = -1;
	protected static final int CMP_GET_CA_FROM_EEP     = -1;
	protected static final int CMP_GET_CA_FROM_KEYID   = -2;

	protected Admin admin;
	protected ICAAdminSessionRemote casession;
	protected IRaAdminSessionRemote rasession;
	protected ICertificateStoreSessionRemote storesession;

	protected BaseCmpMessageHandler() {
	}

	protected BaseCmpMessageHandler(final Admin admin) throws RemoteException, ServiceLocatorException, CreateException {
		this.admin = admin;
		casession = ((ICAAdminSessionHome) ServiceLocator.getInstance().getRemoteHome(ICAAdminSessionHome.JNDI_NAME, ICAAdminSessionHome.class)).create();
		rasession = ((IRaAdminSessionHome) ServiceLocator.getInstance().getRemoteHome(IRaAdminSessionHome.JNDI_NAME, IRaAdminSessionHome.class)).create();
		storesession = ((ICertificateStoreSessionHome) ServiceLocator.getInstance().getRemoteHome(ICertificateStoreSessionHome.JNDI_NAME, ICertificateStoreSessionHome.class)).create();
	}

	/** @return SenderKeyId of in the header or null none was found. */
	protected String getSenderKeyId(PKIHeader head) {
		String keyId = null;
		final DEROctetString os = head.getSenderKID();
		if (os != null) {
			try {
				keyId = new String(os.getOctets(), "UTF-8");
			} catch (UnsupportedEncodingException e2) {
				keyId = new String(os.getOctets());
				LOG.info("UTF-8 not available, using platform default encoding for keyId.");
			}
			if (LOG.isDebugEnabled()) {
				LOG.debug("Found a sender keyId: "+keyId);
			}
			if (keyId == null) {
				LOG.error("No KeyId contained in CMP request.");
			}
		}
		return keyId;
	}

	/** @return the end entity profile id to use for a request based on the current configuration and keyId. */
	protected int getUsedEndEntityProfileId(String keyId) throws RemoteException, NotFoundException {
		int ret = 0;
		String endEntityProfile = CmpConfiguration.getRAEndEntityProfile();
		if (StringUtils.equals(endEntityProfile, "KeyId")) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("Using End Entity Profile with same name as KeyId in request: "+keyId);
			}
			ret = rasession.getEndEntityProfileId(admin, keyId);
		} else {
			ret = rasession.getEndEntityProfileId(admin, endEntityProfile);
		}
		if (ret == 0) {
			LOG.info("No end entity profile found matching keyId: "+keyId);
			throw new NotFoundException("End entity profile with name '"+keyId+"' not found.");
		}
		return ret;
	}

	/** @return the CA id to use for a request based on the current configuration, used end entity profile and keyId. */
	protected int getUsedCaId(String keyId, int eeProfileId) throws RemoteException, NotFoundException {
		int ret = 0;
		final String caName = CmpConfiguration.getRACAName();
		if (StringUtils.equals(caName, "ProfileDefault")) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("Using default CA from End Entity Profile CA when adding users in RA mode.");
			}
			// get default CA id from end entity profile
			final EndEntityProfile eeProfile = rasession.getEndEntityProfile(admin, eeProfileId);
			ret = eeProfile.getDefaultCA();
			if (ret == -1) {
				LOG.error("No default CA id for end entity profile: "+eeProfileId);
			} else {
				if (LOG.isDebugEnabled()) {
					LOG.debug("Using CA with id: "+ret);
				}
			}
		} else if (StringUtils.equals(caName, "KeyId")) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("Using keyId as CA name when adding users in RA mode.");
			}
			// Use keyId as CA name
			final CAInfo info = casession.getCAInfo(admin, keyId);
			if (info == null) {
				LOG.info("No CA found matching keyId: "+keyId);
				throw new NotFoundException("CA with name '"+keyId+"' not found");
			}
			if (LOG.isDebugEnabled()) {
				LOG.debug("Using CA: "+info.getName());
			}
			ret = info.getCAId();																	
		} else {
			final CAInfo info = casession.getCAInfo(admin, caName);
			ret = info.getCAId();					
			if (LOG.isDebugEnabled()) {
				LOG.debug("Using fixed caName when adding users in RA mode: "+caName+"("+ret+")");
			}
		}
		return ret;
	}

	/** @return the certificate profile to use for a request based on the current configuration and keyId. */
	protected int getUsedCertProfileId(String keyId) throws NotFoundException, RemoteException {
		int ret = 0;
		final String certificateProfile = CmpConfiguration.getRACertificateProfile();
		if (StringUtils.equals(certificateProfile, "KeyId")) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("Using Certificate Profile with same name as KeyId in request: " + keyId);
			}
			ret = storesession.getCertificateProfileId(admin, keyId);
		} else {
			ret = storesession.getCertificateProfileId(admin, certificateProfile);					
		}
		if (ret == 0) {
			LOG.info("No certificate profile found matching keyId: "+keyId);
			throw new NotFoundException("Certificate profile with name '"+keyId+"' not found.");
		}
		return ret;
	}
}
