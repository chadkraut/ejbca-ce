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
 
package se.anatom.ejbca.admin;

import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.log4j.Logger;

import se.anatom.ejbca.ca.caadmin.ICAAdminSessionHome;
import se.anatom.ejbca.ca.caadmin.ICAAdminSessionRemote;
import se.anatom.ejbca.ca.publisher.IPublisherSessionHome;
import se.anatom.ejbca.ca.publisher.IPublisherSessionRemote;
import se.anatom.ejbca.ca.store.ICertificateStoreSessionHome;
import se.anatom.ejbca.ca.store.ICertificateStoreSessionRemote;
import se.anatom.ejbca.log.Admin;
import se.anatom.ejbca.ra.IUserAdminSessionHome;
import se.anatom.ejbca.ra.IUserAdminSessionRemote;
import se.anatom.ejbca.ra.raadmin.IRaAdminSessionHome;
import se.anatom.ejbca.ra.raadmin.IRaAdminSessionRemote;


/**
 * Base for all AdminCommands, contains functions for getting initial context and logging
 *
 * @version $Id: BaseAdminCommand.java,v 1.10.2.1 2005-02-03 16:48:18 anatom Exp $
 */
public abstract class BaseAdminCommand implements IAdminCommand {
    /** Log4j instance for Base */
    private static Logger baseLog = Logger.getLogger(BaseAdminCommand.class);
    /** Log4j instance for actual class */
    private Logger log;

    /**
     * UserAdminSession handle, not static since different object should go to different session
     * beans concurrently
     */
    private IUserAdminSessionRemote cacheAdmin;

    /** Handle to AdminSessionHome */
    private static IUserAdminSessionHome cacheHome;
    /** Handle to RaAdminSessionHome */
    private static IRaAdminSessionHome raadminHomesession;    
    /** RaAdminSession handle, not static since different object should go to different session beans concurrently */
    private IRaAdminSessionRemote raadminsession;
    /** Handle to CAAdminSessionRemote */
    protected ICAAdminSessionRemote caadminsession = null;
    /** Handle to CertificateStoreSessionRemote */
    protected ICertificateStoreSessionRemote certstoresession = null;
    /** Handle to PublisherSessionRemote */
    protected IPublisherSessionRemote publishersession = null;

    protected Admin administrator = null;

    /** Cached initial context to save JNDI lookups */
    private static InitialContext cacheCtx = null;

    /** holder of argument array */
    protected String[] args = null;

    /**
     * Creates a new instance of BaseAdminCommand
     *
     * @param args command line arguments
     * @param adminType type of admin Admin.TYPE_RACOMMANDLINE_USER, or Admin.TYPE_CACOMMANDLINE_USER
     */
    public BaseAdminCommand(String[] args, int adminType) {
        log = Logger.getLogger(this.getClass());
        this.args = args;
        administrator = new Admin(adminType);
    }

    /**
     * Gets InitialContext
     *
     * @return InitialContext
     */
    protected InitialContext getInitialContext() throws NamingException {
        baseLog.debug(">getInitialContext()");
        try {
            if (cacheCtx == null) {
                cacheCtx = new InitialContext();
            }
            baseLog.debug("<getInitialContext()");
            return cacheCtx;
        } catch (NamingException e) {
            baseLog.error("Can't get InitialContext", e);
            throw e;
        }
    } // getInitialContext

    /** Gets CA admin session
     *@return ICAAdminSessionRemote
     */
    protected ICAAdminSessionRemote getCAAdminSessionRemote() throws Exception{
        if(caadminsession == null){
          Context ctx = getInitialContext();
          ICAAdminSessionHome home = (ICAAdminSessionHome) javax.rmi.PortableRemoteObject.narrow(ctx.lookup("CAAdminSession"), ICAAdminSessionHome.class );            
          caadminsession = home.create();          
        } 
        return caadminsession;
     } // getCAAdminSessionRemote

    /** Gets certificate store session
     *@return ICertificateStoreSessionRemote
     */
    protected ICertificateStoreSessionRemote getCertificateStoreSession() throws Exception{
        if(certstoresession == null){
          Context ctx = getInitialContext();
          ICertificateStoreSessionHome home = (ICertificateStoreSessionHome) javax.rmi.PortableRemoteObject.narrow(ctx.lookup("CertificateStoreSession"), ICertificateStoreSessionHome.class );            
          certstoresession = home.create();          
        } 
        return certstoresession;
     } // getCertificateStoreSession
    
    /** Gets publisher session
     *@return ICertificateStoreSessionRemote
     */
    protected IPublisherSessionRemote getPublisherSession() throws Exception{
        if(publishersession == null){
          Context ctx = getInitialContext();
          IPublisherSessionHome home = (IPublisherSessionHome) javax.rmi.PortableRemoteObject.narrow(ctx.lookup("PublisherSession"), IPublisherSessionHome.class );            
          publishersession = home.create();          
        } 
        return publishersession;
     } // getPublisherSession

    /** Gets user admin session
     *@return IUserAdminSessionRemote
     */
    protected IUserAdminSessionRemote getAdminSession()
        throws CreateException, NamingException, RemoteException {
        debug(">getAdminSession()");
        try {
            if (cacheAdmin == null) {
                if (cacheHome == null) {
                    Context jndiContext = getInitialContext();
                    Object obj1 = jndiContext.lookup("UserAdminSession");
                    cacheHome = (IUserAdminSessionHome) javax.rmi.PortableRemoteObject.narrow(obj1,
                            IUserAdminSessionHome.class);
                }
                cacheAdmin = cacheHome.create();
            }
            debug("<getAdminSession()");
            return cacheAdmin;
        } catch (NamingException e) {
            error("Can't get Admin session", e);
            throw e;
        }
    } // getAdminSession
    
    /** Gets ra admin session
     *@return IRaAdminSessionRemote
     */
    protected IRaAdminSessionRemote getRaAdminSession() throws CreateException, NamingException, RemoteException {
        debug(">getRaAdminSession()");
        administrator = new Admin(Admin.TYPE_RACOMMANDLINE_USER);
        try {
            if( raadminsession == null ) {
                if (raadminHomesession == null) {
                    Context jndiContext = getInitialContext();
                    Object obj1 = jndiContext.lookup("RaAdminSession");
                    raadminHomesession = (IRaAdminSessionHome) javax.rmi.PortableRemoteObject.narrow(obj1, IRaAdminSessionHome.class);
                }
                raadminsession = raadminHomesession.create();
            }
            debug("<getRaAdminSession()");
            return  raadminsession;
        } catch (NamingException e ) {
            error("Can't get RaAdmin session", e);
            throw e;
        }
    } // getRaAdminSession    
    
    /**
     * Logs a message with priority DEBUG
     *
     * @param msg Message
     */
    public void debug(String msg) {
        log.debug(msg);
    }

    /**
     * Logs a message and an exception with priority DEBUG
     *
     * @param msg Message
     * @param t Exception
     */
    public void debug(String msg, Throwable t) {
        log.debug(msg, t);
    }

    /**
     * Logs a message with priority INFO
     *
     * @param msg Message
     */
    public void info(String msg) {
        log.info(msg);
    }

    /**
     * Logs a message and an exception with priority INFO
     *
     * @param msg Message
     * @param t Exception
     */
    public void info(String msg, Throwable t) {
        log.info(msg, t);
    }

    /**
     * Logs a message with priority ERROR
     *
     * @param msg Message
     */
    public void error(String msg) {
        log.error(msg);
    }

    /**
     * Logs a message and an exception with priority ERROR
     *
     * @param msg Message
     * @param t Exception
     */
    public void error(String msg, Throwable t) {
        log.error(msg, t);
    }
}


//BaseAdminCommand
