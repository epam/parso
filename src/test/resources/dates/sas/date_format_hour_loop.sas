options bufsize=32768 pagesize=10000;

data dev.date_format_hour_loop(drop=i v);
    format HOUR HOUR.
        HOUR2 HOUR2. HOUR3 HOUR3. HOUR4 HOUR4. HOUR5 HOUR5. HOUR6 HOUR6. HOUR7 HOUR7. HOUR8 HOUR8.
        HOUR2_1 HOUR2.1 HOUR3_1 HOUR3.1 HOUR4_1 HOUR4.1 HOUR5_1 HOUR5.1 HOUR6_1 HOUR6.1 HOUR7_1 HOUR7.1 HOUR8_1 HOUR8.1
        HOUR3_2 HOUR3.2 HOUR4_2 HOUR4.2 HOUR5_2 HOUR5.2 HOUR6_2 HOUR6.2 HOUR7_2 HOUR7.2 HOUR8_2 HOUR8.2
        HOUR4_3 HOUR4.3 HOUR5_3 HOUR5.3 HOUR6_3 HOUR6.3 HOUR7_3 HOUR7.3 HOUR8_3 HOUR8.3
        HOUR5_4 HOUR5.4 HOUR6_4 HOUR6.4 HOUR7_4 HOUR7.4 HOUR8_4 HOUR8.4
    ;

    do i=-1111 to 1111;
        v=i*10.001;
        HOUR=v;
        HOUR2=v; HOUR2_1=v;
        HOUR3=v; HOUR3_1=v; HOUR3_2=v;
        HOUR4=v; HOUR4_1=v; HOUR4_2=v; HOUR4_3=v;
        HOUR5=v; HOUR5_1=v; HOUR5_2=v; HOUR5_3=v; HOUR5_4=v;
        HOUR6=v; HOUR6_1=v; HOUR6_2=v; HOUR6_3=v; HOUR6_4=v;
        HOUR7=v; HOUR7_1=v; HOUR7_2=v; HOUR7_3=v; HOUR7_4=v;
        HOUR8=v; HOUR8_1=v; HOUR8_2=v; HOUR8_3=v; HOUR8_4=v;
        output;
    end;

    do i=-1111 to 1111;
        v=i*100.01;
        HOUR=v;
        HOUR2=v; HOUR2_1=v;
        HOUR3=v; HOUR3_1=v; HOUR3_2=v;
        HOUR4=v; HOUR4_1=v; HOUR4_2=v; HOUR4_3=v;
        HOUR5=v; HOUR5_1=v; HOUR5_2=v; HOUR5_3=v; HOUR5_4=v;
        HOUR6=v; HOUR6_1=v; HOUR6_2=v; HOUR6_3=v; HOUR6_4=v;
        HOUR7=v; HOUR7_1=v; HOUR7_2=v; HOUR7_3=v; HOUR7_4=v;
        HOUR8=v; HOUR8_1=v; HOUR8_2=v; HOUR8_3=v; HOUR8_4=v;
        output;
    end;
run;
