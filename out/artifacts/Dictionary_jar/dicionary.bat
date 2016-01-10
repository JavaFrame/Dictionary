@ECHO off

SET pathToDictionary=%1
SET logging=%1

IF "%pathToDictionary%" EQU "-logging" SET pathToDictionary="dictionary.txt"
IF "%pathToDictionary%" NEQ " " SET pathToDictionary="dictionary.txt"

ECHO java -jar Dictionary.jar %pathToDictionary% %logging%

java -jar Dictionary.jar %pathToDictionary% %logging%