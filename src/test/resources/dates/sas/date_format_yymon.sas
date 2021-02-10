options bufsize=8192 pagesize=100;

data dev.date_format_yymon(drop=v);
    format YYMON YYMON. YYMON5 YYMON5. YYMON6 YYMON6. YYMON7 YYMON7.;

    input v DATE9.;
    YYMON=v; YYMON5=v; YYMON6=v; YYMON7=v;
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
