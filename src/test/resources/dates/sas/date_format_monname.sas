options bufsize=8192 pagesize=100;

data dev.date_format_monname(drop=v);
    format MONNAME MONNAME. MONNAME1 MONNAME1. MONNAME2 MONNAME2. MONNAME3 MONNAME3. MONNAME4 MONNAME4. MONNAME5 MONNAME5.;
    format MONNAME6 MONNAME6. MONNAME7 MONNAME7. MONNAME8 MONNAME8. MONNAME9 MONNAME9. MONNAME10 MONNAME10. MONNAME11 MONNAME11.;

    input v DATE9.;
    MONNAME=v; MONNAME1=v; MONNAME2=v; MONNAME3=v; MONNAME4=v; MONNAME5=v;
    MONNAME6=v; MONNAME7=v; MONNAME8=v; MONNAME9=v; MONNAME10=v; MONNAME11=v;
    datalines;
01JAN1582
31DEC1582
31DEC1959
01JAN1960
31DEC1969
01JAN1970
01JAN9999
31DEC9999

30NOV2019
25OCT2019
22SEP2019
19AUG2019
15JUL2019
13JUN2019
10MAY2019
09APR2019
02MAR2019
01FEB2019
;
run;
