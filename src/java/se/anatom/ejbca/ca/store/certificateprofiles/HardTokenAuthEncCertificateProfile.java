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
 
package se.anatom.ejbca.ca.store.certificateprofiles;

import java.util.ArrayList;

/**
 * HardTokenAuthEncCertificateProfile is a class defining the fixed characteristics 
 * of a hard token authentication and encryption certificate.
 *
 * @version $Id: HardTokenAuthEncCertificateProfile.java,v 1.2.2.1 2004-11-07 16:43:29 herrvendil Exp $
 */
public class HardTokenAuthEncCertificateProfile extends CertificateProfile{

    // Public Constants

    public final static String CERTIFICATEPROFILENAME =  "HARDTOKEN_AUTHENC";

    // Public Methods
    /** Creates a certificate with the characteristics of an end user. */
    public HardTokenAuthEncCertificateProfile() {

      setCertificateVersion(VERSION_X509V3);
      setValidity(730);

      setUseBasicConstraints(true);
      setBasicConstraintsCritical(true);


      setUseSubjectKeyIdentifier(true);
      setSubjectKeyIdentifierCritical(false);

      setUseAuthorityKeyIdentifier(true);
      setAuthorityKeyIdentifierCritical(false);

      setUseSubjectAlternativeName(true);
      setSubjectAlternativeNameCritical(false);

      setUseCRLDistributionPoint(false);
      setCRLDistributionPointCritical(false);
      setCRLDistributionPointURI("");

      setUseCertificatePolicies(false);
      setCertificatePoliciesCritical(false);
      setCertificatePolicyId("2.5.29.32.0");

      setType(TYPE_ENDENTITY);

      int[] bitlengths = {512,1024,2048,4096};
      setAvailableBitLengths(bitlengths);

      setUseKeyUsage(true);
      setKeyUsage(new boolean[9]);
      setKeyUsage(KEYENCIPHERMENT,true);
	  setKeyUsage(DIGITALSIGNATURE,true);           
      setKeyUsageCritical(true);

      setUseExtendedKeyUsage(true);
      ArrayList eku = new ArrayList();        
      eku.add(new Integer(CLIENTAUTH));
      eku.add(new Integer(EMAILPROTECTION));      
      eku.add(new Integer(IPSECUSER));
      setExtendedKeyUsage(eku);
      setExtendedKeyUsageCritical(false);
      
      ArrayList availablecas = new ArrayList();
      availablecas.add(new Integer(ANYCA));
      setAvailableCAs(availablecas);      
      setPublisherList(new ArrayList());
    }

    // Public Methods.
    public void upgrade(){
      if(LATEST_VERSION != getVersion()){
        // New version of the class, upgrade

        super.upgrade();         
      }
    }


    // Private fields.
}
