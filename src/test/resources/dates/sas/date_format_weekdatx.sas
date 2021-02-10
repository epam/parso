options bufsize=8192 pagesize=100;

data dev.date_format_weekdatx(drop=v);
    format WEEKDATX WEEKDATX.
      WEEKDATX3 WEEKDATX3. WEEKDATX4 WEEKDATX4. WEEKDATX5 WEEKDATX5. WEEKDATX6 WEEKDATX6.
      WEEKDATX7 WEEKDATX7. WEEKDATX8 WEEKDATX8. WEEKDATX9 WEEKDATX9.
      WEEKDATX10 WEEKDATX10. WEEKDATX11 WEEKDATX11. WEEKDATX12 WEEKDATX12. WEEKDATX13 WEEKDATX13.
      WEEKDATX14 WEEKDATX14. WEEKDATX15 WEEKDATX15. WEEKDATX16 WEEKDATX16. WEEKDATX17 WEEKDATX17.
      WEEKDATX18 WEEKDATX18. WEEKDATX19 WEEKDATX19.
      WEEKDATX20 WEEKDATX20. WEEKDATX21 WEEKDATX21. WEEKDATX22 WEEKDATX22. WEEKDATX23 WEEKDATX23.
      WEEKDATX24 WEEKDATX24. WEEKDATX25 WEEKDATX25. WEEKDATX26 WEEKDATX26. WEEKDATX27 WEEKDATX27.
      WEEKDATX28 WEEKDATX28. WEEKDATX29 WEEKDATX29.
      WEEKDATX30 WEEKDATX30. WEEKDATX31 WEEKDATX31. WEEKDATX32 WEEKDATX32. WEEKDATX33 WEEKDATX33.;

    input v DATE9.;
    WEEKDATX=v;
    WEEKDATX3=v; WEEKDATX4=v; WEEKDATX5=v; WEEKDATX6=v; WEEKDATX7=v; WEEKDATX8=v; WEEKDATX9=v;
    WEEKDATX10=v; WEEKDATX11=v; WEEKDATX12=v; WEEKDATX13=v; WEEKDATX14=v; WEEKDATX15=v; WEEKDATX16=v;
    WEEKDATX17=v; WEEKDATX18=v; WEEKDATX19=v; WEEKDATX20=v;
    WEEKDATX20=v; WEEKDATX21=v; WEEKDATX22=v; WEEKDATX23=v; WEEKDATX24=v; WEEKDATX25=v; WEEKDATX26=v;
    WEEKDATX27=v; WEEKDATX28=v; WEEKDATX29=v; WEEKDATX30=v; WEEKDATX31=v; WEEKDATX32=v; WEEKDATX33=v;
    datalines;
31DEC1959
01JAN1960
31DEC1969
01JAN1970
28FEB2000
29FEB2000
01MAR2000
31DEC2000

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
