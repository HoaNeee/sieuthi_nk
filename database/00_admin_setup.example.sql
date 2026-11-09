-- TEMPLATE ONLY. A human DBA runs this manually in FREEPDB1.
-- Inspect existing users/tablespaces FIRST. Do not rerun CREATE for existing objects.
-- Adjust the datafile path only to match the actual Windows Oracle installation.
-- This file does NOT execute CREATE USER/TABLESPACE until a DBA deliberately uncomments it.
SELECT SYS_CONTEXT('USERENV','CON_NAME') AS PDB_NAME FROM DUAL;
SELECT TABLESPACE_NAME FROM DBA_TABLESPACES WHERE TABLESPACE_NAME='TS_SIEUTHI';
SELECT USERNAME, DEFAULT_TABLESPACE, ACCOUNT_STATUS FROM DBA_USERS WHERE USERNAME='SIEUTHI';

-- Run only in FREEPDB1, if TS_SIEUTHI is absent:
-- CREATE TABLESPACE TS_SIEUTHI
-- DATAFILE 'C:\26AI\ORADATA\FREE\FREEPDB1\TS_SIEUTHI01.DBF'
-- SIZE 100M AUTOEXTEND ON NEXT 10M MAXSIZE 500M;

-- Create SIEUTHI using the SQL Developer user dialog or a private DBA script.
-- Supply the password locally, never commit it or use SYS/SYSTEM in Spring.
-- Required settings:
-- DEFAULT TABLESPACE TS_SIEUTHI
-- TEMPORARY TABLESPACE TEMP
-- QUOTA UNLIMITED ON TS_SIEUTHI

-- After verifying user creation, the DBA may run:
-- GRANT CONNECT, RESOURCE, CREATE VIEW, CREATE TRIGGER, CREATE PROCEDURE, CREATE SEQUENCE TO SIEUTHI;

