-- New columns in CertificateData are added by the JPA provider if there are sufficient privileges
-- if not added automatically the following SQL statements can be run to add the new columns 
-- ALTER TABLE CertificateData ADD crlPartitionIndex INTEGER DEFAULT 0 NOT NULL;
-- ALTER TABLE NoConflictCertificateData ADD crlPartitionIndex INTEGER DEFAULT 0 NOT NULL;
-- ALTER TABLE CRLData ADD crlPartitionIndex INTEGER DEFAULT 0 NOT NULL;
