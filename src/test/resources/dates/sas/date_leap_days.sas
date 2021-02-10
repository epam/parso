/*
  SAS program to generate sas7bdat file with two types of columns: date and datetime.
  Both columns contain data around leap days.
  Years 4000 and 8000 don't have leap days in terms of SAS.
  Years 2000 and 6000 have it.
  All of them necessary for unit tests.
*/

options bufsize=4096;

data dev.date_leap_days;
data dev.date_leap_days;
  input d date9. dt datetime20.;
  format d date9. dt datetime20.;
  datalines;
28FEB2000 28FEB2000:00:00:00
29FEB2000 29FEB2000:00:00:00
01MAR2000 01MAR2000:00:00:00
31DEC2000 31DEC2000:00:00:00
28FEB4000 28FEB4000:00:00:00
01MAR4000 01MAR4000:00:00:00
31DEC4000 31DEC4000:00:00:00
28FEB6000 28FEB6000:00:00:00
29FEB6000 29FEB6000:00:00:00
01MAR6000 01MAR6000:00:00:00
31DEC6000 31DEC6000:00:00:00
28FEB8000 28FEB8000:00:00:00
01MAR8000 01MAR8000:00:00:00
31DEC8000 31DEC8000:00:00:00

31DEC9999 31DEC9999:00:00:00
;
run;
