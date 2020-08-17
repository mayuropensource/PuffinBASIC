10 A% = -10
20 B% = A%
30 PRINT "A=" A%, "B=" B%
40 DIM C%(2, 3)
50 C%(1,2) = 20
60 C%(0,1) = 30
70 print C%(1,2), C%(0,1)
80 DEF FNsqr(X) = sqr(X)
90 print FNsqr(10), FNsqr(2)
100 I% = 1
110 WHILE I% <= 3
120 PRINT I%
130 I% = I% + 1
140 WEND
150 FOR J% = 1 TO 3
160 FOR K% = 1 TO 4
170 PRINT J%, "x", K%, "=", J%*K%
180 NEXT K%, J%
190 X# = 5.0
200 IF X# < 3 THEN PRINT "LESS" ELSE PRINT "MORE"
210 print sin(X#)
220 print int(2.3)
230 print fix(2.3)
240 print TIMER
250 print "FIRST " + "SECOND"
260 print using "!"; "FOO"; "BAR"
270 print using "&"; "FOO"; "BAR"
280 print using "\    \"; "FOO"; "BAR"
290 print using "###.##"; 20; 1.5
300 OPEN "/home/mayur/a.txt" FOR RANDOM AS #1 LEN=24
310 FIELD #1, 4 AS F1$, 4 AS F2$, 8 AS F3$, 8 AS F4$
320 FOR L% = 1 to 5
330 F1$ = "123" + STR$(L%)
340 F2$ = "567" + STR$(L%)
350 F3$ = "abc" + STR$(L%)
360 F4$ = "efg" + STR$(L%)
370 PUT #1
380 NEXT L%
390 FOR L% = 1 to 5
400 GET #1
410 PRINT F1$, F2$, F3$, F4$
420 NEXT L%
430 print loc(1)
440 print lof(1)
450 CLOSE #1
460 DEFINT A-B, R-T
470 XX1$ = space$(20)
480 RSET XX1$ = "XYZ"
490 PRINT XX1$
500 WRITE "aaa, bb", 1.2, 20
510 print DATE$, " ", TIME$
520 DATE$ = "2020-08-15"
530 TIME$ = "10:20"
540 print DATE$, " ", TIME$
550 MM$ = "KANSAS CITY, MO, USA"
560 MID$(MM$, 14) = "KS"
570 PRINT MM$
580 READ R1%, R2#, R3@, R4$
590 RESTORE
600 READ R1%, R2#, R3@, R4$
610 PRINT R1%, R2#, R3@, R4$
620 DATA 1, 2.3, 6@, "aa, bb"
