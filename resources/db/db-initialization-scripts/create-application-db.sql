PRINT "Starting creating application database"
USE master
DECLARE @DatabaseName VARCHAR(50) = 'app';
-- IF EXISTS(select * from sys.databases where name=@DatabaseName)
--     EXEC('DROP DATABASE ' + @DatabaseName)
EXEC('CREATE DATABASE ' + @DatabaseName)
PRINT "Database has been created"