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

package se.anatom.ejbca.protocol.cmp;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.rmi.RemoteException;
import java.security.KeyPair;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Iterator;

import javax.ejb.CreateException;
import javax.ejb.DuplicateKeyException;
import javax.ejb.FinderException;
import javax.naming.Context;
import javax.naming.NamingException;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.DEROutputStream;
import org.ejbca.core.ejb.ca.caadmin.ICAAdminSessionHome;
import org.ejbca.core.ejb.ca.caadmin.ICAAdminSessionRemote;
import org.ejbca.core.ejb.ra.IUserAdminSessionHome;
import org.ejbca.core.ejb.ra.IUserAdminSessionRemote;
import org.ejbca.core.model.SecConst;
import org.ejbca.core.model.approval.ApprovalException;
import org.ejbca.core.model.approval.WaitingForApprovalException;
import org.ejbca.core.model.authorization.AuthorizationDeniedException;
import org.ejbca.core.model.ca.caadmin.CAInfo;
import org.ejbca.core.model.ca.catoken.CATokenConstants;
import org.ejbca.core.model.log.Admin;
import org.ejbca.core.model.ra.UserDataConstants;
import org.ejbca.core.model.ra.raadmin.UserDoesntFullfillEndEntityProfile;
import org.ejbca.core.protocol.cmp.CmpMessageHelper;
import org.ejbca.util.Base64;
import org.ejbca.util.CertTools;
import org.ejbca.util.KeyTools;

import com.novosec.pkix.asn1.cmp.PKIMessage;

/**
 * requires setup of the CMP with a tcp listener on port 5547
 * mode=ra, responseProtection=signature, authenticationsecret=password, allowraverifypopo=true.
 * @author tomas
 * @version $Id: CrmfRATcpRequestTest.java,v 1.8 2008-01-11 13:15:20 anatom Exp $
 */
public class CrmfRATcpRequestTest extends CmpTestCase {
	
    private static Logger log = Logger.getLogger(CrmfRATcpRequestTest.class);

    private static final String PBEPASSWORD = "password";
    
    private static String userDN = "CN=tomas1,UID=tomas2,O=PrimeKey Solutions AB,C=SE";
    private static String issuerDN = "CN=AdminCA1,O=EJBCA Sample,C=SE";
    private KeyPair keys = null;  

    private static IUserAdminSessionRemote usersession;
    private static int caid = 0;
    private static Admin admin;
    private static X509Certificate cacert = null;

	public CrmfRATcpRequestTest(String arg0) throws NamingException, RemoteException, CreateException, CertificateEncodingException, CertificateException {
		super(arg0);
        admin = new Admin(Admin.TYPE_BATCHCOMMANDLINE_USER);
		CertTools.installBCProvider();
		Context ctx = getInitialContext();
        Object obj = ctx.lookup("CAAdminSession");
        ICAAdminSessionHome cahome = (ICAAdminSessionHome) javax.rmi.PortableRemoteObject.narrow(obj, ICAAdminSessionHome.class);
        ICAAdminSessionRemote casession = cahome.create();
        // Try to use AdminCA1 if it exists
        CAInfo adminca1 = casession.getCAInfo(admin, "AdminCA1");
        if (adminca1 == null) {
            Collection caids = casession.getAvailableCAs(admin);
            Iterator iter = caids.iterator();
            while (iter.hasNext()) {
            	caid = ((Integer) iter.next()).intValue();
            }        	
        } else {
        	caid = adminca1.getCAId();
        }
        if (caid == 0) {
        	assertTrue("No active CA! Must have at least one active CA to run tests!", false);
        }        	
        CAInfo cainfo = casession.getCAInfo(admin, caid);
        Collection certs = cainfo.getCertificateChain();
        if (certs.size() > 0) {
            Iterator certiter = certs.iterator();
            X509Certificate cert = (X509Certificate) certiter.next();
            String subject = CertTools.getSubjectDN(cert);
            if (StringUtils.equals(subject, cainfo.getSubjectDN())) {
                // Make sure we have a BC certificate
                cacert = CertTools.getCertfromByteArray(cert.getEncoded());            	
            }
        } else {
            log.error("NO CACERT for caid " + caid);
        }
        obj = ctx.lookup("UserAdminSession");
        IUserAdminSessionHome userhome = (IUserAdminSessionHome) javax.rmi.PortableRemoteObject.narrow(obj, IUserAdminSessionHome.class);
        usersession = userhome.create();
        
        issuerDN = cacert.getIssuerDN().getName();
	}
	
    private Context getInitialContext() throws NamingException {
        log.debug(">getInitialContext");
        Context ctx = new javax.naming.InitialContext();
        log.debug("<getInitialContext");
        return ctx;
    }
	protected void setUp() throws Exception {
		super.setUp();
		if (keys == null) {
			keys = KeyTools.genKeys("512", CATokenConstants.KEYALGORITHM_RSA);
		}
	}
	
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void test01CrmfHttpUnknowUser() throws Exception {
        // A name that does not exis
	    userDN = "CN=abc123rry5774466, O=PrimeKey Solutions AB, C=SE";

		byte[] nonce = CmpMessageHelper.createSenderNonce();
		byte[] transid = CmpMessageHelper.createSenderNonce();
		
        PKIMessage one = genCertReq(issuerDN, userDN, keys, cacert, nonce, transid, true, null);
        PKIMessage req = protectPKIMessage(one, false, PBEPASSWORD);
        int reqId = req.getBody().getIr().getCertReqMsg(0).getCertReq().getCertReqId().getValue().intValue();
		assertNotNull(req);
		ByteArrayOutputStream bao = new ByteArrayOutputStream();
		DEROutputStream out = new DEROutputStream(bao);
		out.writeObject(req);
		byte[] ba = bao.toByteArray();
		// Send request and receive response
		byte[] resp = sendCmpTcp(ba, 5);
		assertNotNull(resp);
		assertTrue(resp.length > 0);
		checkCmpResponseGeneral(resp, issuerDN, userDN, cacert, nonce, transid, true, false);
		checkCmpCertRepMessage(userDN, cacert, resp, reqId);
	}
	
	
	public void test02CrmfHttpOkUser() throws Exception {

		// Create a new good user
		createCmpUser();

		byte[] nonce = CmpMessageHelper.createSenderNonce();
		byte[] transid = CmpMessageHelper.createSenderNonce();
		
        PKIMessage one = genCertReq(issuerDN, userDN, keys, cacert, nonce, transid, true, null);
        PKIMessage req = protectPKIMessage(one, false, PBEPASSWORD);

        int reqId = req.getBody().getIr().getCertReqMsg(0).getCertReq().getCertReqId().getValue().intValue();
		assertNotNull(req);
		ByteArrayOutputStream bao = new ByteArrayOutputStream();
		DEROutputStream out = new DEROutputStream(bao);
		out.writeObject(req);
		byte[] ba = bao.toByteArray();
		// Send request and receive response
		byte[] resp = sendCmpTcp(ba, 5);
		assertNotNull(resp);
		assertTrue(resp.length > 0);
		checkCmpResponseGeneral(resp, issuerDN, userDN, cacert, nonce, transid, true, false);
		checkCmpCertRepMessage(userDN, cacert, resp, reqId);
		
		// Send a confirm message to the CA
		String hash = "foo123";
        PKIMessage confirm = genCertConfirm(userDN, cacert, nonce, transid, hash, reqId);
		assertNotNull(confirm);
		bao = new ByteArrayOutputStream();
		out = new DEROutputStream(bao);
		out.writeObject(confirm);
		ba = bao.toByteArray();
		// Send request and receive response
		resp = sendCmpTcp(ba, 5);
		assertNotNull(resp);
		assertTrue(resp.length > 0);
		checkCmpResponseGeneral(resp, issuerDN, userDN, cacert, nonce, transid, false, false);
		checkCmpPKIConfirmMessage(userDN, cacert, resp);
	}
	
	public void test03BlueXCrmf() throws Exception {
		PKIMessage req = PKIMessage.getInstance(new ASN1InputStream(new ByteArrayInputStream(bluexir)).readObject());
		byte[] resp = sendCmpTcp(bluexir, 5);
		userDN="CN=Some Common Name"; // we know what it is in this request...
		assertNotNull(resp);
		byte[] senderNonce = req.getHeader().getSenderNonce().getOctets();
		byte[] transId = req.getHeader().getTransactionID().getOctets();
        int reqId = req.getBody().getIr().getCertReqMsg(0).getCertReq().getCertReqId().getValue().intValue();
		checkCmpResponseGeneral(resp, issuerDN, "CN=Some Common Name", cacert, senderNonce, transId, true, false);
		checkCmpCertRepMessage(userDN, cacert, resp, reqId);
	}
	
	public void test04CrmfUnuahtenticated() throws Exception {

		byte[] nonce = CmpMessageHelper.createSenderNonce();
		byte[] transid = CmpMessageHelper.createSenderNonce();
		
        PKIMessage req = genCertReq(issuerDN, userDN, keys, cacert, nonce, transid, true, null);

		assertNotNull(req);
		ByteArrayOutputStream bao = new ByteArrayOutputStream();
		DEROutputStream out = new DEROutputStream(bao);
		out.writeObject(req);
		byte[] ba = bao.toByteArray();
		// Send request and receive response
		byte[] resp = sendCmpTcp(ba, 5);
		assertNotNull(resp);
		assertTrue(resp.length > 0);
		checkCmpResponseGeneral(resp, issuerDN, userDN, cacert, nonce, transid, false, false);
		checkCmpPKIErrorMessage(resp, issuerDN, userDN, 2, "Received an unathenticated message in RA mode.");
	}

	public void test05CrmfUnknownProtection() throws Exception {

		byte[] nonce = CmpMessageHelper.createSenderNonce();
		byte[] transid = CmpMessageHelper.createSenderNonce();
		
        PKIMessage one = genCertReq(issuerDN, userDN, keys, cacert, nonce, transid, true, null);
        PKIMessage req = protectPKIMessage(one, true, PBEPASSWORD);

		assertNotNull(req);
		ByteArrayOutputStream bao = new ByteArrayOutputStream();
		DEROutputStream out = new DEROutputStream(bao);
		out.writeObject(req);
		byte[] ba = bao.toByteArray();
		// Send request and receive response
		byte[] resp = sendCmpTcp(ba, 5);
		assertNotNull(resp);
		assertTrue(resp.length > 0);
		checkCmpResponseGeneral(resp, issuerDN, userDN, cacert, nonce, transid, false, false);
		checkCmpPKIErrorMessage(resp, issuerDN, userDN, 2, "Received CMP message with unknown protection alg: 1.2.840.113533.7.66.13.7.");
	}

    
    //
    // Private helper methods
    //
    private void createCmpUser() throws RemoteException, AuthorizationDeniedException, FinderException, UserDoesntFullfillEndEntityProfile, ApprovalException, WaitingForApprovalException {
        // Make user that we know...
        boolean userExists = false;
		userDN = "C=SE,O=PrimeKey,CN=cmptest";
        try {
            usersession.addUser(admin,"cmptest","foo123",userDN,null,"cmptest@primekey.se",false,SecConst.EMPTY_ENDENTITYPROFILE,SecConst.CERTPROFILE_FIXED_ENDUSER,SecConst.USER_ENDUSER,SecConst.TOKEN_SOFT_PEM,0,caid);
            log.debug("created user: cmptest, foo123, "+userDN);
        } catch (RemoteException re) {
            if (re.detail instanceof DuplicateKeyException) {
                userExists = true;
            }
        } catch (DuplicateKeyException dke) {
            userExists = true;
        }

        if (userExists) {
            log.debug("User cmptest already exists.");
            usersession.setUserStatus(admin,"cmptest",UserDataConstants.STATUS_NEW);
            log.debug("Reset status to NEW");
        }
        
    }

    static byte[] bluexir = Base64.decode(("MIICIjCB1AIBAqQCMACkVjBUMQswCQYDVQQGEwJOTDEbMBkGA1UEChMSQS5FLlQu"+
		"IEV1cm9wZSBCLlYuMRQwEgYDVQQLEwtEZXZlbG9wbWVudDESMBAGA1UEAxMJVGVz"+
		"dCBDQSAxoT4wPAYJKoZIhvZ9B0INMC8EEAK/H7Do+55N724Kdvxm7NcwCQYFKw4D"+
		"AhoFAAICA+gwDAYIKwYBBQUIAQIFAKILBAlzc2xjbGllbnSkEgQQpFpBsonfhnW8"+
		"ia1otGchraUSBBAyzd3nkKAzcJqGFrDw0jkYoIIBLjCCASowggEmMIIBIAIBADCC"+
		"ARmkJqARGA8yMDA2MDkxOTE2MTEyNlqhERgPMjAwOTA2MTUxNjExMjZapR0wGzEZ"+
		"MBcGA1UEAwwQU29tZSBDb21tb24gTmFtZaaBoDANBgkqhkiG9w0BAQEFAAOBjgAw"+
		"gYoCgYEAuBgTGPgXrS3AIPN6iXO6LNf5GzAcb/WZhvebXMdxdrMo9+5hw/Le5St/"+
		"Sz4J93rxU95b2LMuHTg8U6njxC2lZarNExZTdEwnI37X6ep7lq1purq80zD9bFXj"+
		"ougRD5MHfhDUAQC+btOgEXkanoAo8St3cbtHoYUacAXN2Zs/RVcCBAABAAGpLTAr"+
		"BgNVHREEJDAioCAGCisGAQQBgjcUAgOgEgwQdXBuQGFldGV1cm9wZS5ubIAAoBcD"+
		"FQAy/vSoNUevcdUxXkCQx3fvxkjh6A==").getBytes());

}
