-- New column in CertificateData and NoConflictCertificateData is added by the JPA provider if there are sufficient privileges
-- if not added automatically the following SQL statements can be run to add the new column 
-- ALTER TABLE CertificateData ADD invalidityDate BIGINT;
-- ALTER TABLE NoConflictCertificateData ADD invalidityDate BIGINT;