options bufsize=32768 pagesize=10000;

data dev.date_format_hhmm_loop(drop=i v);
    format HHMM HHMM.
        HHMM2 HHMM2. HHMM3 HHMM3. HHMM4 HHMM4. HHMM5 HHMM5. HHMM6 HHMM6. HHMM7 HHMM7. HHMM8 HHMM8. HHMM9 HHMM9.
        HHMM2_1 HHMM2.1 HHMM3_1 HHMM3.1 HHMM4_1 HHMM4.1 HHMM5_1 HHMM5.1 HHMM6_1 HHMM6.1 HHMM7_1 HHMM7.1 HHMM8_1 HHMM8.1 HHMM9_1 HHMM9.1
        HHMM3_2 HHMM3.2 HHMM4_2 HHMM4.2 HHMM5_2 HHMM5.2 HHMM6_2 HHMM6.2 HHMM7_2 HHMM7.2 HHMM8_2 HHMM8.2 HHMM9_2 HHMM9.2
        HHMM4_3 HHMM4.3 HHMM5_3 HHMM5.3 HHMM6_3 HHMM6.3 HHMM7_3 HHMM7.3 HHMM8_3 HHMM8.3 HHMM9_3 HHMM9.3
    ;

    do i=-1111 to 1111;
        v=i*10.001;
        HHMM=v;
        HHMM2=v; HHMM2_1=v;
        HHMM3=v; HHMM3_1=v; HHMM3_2=v;
        HHMM4=v; HHMM4_1=v; HHMM4_2=v; HHMM4_3=v;
        HHMM5=v; HHMM5_1=v; HHMM5_2=v; HHMM5_3=v;
        HHMM6=v; HHMM6_1=v; HHMM6_2=v; HHMM6_3=v;
        HHMM7=v; HHMM7_1=v; HHMM7_2=v; HHMM7_3=v;
        HHMM8=v; HHMM8_1=v; HHMM8_2=v; HHMM8_3=v;
        HHMM9=v; HHMM9_1=v; HHMM9_2=v; HHMM9_3=v;
        output;
    end;

    do i=-1111 to 1111;
        v=i*100.01;
        HHMM=v;
        HHMM2=v; HHMM2_1=v;
        HHMM3=v; HHMM3_1=v; HHMM3_2=v;
        HHMM4=v; HHMM4_1=v; HHMM4_2=v; HHMM4_3=v;
        HHMM5=v; HHMM5_1=v; HHMM5_2=v; HHMM5_3=v;
        HHMM6=v; HHMM6_1=v; HHMM6_2=v; HHMM6_3=v;
        HHMM7=v; HHMM7_1=v; HHMM7_2=v; HHMM7_3=v;
        HHMM8=v; HHMM8_1=v; HHMM8_2=v; HHMM8_3=v;
        HHMM9=v; HHMM9_1=v; HHMM9_2=v; HHMM9_3=v;
        output;
    end;
run;
