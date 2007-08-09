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
 
package org.ejbca.core.model.ca.caadmin;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.cert.CRL;
import java.security.cert.CRLException;
import java.security.cert.CertStore;
import java.security.cert.Certificate;
import java.security.cert.CollectionCertStoreParameters;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERIA5String;
import org.bouncycastle.asn1.DERObjectIdentifier;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERTaggedObject;
import org.bouncycastle.asn1.DERUTF8String;
import org.bouncycastle.asn1.x509.Attribute;
import org.bouncycastle.asn1.x509.AuthorityInformationAccess;
import org.bouncycastle.asn1.x509.AuthorityKeyIdentifier;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.CRLDistPoint;
import org.bouncycastle.asn1.x509.CRLNumber;
import org.bouncycastle.asn1.x509.DisplayText;
import org.bouncycastle.asn1.x509.DistributionPoint;
import org.bouncycastle.asn1.x509.DistributionPointName;
import org.bouncycastle.asn1.x509.ExtendedKeyUsage;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.asn1.x509.PolicyInformation;
import org.bouncycastle.asn1.x509.PolicyQualifierId;
import org.bouncycastle.asn1.x509.PolicyQualifierInfo;
import org.bouncycastle.asn1.x509.SubjectKeyIdentifier;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.asn1.x509.UserNotice;
import org.bouncycastle.asn1.x509.X509DefaultEntryConverter;
import org.bouncycastle.asn1.x509.X509Extensions;
import org.bouncycastle.asn1.x509.X509Name;
import org.bouncycastle.asn1.x509.X509NameEntryConverter;
import org.bouncycastle.asn1.x509.X509ObjectIdentifiers;
import org.bouncycastle.asn1.x509.qualified.ETSIQCObjectIdentifiers;
import org.bouncycastle.asn1.x509.qualified.Iso4217CurrencyCode;
import org.bouncycastle.asn1.x509.qualified.MonetaryValue;
import org.bouncycastle.asn1.x509.qualified.QCStatement;
import org.bouncycastle.asn1.x509.qualified.RFC3739QCObjectIdentifiers;
import org.bouncycastle.asn1.x509.qualified.SemanticsInformation;
import org.bouncycastle.cms.CMSEnvelopedData;
import org.bouncycastle.cms.CMSEnvelopedDataGenerator;
import org.bouncycastle.cms.CMSProcessable;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.cms.CMSSignedGenerator;
import org.bouncycastle.cms.RecipientInformation;
import org.bouncycastle.cms.RecipientInformationStore;
import org.bouncycastle.jce.X509KeyUsage;
import org.bouncycastle.x509.X509V2CRLGenerator;
import org.bouncycastle.x509.X509V3CertificateGenerator;
import org.ejbca.core.ejb.ca.sign.SernoGenerator;
import org.ejbca.core.model.InternalResources;
import org.ejbca.core.model.SecConst;
import org.ejbca.core.model.ca.SignRequestSignatureException;
import org.ejbca.core.model.ca.caadmin.extendedcaservices.CmsCAService;
import org.ejbca.core.model.ca.caadmin.extendedcaservices.CmsCAServiceInfo;
import org.ejbca.core.model.ca.caadmin.extendedcaservices.ExtendedCAServiceInfo;
import org.ejbca.core.model.ca.caadmin.extendedcaservices.ExtendedCAServiceNotActiveException;
import org.ejbca.core.model.ca.caadmin.extendedcaservices.ExtendedCAServiceRequest;
import org.ejbca.core.model.ca.caadmin.extendedcaservices.ExtendedCAServiceRequestException;
import org.ejbca.core.model.ca.caadmin.extendedcaservices.ExtendedCAServiceResponse;
import org.ejbca.core.model.ca.caadmin.extendedcaservices.IllegalExtendedCAServiceRequestException;
import org.ejbca.core.model.ca.caadmin.extendedcaservices.OCSPCAServiceRequest;
import org.ejbca.core.model.ca.caadmin.extendedcaservices.XKMSCAService;
import org.ejbca.core.model.ca.caadmin.extendedcaservices.XKMSCAServiceInfo;
import org.ejbca.core.model.ca.catoken.CAToken;
import org.ejbca.core.model.ca.catoken.CATokenConstants;
import org.ejbca.core.model.ca.catoken.CATokenOfflineException;
import org.ejbca.core.model.ca.catoken.NullCAToken;
import org.ejbca.core.model.ca.certextensions.CertificateExtension;
import org.ejbca.core.model.ca.certextensions.CertificateExtensionFactory;
import org.ejbca.core.model.ca.certificateprofiles.CertificateProfile;
import org.ejbca.core.model.ca.crl.RevokedCertInfo;
import org.ejbca.core.model.ra.UserDataVO;
import org.ejbca.util.CertTools;
import org.ejbca.util.cert.PrintableStringEntryConverter;
import org.ejbca.util.cert.SubjectDirAttrExtension;




/**
 * X509CA is a implementation of a CA and holds data specific for Certificate and CRL generation 
 * according to the X509 standard. 
 *
 * @version $Id: X509CA.java,v 1.50.2.8 2007-08-09 09:10:30 anatom Exp $
 */
public class X509CA extends CA implements Serializable {

    private static final Logger log = Logger.getLogger(X509CA.class);

    /** Internal localization of logs and errors */
    private static final InternalResources intres = InternalResources.getInstance();

    // Default Values
    public static final float LATEST_VERSION = 11;

    private byte[]  keyId = new byte[] { 1, 2, 3, 4, 5 };
    
    
    // protected fields.
    protected static final String POLICYID                       = "policyid";
    protected static final String SUBJECTALTNAME                 = "subjectaltname";
    protected static final String USEAUTHORITYKEYIDENTIFIER      = "useauthoritykeyidentifier";
    protected static final String AUTHORITYKEYIDENTIFIERCRITICAL = "authoritykeyidentifiercritical";
    protected static final String USECRLNUMBER                   = "usecrlnumber";
    protected static final String CRLNUMBERCRITICAL              = "crlnumbercritical";
    protected static final String DEFAULTCRLDISTPOINT            = "defaultcrldistpoint";
    protected static final String DEFAULTCRLISSUER               = "defaultcrlissuer";
    protected static final String DEFAULTOCSPSERVICELOCATOR      = "defaultocspservicelocator";
    protected static final String USEUTF8POLICYTEXT              = "useutf8policytext";
    protected static final String USEPRINTABLESTRINGSUBJECTDN    = "useprintablestringsubjectdn";

    // Public Methods
    /** Creates a new instance of CA, this constuctor should be used when a new CA is created */
    public X509CA(X509CAInfo cainfo) {
      super(cainfo);  
      
      data.put(POLICYID, cainfo.getPolicyId());
      data.put(SUBJECTALTNAME,  cainfo.getSubjectAltName());            
      setUseAuthorityKeyIdentifier(cainfo.getUseAuthorityKeyIdentifier());
      setAuthorityKeyIdentifierCritical(cainfo.getAuthorityKeyIdentifierCritical()); 
      setUseCRLNumber(cainfo.getUseCRLNumber());
      setCRLNumberCritical(cainfo.getCRLNumberCritical());
      setDefaultCRLDistPoint(cainfo.getDefaultCRLDistPoint());
      setDefaultCRLIssuer(cainfo.getDefaultCRLIssuer());
      setDefaultOCSPServiceLocator(cainfo.getDefaultOCSPServiceLocator());
      setFinishUser(cainfo.getFinishUser());
      setUseUTF8PolicyText(cainfo.getUseUTF8PolicyText());
      setUsePrintableStringSubjectDN(cainfo.getUsePrintableStringSubjectDN());
      
      data.put(CA.CATYPE, new Integer(CAInfo.CATYPE_X509));
      data.put(VERSION, new Float(LATEST_VERSION));   
    }
    
   /** Constructor used when retrieving existing X509CA from database. 
 * @throws IllegalKeyStoreException */
    public X509CA(HashMap data, int caId, String subjectDN, String name, int status, Date updateTime) throws IllegalKeyStoreException{
    	super(data);
    	ArrayList externalcaserviceinfos = new ArrayList();
    	Iterator iter = getExternalCAServiceTypes().iterator(); 	
    	while(iter.hasNext()){
    		ExtendedCAServiceInfo info = this.getExtendedCAServiceInfo(((Integer) iter.next()).intValue());
    		if (info != null) {
        		externalcaserviceinfos.add(info);  	    			
    		}
    	}
        CAInfo info = new X509CAInfo(subjectDN, name, status, updateTime, getSubjectAltName() ,getCertificateProfileId(),  
        		  getValidity(), getExpireTime(), getCAType(), getSignedBy(), getCertificateChain(),
        		  getCAToken(caId).getCATokenInfo(), getDescription(), getRevokationReason(), getRevokationDate(), getPolicyId(), getCRLPeriod(), getCRLIssueInterval(), getCRLOverlapTime(), getCRLPublishers(),
        		  getUseAuthorityKeyIdentifier(), getAuthorityKeyIdentifierCritical(),
        		  getUseCRLNumber(), getCRLNumberCritical(), getDefaultCRLDistPoint(), getDefaultCRLIssuer(), getDefaultOCSPServiceLocator(), getFinishUser(), externalcaserviceinfos, 
        		  getUseUTF8PolicyText(), getApprovalSettings(), getNumOfRequiredApprovals(), getUsePrintableStringSubjectDN());
        super.setCAInfo(info);
    }

    // Public Methods.
    public String getPolicyId(){ return (String) data.get(POLICYID);}
    public void setPolicyId(String policyid){ data.put(POLICYID, policyid);}
    
    public String getSubjectAltName() { return (String) data.get(SUBJECTALTNAME);}
    
    public boolean  getUseAuthorityKeyIdentifier(){
      return ((Boolean)data.get(USEAUTHORITYKEYIDENTIFIER)).booleanValue();
    }
    public void setUseAuthorityKeyIdentifier(boolean useauthoritykeyidentifier) {
      data.put(USEAUTHORITYKEYIDENTIFIER, Boolean.valueOf(useauthoritykeyidentifier));
    }
    
    public boolean  getAuthorityKeyIdentifierCritical(){
      return ((Boolean)data.get(AUTHORITYKEYIDENTIFIERCRITICAL)).booleanValue();
    }
    public void setAuthorityKeyIdentifierCritical(boolean authoritykeyidentifiercritical) {
      data.put(AUTHORITYKEYIDENTIFIERCRITICAL, Boolean.valueOf(authoritykeyidentifiercritical));
    }

    public boolean  getUseCRLNumber(){return ((Boolean)data.get(USECRLNUMBER)).booleanValue();}
    public void setUseCRLNumber(boolean usecrlnumber) {data.put(USECRLNUMBER, Boolean.valueOf(usecrlnumber));}
    
    public boolean  getCRLNumberCritical(){return ((Boolean)data.get(CRLNUMBERCRITICAL)).booleanValue();}
    public void setCRLNumberCritical(boolean crlnumbercritical) {data.put(CRLNUMBERCRITICAL, Boolean.valueOf(crlnumbercritical));}
    
    public String  getDefaultCRLDistPoint(){return (String) data.get(DEFAULTCRLDISTPOINT);}
    public void setDefaultCRLDistPoint(String defailtcrldistpoint) {
    	if(defailtcrldistpoint == null){
    		data.put(DEFAULTCRLDISTPOINT, "");
    	}else{
    		data.put(DEFAULTCRLDISTPOINT, defailtcrldistpoint);
    	}     
    }
    public String  getDefaultCRLIssuer(){return (String) data.get(DEFAULTCRLISSUER);}
    public void setDefaultCRLIssuer(String defailtcrlissuer) {
    	if(defailtcrlissuer == null){
    		data.put(DEFAULTCRLISSUER, "");
    	}else{
    		data.put(DEFAULTCRLISSUER, defailtcrlissuer);
    	}     
    }
    
    public String  getDefaultOCSPServiceLocator(){return (String) data.get(DEFAULTOCSPSERVICELOCATOR);}
    public void setDefaultOCSPServiceLocator(String defaultocsplocator) {
    	if(defaultocsplocator == null){
    		data.put(DEFAULTOCSPSERVICELOCATOR, "");
    	}else{
    		data.put(DEFAULTOCSPSERVICELOCATOR, defaultocsplocator);
    	}     
    }

    public boolean  getUseUTF8PolicyText(){
        return ((Boolean)data.get(USEUTF8POLICYTEXT)).booleanValue();
      }
      public void setUseUTF8PolicyText(boolean useutf8) {
        data.put(USEUTF8POLICYTEXT, Boolean.valueOf(useutf8));
      }


      public boolean  getUsePrintableStringSubjectDN(){
    	  return ((Boolean)data.get(USEPRINTABLESTRINGSUBJECTDN)).booleanValue();
      }
      public void setUsePrintableStringSubjectDN(boolean useprintablestring) {
    	  data.put(USEPRINTABLESTRINGSUBJECTDN, Boolean.valueOf(useprintablestring));
      }
    
      public void updateCA(CAInfo cainfo) throws Exception{
    	  super.updateCA(cainfo); 
    	  X509CAInfo info = (X509CAInfo) cainfo;
    	  
    	  setUseAuthorityKeyIdentifier(info.getUseAuthorityKeyIdentifier());
    	  setAuthorityKeyIdentifierCritical(info.getAuthorityKeyIdentifierCritical()); 
    	  setUseCRLNumber(info.getUseCRLNumber());
    	  setCRLNumberCritical(info.getCRLNumberCritical());
    	  setDefaultCRLDistPoint(info.getDefaultCRLDistPoint());
    	  setDefaultCRLIssuer(info.getDefaultCRLIssuer());
    	  setDefaultOCSPServiceLocator(info.getDefaultOCSPServiceLocator());
    	  setUseUTF8PolicyText(info.getUseUTF8PolicyText());
          setUsePrintableStringSubjectDN(info.getUsePrintableStringSubjectDN());

      }
    

    public byte[] createPKCS7(Certificate cert, boolean includeChain) throws SignRequestSignatureException {
        // First verify that we signed this certificate
        try {
            if (cert != null)
                cert.verify(getCAToken().getPublicKey(SecConst.CAKEYPURPOSE_CERTSIGN));
        } catch (Exception e) {
            throw new SignRequestSignatureException("Cannot verify certificate in createPKCS7(), did I sign this?");
        }
        Collection chain = getCertificateChain();
        ArrayList certList = new ArrayList();
        if (cert != null) {
            certList.add(cert);
        } 
        if (includeChain) {
            certList.addAll(chain);
        }
        try {
            CMSProcessable msg = new CMSProcessableByteArray("EJBCA".getBytes());
            CertStore certs = CertStore.getInstance("Collection", new CollectionCertStoreParameters(certList), "BC");
            CMSSignedDataGenerator gen = new CMSSignedDataGenerator();
            if (getCAToken().getPrivateKey(SecConst.CAKEYPURPOSE_CERTSIGN) == null) {
            	String msg1 = "createPKCS7: Private key does not exist!";
            	log.debug(msg1);
            	throw new SignRequestSignatureException(msg1);
            }
            gen.addSigner(getCAToken().getPrivateKey(SecConst.CAKEYPURPOSE_CERTSIGN), (X509Certificate)getCACertificate(), CMSSignedGenerator.DIGEST_SHA1);
            gen.addCertificatesAndCRLs(certs);
            CMSSignedData s = null;
            CAToken catoken = getCAToken();
            if (catoken != null && !(catoken instanceof NullCAToken)) {
            	log.debug("createPKCS7: Provider="+catoken.getProvider()+" using algorithm "+getCAToken().getPrivateKey(SecConst.CAKEYPURPOSE_CERTSIGN).getAlgorithm());
            	s = gen.generate(msg, true, catoken.getProvider());
            } else {
            	String msg1 = "CA Token does not exist!";
            	log.debug(msg);
            	throw new SignRequestSignatureException(msg1);
            }
            return s.getEncoded();
        } catch (CATokenOfflineException e) {
        	throw new javax.ejb.EJBException(e);        	
        } catch (Exception e) {
            throw new javax.ejb.EJBException(e);
        }   
    }    
    
    
    public Certificate generateCertificate(UserDataVO subject, 
                                           PublicKey publicKey, 
                                           int keyusage, 
                                           Date notBefore,
                                           Date notAfter,
                                           CertificateProfile certProfile) throws Exception{
                                               
    	    	
        final String sigAlg = getCAToken().getCATokenInfo().getSignatureAlgorithm();
        Date firstDate = new Date();
        // Set back startdate ten minutes to avoid some problems with wrongly set clocks.
        firstDate.setTime(firstDate.getTime() - 10 * 60 * 1000);
        Date lastDate = new Date();
        if ( (notBefore != null) && (certProfile.getAllowValidityOverride()) ) {
        	// If we allow the client (or ra) to specify the startdate
        	firstDate = notBefore;
        	if (log.isDebugEnabled()) {
            	log.debug("Using notBefore validity from request: "+firstDate);        		
        	}
        }
        if ( (notAfter == null) || (!certProfile.getAllowValidityOverride()) ) {
            // validity in days = validity*24*60*60*1000 milliseconds
            long val = certProfile.getValidity();        
        	if (log.isDebugEnabled()) {
        		log.debug("Using validity from profile: "+val);
        	}
            lastDate.setTime(lastDate.getTime() + ( val * 24 * 60 * 60 * 1000));        	
        } else {
        	// only if not null and we allow validity override
        	lastDate = notAfter;
        	if (log.isDebugEnabled()) {
            	log.debug("Using notAfter validity from request: "+lastDate);        		
        	}
        }
        // Do not allow last date to be before first date
        if (!lastDate.after(firstDate)) {
        	// Setting it to the same is silly as well but what the heck
        	lastDate = firstDate;
        }
        X509Certificate cacert = (X509Certificate)getCACertificate();

        String dn = subject.getDN();        
        // Check if this is a root CA we are creating
        boolean isRootCA = false;
        if (certProfile.getType() == CertificateProfile.TYPE_ROOTCA) {
        	isRootCA = true;
        }

        // If our desired after date is after the CA expires, we will not allow this
        // The CA will only issue certificates with maximum the same validity time as it-self
        // We will not limit validity of a self signed cert (RootCA), because it is a renewal.
        if ( !isRootCA ) {
            if (lastDate.after(cacert.getNotAfter())) {
            	String msg = intres.getLocalizedMessage("signsession.limitingvalidity", lastDate.toString(), cacert.getNotAfter());
            	log.info(msg);
                lastDate = cacert.getNotAfter();
            }            
        }
        X509V3CertificateGenerator certgen = new X509V3CertificateGenerator();
        // Serialnumber is random bits, where random generator is initialized by the
        // serno generator.
        BigInteger serno = SernoGenerator.instance().getSerno();
        certgen.setSerialNumber(serno);
        certgen.setNotBefore(firstDate);
        certgen.setNotAfter(lastDate);
        certgen.setSignatureAlgorithm(sigAlg);

        // Make DNs
        if(certProfile.getUseSubjectDNSubSet()){
          dn= certProfile.createSubjectDNSubSet(dn);	
        }
        
        if(certProfile.getUseCNPostfix()){
          dn = CertTools.insertCNPostfix(dn,certProfile.getCNPostfix());	
        }
        
        String altName = subject.getSubjectAltName(); 
        if(certProfile.getUseSubjectAltNameSubSet()){
        	altName = certProfile.createSubjectAltNameSubSet(altName);
        }
        
        X509NameEntryConverter converter = null;
        if (getUsePrintableStringSubjectDN()) {
        	converter = new PrintableStringEntryConverter();
        } else {
        	converter = new X509DefaultEntryConverter();
        }
        certgen.setSubjectDN(CertTools.stringToBcX509Name(dn, converter));
        // We must take the issuer DN directly from the CA-certificate otherwise we risk re-ordering the DN
        // which many applications do not like.
        if (isRootCA) {
        	// This will be an initial root CA, since no CA-certificate exists
        	// Or it is a root CA, since the cert is self signed. If it is a root CA we want to use the same encoding for subject and issuer,
        	// it might have changed over the years.
        	if (log.isDebugEnabled()) {
        		log.debug("Using subject DN also as issuer DN, because it is a root CA");
        	}
            X509Name caname = CertTools.stringToBcX509Name(getSubjectDN(), converter);
            certgen.setIssuerDN(caname);
        } else {
        	if (log.isDebugEnabled()) {
        		log.debug("Using issuer DN directly from the CA certificate");
        	}
            certgen.setIssuerDN(cacert.getSubjectX500Principal());        	
        }
        certgen.setPublicKey(publicKey);

        // Basic constranits, all subcerts are NOT CAs
        if (certProfile.getUseBasicConstraints() == true) {
        	BasicConstraints bc = new BasicConstraints(false);
            if ((certProfile.getType() == CertificateProfile.TYPE_SUBCA)
                || (certProfile.getType() == CertificateProfile.TYPE_ROOTCA)){            	
            	if(certProfile.getUsePathLengthConstraint()){
            		bc = new BasicConstraints(certProfile.getPathLengthConstraint());
            	}else{
            		bc =  new BasicConstraints(true);
            	}            	
            }
                            
            certgen.addExtension(
                X509Extensions.BasicConstraints.getId(),
                certProfile.getBasicConstraintsCritical(),
                bc);
        }
        // Key usage
        int newKeyUsage = -1;
        if (certProfile.getAllowKeyUsageOverride() && (keyusage >= 0)) {
            newKeyUsage = keyusage;
        } else {
            newKeyUsage = CertTools.sunKeyUsageToBC(certProfile.getKeyUsage());
        }
        if ( (certProfile.getUseKeyUsage() == true) && (newKeyUsage >=0) ){
            X509KeyUsage ku = new X509KeyUsage(newKeyUsage);
            certgen.addExtension(
                X509Extensions.KeyUsage.getId(),
                certProfile.getKeyUsageCritical(),
                ku);
        }
        // Extended Key usage
        if (certProfile.getUseExtendedKeyUsage() == true) {
            // Get extended key usage from certificate profile
            Collection c = certProfile.getExtendedKeyUsageAsOIDStrings();
            Vector usage = new Vector();
            Iterator iter = c.iterator();
            while (iter.hasNext()) {
                usage.add(new DERObjectIdentifier((String)iter.next()));
            }
            // Don't add empty key usage extension
            if (!usage.isEmpty()) {
                ExtendedKeyUsage eku = new ExtendedKeyUsage(usage);
                // Extended Key Usage may be either critical or non-critical
                certgen.addExtension(
                    X509Extensions.ExtendedKeyUsage.getId(),
                    certProfile.getExtendedKeyUsageCritical(),
                    eku);            	
            }
        }
        // Subject key identifier
        if (certProfile.getUseSubjectKeyIdentifier() == true) {
            SubjectPublicKeyInfo spki =
                new SubjectPublicKeyInfo(
                    (ASN1Sequence) new ASN1InputStream(new ByteArrayInputStream(publicKey.getEncoded())).readObject());
            SubjectKeyIdentifier ski = new SubjectKeyIdentifier(spki);
            certgen.addExtension(
                X509Extensions.SubjectKeyIdentifier.getId(),
                certProfile.getSubjectKeyIdentifierCritical(), ski);
        }
        // Authority key identifier
        if (certProfile.getUseAuthorityKeyIdentifier() == true) {
        	AuthorityKeyIdentifier aki = null;
            // Default value is that we calculate it from scratch!
            // (If this is a root CA we must calculate the AuthorityKeyIdentifier from scratch)
            // (If the CA signing this cert does not have a SubjectKeyIdentifier we must calculate the AuthorityKeyIdentifier from scratch)
            try{
            	byte[] keybytes = getCAToken().getPublicKey(SecConst.CAKEYPURPOSE_CERTSIGN).getEncoded();
            	SubjectPublicKeyInfo apki = new SubjectPublicKeyInfo((ASN1Sequence) new ASN1InputStream(new ByteArrayInputStream(keybytes)).readObject());
                aki = new AuthorityKeyIdentifier(apki);
            }catch(CATokenOfflineException e){
            	log.debug("X509CA: CA Token Offline Exception: ", e);                
            	throw e; 
            }
            
        	// If we have a CA-certificate (i.e. this is not a Root CA), we must take the authority key identifier from 
        	// the CA-certificates SubjectKeyIdentifier if it exists. If we don't do that we will get the wrong identifier if the 
        	// CA does not follow RFC3280 (guess if MS-CA follows RFC3280?)
            if ( (cacert != null) && (!isRootCA) ) {
                byte[] akibytes = CertTools.getSubjectKeyId(cacert);
                if (akibytes != null) {
                    // TODO: The code below is snipped from AuthorityKeyIdentifier.java in BC 1.36, because there is no method there
                    // to set only a pre-computed key identifier
                    // This should be replaced when such a method is added to BC
                    ASN1OctetString keyidentifier = new DEROctetString(akibytes);
                    ASN1EncodableVector  v = new ASN1EncodableVector();
                    v.add(new DERTaggedObject(false, 0, keyidentifier));
                    ASN1Sequence seq = new DERSequence(v);
                    
                    aki = new AuthorityKeyIdentifier(seq);
                    log.debug("Using AuthorityKeyIdentifier from CA-certificates SubjectKeyIdentifier.");                	
                }
            }
            certgen.addExtension(
                X509Extensions.AuthorityKeyIdentifier.getId(),
                certProfile.getAuthorityKeyIdentifierCritical(), aki);
        }
        
         // Subject Alternative name
        if ( (certProfile.getUseSubjectAlternativeName() == true) && (altName != null) && (altName.length() > 0) ) {
            GeneralNames san = CertTools.getGeneralNamesFromAltName(altName);            
            if (san != null) {
                certgen.addExtension(X509Extensions.SubjectAlternativeName.getId(), certProfile.getSubjectAlternativeNameCritical(), san);
            }
        }
        
        // Certificate Policies
        if ( (certProfile.getUseCertificatePolicies() == true) && (StringUtils.isNotEmpty(certProfile.getCertificatePolicyId())) ) {
            int displayencoding = DisplayText.CONTENT_TYPE_BMPSTRING;
            if (getUseUTF8PolicyText()) {
                displayencoding = DisplayText.CONTENT_TYPE_UTF8STRING;
            }
            String policyId = certProfile.getCertificatePolicyId();
            String cpsurl = certProfile.getCpsUrl();
            String usernotice = certProfile.getUserNoticeText();
            ASN1EncodableVector policys = new ASN1EncodableVector();
            if (StringUtils.isNotEmpty(policyId )) {
                StringTokenizer tokenizer = new StringTokenizer(policyId, ";", false);
                while (tokenizer.hasMoreTokens()) {
                    String id = tokenizer.nextToken();
                    PolicyInformation pi = getPolicyInformation(id, cpsurl, usernotice, displayencoding);
                    // We only support a cpsurl and usernotice on the first policyid
                    cpsurl = null;
                    usernotice = null;
                    if (pi != null) {
                        policys.add(pi);                    	
                    }
                }
                // Add the final extension
                DERSequence seq = new DERSequence(policys);
                certgen.addExtension(X509Extensions.CertificatePolicies.getId(),
                        certProfile.getCertificatePoliciesCritical(), seq);
            }
        }

         // CRL Distribution point URI
         if (certProfile.getUseCRLDistributionPoint() == true) {
        	 String crldistpoint = certProfile.getCRLDistributionPointURI();
             String crlissuer=certProfile.getCRLIssuer();
        	 if(certProfile.getUseDefaultCRLDistributionPoint()){
        		 crldistpoint = getDefaultCRLDistPoint();
        		 crlissuer = getDefaultCRLIssuer();
        	 }
             // Multiple CDPs are spearated with the ';' sign        	         	 
            ArrayList dpns = new ArrayList();
            if (StringUtils.isNotEmpty(crldistpoint)) {
                StringTokenizer tokenizer = new StringTokenizer(crldistpoint, ";", false);
                while (tokenizer.hasMoreTokens()) {
                    // 6 is URI
                    String uri = tokenizer.nextToken();
                    GeneralName gn = new GeneralName(6, new DERIA5String(uri));
                    log.debug("Added CRL distpoint: "+uri);
                    ASN1EncodableVector vec = new ASN1EncodableVector();
                    vec.add(gn);
                    GeneralNames gns = new GeneralNames(new DERSequence(vec));
                    DistributionPointName dpn = new DistributionPointName(0, gns);
                    dpns.add(dpn);
                }            	
            }
            // CRL issuer works much like Dist point URI. If separated by ; it is put in the same global distPoint as the URI, 
            // if there is more of one of them, the one with more is put in an own global distPoint.
            ArrayList issuers = new ArrayList();
            if (StringUtils.isNotEmpty(crlissuer)) {
                StringTokenizer tokenizer = new StringTokenizer(crlissuer, ";", false);
                while (tokenizer.hasMoreTokens()) {
                	String issuer = tokenizer.nextToken();
                	GeneralName gn = new GeneralName(new X509Name(issuer));
                    log.debug("Added CRL issuer: "+issuer);
                    ASN1EncodableVector vec = new ASN1EncodableVector();
                    vec.add(gn);
                    GeneralNames gns = new GeneralNames(new DERSequence(vec));
                    issuers.add(gns);
                }            	
            }
            ArrayList distpoints = new ArrayList();
            if ( (issuers.size() > 0) || (dpns.size() > 0) ) {
            	int i = dpns.size();
            	if (issuers.size() > i) {
            		i = issuers.size();
            	}
            	for (int j = 0; j < i; j++) {
            		DistributionPointName dpn = null;
            		GeneralNames issuer = null;
            		if (dpns.size() > j) {
            			dpn = (DistributionPointName)dpns.get(j);
            		}
            		if (issuers.size() > j) {
            			issuer = (GeneralNames)issuers.get(j);
            		}
            		if ( (dpn != null) || (issuer != null) ) {
                        distpoints.add(new DistributionPoint(dpn, null, issuer));            	            			
            		}
            	}
            }
            if (distpoints.size() > 0) {
                CRLDistPoint ext = new CRLDistPoint((DistributionPoint[])distpoints.toArray(new DistributionPoint[0]));
                certgen.addExtension(X509Extensions.CRLDistributionPoints.getId(),
                    certProfile.getCRLDistributionPointCritical(), ext);
            }
         }
         // Authority Information Access (OCSP url)
         if (certProfile.getUseOCSPServiceLocator() == true) {
             String ocspUrl = certProfile.getOCSPServiceLocatorURI();
             if(certProfile.getUseDefaultOCSPServiceLocator()){
            	 ocspUrl = getDefaultOCSPServiceLocator();
             }
             if (StringUtils.isNotEmpty(ocspUrl)) {
                 // OCSP access location is a URL (GeneralName no 6)
                 GeneralName ocspLocation = new GeneralName(6, new DERIA5String(ocspUrl));
                 certgen.addExtension(X509Extensions.AuthorityInfoAccess.getId(),
                     false, new AuthorityInformationAccess(X509ObjectIdentifiers.ocspAccessMethod, ocspLocation));                 
             }
         }
         
         // Microsoft Template
         if (certProfile.getUseMicrosoftTemplate() == true) {
             String mstemplate = certProfile.getMicrosoftTemplate();             
             DERObjectIdentifier oid = new DERObjectIdentifier(CertTools.OID_MSTEMPLATE);                           
             certgen.addExtension(oid, false, new DERIA5String(mstemplate));             
         }
         
         // QCStatement (rfc3739)
         if (certProfile.getUseQCStatement() == true) {
             String names = certProfile.getQCStatementRAName();
             GeneralNames san = CertTools.getGeneralNamesFromAltName(names);
             SemanticsInformation si = null;
             if (san != null) {
                 if (StringUtils.isNotEmpty(certProfile.getQCSemanticsId())) {
                     si = new SemanticsInformation(new DERObjectIdentifier(certProfile.getQCSemanticsId()), san.getNames());
                 } else {
                     si = new SemanticsInformation(san.getNames());                     
                 }
             } else if (StringUtils.isNotEmpty(certProfile.getQCSemanticsId())) {
                 si = new SemanticsInformation(new DERObjectIdentifier(certProfile.getQCSemanticsId()));                 
             }
             ArrayList qcs = new ArrayList();
             QCStatement qc = null;
             // First the standard rfc3739 QCStatement with an optional SematicsInformation
             DERObjectIdentifier pkixQcSyntax = RFC3739QCObjectIdentifiers.id_qcs_pkixQCSyntax_v1;
             if (certProfile.getUsePkixQCSyntaxV2()) {
            	 pkixQcSyntax = RFC3739QCObjectIdentifiers.id_qcs_pkixQCSyntax_v2;
             }
             if ( (si != null)  ) {
                 qc = new QCStatement(pkixQcSyntax, si);
                 qcs.add(qc);
             } else {
            	 qc = new QCStatement(pkixQcSyntax);
                 qcs.add(qc);
             }
             // ETSI Statement that the certificate is a Qualified Certificate
             if (certProfile.getUseQCEtsiQCCompliance()) {
            	 qc = new QCStatement(ETSIQCObjectIdentifiers.id_etsi_qcs_QcCompliance);
                 qcs.add(qc);
             }
             // ETSI Statement regarding limit on the value of transactions
             if (certProfile.getUseQCEtsiValueLimit()) {
            	 // Both value and currency must be availabel for this extension
            	 if ( (certProfile.getQCEtsiValueLimit() > 0) && (certProfile.getQCEtsiValueLimitCurrency() != null) ) {
            		 int limit = certProfile.getQCEtsiValueLimit();
            		 // The exponent should be default 0
            		 int exponent = certProfile.getQCEtsiValueLimitExp();
            		 MonetaryValue value = new MonetaryValue(new Iso4217CurrencyCode(certProfile.getQCEtsiValueLimitCurrency()), limit, exponent);
            		 qc = new QCStatement(ETSIQCObjectIdentifiers.id_etsi_qcs_LimiteValue, value);
            		 qcs.add(qc);
            	 }
             }
             // ETSI Statement claiming that the private key resides in a Signature Creation Device
             if (certProfile.getUseQCEtsiSignatureDevice()) {
            	 qc = new QCStatement(ETSIQCObjectIdentifiers.id_etsi_qcs_QcSSCD);
                 qcs.add(qc);
             }
             // Custom UTF8String QC-statement:
 			 // qcStatement-YourCustom QC-STATEMENT ::= { SYNTAX YourCustomUTF8String
			 //   IDENTIFIED BY youroid }
			 //   -- This statement gives you the possibility to define your own QC-statement
			 //   -- using an OID and a simple UTF8String, with describing text. A sample text could for example be:
			 //   -- This certificate, according to Act. No. xxxx Electronic Signature Law is a qualified electronic certificate
			 //
			 // YourCustomUTF8String ::= UTF8String
             if (certProfile.getUseQCCustomString()) {
            	 if (!StringUtils.isEmpty(certProfile.getQCCustomStringOid()) && !StringUtils.isEmpty(certProfile.getQCCustomStringText())) {
            		 DERUTF8String str = new DERUTF8String(certProfile.getQCCustomStringText());
            		 DERObjectIdentifier oid = new DERObjectIdentifier(certProfile.getQCCustomStringOid());
                	 qc = new QCStatement(oid, str);
                     qcs.add(qc);            		 
            	 }
             }
             if (qcs.size() >  0) {
            	 ASN1EncodableVector vec = new ASN1EncodableVector();
                 Iterator iter = qcs.iterator();
                 while (iter.hasNext()) {
                	 QCStatement q = (QCStatement)iter.next();
                     vec.add(q);
                 }
                 certgen.addExtension(CertTools.QCSTATEMENTS_OBJECTID, certProfile.getQCStatementCritical(), new DERSequence(vec));                 
             }
         }
         
         // Subject Directory Attributes
         if (certProfile.getUseSubjectDirAttributes() == true) {
        	 // Get the attributes from ExtendedInformation
        	 String dirAttrString = subject.getExtendedinformation().getSubjectDirectoryAttributes();
        	 if (StringUtils.isNotEmpty(dirAttrString)) {
            	 // Subject Directory Attributes is a sequence of Attribute
            	 Collection attr = SubjectDirAttrExtension.getSubjectDirectoryAttributes(dirAttrString);
            	 ASN1EncodableVector vec = new ASN1EncodableVector();
            	 Iterator iter = attr.iterator();
            	 while (iter.hasNext()) {
            		 Attribute a = (Attribute)iter.next();
            		 vec.add(a);
            	 }        		 
            	 // Subject Directory Attributes must always be non-critical
            	 certgen.addExtension(X509Extensions.SubjectDirectoryAttributes, false, new DERSequence(vec));                 
        	 }
        	 
         }         

         // Check for Certificate Extensions
         CertificateExtensionFactory fact = CertificateExtensionFactory.getInstance();
         List usedCertExt = certProfile.getUsedCertificateExtensions();
         Iterator certExtIter = usedCertExt.iterator();
         while(certExtIter.hasNext()){
        	 Integer id = (Integer) certExtIter.next();
        	 CertificateExtension certExt = fact.getCertificateExtensions(id);
        	 certgen.addExtension(new DERObjectIdentifier(certExt.getOID()),certExt.isCriticalFlag(),certExt.getValue(subject, this, certProfile));        	 
         }
         
         X509Certificate cert;
         try{
           cert = certgen.generate(getCAToken().getPrivateKey(SecConst.CAKEYPURPOSE_CERTSIGN), 
                                            getCAToken().getProvider());
         }catch(CATokenOfflineException e){
             log.debug("X509CA : CA Token STATUS OFFLINE: ", e);
             throw e; 
         }
        
        // Verify before returning
        cert.verify(getCAToken().getPublicKey(SecConst.CAKEYPURPOSE_CERTSIGN));
        log.debug(">X509CA: generate certificate, CA "+ this.getCAId() + " for DN: " + subject.getDN());
      return cert;                                                                                        
    }

    
    public CRL generateCRL(Vector certs, int crlnumber) 
    throws CATokenOfflineException, IllegalKeyStoreException, IOException, SignatureException, NoSuchProviderException, InvalidKeyException, CRLException, NoSuchAlgorithmException {
        final String sigAlg= getCAToken().getCATokenInfo().getSignatureAlgorithm();

        Date thisUpdate = new Date();
        Date nextUpdate = new Date();

        // crlperiod is hours = crlperiod*60*60*1000 milliseconds
        nextUpdate.setTime(nextUpdate.getTime() + (getCRLPeriod() * (long)(60 * 60 * 1000)));
        X509V2CRLGenerator crlgen = new X509V2CRLGenerator();
        crlgen.setThisUpdate(thisUpdate);
        crlgen.setNextUpdate(nextUpdate);
        crlgen.setSignatureAlgorithm(sigAlg);
        // Make DNs
        X509Certificate cacert = (X509Certificate)getCACertificate();
        if (cacert == null) {
        	// This is an initial root CA, since no CA-certificate exists
            X509Name caname = CertTools.stringToBcX509Name(getSubjectDN());
            crlgen.setIssuerDN(caname);
        } else {
        	crlgen.setIssuerDN(cacert.getSubjectX500Principal());
        }
        if (certs != null) {            
            Iterator it = certs.iterator();
            while( it.hasNext() ) {
                RevokedCertInfo certinfo = (RevokedCertInfo)it.next();
                crlgen.addCRLEntry(certinfo.getUserCertificate(), certinfo.getRevocationDate(), certinfo.getReason());
            }
        }

        // Authority key identifier
        if (getUseAuthorityKeyIdentifier() == true) {
            SubjectPublicKeyInfo apki = new SubjectPublicKeyInfo((ASN1Sequence)new ASN1InputStream(
                new ByteArrayInputStream(getCAToken().getPublicKey(SecConst.CAKEYPURPOSE_CRLSIGN).getEncoded())).readObject());
            AuthorityKeyIdentifier aki = new AuthorityKeyIdentifier(apki);
            crlgen.addExtension(X509Extensions.AuthorityKeyIdentifier.getId(), getAuthorityKeyIdentifierCritical(), aki);
        }
        // CRLNumber extension
        if (getUseCRLNumber() == true) {
            CRLNumber crlnum = new CRLNumber(BigInteger.valueOf(crlnumber));
            crlgen.addExtension(X509Extensions.CRLNumber.getId(),  this.getCRLNumberCritical(), crlnum);
        }
        
        X509CRL crl;
        crl = crlgen.generate(getCAToken().getPrivateKey(SecConst.CAKEYPURPOSE_CRLSIGN),getCAToken().getProvider());
        // Verify before sending back
        crl.verify(getCAToken().getPublicKey(SecConst.CAKEYPURPOSE_CRLSIGN));

        return crl;        
    }    
    
    /** Implemtation of UpgradableDataHashMap function getLatestVersion */
    public float getLatestVersion(){
       return LATEST_VERSION;
    }

    /** Implemtation of UpgradableDataHashMap function upgrade. 
     */
    public void upgrade(){
    	if(Float.compare(LATEST_VERSION, getVersion()) != 0) {
            // New version of the class, upgrade
            log.info("Upgrading X509CA with version "+getVersion());

            if (data.get(DEFAULTOCSPSERVICELOCATOR) == null) {
                setDefaultCRLDistPoint("");
                setDefaultOCSPServiceLocator("");
            }
            if (data.get(CRLISSUEINTERVAL) == null) {
                setCRLIssueInterval(0);
            }
            if (data.get(CRLOVERLAPTIME) == null) {
            	// Default value 10 minutes
            	setCRLOverlapTime(10);
            }
            boolean useprintablestring = true;
            if (data.get("alwaysuseutf8subjectdn") == null) {
            	// Default value false
            	setUseUTF8PolicyText(false);
            } else {
            	// Use the same value as we had before when we had alwaysuseutf8subjectdn
                boolean useutf8 = ((Boolean)data.get("alwaysuseutf8subjectdn")).booleanValue();
            	setUseUTF8PolicyText(useutf8);
            	// If we had checked to use utf8 on an old CA, we do not want to use PrintableString after upgrading
            	useprintablestring = !useutf8;
            }
            if (data.get(USEPRINTABLESTRINGSUBJECTDN) == null) {
            	// Default value true (as before)
            	setUsePrintableStringSubjectDN(useprintablestring);
            }
            if (data.get(DEFAULTCRLISSUER) == null) {
            	setDefaultCRLIssuer(null);
            }
            
            
            data.put(VERSION, new Float(LATEST_VERSION));
        }  
    }

    /**
     * Method to upgrade new (or existing externacaservices)
     * This method needs to be called outside the regular upgrade
     * since the CA isn't instansiated in the regular upgrade.
     *
     */
    public boolean upgradeExtendedCAServices() {
    	boolean retval = false;
    	Collection extendedServiceTypes = getExternalCAServiceTypes();

    	if(getCAInfo().getStatus() != SecConst.CA_EXTERNAL){
    		// Create XKMS service if it does not exist
    		if (!extendedServiceTypes.contains(new Integer(ExtendedCAServiceInfo.TYPE_XKMSEXTENDEDSERVICE))){

    			String keytype = CATokenConstants.KEYALGORITHM_RSA;
    			String keyspec = "2048";

    			XKMSCAServiceInfo xKMSCAInfo =  new XKMSCAServiceInfo(ExtendedCAServiceInfo.STATUS_INACTIVE,
    					"CN=XKMSCertificate, " + getSubjectDN(),
    					"",
    					keyspec,
    					keytype);

    			XKMSCAService xkmsservice = new XKMSCAService(xKMSCAInfo);
    			try {
    				xkmsservice.init(this);
    				retval = true;
    			} catch (Exception e) {
    				CAInfo info = this.getCAInfo();
    				String caname = null;
    				if (info != null) {
    					caname = info.getName();
    				}
    				log.error(intres.getLocalizedMessage("signsession.errorupgradingxkmsservice",caname), e);
    			}
    			setExtendedCAService(xkmsservice);
    			extendedServiceTypes.add(new Integer(ExtendedCAServiceInfo.TYPE_XKMSEXTENDEDSERVICE));
    			data.put(EXTENDEDCASERVICES, extendedServiceTypes);
    		}		

    		// Create CMS service if it does not exist
    		if (!extendedServiceTypes.contains(new Integer(ExtendedCAServiceInfo.TYPE_CMSEXTENDEDSERVICE))){

    			String keytype = CATokenConstants.KEYALGORITHM_RSA;
    			String keyspec = "2048";

    			CmsCAServiceInfo cmsCAInfo =  new CmsCAServiceInfo(ExtendedCAServiceInfo.STATUS_INACTIVE,
    					"CN=CMSCertificate, " + getSubjectDN(),
    					"",
    					keyspec,
    					keytype);

    			CmsCAService cmsservice = new CmsCAService(cmsCAInfo);
    			try {
    				cmsservice.init(this);
    				retval = true;
    			} catch (Exception e) {
    				CAInfo info = this.getCAInfo();
    				String caname = null;
    				if (info != null) {
    					caname = info.getName();
    				}
    				log.error(intres.getLocalizedMessage("signsession.errorupgradingcmsservice",caname), e);
    			}
    			setExtendedCAService(cmsservice);
    			extendedServiceTypes.add(new Integer(ExtendedCAServiceInfo.TYPE_CMSEXTENDEDSERVICE));
    			data.put(EXTENDEDCASERVICES, extendedServiceTypes);
    		}		
    	}
    	return retval;
    }
	
	/** 
	 * Method used to perform an extended service.
	 */
    public ExtendedCAServiceResponse extendedService(ExtendedCAServiceRequest request) 
      throws ExtendedCAServiceRequestException, IllegalExtendedCAServiceRequestException, ExtendedCAServiceNotActiveException{
          log.debug(">extendedService()");
          if(request instanceof OCSPCAServiceRequest) {
        	  OCSPCAServiceRequest ocspServiceReq = (OCSPCAServiceRequest)request;
              boolean useCACert = ocspServiceReq.useCACert();
              try {
                  if (useCACert) {
                	  ocspServiceReq.setPrivKey(getCAToken().getPrivateKey(SecConst.CAKEYPURPOSE_CERTSIGN));
                	  ocspServiceReq.setPrivKeyProvider(getCAToken().getProvider());
                	  X509Certificate[] signerChain = (X509Certificate[])getCertificateChain().toArray(new X509Certificate[0]);
                	  List chain = Arrays.asList(signerChain);
                	  ocspServiceReq.setCertificateChain(chain);
                      // Super class handles signing with the OCSP signing certificate
                      log.debug("<extendedService(super with ca cert)");
                      return super.extendedService(ocspServiceReq);                      
                  } else {
                      // Super class handles signing with the OCSP signing certificate
                      log.debug("<extendedService(super no ca cert)");
                      return super.extendedService(request);                      
                  }
              } catch (IllegalKeyStoreException ike) {
            	  throw new ExtendedCAServiceRequestException(ike);
              } catch (CATokenOfflineException ctoe) {
            	  throw new ExtendedCAServiceRequestException(ctoe);
              } catch (IllegalArgumentException e) {
            	  log.error("IllegalArgumentException: ", e);
            	  throw new IllegalExtendedCAServiceRequestException(e);
              }
          } else {
              log.debug("<extendedService(super)");
              return super.extendedService(request);
          }
    }
    
    public byte[] encryptKeys(KeyPair keypair) throws IOException, CATokenOfflineException{    
    	ByteArrayOutputStream baos = new ByteArrayOutputStream();
    	ObjectOutputStream os = new ObjectOutputStream(baos);
    	os.writeObject(keypair);    	    
    	
    	CertTools.installBCProvider();
    		
        CMSEnvelopedDataGenerator edGen = new CMSEnvelopedDataGenerator();    	    	    	    	      
       
    	CMSEnvelopedData ed;
		try {
			edGen.addKeyTransRecipient( this.getCAToken().getPublicKey(SecConst.CAKEYPURPOSE_KEYENCRYPT), this.keyId);
			ed = edGen.generate(
					new CMSProcessableByteArray(baos.toByteArray()), CMSEnvelopedDataGenerator.AES256_CBC,"BC");
		} catch (Exception e) {
            log.error("-encryptKeys: ", e);
            throw new IOException(e.getMessage());        
		}
				
		
		return ed.getEncoded(); 
    }
    
    public KeyPair decryptKeys(byte[] data) throws Exception {
    	CMSEnvelopedData ed = new CMSEnvelopedData(data);   	    	
    	     
		RecipientInformationStore  recipients = ed.getRecipientInfos();           	
    	Iterator    it =  recipients.getRecipients().iterator();
    	RecipientInformation   recipient = (RecipientInformation) it.next();
    	ObjectInputStream ois = null;
    	byte[] recdata = recipient.getContent(getCAToken().getPrivateKey(SecConst.CAKEYPURPOSE_KEYENCRYPT),getCAToken().getProvider());
    	ois = new ObjectInputStream(new ByteArrayInputStream(recdata));
    	    	    	
    	return (KeyPair) ois.readObject();  
    }
    
    
    /**
     * Obtains the Policy Notice
     * 
     * @param policyOID,
     *          OID of the policy
     * @param cps,
     *          url to cps document
     * @param unotice,
     *          user notice text
     * @param displayencoding,
     *          the encoding used for UserNotice text, DisplayText.CONTENT_TYPE_BMPSTRING, CONTENT_TYPE_UTF8STRING, CONTENT_TYPE_IA5STRING or CONTENT_TYPE_VISIBLESTRING 
     *          
     * @return
     */
    private PolicyInformation getPolicyInformation(String policyOID, String cps, String unotice, int displayencoding) {
        
    	ASN1EncodableVector qualifiers = new ASN1EncodableVector();
        if ((unotice != null) && !StringUtils.isEmpty(unotice.trim())) {
            // Normally we would just use 'DisplayText(unotice)' here. IE has problems with UTF8 though, so lets stick with BMSSTRING to satisfy Bills sick needs.
            UserNotice un = new UserNotice(null, new DisplayText(displayencoding, unotice));
            PolicyQualifierInfo pqiUNOTICE = new PolicyQualifierInfo(PolicyQualifierId.id_qt_unotice, un);
            qualifiers.add(pqiUNOTICE);
        }
        if ((cps != null) && !StringUtils.isEmpty(cps.trim())) {
            PolicyQualifierInfo pqiCPS = new PolicyQualifierInfo(cps);
            qualifiers.add(pqiCPS);
        }
        PolicyInformation policyInformation = null;
        if ( StringUtils.isNotEmpty(policyOID) && (qualifiers.size() > 0) ) {
            policyInformation = new PolicyInformation(new DERObjectIdentifier(policyOID), new DERSequence(qualifiers));            
        } else {
            policyInformation = new PolicyInformation(new DERObjectIdentifier(policyOID));
        }
        
        return policyInformation;
    }   
}