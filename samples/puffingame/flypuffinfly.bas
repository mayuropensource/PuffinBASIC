10 ' Fly Puffin Fly - A horizontal scrolling game
20 GRIDY% = 16 : GRIDX% = 30 : TILEX% = 32 : TILEY% = 32 : W% = GRIDX%*TILEX% : H% = GRIDY%*TILEY%
30 DIM PLAYER1%(32, 32) ' 1
40 DIM PLAYER2%(32, 32) ' 2
50 DIM BG1%(32, 32)     ' 11 (sky)
60 DIM BG2%(32, 32)     ' 12 (building)
70 DIM ENEMY1%(32, 32)  ' 21
80 DIM REWARD1%(32, 32) ' 31
90 DIM GRIDBG%(16, 30)
100 DIM GRID%(16, 30)
110 DIM GRIDPREV%(16, 30)
120 playery%=5 : playerx%=2 : playerspr%=1 : playerPrevSpr% = -1
130 MAXENEMY%=10 : MAXREWARD%=10
140 GOSUB 1000 : GOSUB 1200 ' Load Images & Sounds
150 SCREEN "Fly Puffin Fly - A Scrolling Game Written in PuffinBASIC", W%, H%, MANUALREPAINT
160 hi% = 0
170 WHILE -1
180   maxenemy% = 2 : maxreward% = 2
190   speed% = 1 : speedstep% = 1 : nspeed% = 4
200   ARRAYFILL  GRIDBG%, 11
210   ARRAYFILL  GRID%, 0
220   ARRAYFILL  GRIDPREV%, 0
230   GOSUB 2500 ' Init Grid BG
240   GOSUB 3500 ' Init Enemy
250   GOSUB 4000 ' Init Reward
255   LOOPWAV SOUNDBG1%
260   run% = 1 : points% = 0 : sound% = 1
270   WHILE run% = 1
280     GOSUB 1500 ' Draw Grid BG
290     GOSUB 2000 ' Draw Player
300     GOSUB 4500 ' Draw FG
310     GOSUB 6000 ' Draw Points
320     GOSUB 5500 ' Check collision
330     IF collenemy% <> 0 THEN STOPWAV SOUNDBG1% : STOPWAV SOUNDBG2% : PLAYWAV SOUNDDEAD1% : hi% = points% : run% = 0 : GOSUB 10000
335     IF collreward% = 1 THEN PLAYWAV SOUNDEAT1%
340     REPAINT : SLEEP 30
350     GOSUB 2200 ' Erase Player
360     GOSUB 4800 ' Erase FG
370     playerspr% = (playerspr% + 1) MOD 4
380     k$ = INKEY$
390     IF k$ = CHR$(0) + CHR$(38) THEN playery% = MAX(0, playery% - 1)
400     IF k$ = CHR$(0) + CHR$(40) THEN playery% = MIN(GRIDY% - 1, playery% + 1)
410     speed% = speed% + speedstep%
420     IF speed% >= nspeed% THEN speed% = 0 : GOSUB 3000 : GOSUB 3100 : GOSUB 5000 : GOSUB 5100 ' Scroll & Insert New Enemy
430     points% = points% + 1 : ptsstep% = points% \ 1000
440     IF ptsstep% = 1 THEN speedstep% = 2 : maxenemy% = 3 : maxreward% = 3
450     IF ptsstep% = 2 THEN speedstep% = 3 : maxenemy% = 4 : maxreward% = 4
455     IF ptsstep% = 2 AND sound% = 1 THEN sound% = 2 : STOPWAV SOUNDBG1% : LOOPWAV SOUNDBG2%
460     IF ptsstep% = 3 THEN speedstep% = 4 : maxenemy% = 5 : maxreward% = 5
470     IF ptsstep% = 5 THEN maxenemy% = 6 : maxreward% = 6
480     IF ptsstep% = 6 THEN maxenemy% = 7 : maxreward% = 7
490     IF ptsstep% = 7 THEN maxenemy% = 8 : maxreward% = 8
491     IF ptsstep% = 8 THEN maxenemy% = 9 : maxreward% = 9
492     IF ptsstep% = 9 THEN maxenemy% = 10 : maxreward% = 10
500   WEND
510   SLEEP 5000 : CLS
520 WEND
990 END
1000 ' LOAD IMAGES
1010 LOADIMG "samples/puffingame/images/puffin1.png", PLAYER1%
1020 LOADIMG "samples/puffingame/images/puffin2.png", PLAYER2%
1030 LOADIMG "samples/puffingame/images/bg1.png", BG1%
1040 LOADIMG "samples/puffingame/images/bg2.png", BG2%
1050 LOADIMG "samples/puffingame/images/enemy1.png", ENEMY1%
1060 LOADIMG "samples/puffingame/images/reward1.png", REWARD1%
1070 RETURN
1200 ' LOAD SOUNDS
1210 LOADWAV "samples/puffingame/sounds/eat2.wav", SOUNDEAT1%
1220 LOADWAV "samples/puffingame/sounds/bg1.wav", SOUNDBG1%
1230 LOADWAV "samples/puffingame/sounds/bg2.wav", SOUNDBG2%
1240 LOADWAV "samples/puffingame/sounds/dead1.wav", SOUNDDEAD1%
1250 RETURN
1500 ' DRAW GRID BG
1510 FOR y% = 0 TO GRIDY% - 1
1520   FOR x% = 0 TO GRIDX% - 1
1530     v% = GRIDBG%(y%, x%) : xx% = x%*TILEX% : yy% = y%*TILEY%
1540     IF v% = 11 THEN PUT(xx%, yy%), BG1%, "PSET"
1550     IF v% = 12 THEN PUT(xx%, yy%), BG2%, "PSET"
1570   NEXT x%
1580 NEXT y%
1590 RETURN
2000 ' Draw Player
2020 x% = playerx% * TILEX% : y% = playery% * TILEY%
2030 playerPrevSpr% = GRIDBG%(playery%, playerx%)
2040 IF playerspr% \ 2 = 1 THEN PUT(x%, y%), PLAYER1%, "MIX" ELSE PUT(x%, y%), PLAYER2%, "MIX"
2050 RETURN
2200 ' Erase Player
2210 x% = playerx% * TILEX% : y% = playery% * TILEY%
2220 IF playerPrevSpr% = 11 THEN PUT(x%, y%), BG1%, "PSET"
2230 IF playerPrevSpr% = 12 THEN PUT(x%, y%), BG2%, "PSET"
2240 RETURN
2500 ' Init BG
2510 FOR x% = 0 TO GRIDX% - 1
2520   maxy% = cint(RND * 2)
2540   FOR y% = 0 TO maxy%
2550     yy% = GRIDY% - y% - 1
2560     GRIDBG%(yy%, x%) = 12
2570   NEXT y%
2580 NEXT x%
2590 RETURN
3000 ' Scroll
3010 ARRAY2DSHIFTHOR GRIDBG%, -1
3020 ARRAY2DSHIFTHOR GRIDPREV%, -1
3030 ARRAY2DSHIFTHOR GRID%, -1
3040 RETURN
3100 ' Add BG in rightmost column
3120 maxy% = cint(RND * 2) : xx% = GRIDX% - 1
3130 FOR y% = 0 TO GRIDY% - 1
3140   GRIDBG%(y%, xx%) = 11
3150 NEXT y%
3160 FOR y% = 0 TO maxy%
3170   yy% = GRIDY% - y% - 1
3180   GRIDBG%(yy%, xx%) = 12
3190 NEXT y%
3200 RETURN
3500 ' Init Enemy
3510 chance = maxenemy% / MAXENEMY%
3520 FOR x% = 10 TO GRIDX% - 1
3530   v = RND
3540   IF v < chance THEN y%=MAX(0, cint(RND * GRIDY%) - 1) : GRID%(y%, x%)=21
3550 NEXT x%
3560 RETURN
4000 ' Init Reward
4010 chance = maxreward% / MAXREWARD%
4020 FOR x% = 10 TO GRIDX% - 1
4030   v = RND : y%=MAX(0, cint(RND * GRIDY%) - 1) : g% = GRID%(y%, x%)
4040   IF v < chance AND g% = 0 THEN GRID%(y%, x%)=31
4050 NEXT x%
4060 RETURN
4200 ' Scroll FG
4210 ARRAY2DSHIFTHOR GRID%, -1
4220 RETURN
4500 ' Draw FG
4510 FOR y% = 0 TO GRIDY% - 1
4520   FOR x% = 0 TO GRIDX% - 1
4530     g% = GRIDBG%(y%, x%)
4540     v% = GRID%(y%, x%)
4550     GRIDPREV%(y%, x%) = g%
4560     IF v% = 21 THEN PUT(x%*TILEX%, y%*TILEY%), ENEMY1%, "MIX"
4570     IF v% = 31 THEN PUT(x%*TILEX%, y%*TILEY%), REWARD1%, "MIX"
4580   NEXT x%
4590 NEXT y%
4600 RETURN
4800 ' Erase FG
4810 FOR y% = 0 TO GRIDY% - 1
4820   FOR x% = 0 TO GRIDX% - 1
4830     g% = GRIDPREV%(y%, x%)
4840     IF g% = 11 THEN PUT(x%*TILEX%, y%*TILEY%), BG1%, "PSET"
4850     IF g% = 12 THEN PUT(x%*TILEX%, y%*TILEY%), BG2%, "PSET"
4860   NEXT x%
4870 NEXT y%
4880 RETURN
5000 ' Add new enemy
5010 chance = maxenemy% / MAXENEMY%
5020 x% = GRIDX% - 1
5030 v = RND
5040 IF v < chance THEN y%=MAX(0, cint(RND * GRIDY%) - 1) : GRID%(y%, x%)=21
5060 RETURN
5100 ' Add new reward
5110 chance = maxreward% / MAXREWARD%
5120 x% = GRIDX% - 1
5130 v = RND : y%=MAX(0, cint(RND * GRIDY%) - 1) : g% = GRID%(y%, x%)
5140 IF v < chance AND g% = 0 THEN GRID%(y%, x%)=31
5160 RETURN
5500 ' Check collision
5510 g% = GRID%(playery%, playerx%)
5520 collenemy% = 0 : collreward% = 0
5530 IF g% = 21 THEN collenemy% = 1
5540 IF g% = 31 THEN collreward% = 1 : GRID%(playery%, playerx%) = 0 : points% = points% + 50
5550 RETURN
6000 ' Show Points
6010 pointsstr$ = SPACE$(64)
6020 LSET pointsstr$ = STR$(points%) + "  " + STR$(MAX(hi%, points%))
6030 FONT "Courier", "B", 16
6040 COLOR 255, 255, 255
6050 DRAWSTR pointsstr$, W% - 150, 20
6090 RETURN
10000 ' GAME OVER
10010 gover$ = "Oops! GAME OVER! Your Score=" + str$(points%) + " hi=" + STR$(hi%) + ", try again" : PRINT gover$
10020 FONT "Courier", "B", 24
10030 COLOR 255, 0, 0
10040 DRAWSTR gover$, 10, H% / 2
10050 RETURN
