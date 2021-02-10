options bufsize=2048 pagesize=100;

data dev.date_format_qtrr;
    format QTRR QTRR.;
    QTRR = '25OCT2019'd;
    output;
run;
