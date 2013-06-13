CREATE TABLE AccessRulesData (
    pK NUMBER(10) NOT NULL,
    accessRule VARCHAR2(255 byte) NOT NULL,
    isRecursive NUMBER(1) NOT NULL,
    rowProtection CLOB,
    rowVersion NUMBER(10) NOT NULL,
    rule NUMBER(10) NOT NULL,
    AdminGroupData_accessRules NUMBER(10),
    PRIMARY KEY (pK)
);

CREATE TABLE AdminEntityData (
    pK NUMBER(10) NOT NULL,
    cAId NUMBER(10) NOT NULL,
    matchType NUMBER(10) NOT NULL,
    matchValue VARCHAR2(255 byte),
    matchWith NUMBER(10) NOT NULL,
    rowProtection CLOB,
    rowVersion NUMBER(10) NOT NULL,
    tokenType VARCHAR2(255 byte),
    AdminGroupData_adminEntities NUMBER(10),
    PRIMARY KEY (pK)
);

CREATE TABLE AdminGroupData (
    pK NUMBER(10) NOT NULL,
    adminGroupName VARCHAR2(255 byte) NOT NULL,
    rowProtection CLOB,
    rowVersion NUMBER(10) NOT NULL,
    PRIMARY KEY (pK)
);

CREATE TABLE AdminPreferencesData (
    id VARCHAR2(255 byte) NOT NULL,
    data BLOB NOT NULL,
    rowProtection CLOB,
    rowVersion NUMBER(10) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE ApprovalData (
    id NUMBER(10) NOT NULL,
    approvalData CLOB NOT NULL,
    approvalId NUMBER(10) NOT NULL,
    approvalType NUMBER(10) NOT NULL,
    cAId NUMBER(10) NOT NULL,
    endEntityProfileId NUMBER(10) NOT NULL,
    expireDate NUMBER(19) NOT NULL,
    remainingApprovals NUMBER(10) NOT NULL,
    reqAdminCertIssuerDn VARCHAR2(255 byte),
    reqAdminCertSn VARCHAR2(255 byte),
    requestData CLOB NOT NULL,
    requestDate NUMBER(19) NOT NULL,
    rowProtection CLOB,
    rowVersion NUMBER(10) NOT NULL,
    status NUMBER(10) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE AuditRecordData (
    pk VARCHAR2(255 byte) NOT NULL,
    additionalDetails CLOB,
    authToken VARCHAR2(255 byte) NOT NULL,
    customId VARCHAR2(255 byte),
    eventStatus VARCHAR2(255 byte) NOT NULL,
    eventType VARCHAR2(255 byte) NOT NULL,
    module VARCHAR2(255 byte) NOT NULL,
    nodeId VARCHAR2(255 byte) NOT NULL,
    rowProtection CLOB,
    rowVersion NUMBER(10) NOT NULL,
    searchDetail1 VARCHAR2(255 byte),
    searchDetail2 VARCHAR2(255 byte),
    sequenceNumber NUMBER(19) NOT NULL,
    service VARCHAR2(255 byte) NOT NULL,
    timeStamp NUMBER(19) NOT NULL,
    PRIMARY KEY (pk)
);

CREATE TABLE AuthorizationTreeUpdateData (
    pK NUMBER(10) NOT NULL,
    authorizationTreeUpdateNumber NUMBER(10) NOT NULL,
    rowProtection CLOB,
    rowVersion NUMBER(10) NOT NULL,
    PRIMARY KEY (pK)
);

CREATE TABLE CAData (
    cAId NUMBER(10) NOT NULL,
    data CLOB NOT NULL,
    expireTime NUMBER(19) NOT NULL,
    name VARCHAR2(255 byte),
    rowProtection CLOB,
    rowVersion NUMBER(10) NOT NULL,
    status NUMBER(10) NOT NULL,
    subjectDN VARCHAR2(255 byte),
    updateTime NUMBER(19) NOT NULL,
    PRIMARY KEY (cAId)
);

CREATE TABLE CRLData (
    fingerprint VARCHAR2(255 byte) NOT NULL,
    base64Crl CLOB NOT NULL,
    cAFingerprint VARCHAR2(255 byte) NOT NULL,
    cRLNumber NUMBER(10) NOT NULL,
    deltaCRLIndicator NUMBER(10) NOT NULL,
    issuerDN VARCHAR2(255 byte) NOT NULL,
    nextUpdate NUMBER(19) NOT NULL,
    rowProtection CLOB,
    rowVersion NUMBER(10) NOT NULL,
    thisUpdate NUMBER(19) NOT NULL,
    PRIMARY KEY (fingerprint)
);

CREATE TABLE CertReqHistoryData (
    fingerprint VARCHAR2(255 byte) NOT NULL,
    issuerDN VARCHAR2(255 byte) NOT NULL,
    rowProtection CLOB,
    rowVersion NUMBER(10) NOT NULL,
    serialNumber VARCHAR2(255 byte) NOT NULL,
    timestamp NUMBER(19) NOT NULL,
    userDataVO CLOB NOT NULL,
    username VARCHAR2(255 byte) NOT NULL,
    PRIMARY KEY (fingerprint)
);

CREATE TABLE CertificateData (
    fingerprint VARCHAR2(255 byte) NOT NULL,
    base64Cert CLOB,
    cAFingerprint VARCHAR2(255 byte),
    certificateProfileId NUMBER(10) NOT NULL,
    expireDate NUMBER(19) NOT NULL,
    issuerDN VARCHAR2(255 byte) NOT NULL,
    revocationDate NUMBER(19) NOT NULL,
    revocationReason NUMBER(10) NOT NULL,
    rowProtection CLOB,
    rowVersion NUMBER(10) NOT NULL,
    serialNumber VARCHAR2(255 byte) NOT NULL,
    status NUMBER(10) NOT NULL,
    subjectDN VARCHAR2(255 byte) NOT NULL,
    subjectKeyId VARCHAR2(255 byte),
    tag VARCHAR2(255 byte),
    type NUMBER(10) NOT NULL,
    updateTime NUMBER(19) NOT NULL,
    username VARCHAR2(255 byte),
    PRIMARY KEY (fingerprint)
);

CREATE TABLE Base64CertData (
    fingerprint VARCHAR2(255 byte) NOT NULL,
    base64Cert CLOB,
    rowProtection CLOB,
    rowVersion NUMBER(10) NOT NULL,
    PRIMARY KEY (fingerprint)
);

CREATE TABLE CertificateProfileData (
    id NUMBER(10) NOT NULL,
    certificateProfileName VARCHAR2(255 byte) NOT NULL,
    data BLOB NOT NULL,
    rowProtection CLOB,
    rowVersion NUMBER(10) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE CryptoTokenData (
    id NUMBER(10) NOT NULL,
    lastUpdate NUMBER(19) NOT NULL,
    rowProtection CLOB,
    rowVersion NUMBER(10) NOT NULL,
    tokenData CLOB,
    tokenName VARCHAR2(255 byte) NOT NULL,
    tokenProps CLOB,
    tokenType VARCHAR2(255 byte) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE EndEntityProfileData (
    id NUMBER(10) NOT NULL,
    data BLOB NOT NULL,
    profileName VARCHAR2(255 byte) NOT NULL,
    rowProtection CLOB,
    rowVersion NUMBER(10) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE InternalKeyBindingData (
    id NUMBER(10) NOT NULL,
    name VARCHAR2(255 byte) NOT NULL,
    status VARCHAR2(255 byte) NOT NULL,
    keyBindingType VARCHAR2(255 byte) NOT NULL,
    certificateId VARCHAR2(255 byte),
    cryptoTokenId NUMBER(10) NOT NULL,
    keyPairAlias VARCHAR2(255 byte) NOT NULL,
    rawData CLOB,
    lastUpdate NUMBER(19) NOT NULL,
    rowProtection CLOB,
    rowVersion NUMBER(10) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE GlobalConfigurationData (
    configurationId VARCHAR2(255 byte) NOT NULL,
    data BLOB NOT NULL,
    rowProtection CLOB,
    rowVersion NUMBER(10) NOT NULL,
    PRIMARY KEY (configurationId)
);

CREATE TABLE HardTokenCertificateMap (
    certificateFingerprint VARCHAR2(255 byte) NOT NULL,
    rowProtection CLOB,
    rowVersion NUMBER(10) NOT NULL,
    tokenSN VARCHAR2(255 byte) NOT NULL,
    PRIMARY KEY (certificateFingerprint)
);

CREATE TABLE HardTokenData (
    tokenSN VARCHAR2(255 byte) NOT NULL,
    cTime NUMBER(19) NOT NULL,
    data BLOB,
    mTime NUMBER(19) NOT NULL,
    rowProtection CLOB,
    rowVersion NUMBER(10) NOT NULL,
    significantIssuerDN VARCHAR2(255 byte),
    tokenType NUMBER(10) NOT NULL,
    username VARCHAR2(255 byte),
    PRIMARY KEY (tokenSN)
);

CREATE TABLE HardTokenIssuerData (
    id NUMBER(10) NOT NULL,
    adminGroupId NUMBER(10) NOT NULL,
    alias VARCHAR2(255 byte) NOT NULL,
    data BLOB NOT NULL,
    rowProtection CLOB,
    rowVersion NUMBER(10) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE HardTokenProfileData (
    id NUMBER(10) NOT NULL,
    data CLOB,
    name VARCHAR2(255 byte) NOT NULL,
    rowProtection CLOB,
    rowVersion NUMBER(10) NOT NULL,
    updateCounter NUMBER(10) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE HardTokenPropertyData (
    id VARCHAR2(255 byte) NOT NULL,
    property VARCHAR2(255 byte) NOT NULL,
    rowProtection CLOB,
    rowVersion NUMBER(10) NOT NULL,
    value VARCHAR2(255 byte),
    PRIMARY KEY (id,
    property)
);

CREATE TABLE KeyRecoveryData (
    certSN VARCHAR2(255 byte) NOT NULL,
    issuerDN VARCHAR2(255 byte) NOT NULL,
    keyData CLOB NOT NULL,
    markedAsRecoverable NUMBER(1) NOT NULL,
    rowProtection CLOB,
    rowVersion NUMBER(10) NOT NULL,
    username VARCHAR2(255 byte),
    PRIMARY KEY (certSN,
    issuerDN)
);

CREATE TABLE PublisherData (
    id NUMBER(10) NOT NULL,
    data CLOB,
    name VARCHAR2(255 byte),
    rowProtection CLOB,
    rowVersion NUMBER(10) NOT NULL,
    updateCounter NUMBER(10) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE PublisherQueueData (
    pk VARCHAR2(255 byte) NOT NULL,
    fingerprint VARCHAR2(255 byte),
    lastUpdate NUMBER(19) NOT NULL,
    publishStatus NUMBER(10) NOT NULL,
    publishType NUMBER(10) NOT NULL,
    publisherId NUMBER(10) NOT NULL,
    rowProtection CLOB,
    rowVersion NUMBER(10) NOT NULL,
    timeCreated NUMBER(19) NOT NULL,
    tryCounter NUMBER(10) NOT NULL,
    volatileData CLOB,
    PRIMARY KEY (pk)
);

CREATE TABLE ServiceData (
    id NUMBER(10) NOT NULL,
    data CLOB,
    name VARCHAR2(255 byte) NOT NULL,
    nextRunTimeStamp NUMBER(19) NOT NULL,
    rowProtection CLOB,
    rowVersion NUMBER(10) NOT NULL,
    runTimeStamp NUMBER(19) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE UserData (
    username VARCHAR2(255 byte) NOT NULL,
    cAId NUMBER(10) NOT NULL,
    cardNumber VARCHAR2(255 byte),
    certificateProfileId NUMBER(10) NOT NULL,
    clearPassword VARCHAR2(255 byte),
    endEntityProfileId NUMBER(10) NOT NULL,
    extendedInformationData CLOB,
    hardTokenIssuerId NUMBER(10) NOT NULL,
    keyStorePassword VARCHAR2(255 byte),
    passwordHash VARCHAR2(255 byte),
    rowProtection CLOB,
    rowVersion NUMBER(10) NOT NULL,
    status NUMBER(10) NOT NULL,
    subjectAltName VARCHAR2(255 byte),
    subjectDN VARCHAR2(255 byte),
    subjectEmail VARCHAR2(255 byte),
    timeCreated NUMBER(19) NOT NULL,
    timeModified NUMBER(19) NOT NULL,
    tokenType NUMBER(10) NOT NULL,
    type NUMBER(10) NOT NULL,
    PRIMARY KEY (username)
);

CREATE TABLE UserDataSourceData (
    id NUMBER(10) NOT NULL,
    data CLOB,
    name VARCHAR2(255 byte) NOT NULL,
    rowProtection CLOB,
    rowVersion NUMBER(10) NOT NULL,
    updateCounter NUMBER(10) NOT NULL,
    PRIMARY KEY (id)
);

alter table AccessRulesData add constraint FKABB4C1DFDBBC970 foreign key (AdminGroupData_accessRules) references AdminGroupData;

alter table AdminEntityData add constraint FKD9A99EBCB3A110AD foreign key (AdminGroupData_adminEntities) references AdminGroupData;

