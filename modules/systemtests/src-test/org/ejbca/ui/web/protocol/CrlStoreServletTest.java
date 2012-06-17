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

package org.ejbca.ui.web.protocol;

import static org.junit.Assert.assertNull;

import java.security.cert.X509Certificate;

import org.apache.log4j.Logger;
import org.cesecore.certificates.crl.CrlStoreSessionRemote;
import org.cesecore.jndi.JndiHelper;
import org.ejbca.config.WebConfiguration;
import org.ejbca.core.ejb.ca.CaTestCase;
import org.ejbca.core.ejb.config.ConfigurationSessionRemote;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Testing of CrlStoreServlet
 * 
 * @version $Id$
 * 
 */
public class CrlStoreServletTest extends CaTestCase {
	private final static Logger log = Logger.getLogger(CrlStoreServletTest.class);

	private final ConfigurationSessionRemote configurationSession = JndiHelper.getRemoteSession(ConfigurationSessionRemote.class);
	private final CrlStoreSessionRemote crlSession = JndiHelper.getRemoteSession(CrlStoreSessionRemote.class);
	@Override
	@Before
	public void setUp() throws Exception{
		super.setUp();
	}

	@Override
	@After
	public void tearDown() throws Exception {
		super.tearDown();
	}

	@Test
	public void testCRLStore() throws Exception {
		log.trace(">testCRLStore()");
		final String HTTP_PORT = this.configurationSession.getProperty(WebConfiguration.CONFIG_HTTPSERVERPUBHTTP);
		final X509Certificate cacert = (X509Certificate)getTestCACert();
		final String result = ValidationAuthorityTst.testCRLStore(cacert, this.crlSession, HTTP_PORT);
		assertNull(result, result);
		log.trace("<testCRLStore()");
	}

	@Override
    public String getRoleName() {
		return this.getClass().getSimpleName();
	}
}
