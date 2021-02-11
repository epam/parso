options bufsize=32768 pagesize=10000;

data dev.date_format_mmss_loop(drop=i v);
    format MMSS MMSS.
        MMSS2 MMSS2. MMSS3 MMSS3. MMSS4 MMSS4. MMSS5 MMSS5. MMSS6 MMSS6. MMSS7 MMSS7. MMSS8 MMSS8. MMSS9 MMSS9.
        MMSS2_1 MMSS2.1 MMSS3_1 MMSS3.1 MMSS4_1 MMSS4.1 MMSS5_1 MMSS5.1 MMSS6_1 MMSS6.1 MMSS7_1 MMSS7.1 MMSS8_1 MMSS8.1 MMSS9_1 MMSS9.1
        MMSS3_2 MMSS3.2 MMSS4_2 MMSS4.2 MMSS5_2 MMSS5.2 MMSS6_2 MMSS6.2 MMSS7_2 MMSS7.2 MMSS8_2 MMSS8.2 MMSS9_2 MMSS9.2
        MMSS4_3 MMSS4.3 MMSS5_3 MMSS5.3 MMSS6_3 MMSS6.3 MMSS7_3 MMSS7.3 MMSS8_3 MMSS8.3 MMSS9_3 MMSS9.3
    ;

    do i=-1111 to 1111;
        v=i*10.001;
        MMSS=v;
        MMSS2=v; MMSS2_1=v;
        MMSS3=v; MMSS3_1=v; MMSS3_2=v;
        MMSS4=v; MMSS4_1=v; MMSS4_2=v; MMSS4_3=v;
        MMSS5=v; MMSS5_1=v; MMSS5_2=v; MMSS5_3=v;
        MMSS6=v; MMSS6_1=v; MMSS6_2=v; MMSS6_3=v;
        MMSS7=v; MMSS7_1=v; MMSS7_2=v; MMSS7_3=v;
        MMSS8=v; MMSS8_1=v; MMSS8_2=v; MMSS8_3=v;
        MMSS9=v; MMSS9_1=v; MMSS9_2=v; MMSS9_3=v;
        output;
    end;

    do i=-1111 to 1111;
        v=i*100.01;
        MMSS=v;
        MMSS2=v; MMSS2_1=v;
        MMSS3=v; MMSS3_1=v; MMSS3_2=v;
        MMSS4=v; MMSS4_1=v; MMSS4_2=v; MMSS4_3=v;
        MMSS5=v; MMSS5_1=v; MMSS5_2=v; MMSS5_3=v;
        MMSS6=v; MMSS6_1=v; MMSS6_2=v; MMSS6_3=v;
        MMSS7=v; MMSS7_1=v; MMSS7_2=v; MMSS7_3=v;
        MMSS8=v; MMSS8_1=v; MMSS8_2=v; MMSS8_3=v;
        MMSS9=v; MMSS9_1=v; MMSS9_2=v; MMSS9_3=v;
        output;
    end;
run;
