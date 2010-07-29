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

package org.ejbca.core.model.log;

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.ejbca.core.ejb.ca.CaTestCase;
import org.ejbca.core.ejb.log.LogSessionRemote;
import org.ejbca.util.InterfaceCache;
import org.ejbca.util.query.BasicMatch;
import org.ejbca.util.query.LogMatch;
import org.ejbca.util.query.Query;

/**
 * Tests the log modules entity and session beans.
 *
 * @version $Id$
 */
public class LogTest extends CaTestCase {
    private static final Logger log = Logger.getLogger(LogTest.class);

    private final Admin admin = new Admin(Admin.TYPE_INTERNALUSER);

    private LogSessionRemote logSession = InterfaceCache.getLogSession();
    
    /**
     * Creates a new TestLog object.
     *
     * @param name name
     */
    public LogTest(String name) {
        super(name);
    }

    public void setUp() throws Exception {
    }

    public void tearDown() throws Exception {
    }

    /**
     * tests adding a log configuration and checks if it can be read again.
     *
     * @throws Exception error
     */
    public void test01AddLogConfiguration() throws Exception {
        log.trace(">test01AddLogConfiguration()");
        assertTrue("Could not create TestCA.", createTestCA());
        
        LogConfiguration logconf = new LogConfiguration();
        logconf.setLogEvent(LogConstants.EVENT_INFO_DATABASE, false);
        logconf.setLogEvent(LogConstants.EVENT_ERROR_DATABASE, true);

        logSession.saveLogConfiguration(admin, getTestCAId(), logconf);

        LogConfiguration logconf2 = logSession.loadLogConfiguration(getTestCAId());
        assertTrue("Couldn't retrieve correct log confirguration data from database.", !logconf2.getLogEvent(LogConstants.EVENT_INFO_DATABASE).booleanValue());
        assertTrue("Couldn't retrieve correct log confirguration data from database.", logconf2.getLogEvent(LogConstants.EVENT_ERROR_DATABASE).booleanValue());

        log.trace("<test01AddLogConfiguration()");
    }

    /**
     * tests adds some log events and checks that they have been stored
     * correctly.
     *
     * @throws Exception error
     */
    public void test02AddAndCheckLogEvents() throws Exception {
        log.trace(">test02AddAndCheckLogEvents()");
        logSession.log(admin, getTestCAId(), LogConstants.MODULE_LOG, new Date(), null, null, LogConstants.EVENT_ERROR_UNKNOWN, "Test");
        Collection<String> logDeviceNames = logSession.getAvailableLogDevices();
        Iterator<String> iterator = logDeviceNames.iterator();
        Collection<LogEntry> result = null;
        while (iterator.hasNext()) {
        	String logDeviceName = iterator.next();
        	if (logDeviceName.equalsIgnoreCase(Log4jLogDevice.DEFAULT_DEVICE_NAME)) {
        		continue;
        	}
        	Query query = new Query(Query.TYPE_LOGQUERY);
        	query.add(LogMatch.MATCH_WITH_COMMENT,BasicMatch.MATCH_TYPE_EQUALS,"Test");
        	result = logSession.query(logDeviceName, query, "", "caid=" + Integer.toString(getTestCAId()), 500);
        	Iterator<LogEntry> iter = result.iterator();
        	boolean found = false;
        	while (iter.hasNext()) {
        		LogEntry entry = iter.next();
        		if ( (entry.getComment() != null) && (entry.getComment().equals("Test")) ) {
        			found = true;
        		}
        	}
        	assertTrue("Couldn't retrieve correct log data from database.", found);
        }
 	   ILogExporter exporter = new CsvLogExporter();
 	   exporter.setEntries(result);
	   byte[] export = exporter.export(admin);
	   assertNotNull(export);
	   String str = new String(export);
	   //assertEquals("foo", str);
	   int ind = str.indexOf("Test\t");
	   assertTrue(ind > 0);
	   log.trace("<test02AddAndCheckLogEvents()");
    }
    
	public void test99RemoveTestCA() throws Exception {
		removeTestCA();
	}
}
