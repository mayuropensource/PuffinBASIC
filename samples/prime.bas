10 FOR I% = 1 TO 100
20   J% = 3
30   N% = I% \ 2
40   ISPRIME% = (I% > 1) AND ((I% MOD 2 <> 0) OR (I% = 2))
50   WHILE J% <= N% AND ISPRIME% = -1
60     ISPRIME% = I% MOD J% <> 0
70     J% = J% + 2
80   WEND
90   IF ISPRIME% THEN PRINT STR$(I%), " is prime"
100 NEXT I%
