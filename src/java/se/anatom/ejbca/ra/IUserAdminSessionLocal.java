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
 
package se.anatom.ejbca.ra;

import java.math.BigInteger;
import java.util.Collection;

import javax.ejb.DuplicateKeyException;
import javax.ejb.FinderException;
import javax.ejb.RemoveException;

import se.anatom.ejbca.SecConst;
import se.anatom.ejbca.authorization.AuthorizationDeniedException;
import se.anatom.ejbca.log.Admin;
import se.anatom.ejbca.ra.exception.NotFoundException;
import se.anatom.ejbca.ra.raadmin.UserDoesntFullfillEndEntityProfile;
import se.anatom.ejbca.util.query.IllegalQueryException;
import se.anatom.ejbca.util.query.Query;

/**
 *
 * @version $Id: IUserAdminSessionLocal.java,v 1.6.2.1 2005-02-03 16:48:20 anatom Exp $
 */
public interface IUserAdminSessionLocal extends javax.ejb.EJBLocalObject {

    // Public constants
    public static final int MAXIMUM_QUERY_ROWCOUNT = SecConst.MAXIMUM_QUERY_ROWCOUNT; // The maximun number of rows passed back in a query.
    // Public methods


   /**
    * @see se.anatom.ejbca.ra.IUserAdminSessionRemote
    */
    public void addUser(Admin admin, String username, String password, String subjectdn, String subjectaltname, String email,  boolean clearpwd,
                        int endentityprofileid, int certificateprofileid, int type, int tokentype, int hardtokenissuerid, int caid)
                         throws AuthorizationDeniedException, UserDoesntFullfillEndEntityProfile, DuplicateKeyException;


   /**
    * @see se.anatom.ejbca.ra.IUserAdminSessionRemote
    */
    public void changeUser(Admin admin, String username,  String password, String subjectdn, String subjectaltname, String email, boolean clearpwd,
                        int endentityprofileid, int certificateprofileid, int type,
                        int tokentype, int hardtokenissuerid, int status, int caid)
                        throws AuthorizationDeniedException, UserDoesntFullfillEndEntityProfile;

   /**
    * @see se.anatom.ejbca.ra.IUserAdminSessionRemote
    */
    public void deleteUser(Admin admin, String username) throws AuthorizationDeniedException, NotFoundException, RemoveException;

   /**
    * @see se.anatom.ejbca.ra.IUserAdminSessionRemote
    */
    public void setUserStatus(Admin admin, String username, int status) throws AuthorizationDeniedException, FinderException;

   /**
    * @see se.anatom.ejbca.ra.IUserAdminSessionRemote
    */
    public void revokeUser(Admin admin, String username,int reason) throws AuthorizationDeniedException,FinderException;

   /**
    * @see se.anatom.ejbca.ra.IUserAdminSessionRemote
    */
    public void revokeCert(Admin admin, BigInteger certserno, String issuerdn, String username, int reason) throws AuthorizationDeniedException,FinderException;

   /**
    * @see se.anatom.ejbca.ra.IUserAdminSessionRemote
    */
    public void setPassword(Admin admin, String username, String password) throws UserDoesntFullfillEndEntityProfile, AuthorizationDeniedException,FinderException;

   /**
    * @see se.anatom.ejbca.ra.IUserAdminSessionRemote
    */
    public void setClearTextPassword(Admin admin, String username, String password) throws UserDoesntFullfillEndEntityProfile, AuthorizationDeniedException,FinderException;

   /**
    * @see se.anatom.ejbca.ra.IUserAdminSessionRemote
    */
    public UserAdminData findUser(Admin admin, String username) throws FinderException,  AuthorizationDeniedException;

   /**
    * @see se.anatom.ejbca.ra.IUserAdminSessionRemote
    */
    public UserAdminData findUserBySubjectDN(Admin admin, String subjectdn, String issuerdn) throws AuthorizationDeniedException, FinderException;

   /**
    * @see se.anatom.ejbca.ra.IUserAdminSessionRemote
    */
    public Collection findUserByEmail(Admin admin, String email) throws AuthorizationDeniedException;

   /**
    * @see se.anatom.ejbca.ra.IUserAdminSessionRemote
    */
    public void checkIfCertificateBelongToAdmin(Admin admin, BigInteger certificatesnr, String issuerdn) throws AuthorizationDeniedException;

   /**
    * @see se.anatom.ejbca.ra.IUserAdminSessionRemote
    */
    public Collection findAllUsersByStatus(Admin admin, int status) throws FinderException;
    /**
     * @see se.anatom.ejbca.ra.IUserAdminSessionRemote
     */
    public Collection findAllUsersByCaId(Admin admin, int caid) throws FinderException;

   /**
    * @see se.anatom.ejbca.ra.IUserAdminSessionRemote
    */
    public Collection findAllUsersWithLimit(Admin admin) throws FinderException;
    
   /**
    * @see se.anatom.ejbca.ra.IUserAdminSessionRemote
    */
    public Collection findAllUsersByStatusWithLimit(Admin admin, int status, boolean onlybatchusers) throws FinderException;

   /**
    * @see se.anatom.ejbca.ra.IUserAdminSessionRemote
    */
    public void startExternalService(String args[]);

   /**
    * @see se.anatom.ejbca.ra.IUserAdminSessionRemote
    */
     public Collection query(Admin admin, Query query, String caauthorizationstring, String endentityprofilestring) throws IllegalQueryException;

   /**
    * @see se.anatom.ejbca.ra.IUserAdminSessionRemote
    */
    public boolean checkForCAId(Admin admin, int caid);
          
   /**
    * @see se.anatom.ejbca.ra.IUserAdminSessionRemote
    */
    public boolean checkForEndEntityProfileId(Admin admin, int endentityprofileid);

    /**
    * @see se.anatom.ejbca.ra.IUserAdminSessionRemote
    */
    public boolean checkForCertificateProfileId(Admin admin, int certificaterofileid);

	/**
	* @see se.anatom.ejbca.ra.IUserAdminSessionRemote
	*/
	public boolean checkForHardTokenProfileId(Admin admin, int profileid);

	/**
    * @see se.anatom.ejbca.ra.IUserAdminSessionRemote
    */
	public boolean existsUser(Admin admin, String username);


}

