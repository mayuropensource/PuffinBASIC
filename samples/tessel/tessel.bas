10 ' TESSEL - A Tile Builder Game - Still has bugs - TODO FIX
20 DIM GRID%(24, 16) : GRIDW% = 16 : GRIDH% = 24 : DIM ROWS%(24)
30 DIM TILE0%(32, 32)
40 DIM TILE1%(32, 32)
50 DIM TILE2%(32, 32)
60 DIM TILE3%(32, 32)
70 DIM TILE4%(32, 32)
80 DIM TILE5%(32, 32)
90 DIM TILE6%(32, 32)
100 DIM TILE7%(32, 32)
110 DIM tile%(4, 4)
500 GOSUB 1000 ' LOAD IMAGES
510 GOSUB 2000 ' CREATE SCREEN
520 GOSUB 3000 ' INIT TILES
530 WHILE -1
540     PRINT "Starting New Game"
550     GOSUB 1500 ' INIT GRID
560     CLS
600     GOSUB 5000 ' GAME LOOP
610     PRINT "Waiting for a few seconds before starting new game ..."
620     SLEEP 5000
630 WEND
990 END
1000 ' LOAD IMAGES
1010 LOADIMG "samples/tessel/images/tile0.png", TILE0%
1020 LOADIMG "samples/tessel/images/tile1.png", TILE1%
1030 LOADIMG "samples/tessel/images/tile2.png", TILE2%
1040 LOADIMG "samples/tessel/images/tile3.png", TILE3%
1050 LOADIMG "samples/tessel/images/tile4.png", TILE4%
1060 LOADIMG "samples/tessel/images/tile5.png", TILE5%
1070 LOADIMG "samples/tessel/images/tile6.png", TILE6%
1080 LOADIMG "samples/tessel/images/tile7.png", TILE7%
1190 TILEW% = 32 : TILEH% = 32
1200 PRINT "Loaded images"
1210 RETURN
1500 ' CREATE GRID
1510 FOR I% = 0 TO GRIDH% - 1
1520    FOR J% = 0 TO GRIDW% - 1
1530        GRID%(I%, J%) = 0
1540    NEXT : NEXT
1550 PRINT "Initialized grid"
1560 RETURN
2000 ' CREATE SCREEN
2010 OFFSETX% = 0
2020 OFFSETY% = 2
2030 SCRSIZEX% = GRIDW% * TILEW% + OFFSETX% * GRIDW%
2040 SCRSIZEY% = GRIDH% * TILEH% + OFFSETY% * GRIDH%
2050 SCREEN "TESSEL - A Tile Builder Game in PuffinBASIC", SCRSIZEX%, SCRSIZEY%, MANUALREPAINT
2060 PRINT "Created Screen: ", SCRSIZEX%, SCRSIZEY%
2070 RETURN
2500 ' DRAW GRID
2510 FOR Y% = 0 TO GRIDH% - 1
2520    FOR X% = 0 TO GRIDW% - 1
2530        t% = GRID%(Y%, X%)
2540        x% = X% * TILEW% : y% = Y% * TILEH% + OFFSETY% * GRIDH%
2550        IF t% = 1 THEN PUT (x%, y%), TILE0%, "PSET"
2620    NEXT
2630 NEXT
2640 COLOR 20, 25, 25
2650 LINE (0, 0) - (GRIDW% * TILEW% - 1, OFFSETY% * GRIDH% - 1), "BF"
2660 score$ = SPACE$(256)
2670 LSET score$ = "Rotate: U arrow, Move: L/R/D arrow, SCORE: " + str$(points%)
2680 FONT "Courier", "", 16
2690 COLOR 220, 255, 255
2700 DRAWSTR score$, 10, 32
2990 RETURN
3000 ' tile1
3010 DIM T11%(4, 4) : T11%(0, 2) = 1 : T11%(1, 0) = 1 : T11%(1, 1) = 1 : T11%(1, 2) = 1
3020 DIM T12%(4, 4) : T12%(0, 0) = 1 : T12%(1, 0) = 1 : T12%(2, 0) = 1 : T12%(2, 1) = 1
3030 DIM T13%(4, 4) : T13%(0, 0) = 1 : T13%(0, 1) = 1 : T13%(0, 2) = 1 : T13%(1, 0) = 1
3040 DIM T14%(4, 4) : T14%(0, 0) = 1 : T14%(0, 1) = 1 : T14%(1, 1) = 1 : T14%(2, 1) = 1
3050 ' tile2
3060 DIM T21%(4, 4) : T21%(0, 0) = 1 : T21%(0, 1) = 1 : T21%(0, 2) = 1 : T21%(0, 3) = 1
3070 DIM T22%(4, 4) : T22%(0, 0) = 1 : T22%(1, 0) = 1 : T22%(2, 0) = 1 : T22%(3, 0) = 1
3080 DIM T23%(4, 4) : T23%(0, 0) = 1 : T23%(0, 1) = 1 : T23%(0, 2) = 1 : T23%(0, 3) = 1
3090 DIM T24%(4, 4) : T24%(0, 0) = 1 : T24%(1, 0) = 1 : T24%(2, 0) = 1 : T24%(3, 0) = 1
3100 ' tile3
3110 DIM T31%(4, 4) : T31%(0, 0) = 1 : T31%(1, 0) = 1 : T31%(1, 1) = 1 : T31%(1, 2) = 1
3120 DIM T32%(4, 4) : T32%(0, 0) = 1 : T32%(0, 1) = 1 : T32%(1, 0) = 1 : T32%(2, 0) = 1
3130 DIM T33%(4, 4) : T33%(0, 0) = 1 : T33%(0, 1) = 1 : T33%(0, 2) = 1 : T33%(1, 2) = 1
3140 DIM T34%(4, 4) : T34%(0, 1) = 1 : T34%(1, 1) = 1 : T34%(2, 0) = 1 : T34%(2, 1) = 1
3150 ' tile4
3160 DIM T41%(4, 4) : T41%(0, 0) = 1 : T41%(0, 1) = 1 : T41%(1, 0) = 1 : T41%(1, 1) = 1
3170 DIM T42%(4, 4) : T42%(0, 0) = 1 : T42%(0, 1) = 1 : T42%(1, 0) = 1 : T42%(1, 1) = 1
3180 DIM T43%(4, 4) : T43%(0, 0) = 1 : T43%(0, 1) = 1 : T43%(1, 0) = 1 : T43%(1, 1) = 1
3190 DIM T44%(4, 4) : T44%(0, 0) = 1 : T44%(0, 1) = 1 : T44%(1, 0) = 1 : T44%(1, 1) = 1
3200 ' tile5
3210 DIM T51%(4, 4) : T51%(0, 1) = 1 : T51%(1, 0) = 1 : T51%(1, 1) = 1 : T51%(1, 2) = 1
3220 DIM T52%(4, 4) : T52%(0, 0) = 1 : T52%(1, 0) = 1 : T52%(1, 1) = 1 : T52%(2, 0) = 1
3230 DIM T53%(4, 4) : T53%(0, 0) = 1 : T53%(0, 1) = 1 : T53%(0, 2) = 1 : T53%(1, 1) = 1
3240 DIM T54%(4, 4) : T54%(0, 1) = 1 : T54%(1, 0) = 1 : T54%(1, 1) = 1 : T54%(2, 1) = 1
3250 ' tile6
3260 DIM T61%(4, 4) : T61%(0, 1) = 1 : T61%(1, 0) = 1 : T61%(1, 1) = 1 : T61%(2, 0) = 1
3270 DIM T62%(4, 4) : T62%(0, 0) = 1 : T62%(0, 1) = 1 : T62%(1, 1) = 1 : T62%(1, 2) = 1
3280 DIM T63%(4, 4) : T63%(0, 1) = 1 : T63%(1, 0) = 1 : T63%(1, 1) = 1 : T63%(2, 0) = 1
3290 DIM T64%(4, 4) : T64%(0, 0) = 1 : T64%(0, 1) = 1 : T64%(1, 1) = 1 : T64%(1, 2) = 1
3300 ' tile7
3310 DIM T71%(4, 4) : T71%(0, 0) = 1 : T71%(0, 1) = 1 : T71%(1, 0) = 1
3320 DIM T72%(4, 4) : T72%(0, 0) = 1 : T72%(0, 1) = 1 : T72%(1, 1) = 1
3330 DIM T73%(4, 4) : T73%(0, 1) = 1 : T73%(1, 0) = 1 : T73%(1, 1) = 1
3340 DIM T74%(4, 4) : T74%(0, 0) = 1 : T74%(1, 0) = 1 : T74%(1, 1) = 1
4990 RETURN
5000 ' GAME LOOP
5010 run% = -1 : points% = 0 : drawGrid% = 1' Draw Grid
5020 tileid% = 0 : tilex% = 0 : tiley% = 0 : rot% = 0 : dStep% = 0 : nSteps% = 4 : sStep% = 1
5030 WHILE run%
5040    IF tileid% = 0 THEN tileid% = 1 + int(RND * 7) : tiley% = 0 : tilex% = int(RND * (GRIDW% - 4)) : rot% = 0
5050    collision% = 0 : drot% = 0
5060    GOSUB 6000 ' Set Tile
5070    IF drawGrid% <> 0 THEN drawGrid% = 0 : GOSUB 2500 ' Draw Grid
5080    GOSUB 7000 ' Draw Tile
5090    k$ = INKEY$
5100    dx% = 0 : dy% = 0 : drot% = 0
5110    IF k$ = CHR$(0) + CHR$(37) THEN dx% = -1
5120    IF k$ = CHR$(0) + CHR$(39) THEN dx% = 1
5130    IF k$ = CHR$(0) + CHR$(38) THEN drot% = 1
5140    IF k$ = CHR$(0) + CHR$(40) THEN dy% = 1
5150    ' Check for collision
5160    IF dStep% = nSteps% - 1 THEN dy% = 1 : dStep% = 0 ELSE dStep% = dStep% + sStep%
5170    GOSUB 8000 ' Check collision and hitbottom
5180    IF collision% <> 0 AND tiley% = 0 THEN run% = 0 : GOSUB 10000
5181    oldrot% = rot% : newrot% = rot%
5185    IF drot% = 1 AND collision% <> 2 THEN newrot% = (rot% + 1) MOD 4 ' Rotate if no collision
5190    IF (hitbottom% = -1 OR collision% <> 0) AND tiley% > 0 THEN tileid% = 0 : dy% = 0 : GOSUB 9000 ' Copy Tile to Grid
5191    IF hitbottom% = 0 AND collision% = 0 AND drot% = 1 THEN checkrot% = 1 ELSE checkrot% = 0 ' Check if rotation causes collision
5192    IF checkrot% = 1 THEN collision% = 0 : rot% = newrot% : GOSUB 6000 : GOSUB 8000 ' Copy rotated tile and check for collision
5193    IF checkrot% = 1 THEN rot% = oldrot% : GOSUB 6000 ' Revert rotated tile
5194    IF checkrot% = 1 AND collision% <> 0 THEN rot% = oldrot% ELSE IF checkrot% = 1 THEN rot% = newrot%
5200    REPAINT : SLEEP 40
5210    GOSUB 7000 ' Erase Tile and Update tile x,y
5220    tilex% = tilex% + dx%
5230    tiley% = tiley% + dy%
5250 WEND
5260 RETURN
6000 ' Copy tile
6020 FOR I% = 0 TO 3
6030    FOR J% = 0 TO 3
6040        tile%(I%, J%) = 0
6050        IF tileid% = 1 AND rot% = 0 THEN tile%(I%, J%) = T11%(I%, J%)
6060        IF tileid% = 1 AND rot% = 1 THEN tile%(I%, J%) = T12%(I%, J%)
6070        IF tileid% = 1 AND rot% = 2 THEN tile%(I%, J%) = T13%(I%, J%)
6080        IF tileid% = 1 AND rot% = 3 THEN tile%(I%, J%) = T14%(I%, J%)
6090        IF tileid% = 2 AND rot% = 0 THEN tile%(I%, J%) = T21%(I%, J%)
6100        IF tileid% = 2 AND rot% = 1 THEN tile%(I%, J%) = T22%(I%, J%)
6110        IF tileid% = 2 AND rot% = 2 THEN tile%(I%, J%) = T23%(I%, J%)
6120        IF tileid% = 2 AND rot% = 3 THEN tile%(I%, J%) = T24%(I%, J%)
6130        IF tileid% = 3 AND rot% = 0 THEN tile%(I%, J%) = T31%(I%, J%)
6140        IF tileid% = 3 AND rot% = 1 THEN tile%(I%, J%) = T32%(I%, J%)
6150        IF tileid% = 3 AND rot% = 2 THEN tile%(I%, J%) = T33%(I%, J%)
6160        IF tileid% = 3 AND rot% = 3 THEN tile%(I%, J%) = T34%(I%, J%)
6170        IF tileid% = 4 AND rot% = 0 THEN tile%(I%, J%) = T41%(I%, J%)
6180        IF tileid% = 4 AND rot% = 1 THEN tile%(I%, J%) = T42%(I%, J%)
6190        IF tileid% = 4 AND rot% = 2 THEN tile%(I%, J%) = T43%(I%, J%)
6200        IF tileid% = 4 AND rot% = 3 THEN tile%(I%, J%) = T44%(I%, J%)
6210        IF tileid% = 5 AND rot% = 0 THEN tile%(I%, J%) = T51%(I%, J%)
6220        IF tileid% = 5 AND rot% = 1 THEN tile%(I%, J%) = T52%(I%, J%)
6230        IF tileid% = 5 AND rot% = 2 THEN tile%(I%, J%) = T53%(I%, J%)
6240        IF tileid% = 5 AND rot% = 3 THEN tile%(I%, J%) = T54%(I%, J%)
6250        IF tileid% = 6 AND rot% = 0 THEN tile%(I%, J%) = T61%(I%, J%)
6260        IF tileid% = 6 AND rot% = 1 THEN tile%(I%, J%) = T62%(I%, J%)
6270        IF tileid% = 6 AND rot% = 2 THEN tile%(I%, J%) = T63%(I%, J%)
6280        IF tileid% = 6 AND rot% = 3 THEN tile%(I%, J%) = T64%(I%, J%)
6290        IF tileid% = 7 AND rot% = 0 THEN tile%(I%, J%) = T71%(I%, J%)
6300        IF tileid% = 7 AND rot% = 1 THEN tile%(I%, J%) = T72%(I%, J%)
6310        IF tileid% = 7 AND rot% = 2 THEN tile%(I%, J%) = T73%(I%, J%)
6320        IF tileid% = 7 AND rot% = 3 THEN tile%(I%, J%) = T74%(I%, J%)
6330    NEXT
6340 NEXT
6350 RETURN
7000 ' Draw tile
7010 FOR Y% = 0 TO 3
7020    FOR X% = 0 TO 3
7030        v% = tile%(Y%, X%)
7040        y% = (tiley% + Y%) * TILEH% + OFFSETY% * GRIDH%
7050        x% = (tilex% + X%) * TILEW%
7060        IF v% = 1 AND tileid% = 1 THEN PUT (x%, y%), TILE1%, "XOR"
7070        IF v% = 1 AND tileid% = 2 THEN PUT (x%, y%), TILE2%, "XOR"
7080        IF v% = 1 AND tileid% = 3 THEN PUT (x%, y%), TILE3%, "XOR"
7090        IF v% = 1 AND tileid% = 4 THEN PUT (x%, y%), TILE4%, "XOR"
7100        IF v% = 1 AND tileid% = 5 THEN PUT (x%, y%), TILE5%, "XOR"
7110        IF v% = 1 AND tileid% = 6 THEN PUT (x%, y%), TILE6%, "XOR"
7120        IF v% = 1 AND tileid% = 7 THEN PUT (x%, y%), TILE7%, "XOR"
7130    NEXT
7140 NEXT
7150 RETURN
8000 ' Collision Check
8010 maxx% = 0 : maxy% = 0 : collision% = 0
8020 FOR Y% = 0 TO 3
8030    FOR X% = 0 TO 3
8040        v% = tile%(Y%, X%)
8050        IF v% = 1 AND X% > maxx% THEN maxx% = X%
8060        IF v% = 1 AND Y% > maxy% THEN maxy% = Y%
8070    NEXT
8080 NEXT
8090 IF tilex% + dx% < 0 THEN dx% = 0
8100 IF tilex% + dx% + maxx% >= GRIDW% THEN dx% = 0
8110 IF tiley% + maxy% + 1 >= GRIDH% THEN hitbottom% = -1 ELSE hitbottom% = 0
8120 FOR Y% = 0 TO maxy%
8130    FOR X% = 0 TO maxx%
8140        v% = tile%(Y%, X%)
8150        hgv% = GRID%(tiley% + Y%, tilex% + X%)
8160        gvdx% = GRID%(tiley% + Y%, tilex% + X% + dx%)
8170        IF hitbottom% = 0 THEN vgv% = GRID%(tiley% + Y% + 1, tilex% + X%) ELSE vgv% = 0
8180        IF v% = 1 AND vgv% = 1 THEN collision% = 1
8185        IF v% = 1 AND hgv% = 1 THEN collision% = 2
8190        IF v% = 1 AND gvdx% = 1 THEN dx% = 0
8200    NEXT
8210 NEXT
8220 RETURN
9000 ' Copy Tile to Grid
9005 drawGrid% = 1
9010 FOR Y% = 0 TO maxy%
9020    FOR X% = 0 TO maxx%
9030        v% = tile%(Y%, X%)
9040        gv% = GRID%(tiley% + Y%, tilex% + X%)
9050        IF v% = 1 AND gv% = 0 THEN GRID%(tiley% + Y%, tilex% + X%) = 1 : points% = points% + 1
9060    NEXT
9070 NEXT
9080 FOR Y% = 0 TO GRIDH% - 1
9090    ROWS%(Y%) = 0
9100 NEXT
9110 minfilledy% = GRIDH%
9120 FOR Y% = GRIDH% - 1 TO 0 STEP -1
9130    filled% = 1 : X% = 0
9140    WHILE X% < GRIDW%
9150        v% = GRID%(Y%, X%)
9160        IF v% = 0 THEN filled% = 0 : X% = GRIDW%
9170        X% = X% + 1
9180    WEND
9190    IF filled% = 1 THEN ROWS%(Y%) = 1 : points% = points% + GRIDW% * 2
9200    IF filled% = 1 AND Y% < minfilledy% THEN minfilledy% = Y%
9210 NEXT
9230 DESTY% = GRIDH% - 1 : SRCY1% = DESTY% - 1 : SRCY2% = 0
9240 WHILE DESTY% >= minfilledy%
9250    r% = ROWS%(DESTY%)
9260    WHILE SRCY1% > 0 AND ROWS%(SRCY1%) = 1
9270        SRCY1% = SRCY1% - 1
9280    WEND
9290    SRCY2% = SRCY1% - 1
9300    WHILE SRCY2% > 0 AND ROWS%(SRCY2%) = 0
9310        SRCY2% = SRCY2% - 1
9320    WEND
9330    ' SHIFT
9340    DY% = DESTY% - SRCY1%
9350    FOR I% = SRCY1% to SRCY2% + 1 STEP -1
9360        FOR J% = 0 TO GRIDW% - 1
9370            GRID%(I% + DY%, J%) = GRID%(I%, J%)
9380            GRID%(I%, J%) = 0
9390        NEXT
9400    NEXT
9410    DESTY% = SRCY2%
9420 WEND
9430 FOR I% = 0 TO SRCY2% - 1
9440    FOR J% = 0 TO GRIDW% - 1
9450        GRID%(I%, J%) = 0
9460    NEXT
9470 NEXT
9480 IF minfilledy% < GRIDH% THEN CLS
9990 RETURN
10000 ' GAME OVER
10010 gover$ = "GAME OVER! Your Score=" + str$(points%) + ", try again" : PRINT gover$
10020 FONT "Courier", "B", 24
10030 COLOR 255, 0, 0
10040 DRAWSTR gover$, 10, GRIDH% * TILEH% / 2
10050 RETURN
