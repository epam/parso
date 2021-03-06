options bufsize=32768 pagesize=10000;

data dev.date_format_time(drop=v);
    format TIME TIME.
        TIME2 TIME2. TIME3 TIME3. TIME4 TIME4. TIME5 TIME5. TIME6 TIME6. TIME7 TIME7. TIME8 TIME8. TIME9 TIME9.
        TIME10 TIME10. TIME11 TIME11. TIME12 TIME12.
        TIME2_1 TIME2.1 TIME3_1 TIME3.1 TIME4_1 TIME4.1 TIME5_1 TIME5.1 TIME6_1 TIME6.1 TIME7_1 TIME7.1 TIME8_1 TIME8.1 TIME9_1 TIME9.1
        TIME10_1 TIME10.1 TIME11_1 TIME11.1 TIME12_1 TIME12.1
        TIME3_2 TIME3.2 TIME4_2 TIME4.2 TIME5_2 TIME5.2 TIME6_2 TIME6.2 TIME7_2 TIME7.2 TIME8_2 TIME8.2 TIME9_2 TIME9.2
        TIME10_2 TIME10.2 TIME11_2 TIME11.2 TIME12_2 TIME12.2
        TIME4_3 TIME4.3 TIME5_3 TIME5.3 TIME6_3 TIME6.3 TIME7_3 TIME7.3 TIME8_3 TIME8.3 TIME9_3 TIME9.3
        TIME10_3 TIME10.3 TIME11_3 TIME11.3 TIME12_3 TIME12.3
    ;

    input v TIME15.4;
    TIME=v;
    TIME2=v; TIME2_1=v;
    TIME3=v; TIME3_1=v; TIME3_2=v;
    TIME4=v; TIME4_1=v; TIME4_2=v; TIME4_3=v;
    TIME5=v; TIME5_1=v; TIME5_2=v; TIME5_3=v;
    TIME6=v; TIME6_1=v; TIME6_2=v; TIME6_3=v;
    TIME7=v; TIME7_1=v; TIME7_2=v; TIME7_3=v;
    TIME8=v; TIME8_1=v; TIME8_2=v; TIME8_3=v;
    TIME9=v; TIME9_1=v; TIME9_2=v; TIME9_3=v;
    TIME10=v; TIME10_1=v; TIME10_2=v; TIME10_3=v;
    TIME11=v; TIME11_1=v; TIME11_2=v; TIME11_3=v;
    TIME12=v; TIME12_1=v; TIME12_2=v; TIME12_3=v;

    datalines;
19:53:01.321

0:00:00.000
0:00:00.001
0:00:00.05
0:00:01.02
0:00:59.50
0:01:01.3
0:25:00.45
0:29:35
0:31:15
0:41:40.45
0:59:59.9321
0:59:59.9875
0:59:59.9987
0:59:59.9999
1:23:45.6789
2:05:00.45
2:21:40.45
9:29:59
9:31:01
9:59:59.9321
9:59:59.9875
9:59:59.9987
9:59:59.9999
10:00:03.60
10:08:23.65
10:41:43.85
12:21:44.45
12:21:44.45
19:18:26.05
19:18:26.15
19:18:26.25
19:18:26.35
19:18:26.45
19:18:26.55
19:18:26.65
19:18:26.75
19:18:26.85
19:18:26.95
19:18:27.05
19:18:27.15
19:18:27.25
19:18:27.35
19:18:27.45
19:18:27.55
19:18:27.65
19:18:27.75
19:18:27.85
19:18:27.95
19:54:32.1
99:48:00.000
99:59:20.400
99:59:59.9321
99:59:59.9875
99:59:59.9987
99:59:59.9999
999:59:59.9321
999:59:59.9875
999:59:59.9987
999:59:59.9999
9876:54:32.1

-0:00:00.001
-0:00:00.05
-0:00:01.02
-0:00:59.50
-0:01:01.3
-0:29:35
-0:31:15
-0:41:40.45
-0:59:59.9321
-0:59:59.9875
-0:59:59.9987
-0:59:59.9999
-1:23:45.6789
-9:29:59
-9:31:01
-9:59:59.9321
-9:59:59.9875
-9:59:59.9987
-9:59:59.9999
-19:18:27.65
-19:18:27.75
-19:18:27.85
-19:18:27.95
-19:54:32.1
-99:59:20.400
-99:59:59.9321
-99:59:59.9875
-99:59:59.9987
-99:59:59.9999
-999:59:59.9321
;
run;
