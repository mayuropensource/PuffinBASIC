10 SCREEN "PuffinBASIC 2D Graphics", 800, 600
20 CIRCLE (100, 200), 50, 50
30 COLOR 0, 255, 0
40 CIRCLE (200, 200), 50, 50, 0, 3.14159
50 COLOR 255, 255, 255
60 LINE (0, 0) - (100, 100)
70 LINE (0, 0) - (50, 50), "B"
80 COLOR 255, 0, 0
90 LINE (0, 0) - (10, 10), "BF"
100 COLOR 255, 255, 255
110 LINE (150, 150) - (170, 170), "B"
120 COLOR 0, 0, 255
130 PAINT (155, 155), 255, 255, 255
140 PSET (400, 300), 255, 255, 255
150 DIM A%(20, 20)
160 GET (0, 0) - (20, 20), A%
290 x% = 500 : y% = 400 : x2% = x% : y2% = y% : PUT (x%, y%), A%
300 WHILE -1
310     K$ = INKEY$
320     IF K$ = "0" + CHR$(38) THEN y2% = y% - 1
330     IF K$ = "0" + CHR$(37) THEN x2% = x% - 1
340     IF K$ = "0" + CHR$(39) THEN x2% = x% + 1
350     IF K$ = "0" + CHR$(40) THEN y2% = y% + 1
360     IF x% <> x2% or y% <> y2% THEN PUT (x%, y%), A% : PUT (x2%, y2%), A%
370     x% = x2% : y% = y2%
380     SLEEP 20
400 WEND
1000 SLEEP 5000
