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

package org.ejbca.core.ejb;

import javax.ejb.Local;

import org.cesecore.audit.audit.SecurityEventsAuditorSessionLocal;
import org.cesecore.audit.log.SecurityEventsLoggerSessionLocal;
import org.cesecore.authentication.AuthenticationSessionLocal;
import org.cesecore.authorization.control.AccessControlSessionLocal;
import org.cesecore.authorization.rules.AccessRuleManagementSessionLocal;
import org.cesecore.certificates.ca.CaSessionLocal;
import org.cesecore.certificates.certificate.CertificateCreateSessionLocal;
import org.cesecore.certificates.certificate.CertificateStoreSessionLocal;
import org.cesecore.certificates.certificateprofile.CertificateProfileSessionLocal;
import org.cesecore.certificates.crl.CrlCreateSessionLocal;
import org.cesecore.certificates.crl.CrlStoreSessionLocal;
import org.cesecore.core.ejb.authorization.AdminEntitySessionLocal;
import org.cesecore.mock.authentication.SimpleAuthenticationProviderLocal;
import org.cesecore.roles.access.RoleAccessSessionLocal;
import org.ejbca.core.ejb.approval.ApprovalExecutionSessionLocal;
import org.ejbca.core.ejb.approval.ApprovalSessionLocal;
import org.ejbca.core.ejb.ca.auth.OldAuthenticationSessionLocal;
import org.ejbca.core.ejb.ca.caadmin.CAAdminSessionLocal;
import org.ejbca.core.ejb.ca.publisher.PublisherQueueSessionLocal;
import org.ejbca.core.ejb.ca.publisher.PublisherSessionLocal;
import org.ejbca.core.ejb.ca.revoke.RevocationSessionLocal;
import org.ejbca.core.ejb.ca.sign.SignSessionLocal;
import org.ejbca.core.ejb.ca.store.CertReqHistorySessionLocal;
import org.ejbca.core.ejb.config.GlobalConfigurationSessionLocal;
import org.ejbca.core.ejb.hardtoken.HardTokenBatchJobSessionLocal;
import org.ejbca.core.ejb.hardtoken.HardTokenSessionLocal;
import org.ejbca.core.ejb.keyrecovery.KeyRecoverySessionLocal;
import org.ejbca.core.ejb.log.LogConfigurationSessionLocal;
import org.ejbca.core.ejb.log.LogSessionLocal;
import org.ejbca.core.ejb.log.OldLogSessionLocal;
import org.ejbca.core.ejb.ra.UserAdminSessionLocal;
import org.ejbca.core.ejb.ra.raadmin.EndEntityProfileSessionLocal;
import org.ejbca.core.ejb.ra.raadmin.RaAdminSessionLocal;
import org.ejbca.core.ejb.ra.userdatasource.UserDataSourceSessionLocal;
import org.ejbca.core.ejb.services.ServiceSessionLocal;
import org.ejbca.core.protocol.cmp.CmpMessageDispatcherSessionLocal;

/**
 * Due to the lack of standardization in JEE5 there is no way to lookup local interfaces.
 * 
 * This Stateless Session Bean (SSB) act as a bridge between calling classes in the same JVM,
 * and the real ejb references.
 * 
 * This will allow us to define a single (this) local EJB in all web.xml and ejb-jar.xml files
 * and are then free to change and move around SSBs and their interfaces without XML changes.
 * 
 * @version $Id$
 */
@Local
public interface EjbBridgeSessionLocal {

        AccessControlSessionLocal getAccessControlSession();
	AccessRuleManagementSessionLocal getAccessRuleManagementSession();
        AdminEntitySessionLocal getAdminEntitySession();	
	ApprovalExecutionSessionLocal getApprovalExecutionSession();
	ApprovalSessionLocal getApprovalSession();
	AuthenticationSessionLocal getAuthenticationSession();
	@Deprecated OldAuthenticationSessionLocal getOldAuthenticationSession(); //TODO: This will be removed.
	CAAdminSessionLocal getCaAdminSession();
	CaSessionLocal getCaSession();
	CertificateProfileSessionLocal getCertificateProfileSession();
	CertificateStoreSessionLocal getCertificateStoreSession();
	CertReqHistorySessionLocal getCertReqHistorySession();
	RevocationSessionLocal getRevocationSession();
	CmpMessageDispatcherSessionLocal getCmpMessageDispatcherSession();
	CrlStoreSessionLocal getCrlStoreSession();
	CrlCreateSessionLocal getCrlCreateSession();
	EndEntityProfileSessionLocal getEndEntityProfileSession();
	LogConfigurationSessionLocal getLogConfigurationSession();
	GlobalConfigurationSessionLocal getGlobalConfigurationSession();
	HardTokenBatchJobSessionLocal getHardTokenBatchJobSession();
	HardTokenSessionLocal getHardTokenSession();
	KeyRecoverySessionLocal getKeyRecoverySession();
	LogSessionLocal getLogSession();
	OldLogSessionLocal getOldLogSession();
	PublisherQueueSessionLocal getPublisherQueueSession();
	PublisherSessionLocal getPublisherSession();
	RaAdminSessionLocal getRaAdminSession();
	RoleAccessSessionLocal getRoleAccessSession();
	SecurityEventsAuditorSessionLocal getSecurityEventsAuditorSession();
	SecurityEventsLoggerSessionLocal getSecurityEventsLoggerSession();
	ServiceSessionLocal getServiceSession();
	SignSessionLocal getSignSession();
	CertificateCreateSessionLocal getCertificateCreateSession();
	UserDataSourceSessionLocal getUserDataSourceSession();
	UserAdminSessionLocal getUserAdminSession();
	
	@Deprecated SimpleAuthenticationProviderLocal getSimpleAuthenticationProvider();
}
