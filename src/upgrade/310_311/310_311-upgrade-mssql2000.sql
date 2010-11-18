
-- ServiceData gets two new columns
ALTER TABLE ServiceData ADD nextRunTimeStamp BIGINT NOT NULL DEFAULT '0';
ALTER TABLE ServiceData ADD runTimeStamp BIGINT NOT NULL DEFAULT '0';

-- Add rowVersion column to all tables
ALTER TABLE AccessRulesData ADD COLUMN rowVersion INTEGER NOT NULL DEFAULT '0';
ALTER TABLE AdminEntityData ADD COLUMN rowVersion INTEGER NOT NULL DEFAULT '0';
ALTER TABLE AdminGroupData ADD COLUMN rowVersion INTEGER NOT NULL DEFAULT '0';
ALTER TABLE AdminPreferencesData ADD COLUMN rowVersion INTEGER NOT NULL DEFAULT '0';
ALTER TABLE ApprovalData ADD COLUMN rowVersion INTEGER NOT NULL DEFAULT '0';
ALTER TABLE AuthorizationTreeUpdateData ADD COLUMN rowVersion INTEGER NOT NULL DEFAULT '0';
ALTER TABLE CAData ADD COLUMN rowVersion INTEGER NOT NULL DEFAULT '0';
ALTER TABLE CRLData ADD COLUMN rowVersion INTEGER NOT NULL DEFAULT '0';
ALTER TABLE CertReqHistoryData ADD COLUMN rowVersion INTEGER NOT NULL DEFAULT '0';
ALTER TABLE CertificateData ADD COLUMN rowVersion INTEGER NOT NULL DEFAULT '0';
ALTER TABLE CertificateProfileData ADD COLUMN rowVersion INTEGER NOT NULL DEFAULT '0';
ALTER TABLE EndEntityProfileData ADD COLUMN rowVersion INTEGER NOT NULL DEFAULT '0';
ALTER TABLE GlobalConfigurationData ADD COLUMN rowVersion INTEGER NOT NULL DEFAULT '0';
ALTER TABLE HardTokenCertificateMap ADD COLUMN rowVersion INTEGER NOT NULL DEFAULT '0';
ALTER TABLE HardTokenData ADD COLUMN rowVersion INTEGER NOT NULL DEFAULT '0';
ALTER TABLE HardTokenIssuerData ADD COLUMN rowVersion INTEGER NOT NULL DEFAULT '0';
ALTER TABLE HardTokenProfileData ADD COLUMN rowVersion INTEGER NOT NULL DEFAULT '0';
ALTER TABLE HardTokenPropertyData ADD COLUMN rowVersion INTEGER NOT NULL DEFAULT '0';
ALTER TABLE KeyRecoveryData ADD COLUMN rowVersion INTEGER NOT NULL DEFAULT '0';
ALTER TABLE LogConfigurationData ADD COLUMN rowVersion INTEGER NOT NULL DEFAULT '0';
ALTER TABLE LogEntryData ADD COLUMN rowVersion INTEGER NOT NULL DEFAULT '0';
ALTER TABLE PublisherData ADD COLUMN rowVersion INTEGER NOT NULL DEFAULT '0';
ALTER TABLE ServiceData ADD COLUMN rowVersion INTEGER NOT NULL DEFAULT '0';
ALTER TABLE UserData ADD COLUMN rowVersion INTEGER NOT NULL DEFAULT '0';
ALTER TABLE UserDataSourceData ADD COLUMN rowVersion INTEGER NOT NULL DEFAULT '0';
