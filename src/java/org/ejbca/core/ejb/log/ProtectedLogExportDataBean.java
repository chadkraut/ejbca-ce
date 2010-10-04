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

package org.ejbca.core.ejb.log;

import javax.ejb.CreateException;

import org.ejbca.core.ejb.BaseEntityBean;
import org.ejbca.util.Base64;
import org.ejbca.util.GUIDGenerator;

/** Entity bean should not be used directly, use though Session beans.
*
* Entity Bean representing a ProtectedLogExport in the log database.
* Information stored:
* <pre>
*  pk (Primary Key) is a 32 byte GUID generated by org.ejbca.util.GUIDGenerator
*  timeOfExport = The time when the export was done.
*  exportEndTime = The newest event for this export
*  exportStartTime = The oldest event for this export
*  b64LogDataHash = The hashed log-related data
*  b64PreviosExportHash = The hash of the previous export.
*  currentHashAlgorithm = The used hash algorithm
*  b64SignatureCertificate = The certificate that corresponds to the signing key
*  deleted = If the LogEvents were removed after the export.
*  b64Signature = The signature of all the previous columns (except pk)
* </pre>
*
* @ejb.bean
*   description="This enterprise bean entity represents a ProtectedLogExport with accompanying data"
*   display-name="ProtectedLogExportDataEB"
*   name="ProtectedLogExportData"
*   jndi-name="ProtectedLogExportData"
*   view-type="local"
*   type="CMP"
*   reentrant="False"
*   cmp-version="2.x"
*   transaction-type="Container"
*   schema="ProtectedLogExportDataBean"
*   primkey-field="pk"
*
* @ejb.pk
*   generate="false"
*   class="java.lang.String"
*
* @ejb.persistence table-name = "ProtectedLogExportData"
* 
* @ejb.home
*   generate="local"
*   local-extends="javax.ejb.EJBLocalHome"
*   local-class="org.ejbca.core.ejb.log.ProtectedLogExportDataLocalHome"
*
* @ejb.interface
*   generate="local"
*   local-extends="javax.ejb.EJBLocalObject"
*   local-class="org.ejbca.core.ejb.log.ProtectedLogExportDataLocal"
*
* @ejb.transaction type="Required"
* 
* @ejb.finder
*   description="findByExportStartTime"
*   signature="org.ejbca.core.ejb.log.ProtectedLogExportDataLocal findByExportStartTime(long exportStartTime)"
*   query="SELECT OBJECT(a) from ProtectedLogExportDataBean a WHERE a.exportStartTime=?1"
*   
* @jboss.method-attributes
*   pattern = "get*"
*   read-only = "true"
*
* @jboss.method-attributes
*   pattern = "find*"
*   read-only = "true"
*   
* @jonas.jdbc-mapping
*   jndi-name="${datasource.jndi-name}"
* @deprecated
*/
public abstract class ProtectedLogExportDataBean extends BaseEntityBean {

    /**
     * @ejb.create-method view-type="local"
     */
	public String ejbCreate(long timeOfExport, long exportEndTime, long exportStartTime, byte[] logDataHash, byte[] previosExportHash, String currentHashAlgorithm,
			byte[] signatureCertificate, boolean deleted, byte[] signature) throws CreateException {
		setPk(GUIDGenerator.generateGUID(this));
		setTimeOfExport(timeOfExport);
	    setExportEndTime(exportEndTime);
	    setExportStartTime(exportStartTime);
	    setLogDataHash(logDataHash);
	    setPreviosExportHash(previosExportHash);
	    setCurrentHashAlgorithm(currentHashAlgorithm);
	    setSignatureCertificate(signatureCertificate);
	    setDeleted(deleted);
	    setSignature(signature);		
		return null;
	}
	
	public void ejbPostCreate(long timeOfExport, long exportEndTime, long exportStartTime, byte[] logDataHash, byte[] previosExportHash, String currentHashAlgorithm,
			byte[] signatureCertificate, boolean deleted, byte[] signature) {
	}

	/**
     * @ejb.pk-field
     * @ejb.persistence column-name="pk"
     * @ejb.interface-method
     */
    public abstract String getPk();
    public abstract void setPk(String pk);

    /**
     * @ejb.persistence column-name="rowVersion"
     */
    public abstract int getRowVersion();
    public abstract void setRowVersion(int rowVersion);

    /**
     * @ejb.persistence column-name="timeOfExport"
     * @ejb.interface-method
     */
    public abstract long getTimeOfExport();
    /** @ejb.interface-method  */
    public abstract void setTimeOfExport(long timeOfExport);
    
    /**
     * @ejb.persistence column-name="exportEndTime"
     * @ejb.interface-method
     */
    public abstract long getExportEndTime();
    public abstract void setExportEndTime(long exportEndTime);

    /**
     * @ejb.persistence column-name="exportStartTime"
     * @ejb.interface-method
     */
    public abstract long getExportStartTime();
    public abstract void setExportStartTime(long exportStartTime);
    
    /**
     * @ejb.persistence column-name="b64LogDataHash"
     * @ejb.interface-method
     */
    public abstract String getB64LogDataHash();
    public abstract void setB64LogDataHash(String b64LogDataHash);

    /** @ejb.interface-method  */
    public byte[] getLogDataHash() {
    	String b64LogDataHash = getB64LogDataHash();
    	if (b64LogDataHash == null) {
    		return null;
    	}
    	return Base64.decode(b64LogDataHash.getBytes());
    }
    /** @ejb.interface-method  */
    public void setLogDataHash(byte[] data) {
    	if (data == null) {
    		setB64LogDataHash(null);
    	} else {
            setB64LogDataHash(new String(Base64.encode(data, false)));
    	}
    }
    
    /**
     * @ejb.persistence column-name="b64PreviosExportHash"
     * @ejb.interface-method
     */
    public abstract String getB64PreviosExportHash();
    public abstract void setB64PreviosExportHash(String b64PreviosExportHash);

    /** @ejb.interface-method  */
    public byte[] getPreviosExportHash() {
    	String b64PreviosExportHash = getB64PreviosExportHash();
    	if (b64PreviosExportHash == null) {
    		return null;
    	}
        return Base64.decode(b64PreviosExportHash.getBytes());
    }
    /** @ejb.interface-method  */
    public void setPreviosExportHash(byte[] data) {
    	if (data == null) {
    		setB64PreviosExportHash(null);
    	} else {
            setB64PreviosExportHash(new String(Base64.encode(data, false)));
    	}
    }
    
    /**
     * @ejb.persistence column-name="currentHashAlgorithm"
     * @ejb.interface-method
     */
    public abstract String getCurrentHashAlgorithm();
    /** @ejb.interface-method  */
    public abstract void setCurrentHashAlgorithm(String currentHashAlgorithm);
    
    /**
     * @ejb.persistence jdbc-type="LONGVARCHAR" column-name="b64SignatureCertificate"
     * @ejb.interface-method
     */
    public abstract String getB64SignatureCertificate();
    public abstract void setB64SignatureCertificate(String b64SignatureCertificate);

    /** @ejb.interface-method  */
    public byte[] getSignatureCertificate() {
    	String b64SignatureCertificate = getB64SignatureCertificate();
    	if (b64SignatureCertificate == null) {
    		return null;
    	}
        return Base64.decode(b64SignatureCertificate.getBytes());
    }
    /** @ejb.interface-method  */
    public void setSignatureCertificate(byte[] data) {
    	if (data == null) {
    		setB64SignatureCertificate(null);
    	} else {
            setB64SignatureCertificate(new String(Base64.encode(data, false)));
    	}
    }

    /**
     * @ejb.persistence column-name="deleted"
     * @ejb.interface-method
     */
    public abstract boolean getDeleted();
    /** @ejb.interface-method  */
    public abstract void setDeleted(boolean deleted);

    /**
     * @ejb.persistence jdbc-type="LONGVARCHAR" column-name="b64Signature"
     * @ejb.interface-method
     */
    public abstract String getB64Signature();
    public abstract void setB64Signature(String b64Signature);

    /** @ejb.interface-method  */
    public byte[] getSignature() {
    	String b64Signature = getB64Signature();
    	if (b64Signature == null) {
    		return null;
    	}
        return Base64.decode(b64Signature.getBytes());
    }
    /** @ejb.interface-method  */
    public void setSignature(byte[] data) {
    	if (data == null) {
    		setB64Signature(null);
    	} else {
            setB64Signature(new String(Base64.encode(data, false)));
    	}
    }
}
