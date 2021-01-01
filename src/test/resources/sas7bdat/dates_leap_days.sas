/*
  SAS program to generate sas7bdat file with two types of columns: date and datetime.
  Both columns contain data around leap days.
  Years 4000 and 8000 don't have leap days in terms of SAS.
  Years 2000 and 6000 have it.
  All of them necessary for unit tests.
*/

options bufsize=4096 pagesize=15;

data dev.dates_leap_days(label='Leap days dataset');
  format d date9.;
  format dt datetime20.;

  d='28FEB2000'd;
  dt='28FEB2000:00:00:00'dt;
  output;
  d='29FEB2000'd;
  dt='29FEB2000:00:00:00'dt;
  output;
  d='01MAR2000'd;
  dt='01MAR2000:00:00:00'dt;
  output;
  d='31DEC2000'd;
  dt='31DEC2000:00:00:00'dt;
  output;
  d='28FEB4000'd;
  dt='28FEB4000:00:00:00'dt;
  output;
  d='01MAR4000'd;
  dt='01MAR4000:00:00:00'dt;
  output;
  d='31DEC4000'd;
  dt='31DEC4000:00:00:00'dt;
  output;
  d='28FEB6000'd;
  dt='28FEB6000:00:00:00'dt;
  output;
  d='29FEB6000'd;
  dt='29FEB6000:00:00:00'dt;
  output;
  d='01MAR6000'd;
  dt='01MAR6000:00:00:00'dt;
  output;
  d='31DEC6000'd;
  dt='31DEC6000:00:00:00'dt;
  output;
  d='28FEB8000'd;
  dt='28FEB8000:00:00:00'dt;
  output;
  d='01MAR8000'd;
  dt='01MAR8000:00:00:00'dt;
  output;
  d='31DEC8000'd;
  dt='31DEC8000:00:00:00'dt;
  output;
run;
