/*************************************************************************
 *                                                                       *
 *  EJBCA Community: The OpenSource Certificate Authority                *
 *                                                                       *
 *  This software is free software; you can redistribute it and/or       *
 *  modify it under the terms of the GNU Lesser General Public           *
 *  License as published by the Free Software Foundation; either         *
 *  version 2.1 of the License, or any later version.                    *
 *                                                                       *
 *  See terms of license at gnu.org.                                     *
 *                                                                       *
 *************************************************************************/
package org.ejbca.core.model.ra;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.cesecore.certificates.endentity.EndEntityInformation;
import org.cesecore.certificates.endentity.EndEntityType;
import org.cesecore.certificates.endentity.EndEntityTypes;
import org.cesecore.certificates.endentity.ExtendedInformation;
import org.cesecore.certificates.util.DNFieldExtractor;
import org.cesecore.certificates.util.DnComponents;
import org.cesecore.util.CertTools;
import org.ejbca.core.model.ra.raadmin.EndEntityProfile;
import org.ejbca.util.dn.DistinguishedName;

import javax.naming.InvalidNameException;
import javax.naming.ldap.Rdn;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** This class gives facilities to populate user data with default values from profile.
 *
 * @version $Id: EndEntityInformationFiller.java 34943 2020-04-29 12:12:05Z anatom $
 */
public class EndEntityInformationFiller {

    /** For log purpose. */
    private static final Logger log = Logger.getLogger(EndEntityInformationFiller.class.getName());

    /** This method fill user data with the default values from the specified profile.
     *
     * @param userData user data.
     * @param profile user associated profile.
     * @param onlyEnforcedFields include enforced Subject DN or alternate name field in End Entity profile
     * @return update user.
     */
    public static EndEntityInformation fillUserDataWithDefaultValues(final EndEntityInformation userData, 
            final EndEntityProfile profile) {
        
        if(userData.isDnMerged() && userData.isSanMerged()) {
            return userData;
        }

    	if (StringUtils.isEmpty(userData.getUsername())) {
        	userData.setUsername(profile.getUsernameDefault());
        }
    	if (userData.getSendNotification() == false) {
    		if(StringUtils.isNotEmpty(profile.getValue(EndEntityProfile.SENDNOTIFICATION, 0))) {
    			final Boolean isSendNotification = Boolean.valueOf(profile.getValue(EndEntityProfile.SENDNOTIFICATION, 0));
    			userData.setSendNotification(isSendNotification.booleanValue());
    		}
        }
    	if (StringUtils.isEmpty(userData.getEmail())) {
			final String email = profile.getValue(EndEntityProfile.EMAIL, 0);
			if (StringUtils.isNotEmpty(email) && email.indexOf("@") > 0) {
				userData.setEmail(email);
			}
		}
        //Batch generation (clear text pwd storage) is only active when password 
        //is not empty so is not necessary to do something here
        if (StringUtils.isEmpty(userData.getPassword())) {
            // check if the password is autogenerated
        	if(!profile.useAutoGeneratedPasswd()) {
        		userData.setPassword(profile.getPredefinedPassword());
        	}
        }

        // Processing Subject DN values
        String subjectDn = userData.getDN();
        if(!userData.isDnMerged()) {
            subjectDn = mergeSubjectDnWithDefaultValues(subjectDn, profile, userData.getEmail());
            userData.setDN(subjectDn);
            userData.setDnMerged();
        }
        String subjectAltName = userData.getSubjectAltName();
        // Processing Subject Altname values
        if(!userData.isDnMerged()) {
            subjectAltName = mergeSubjectAltNameWithDefaultValues(subjectAltName, profile, userData.getEmail());
            userData.setSubjectAltName(subjectAltName);
            userData.setSanMerged();
        }
        if (userData.getType().getHexValue() == EndEntityTypes.INVALID.hexValue()) {
        	if (StringUtils.isNotEmpty(profile.getValue(EndEntityProfile.FIELDTYPE, 0))) {
        	    final int type = Integer.parseInt(profile.getValue(EndEntityProfile.FIELDTYPE, 0));
        		userData.setType(new EndEntityType(type));
        	}
        }
        
        if (profile.isCabfOrganizationIdentifierUsed()) {
            ExtendedInformation extInfo = userData.getExtendedInformation();
            if (extInfo == null) {
                extInfo = new ExtendedInformation();
                extInfo.setCabfOrganizationIdentifier(profile.getCabfOrganizationIdentifier());
                userData.setExtendedInformation(extInfo);
            } else if (StringUtils.isEmpty(extInfo.getCabfOrganizationIdentifier())) {
                extInfo.setCabfOrganizationIdentifier(profile.getCabfOrganizationIdentifier());
            }
        }
                
        return userData;
    }

    /**
     * This method merge subject DN with data from End entity profile.
     *
     * @param subjectDN   user Distinguished Name.
     * @param profile     user associated profile.
     * @param entityEmail entity email.
     * @return updated DN.
     */
    public static String mergeSubjectDnWithDefaultValues(String subjectDN, EndEntityProfile profile,
                                                          String entityEmail) {
        DistinguishedName profiledn;
        DistinguishedName userdn;
        if (StringUtils.isNotEmpty(subjectDN)) {
            try {
                userdn = new DistinguishedName(subjectDN);
            } catch (InvalidNameException ine) {
                log.debug(subjectDN, ine);
                throw new RuntimeException(ine);
            }
        } else {
            userdn = new DistinguishedName(Collections.emptyList());
        }
        final int numberofsubjectdnfields = profile.getSubjectDNFieldOrderLength();
        final List<Rdn> rdnList = new ArrayList<Rdn>(numberofsubjectdnfields);
        int[] fielddata = null;
        String value;
        //Build profile's DN
        for (int i = 0; i < numberofsubjectdnfields; i++) {
            fielddata = profile.getSubjectDNFieldsInOrder(i);
            value = profile.getValue(fielddata[EndEntityProfile.FIELDTYPE], fielddata[EndEntityProfile.NUMBER]);
            if (!StringUtils.isEmpty(value) && !StringUtils.isWhitespace(value)) {
                value = value.trim();
                addFieldValueToRdnList(rdnList, fielddata, value, DNFieldExtractor.TYPE_SUBJECTDN);
            }
        }
        // As the constructor taking a list of RDNs numbers them from behind, which is typically not what we want when mergin DNs from a profile
        // that has specified CN=User,OU=Org1,OU=Org2, we'll reverse them. This is of little importance when you only have one item of each component,
        // but when you have multiple, such as sevral OUs it becomes important. See DistingushedNameTest for more detailed tests of this behavios
        Collections.reverse(rdnList);
        profiledn = new DistinguishedName(rdnList);
        if (log.isDebugEnabled()) {
            log.debug("user subject DN: " + userdn);
            log.debug("Profile DN to merge with subject DN: " + profiledn.toString());
        }

        Map<String, String> dnMap = new HashMap<String, String>();
        if (profile.getUse(DnComponents.DNEMAILADDRESS, 0)) {
            dnMap.put(DnComponents.DNEMAILADDRESS, entityEmail);
        }

        return CertTools.stringToBCDNString(profiledn.mergeDN(userdn, true, dnMap).toString());
    }

    /**
     * This method merge subject Alt name with data from End entity profile.
     *
     * @param subjectAltName user subject alt name.
     * @param profile        user associated profile.
     * @param entityEmail    entity email field
     * @return updated subject alt name
     */
    public static String mergeSubjectAltNameWithDefaultValues(String subjectAltName, EndEntityProfile profile, 
                                    String entityEmail) {
        DistinguishedName profileAltName;
        DistinguishedName userAltName;
        try {
            if(subjectAltName==null) {
                subjectAltName = "";
            }
            userAltName = new DistinguishedName(subjectAltName);
        } catch (InvalidNameException ine) {
            log.debug(subjectAltName,ine);
            throw new RuntimeException(ine);
        }
        int numberofsubjectAltNamefields = profile.getSubjectAltNameFieldOrderLength();
        List<Rdn> rdnList = new ArrayList<Rdn>(numberofsubjectAltNamefields);
        int[] fielddata = null;
        String value;
        //Build profile's Alt Name
        for (int i = 0; i < numberofsubjectAltNamefields; i++) {
            fielddata = profile.getSubjectAltNameFieldsInOrder(i);
            value = profile.getValue(fielddata[EndEntityProfile.FIELDTYPE], fielddata[EndEntityProfile.NUMBER]);
            if (value != null) {
                value = value.trim();
                if (!value.equals("")) {
                    addFieldValueToRdnList(rdnList, fielddata, value, DNFieldExtractor.TYPE_SUBJECTALTNAME);
                }
            }
        }
        profileAltName = new DistinguishedName(rdnList);

        Map<String, String> dnMap = new HashMap<String, String>();
        if (profile.getUse(DnComponents.RFC822NAME, 0)) {
            dnMap.put(DnComponents.RFC822NAME, entityEmail);
        }

        return  profileAltName.mergeDN(userAltName, true, dnMap).toString();
    }

    /**
     *  Adds a value to rdnList.
     * @param rdnList rdnList to be updated
     * @param fielddata a field data, what will be added to rdnList
     * @param value field value to be added to rdnList
     * @param dnFieldExtractorType subject DNFieldExtractor.TYPE_SUBJECTALTNAME or subject DNFieldExtractor.TYPE_SUBJECTDN
     */
    private static void addFieldValueToRdnList(List<Rdn> rdnList, final int[] fielddata, final String value, final int dnFieldExtractorType) {
        String parameter = DNFieldExtractor.getFieldComponent(
                DnComponents.profileIdToDnId(fielddata[EndEntityProfile.FIELDTYPE]),
                dnFieldExtractorType);
        try {
            parameter = StringUtils.replace(parameter, "=", "");
            rdnList.add(new Rdn(parameter, value));
        } catch (InvalidNameException ine) {
            log.debug("InvalidNameException while creating new Rdn with parameter " + parameter + " and value " + value, ine);
            throw new RuntimeException(ine);
        }
    }


    /**
     * Gets the first Common Name value from subjectDn and sets this value to all dns's with "use from CN" checked
     *
     * @param endEntityProfile EEP selected for end entity
     * @param subjectDn        provided subjectDn
     * @return String with comma separated DNSNames
     */
    public static String copyDnsNameValueFromCn(final EndEntityProfile endEntityProfile, String subjectDn) {
        if (endEntityProfile == null) {
            return StringUtils.EMPTY;
        }
        StringBuilder dnses = new StringBuilder();
        String commonName = CertTools.getCommonNameFromSubjectDn(subjectDn);
        if (StringUtils.isNotEmpty(commonName)) {
            int[] field = null;
            final int numberOfFields = endEntityProfile.getSubjectAltNameFieldOrderLength();
            for (int i = 0; i < numberOfFields; i++) {
                field = endEntityProfile.getSubjectAltNameFieldsInOrder(i);
                final boolean isDnsField = EndEntityProfile.isFieldOfType(field[EndEntityProfile.FIELDTYPE], DnComponents.DNSNAME);
                final boolean isCopy = endEntityProfile.getCopy(field[EndEntityProfile.FIELDTYPE], field[EndEntityProfile.NUMBER]);
                if (isDnsField && isCopy) {
                    if (dnses.length() > 0) {
                        dnses.append(", ");
                    }
                    int dnId = DnComponents.profileIdToDnId(field[EndEntityProfile.FIELDTYPE]);
                    String nameValueDnPart = DNFieldExtractor.getFieldComponent(dnId, DNFieldExtractor.TYPE_SUBJECTALTNAME) + commonName;
                    dnses.append(nameValueDnPart);
                }
            }
        }
        return dnses.toString();
    }
}
