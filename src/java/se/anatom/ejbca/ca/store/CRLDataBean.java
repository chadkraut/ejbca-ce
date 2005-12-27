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
 
package se.anatom.ejbca.ca.store;

import java.io.IOException;
import java.security.cert.*;
import java.util.Date;

import javax.ejb.CreateException;

import org.apache.log4j.Logger;

import se.anatom.ejbca.BaseEntityBean;
import se.anatom.ejbca.util.Base64;
import se.anatom.ejbca.util.CertTools;


/**
 * Entity Bean representing a CRL. Information stored:
 * <pre>
 * CRL (base64Crl)
 * IssuerDN (issuerDN)
 * CRLNumber (CRLNumber)
 * SHA1 fingerprint (fingerprint)
 * CA SHA1 fingerprint (cAFingerprint)
 * thisUpdate (thisUpdate)
 * nextUpdate (nextUpdate)
 * </pre>
 *
 * @version $Id: CRLDataBean.java,v 1.25.2.1 2005-12-27 16:16:00 anatom Exp $
 *
 * @ejb.bean description="This enterprise bean entity represents a CRL with accompanying data"
 * display-name="CRLDataEB"
 * name="CRLData"
 * view-type="local"
 * type="CMP"
 * reentrant="False"
 * cmp-version="2.x"
 * transaction-type="Container"
 * schema="CRLDataBean"
 *
 * @ejb.transaction type="Required"
 *
 * @ejb.pk class="se.anatom.ejbca.ca.store.CRLDataPK"
 * extends="java.lang.Object"
 * implements="java.io.Serializable"
 *
 * @ejb.home
 * generate="local"
 * local-extends="javax.ejb.EJBLocalHome"
 * local-class="se.anatom.ejbca.ca.store.CRLDataLocalHome"
 *
 * @ejb.interface
 * generate="local"
 * local-extends="javax.ejb.EJBLocalObject"
 * local-class="se.anatom.ejbca.ca.store.CRLDataLocal"
 *
 * @ejb.finder description="findByIssuerDNAndCRLNumber"
 *   signature="se.anatom.ejbca.ca.store.CRLDataLocal findByIssuerDNAndCRLNumber(java.lang.String issuerdn, int cRLNumber)"
 *   query="SELECT DISTINCT OBJECT(a) from CRLDataBean a WHERE a.issuerDN=?1 AND a.crlNumber=?2"
 *
 * @jonas.jdbc-mapping
 *   jndi-name="${datasource.jndi-name}"
 */
public abstract class CRLDataBean extends BaseEntityBean {
    private static final Logger log = Logger.getLogger(CRLDataBean.class);

    /**
     * @ejb.persistence column-name="cRLNumber"
     * @ejb.interface-method
     */
    public abstract int getCrlNumber();

    /**
     * @ejb.persistence column-name="cRLNumber"
     * @ejb.interface-method
     */
    public abstract void setCrlNumber(int crlNumber);

    /**
     * @ejb.persistence
     * @ejb.interface-method
     */
    public abstract String getIssuerDN();

    /**
     * Use setIssuer instead
     *
     * @see #setIssuer(String)
     * @ejb.persistence
     */
    public abstract void setIssuerDN(String issuerDN);

    /**
     * @ejb.pk-field
     * @ejb.persistence
     * @ejb.interface-method
     */
    public abstract String getFingerprint();

    /**
     * @ejb.persistence
     * @ejb.interface-method
     */
    public abstract void setFingerprint(String fingerprint);

    /**
     * @ejb.persistence column-name="cAFingerprint"
     * @ejb.interface-method
     */
    public abstract String getCaFingerprint();

    /**
     * @ejb.persistence column-name="cAFingerprint"
     * @ejb.interface-method
     */
    public abstract void setCaFingerprint(String caFingerprint);

    /**
     * @ejb.persistence
     * @ejb.interface-method
     */
    public abstract long getThisUpdate();

    /**
     * Date formated as seconds since 1970 (== Date.getTime())
     * @ejb.persistence
     * @ejb.interface-method
     */
    public abstract void setThisUpdate(long thisUpdate);

    /**
     * @ejb.persistence
     * @ejb.interface-method
     */
    public abstract long getNextUpdate();

    /**
     * Date formated as seconds since 1970 (== Date.getTime())
     * @ejb.persistence
     * @ejb.interface-method
     */
    public abstract void setNextUpdate(long nextUpdate);

    /**
     * @ejb.persistence jdbc-type="LONGVARCHAR"
     * @ejb.interface-method
     */
    public abstract String getBase64Crl();

    /**
     * @ejb.persistence jdbc-type="LONGVARCHAR"
     * @ejb.interface-method
     */
    public abstract void setBase64Crl(String base64Crl);

    //
    // Public methods used to help us manage CRLs
    //
    /**
     * @ejb.interface-method
     */
    public X509CRL getCRL() {
        X509CRL crl = null;
        try {
            String b64Crl = getBase64Crl();
            crl = CertTools.getCRLfromByteArray(Base64.decode(b64Crl.getBytes()));
        } catch (IOException ioe) {
            log.error("Can't decode CRL.", ioe);
            return null;
        } catch (CRLException ce) {
            log.error("Can't decode CRL.", ce);
            return null;
        } 
        return crl;
    }

    /**
     * @ejb.interface-method
     */
    public void setCRL(X509CRL incrl) {
        try {
            String b64Crl = new String(Base64.encode((incrl).getEncoded()));
            setBase64Crl(b64Crl);
        } catch (CRLException ce) {
            log.error("Can't extract DER encoded CRL.", ce);
        }
    }

    /**
     * @ejb.interface-method
     */
    public void setIssuer(String dn) {
        setIssuerDN(CertTools.stringToBCDNString(dn));
    }

    /**
     * @ejb.interface-method
     */
    public void setThisUpdate(Date thisUpdate) {
        if (thisUpdate == null) {
            setThisUpdate(-1L);
        }

        setThisUpdate(thisUpdate.getTime());
    }

    /**
     * @ejb.interface-method
     */
    public void setNextUpdate(Date nextUpdate) {
        if (nextUpdate == null) {
            setNextUpdate(-1L);
        }

        setNextUpdate(nextUpdate.getTime());
    }

    //
    // Fields required by Container
    //

    /**
     * Entity Bean holding info about a CRL. Create by sending in the CRL, which extracts (from the
     * crl) fingerprint (primary key), CRLNumber, issuerDN, thisUpdate, nextUpdate. CAFingerprint
     * are set to default values (null) and should be set using the respective set-methods.
     *
     * @param incrl the (X509)CRL to be stored in the database.
     * @param number monotonically increasnig CRL number
     *
     * @ejb.create-method
     */
    public CRLDataPK ejbCreate(X509CRL incrl, int number)
        throws CreateException {
        // Exctract all fields to store with the certificate.
        try {
            String b64Crl = new String(Base64.encode(incrl.getEncoded()));
            setBase64Crl(b64Crl);
            setFingerprint(CertTools.getFingerprintAsString(incrl));

            // Make sure names are always looking the same
            setIssuerDN(CertTools.getIssuerDN(incrl));
            log.debug("Creating crldata, issuer=" + getIssuerDN());

            // Default values for cafp
            setCaFingerprint(null);
            setCrlNumber(number);
            setThisUpdate(incrl.getThisUpdate());
            setNextUpdate(incrl.getNextUpdate());
        } catch (CRLException ce) {
            log.error("Can't extract DER encoded CRL.", ce);

            return null;
        }

        CRLDataPK pk = new CRLDataPK(getFingerprint());

        return pk;
    }

    /**
     * DOCUMENT ME!
     *
     * @param incrl DOCUMENT ME!
     * @param number DOCUMENT ME!
     */
    public void ejbPostCreate(X509CRL incrl, int number) {
        // Do nothing. Required.
    }
}
