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

package se.anatom.ejbca.ca.caadmin;

import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.bouncycastle.jce.provider.JCEECPublicKey;
import org.ejbca.core.model.SecConst;
import org.ejbca.core.model.ca.caadmin.CAExistsException;
import org.ejbca.core.model.ca.caadmin.CAInfo;
import org.ejbca.core.model.ca.caadmin.X509CAInfo;
import org.ejbca.core.model.ca.caadmin.extendedcaservices.ExtendedCAServiceInfo;
import org.ejbca.core.model.ca.caadmin.extendedcaservices.OCSPCAServiceInfo;
import org.ejbca.core.model.ca.caadmin.extendedcaservices.XKMSCAServiceInfo;
import org.ejbca.core.model.ca.catoken.CATokenConstants;
import org.ejbca.core.model.ca.catoken.CATokenInfo;
import org.ejbca.core.model.ca.catoken.SoftCATokenInfo;
import org.ejbca.core.model.ca.certificateprofiles.CertificatePolicy;
import org.ejbca.core.model.log.Admin;
import org.ejbca.util.CertTools;
import org.ejbca.util.TestTools;

/**
 * Tests the ca data entity bean.
 *
 * @version $Id: TestCAs.java,v 1.28.2.1 2008-04-09 22:46:29 anatom Exp $
 */
public class TestCAs extends TestCase {
    private static final Logger log = Logger.getLogger(TestCAs.class);
    private static final Admin admin = new Admin(Admin.TYPE_INTERNALUSER);
    
    /**
     * Creates a new TestCAs object.
     *
     * @param name name
     */
    public TestCAs(String name) {
        super(name);
        CertTools.installBCProvider();
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }

    /**
     * adds a CA using RSA keys to the database.
     *
     * It also checks that the CA is stored correctly.
     *
     * @throws Exception error
     */
    public void test01AddRSACA() throws Exception {
        log.debug(">test01AddRSACA()");
        boolean ret = false;
        try {
        	TestTools.removeTestCA();	// We cant be sure this CA was not left over from some other failed test
            TestTools.getAuthorizationSession().initialize(admin, TestTools.getTestCAId());
            SoftCATokenInfo catokeninfo = new SoftCATokenInfo();
            catokeninfo.setSignKeySpec("1024");
            catokeninfo.setEncKeySpec("1024");
            catokeninfo.setSignKeyAlgorithm(SoftCATokenInfo.KEYALGORITHM_RSA);
            catokeninfo.setEncKeyAlgorithm(SoftCATokenInfo.KEYALGORITHM_RSA);
            catokeninfo.setSignatureAlgorithm(CATokenInfo.SIGALG_SHA1_WITH_RSA);
            catokeninfo.setEncryptionAlgorithm(CATokenInfo.SIGALG_SHA1_WITH_RSA);
            // Create and active OSCP CA Service.
            ArrayList extendedcaservices = new ArrayList();
            extendedcaservices.add(new OCSPCAServiceInfo(ExtendedCAServiceInfo.STATUS_ACTIVE,
                    "CN=OCSPSignerCertificate, " + "CN=TEST",
                    "",
                    "1024",
                    CATokenConstants.KEYALGORITHM_RSA));
            extendedcaservices.add(new XKMSCAServiceInfo(ExtendedCAServiceInfo.STATUS_INACTIVE,
                    "CN=XKMSCertificate, " + "CN=TEST",
                    "",
                    "1024",
                    CATokenConstants.KEYALGORITHM_RSA));


            X509CAInfo cainfo = new X509CAInfo("CN=TEST",
                    "TEST", SecConst.CA_ACTIVE, new Date(),
                    "", SecConst.CERTPROFILE_FIXED_ROOTCA,
                    3650,
                    null, // Expiretime
                    CAInfo.CATYPE_X509,
                    CAInfo.SELFSIGNED,
                    (Collection) null,
                    catokeninfo,
                    "JUnit RSA CA",
                    -1, null,
                    null, // PolicyId
                    24, // CRLPeriod
                    0, // CRLIssueInterval
                    10, // CRLOverlapTime
                    10, // Delta CRL period
                    new ArrayList(),
                    true, // Authority Key Identifier
                    false, // Authority Key Identifier Critical
                    true, // CRL Number
                    false, // CRL Number Critical
                    null, // defaultcrldistpoint 
                    null, // defaultcrlissuer 
                    null, // defaultocsplocator
                    null, // defaultfreshestcrl
                    true, // Finish User
                    extendedcaservices,
                    false, // use default utf8 settings
                    new ArrayList(), // Approvals Settings
                    1, // Number of Req approvals
                    false, // Use UTF8 subject DN by default
            		true, // Use LDAP DN order by default
            		false, // Use CRL Distribution Point on CRL
            		false,  // CRL Distribution Point on CRL critical
            		true);

            TestTools.getCAAdminSession().createCA(admin, cainfo);


            CAInfo info = TestTools.getCAAdminSession().getCAInfo(admin, "TEST");

            X509Certificate cert = (X509Certificate) info.getCertificateChain().iterator().next();
            assertTrue("Error in created ca certificate", cert.getSubjectDN().toString().equals("CN=TEST"));
            assertTrue("Creating CA failed", info.getSubjectDN().equals("CN=TEST"));
            PublicKey pk = cert.getPublicKey();
            if (pk instanceof RSAPublicKey) {
            	RSAPublicKey rsapk = (RSAPublicKey) pk;
				assertEquals(rsapk.getAlgorithm(), "RSA");
			} else {
				assertTrue("Public key is not EC", false);
			}
            assertTrue("CA is not valid for the specified duration.",cert.getNotAfter().after(new Date(new Date().getTime()+10*364*24*60*60*1000L)) && cert.getNotAfter().before(new Date(new Date().getTime()+10*366*24*60*60*1000L)));
            ret = true;
        } catch (CAExistsException pee) {
            log.info("CA exists.");
        }

        assertTrue("Creating RSA CA failed", ret);
        log.debug("<test01AddRSACA()");
    }

    /**
     * renames CA in database.
     *
     * @throws Exception error
     */
    public void test02RenameCA() throws Exception {
        log.debug(">test02RenameCA()");

        boolean ret = false;
        try {
            TestTools.getCAAdminSession().renameCA(admin, "TEST", "TEST2");
            TestTools.getCAAdminSession().renameCA(admin, "TEST2", "TEST");
            ret = true;
        } catch (CAExistsException cee) {
        }
        assertTrue("Renaming CA failed", ret);

        log.debug("<test02RenameCA()");
    }


    /**
     * edits ca and checks that it's stored correctly.
     *
     * @throws Exception error
     */
    public void test03EditCA() throws Exception {
        log.debug(">test03EditCA()");

        X509CAInfo info = (X509CAInfo) TestTools.getCAAdminSession().getCAInfo(admin, "TEST");
        info.setCRLPeriod(33);
        TestTools.getCAAdminSession().editCA(admin, info);
        X509CAInfo info2 = (X509CAInfo) TestTools.getCAAdminSession().getCAInfo(admin, "TEST");
        assertTrue("Editing CA failed", info2.getCRLPeriod() == 33);

        log.debug("<test03EditCA()");
    }

    /**
     * adds a CA Using ECDSA keys to the database.
     *
     * It also checks that the CA is stored correctly.
     *
     * @throws Exception error
     */
    public void test04AddECDSACA() throws Exception {
        log.debug(">test04AddECDSACA()");
        boolean ret = false;
        try {
        	TestTools.getAuthorizationSession().initialize(admin, "CN=TESTECDSA".hashCode());

            SoftCATokenInfo catokeninfo = new SoftCATokenInfo();
            catokeninfo.setSignKeySpec("prime192v1");
            catokeninfo.setEncKeySpec("1024");
            catokeninfo.setSignKeyAlgorithm(SoftCATokenInfo.KEYALGORITHM_ECDSA);
            catokeninfo.setEncKeyAlgorithm(SoftCATokenInfo.KEYALGORITHM_RSA);
            catokeninfo.setSignatureAlgorithm(CATokenInfo.SIGALG_SHA256_WITH_ECDSA);
            catokeninfo.setEncryptionAlgorithm(CATokenInfo.SIGALG_SHA1_WITH_RSA);
            // Create and active OSCP CA Service.
            ArrayList extendedcaservices = new ArrayList();
            extendedcaservices.add(new OCSPCAServiceInfo(ExtendedCAServiceInfo.STATUS_ACTIVE,
                    "CN=OCSPSignerCertificate, " + "CN=TESTECDSA",
                    "",
                    "prime192v1",
                    CATokenConstants.KEYALGORITHM_ECDSA));
            extendedcaservices.add(new XKMSCAServiceInfo(ExtendedCAServiceInfo.STATUS_INACTIVE,
                    "CN=XKMSSignerCertificate, " + "CN=TESTECDSA",
                    "",
                    "prime192v1",
                    CATokenConstants.KEYALGORITHM_ECDSA));

            ArrayList policies = new ArrayList(1);
            policies.add(new CertificatePolicy("2.5.29.32.0", "", ""));
            
            X509CAInfo cainfo = new X509CAInfo("CN=TESTECDSA",
                    "TESTECDSA", SecConst.CA_ACTIVE, new Date(),
                    "", SecConst.CERTPROFILE_FIXED_ROOTCA,
                    365,
                    null, // Expiretime
                    CAInfo.CATYPE_X509,
                    CAInfo.SELFSIGNED,
                    (Collection) null,
                    catokeninfo,
                    "JUnit ECDSA CA",
                    -1, null,
                    policies, // PolicyId
                    24, // CRLPeriod
                    0, // CRLIssueInterval
                    10, // CRLOverlapTime
                    0, // Delta CRL period
                    new ArrayList(),
                    true, // Authority Key Identifier
                    false, // Authority Key Identifier Critical
                    true, // CRL Number
                    false, // CRL Number Critical
                    null, // defaultcrldistpoint 
                    null, // defaultcrlissuer 
                    null, // defaultocsplocator
                    null, // defaultfreshestcrl
                    true, // Finish User
                    extendedcaservices,
                    false, // use default utf8 settings
                    new ArrayList(), // Approvals Settings
                    1, // Number of Req approvals
                    false, // Use UTF8 subject DN by default
                    true, // Use LDAP DN order by default
                    false, // Use CRL Distribution Point on CRL
                    false,  // CRL Distribution Point on CRL critical
                    true // include in Health Check
                    );


            TestTools.getCAAdminSession().createCA(admin, cainfo);


            CAInfo info = TestTools.getCAAdminSession().getCAInfo(admin, "TESTECDSA");

            X509Certificate cert = (X509Certificate) info.getCertificateChain().iterator().next();
            assertTrue("Error in created ca certificate", cert.getSubjectDN().toString().equals("CN=TESTECDSA"));
            assertTrue("Creating CA failed", info.getSubjectDN().equals("CN=TESTECDSA"));
            PublicKey pk = cert.getPublicKey();
            if (pk instanceof JCEECPublicKey) {
				JCEECPublicKey ecpk = (JCEECPublicKey) pk;
				assertEquals(ecpk.getAlgorithm(), "EC");
				org.bouncycastle.jce.spec.ECParameterSpec spec = ecpk.getParameters();
				assertNotNull("ImplicitlyCA must have null spec", spec);
			} else {
				assertTrue("Public key is not EC", false);
			}

            ret = true;
        } catch (CAExistsException pee) {
            log.info("CA exists.");
        }

        assertTrue("Creating ECDSA CA failed", ret);
        log.debug("<test04AddECDSACA()");
    }

    /**
     * adds a CA Using ECDSA 'implicitlyCA' keys to the database.
     *
     * It also checks that the CA is stored correctly.
     *
     * @throws Exception error
     */
    public void test05AddECDSAImplicitlyCACA() throws Exception {
        log.debug(">test05AddECDSAImplicitlyCACA()");
        boolean ret = false;
        try {
        	TestTools.getAuthorizationSession().initialize(admin, "CN=TESTECDSAImplicitlyCA".hashCode());

            SoftCATokenInfo catokeninfo = new SoftCATokenInfo();
            catokeninfo.setSignKeySpec("implicitlyCA");
            catokeninfo.setEncKeySpec("1024");
            catokeninfo.setSignKeyAlgorithm(SoftCATokenInfo.KEYALGORITHM_ECDSA);
            catokeninfo.setEncKeyAlgorithm(SoftCATokenInfo.KEYALGORITHM_RSA);
            catokeninfo.setSignatureAlgorithm(CATokenInfo.SIGALG_SHA256_WITH_ECDSA);
            catokeninfo.setEncryptionAlgorithm(CATokenInfo.SIGALG_SHA1_WITH_RSA);
            // Create and active OSCP CA Service.
            ArrayList extendedcaservices = new ArrayList();
            extendedcaservices.add(new OCSPCAServiceInfo(ExtendedCAServiceInfo.STATUS_ACTIVE,
                    "CN=OCSPSignerCertificate, " + "CN=TESTECDSAImplicitlyCA",
                    "",
                    "prime192v1",
                    CATokenConstants.KEYALGORITHM_ECDSA));
            
            extendedcaservices.add(new XKMSCAServiceInfo(ExtendedCAServiceInfo.STATUS_INACTIVE,
                    "CN=XKMSCertificate, " + "CN=TESTECDSAImplicitlyCA",
                    "",
                    "prime192v1",
                    CATokenConstants.KEYALGORITHM_ECDSA));

            ArrayList policies = new ArrayList(1);
            policies.add(new CertificatePolicy("2.5.29.32.0", "", ""));
            
            X509CAInfo cainfo = new X509CAInfo("CN=TESTECDSAImplicitlyCA",
                    "TESTECDSAImplicitlyCA", SecConst.CA_ACTIVE, new Date(),
                    "", SecConst.CERTPROFILE_FIXED_ROOTCA,
                    365,
                    null, // Expiretime
                    CAInfo.CATYPE_X509,
                    CAInfo.SELFSIGNED,
                    (Collection) null,
                    catokeninfo,
                    "JUnit ECDSA ImplicitlyCA CA",
                    -1, null,
                    policies, // PolicyId
                    24, // CRLPeriod
                    0, // CRLIssueInterval
                    10, // CRLOverlapTime
                    0, // Delta CRL period
                    new ArrayList(),
                    true, // Authority Key Identifier
                    false, // Authority Key Identifier Critical
                    true, // CRL Number
                    false, // CRL Number Critical
                    null, // defaultcrldistpoint 
                    null, // defaultcrlissuer 
                    null, // defaultocsplocator
                    null, // defaultfreshestcrl
                    true, // Finish User
                    extendedcaservices,
                    false, // use default utf8 settings
                    new ArrayList(), // Approvals Settings
                    1, // Number of Req approvals
                    false, // Use UTF8 subject DN by default 
                    true, // Use LDAP DN order by default
                    false, // Use CRL Distribution Point on CRL
                    false,  // CRL Distribution Point on CRL critical
                    true // Include in healthCheck
            );

            TestTools.getCAAdminSession().createCA(admin, cainfo);


            CAInfo info = TestTools.getCAAdminSession().getCAInfo(admin, "TESTECDSAImplicitlyCA");

            X509Certificate cert = (X509Certificate) info.getCertificateChain().iterator().next();
            assertTrue("Error in created ca certificate", cert.getSubjectDN().toString().equals("CN=TESTECDSAImplicitlyCA"));
            assertTrue("Creating CA failed", info.getSubjectDN().equals("CN=TESTECDSAImplicitlyCA"));
            PublicKey pk = cert.getPublicKey();
            if (pk instanceof JCEECPublicKey) {
				JCEECPublicKey ecpk = (JCEECPublicKey) pk;
				assertEquals(ecpk.getAlgorithm(), "EC");
				org.bouncycastle.jce.spec.ECParameterSpec spec = ecpk.getParameters();
				assertNull("ImplicitlyCA must have null spec", spec);
				
			} else {
				assertTrue("Public key is not EC", false);
			}

            ret = true;
        } catch (CAExistsException pee) {
            log.info("CA exists.");
        }

        assertTrue("Creating ECDSA ImplicitlyCA CA failed", ret);
        log.debug("<test05AddECDSAImplicitlyCACA()");
    }

    /**
     * adds a CA using RSA keys to the database.
     *
     * It also checks that the CA is stored correctly.
     *
     * @throws Exception error
     */
    public void test06AddRSASha256WithMGF1CA() throws Exception {
        log.debug(">test06AddRSASha256WithMGF1CA()");
        boolean ret = false;
        try {
        	String cadn = "CN=TESTSha256WithMGF1";

        	TestTools.getAuthorizationSession().initialize(admin, cadn.hashCode());

            SoftCATokenInfo catokeninfo = new SoftCATokenInfo();
            catokeninfo.setSignKeySpec("1024");
            catokeninfo.setEncKeySpec("1024");
            catokeninfo.setSignKeyAlgorithm(SoftCATokenInfo.KEYALGORITHM_RSA);
            catokeninfo.setEncKeyAlgorithm(SoftCATokenInfo.KEYALGORITHM_RSA);
            catokeninfo.setSignatureAlgorithm(CATokenInfo.SIGALG_SHA256_WITH_RSA_AND_MGF1);
            catokeninfo.setEncryptionAlgorithm(CATokenInfo.SIGALG_SHA256_WITH_RSA_AND_MGF1);
            // Create and active OSCP CA Service.
            ArrayList extendedcaservices = new ArrayList();
            extendedcaservices.add(new OCSPCAServiceInfo(ExtendedCAServiceInfo.STATUS_ACTIVE,
                    "CN=OCSPSignerCertificate, " + cadn,
                    "",
                    "1024",
                    CATokenConstants.KEYALGORITHM_RSA));
            extendedcaservices.add(new XKMSCAServiceInfo(ExtendedCAServiceInfo.STATUS_INACTIVE,
                    "CN=XKMSCertificate, " + cadn,
                    "",
                    "1024",
                    CATokenConstants.KEYALGORITHM_RSA));


            X509CAInfo cainfo = new X509CAInfo(cadn,
                    "TESTSha256WithMGF1", SecConst.CA_ACTIVE, new Date(),
                    "", SecConst.CERTPROFILE_FIXED_ROOTCA,
                    365,
                    null, // Expiretime
                    CAInfo.CATYPE_X509,
                    CAInfo.SELFSIGNED,
                    (Collection) null,
                    catokeninfo,
                    "JUnit RSA CA",
                    -1, null,
                    null, // PolicyId
                    24, // CRLPeriod
                    0, // CRLIssueInterval
                    10, // CRLOverlapTime
                    0, // Delta CRL period
                    new ArrayList(),
                    true, // Authority Key Identifier
                    false, // Authority Key Identifier Critical
                    true, // CRL Number
                    false, // CRL Number Critical
                    null, // defaultcrldistpoint 
                    null, // defaultcrlissuer 
                    null, // defaultocsplocator
                    null, // defaultfreshestcrl
                    true, // Finish User
                    extendedcaservices,
                    false, // use default utf8 settings
                    new ArrayList(), // Approvals Settings
                    1, // Number of Req approvals
                    false, // Use UTF8 subject DN by default
                    true, // Use LDAP DN order by default
                    false, // Use CRL Distribution Point on CRL
                    false,  // CRL Distribution Point on CRL critical
                    true // Include in healthCheck
                    );
            TestTools.getCAAdminSession().createCA(admin, cainfo);

            CAInfo info = TestTools.getCAAdminSession().getCAInfo(admin, "TESTSha256WithMGF1");

            X509Certificate cert = (X509Certificate) info.getCertificateChain().iterator().next();
            assertTrue("Error in created ca certificate", cert.getSubjectDN().toString().equals(cadn));
            assertTrue("Creating CA failed", info.getSubjectDN().equals(cadn));
            PublicKey pk = cert.getPublicKey();
            if (pk instanceof RSAPublicKey) {
            	RSAPublicKey rsapk = (RSAPublicKey) pk;
				assertEquals(rsapk.getAlgorithm(), "RSA");
			} else {
				assertTrue("Public key is not RSA", false);
			}

            ret = true;
        } catch (CAExistsException pee) {
            log.info("CA exists.");
        }

        assertTrue("Creating RSA CA failed", ret);
        log.debug("<test06AddRSASha256WithMGF1CA()");
    }

    public void test07AddRSACA4096() throws Exception {
        log.debug(">test07AddRSACA4096()");
        boolean ret = false;
        try {
        	String dn = CertTools.stringToBCDNString("CN=TESTRSA4096,OU=FooBaaaaaar veeeeeeeery long ou,OU=Another very long very very long ou,O=FoorBar Very looong O,L=Lets ad a loooooooooooooooooong Locality as well,C=SE");
        	TestTools.getAuthorizationSession().initialize(admin, dn.hashCode());

            SoftCATokenInfo catokeninfo = new SoftCATokenInfo();
            catokeninfo.setSignKeySpec("4096");
            catokeninfo.setEncKeySpec("2048");
            catokeninfo.setSignKeyAlgorithm(SoftCATokenInfo.KEYALGORITHM_RSA);
            catokeninfo.setEncKeyAlgorithm(SoftCATokenInfo.KEYALGORITHM_RSA);
            catokeninfo.setSignatureAlgorithm(CATokenInfo.SIGALG_SHA1_WITH_RSA);
            catokeninfo.setEncryptionAlgorithm(CATokenInfo.SIGALG_SHA1_WITH_RSA);
            // Create and active OSCP CA Service.
            ArrayList extendedcaservices = new ArrayList();
            extendedcaservices.add(new OCSPCAServiceInfo(ExtendedCAServiceInfo.STATUS_ACTIVE,
                    "CN=OCSPSignerCertificate, " + dn,
                    "",
                    "2048",
                    CATokenConstants.KEYALGORITHM_RSA));
            extendedcaservices.add(new XKMSCAServiceInfo(ExtendedCAServiceInfo.STATUS_INACTIVE,
                    "CN=XKMSCertificate, " + dn,
                    "",
                    "2048",
                    CATokenConstants.KEYALGORITHM_RSA));


            X509CAInfo cainfo = new X509CAInfo(dn,
            		"TESTRSA4096", SecConst.CA_ACTIVE, new Date(),
            		"", SecConst.CERTPROFILE_FIXED_ROOTCA,
            		365,
            		null, // Expiretime
            		CAInfo.CATYPE_X509,
            		CAInfo.SELFSIGNED,
            		(Collection) null,
            		catokeninfo,
            		"JUnit RSA CA, we ned also a very long CA description for this CA, because we want to create a CA Data string that is more than 36000 characters or something like that. All this is because Oracle can not set very long strings with the JDBC provider and we must test that we can handle long CAs",
            		-1, null,
            		null, // PolicyId
            		24, // CRLPeriod
            		0, // CRLIssueInterval
            		10, // CRLOverlapTime
            		0, // Delta CRL period
            		new ArrayList(),
            		true, // Authority Key Identifier
            		false, // Authority Key Identifier Critical
            		true, // CRL Number
            		false, // CRL Number Critical
            		null, // defaultcrldistpoint 
            		null, // defaultcrlissuer 
            		null, // defaultocsplocator
            		null, // defaultfreshestcrl
            		true, // Finish User
            		extendedcaservices,
            		false, // use default utf8 settings
            		new ArrayList(), // Approvals Settings
            		1, // Number of Req approvals
            		false, // Use UTF8 subject DN by default
                    true, // Use LDAP DN order by default
                    false, // Use CRL Distribution Point on CRL
                    false,  // CRL Distribution Point on CRL critical
                    true // Include in HealthCheck
                    );

            TestTools.getCAAdminSession().createCA(admin, cainfo);


            CAInfo info = TestTools.getCAAdminSession().getCAInfo(admin, "TESTRSA4096");

            X509Certificate cert = (X509Certificate) info.getCertificateChain().iterator().next();
            assertTrue("Error in created ca certificate", CertTools.stringToBCDNString(cert.getSubjectDN().toString()).equals(dn));
            assertTrue("Creating CA failed", info.getSubjectDN().equals(dn));
            // Normal order
            assertEquals(cert.getSubjectX500Principal().getName(), "C=SE,L=Lets ad a loooooooooooooooooong Locality as well,O=FoorBar Very looong O,OU=Another very long very very long ou,OU=FooBaaaaaar veeeeeeeery long ou,CN=TESTRSA4096");
            PublicKey pk = cert.getPublicKey();
            if (pk instanceof RSAPublicKey) {
            	RSAPublicKey rsapk = (RSAPublicKey) pk;
				assertEquals(rsapk.getAlgorithm(), "RSA");
			} else {
				assertTrue("Public key is not EC", false);
			}

            ret = true;
        } catch (CAExistsException pee) {
            log.info("CA exists.");
        }

        assertTrue("Creating RSA CA 4096 failed", ret);
        log.debug("<test07AddRSACA4096()");
    }

    public void test08AddRSACAReverseDN() throws Exception {
        log.debug(">test08AddRSACAReverseDN()");
        boolean ret = false;
        try {
        	String dn = CertTools.stringToBCDNString("CN=TESTRSAReverse,O=FooBar,OU=BarFoo,C=SE");
        	String name = "TESTRSAREVERSE";
        	TestTools.getAuthorizationSession().initialize(admin, dn.hashCode());

            SoftCATokenInfo catokeninfo = new SoftCATokenInfo();
            catokeninfo.setSignKeySpec("1024");
            catokeninfo.setEncKeySpec("1024");
            catokeninfo.setSignKeyAlgorithm(SoftCATokenInfo.KEYALGORITHM_RSA);
            catokeninfo.setEncKeyAlgorithm(SoftCATokenInfo.KEYALGORITHM_RSA);
            catokeninfo.setSignatureAlgorithm(CATokenInfo.SIGALG_SHA1_WITH_RSA);
            catokeninfo.setEncryptionAlgorithm(CATokenInfo.SIGALG_SHA1_WITH_RSA);
            // Create and active OSCP CA Service.
            ArrayList extendedcaservices = new ArrayList();
            extendedcaservices.add(new OCSPCAServiceInfo(ExtendedCAServiceInfo.STATUS_ACTIVE,
                    "CN=OCSPSignerCertificate, " + dn,
                    "",
                    "1024",
                    CATokenConstants.KEYALGORITHM_RSA));
            extendedcaservices.add(new XKMSCAServiceInfo(ExtendedCAServiceInfo.STATUS_INACTIVE,
                    "CN=XKMSCertificate, " + dn,
                    "",
                    "1024",
                    CATokenConstants.KEYALGORITHM_RSA));


            X509CAInfo cainfo = new X509CAInfo(dn,
            		name, SecConst.CA_ACTIVE, new Date(),
            		"", SecConst.CERTPROFILE_FIXED_ROOTCA,
            		365,
            		null, // Expiretime
            		CAInfo.CATYPE_X509,
            		CAInfo.SELFSIGNED,
            		(Collection) null,
            		catokeninfo,
            		"JUnit RSA CA, we ned also a very long CA description for this CA, because we want to create a CA Data string that is more than 36000 characters or something like that. All this is because Oracle can not set very long strings with the JDBC provider and we must test that we can handle long CAs",
            		-1, null,
            		null, // PolicyId
            		24, // CRLPeriod
            		0, // CRLIssueInterval
            		10, // CRLOverlapTime
            		0, // Delta CRL period
            		new ArrayList(),
            		true, // Authority Key Identifier
            		false, // Authority Key Identifier Critical
            		true, // CRL Number
            		false, // CRL Number Critical
            		null, // defaultcrldistpoint 
            		null, // defaultcrlissuer 
            		null, // defaultocsplocator
            		null, // defaultfreshestcrl
            		true, // Finish User
            		extendedcaservices,
            		false, // use default utf8 settings
            		new ArrayList(), // Approvals Settings
            		1, // Number of Req approvals
            		false, // Use UTF8 subject DN by default
                    false, // Use X500 DN order
                    false, // Use CRL Distribution Point on CRL
                    false,  // CRL Distribution Point on CRL critical
                    true // Include in health check
                    );

            TestTools.getCAAdminSession().createCA(admin, cainfo);


            CAInfo info = TestTools.getCAAdminSession().getCAInfo(admin, name);

            X509Certificate cert = (X509Certificate) info.getCertificateChain().iterator().next();
            assertEquals("Error in created ca certificate", CertTools.stringToBCDNString(cert.getSubjectDN().toString()),dn);
            assertTrue("Creating CA failed", info.getSubjectDN().equals(dn));
            // reverse order
            assertEquals(cert.getSubjectX500Principal().getName(), "CN=TESTRSAReverse,OU=BarFoo,O=FooBar,C=SE");
            PublicKey pk = cert.getPublicKey();
            if (pk instanceof RSAPublicKey) {
            	RSAPublicKey rsapk = (RSAPublicKey) pk;
				assertEquals(rsapk.getAlgorithm(), "RSA");
			} else {
				assertTrue("Public key is not EC", false);
			}

            ret = true;
        } catch (CAExistsException pee) {
            log.info("CA exists.");
        }

        assertTrue("Creating RSA CA reverse failed", ret);
        log.debug("<test08AddRSACAReverseDN()");
    }
}