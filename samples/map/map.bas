FUNCTION drawBg%(DIM bgmap%(0, 0), scrw%, scrh%, tilew%, tileh%, xoffset%, LIST<DIM %> bgSprites, numMapEntities%, DIM BgEntityNumSprites%(0), DIM BgEntitySpriteItr%(0), DIM BgEntitySprites%(0, 0), incrBgAnimation%) {
    FOR r% = 0 TO scrh% - 1
        FOR c% = 0 TO scrw%
            cell% = bgmap%(r%, c%)
            x% = c% * tilew%
            y% = r% * tileh%
            n% = BgEntityNumSprites%(cell%)
            i% = BgEntitySpriteItr%(cell%)
            spriteIdx% = BgEntitySprites%(cell%, i%)
            AUTO sprite = bgSprites.get(spriteIdx%)
            PUT(x% - xoffset%, y%), sprite, "PSET"
        NEXT c%
    NEXT r%

    FOR entity% = 0 TO numMapEntities% - 1
        n% = BgEntityNumSprites%(entity%)
        i% = BgEntitySpriteItr%(entity%)
        BgEntitySpriteItr%(entity%) = (i% + incrBgAnimation%) MOD n%
    NEXT entity%

    RETURN 0
}

FUNCTION createScreen(w%, h%, iw%, ih%) {
    PRINT "Create Screen: ", w%, h%, iw%, ih%
    SCREEN "Map - Written in PuffinBASIC", w%, h%, iw%, ih%, MANUALREPAINT, DOUBLEBUFFER
    RETURN 0
}

FUNCTION loadAndAddSprite(root$, path$, DIM sprite%(0, 0), LIST<DIM %> result) {
    fullPath$ = root$ + path$
    PRINT "Reading sprite: " + fullPath$
    LOADIMG fullPath$, sprite%
    result.append(sprite%)
    RETURN 0
}

FUNCTION clipSpeedX%(DIM gamemap%(0, 0), x%, y%, bbx1%, bby1%, bbx2%, bby2%, speedX%) {
    IF speedX% > 0 THEN BEGIN
        rightX1% = x% + bbx2% + 1
        rightX2% = rightX1% + speedX%
        rightCollisionCol% = ARRAY2DFINDCOLUMN(gamemap%, rightX1%, y% + bby1%, rightX2%, y% + bby2%, 1)
        IF rightCollisionCol% = -1 THEN clippedX% = 0 ELSE clippedX% = rightX2% - rightCollisionCol%
        speedX% = speedX% - clippedX%
    END IF
    IF speedX% < 0 AND x% > 0 THEN BEGIN
        leftX1% = MAX(0, x% + bbx1% - 1)
        leftX2% = MAX(0, leftX1% + speedX%)
        leftCollisionCol% = ARRAY2DFINDCOLUMN(gamemap%, leftX1%, y% + bby1%, leftX2%, y% + bby2%, 1)
        IF leftCollisionCol% = -1 THEN clippedX% = 0 ELSE clippedX% = leftX2% - leftCollisionCol%
        speedX% = speedX% - clippedX%
    END IF
    RETURN speedX%
}

FUNCTION isKilledByVerticalWall%(DIM gamemap%(0, 0), x%, y%, bbx1%, bby1%, bbx2%, bby2%, speedX%) {
    collisionCol% = -1
    IF speedX% > 0 THEN BEGIN
        rightX1% = x% + bbx2% + 1
        rightX2% = rightX1% + speedX%
        collisionCol% = ARRAY2DFINDCOLUMN(gamemap%, rightX1%, y% + bby1%, rightX2%, y% + bby2%, 2)
    END IF
    IF speedX% < 0 AND x% > 0 THEN BEGIN
        leftX1% = MAX(0, x% + bbx1% - 1)
        leftX2% = MAX(0, leftX1% + speedX%)
        collisionCol% = ARRAY2DFINDCOLUMN(gamemap%, leftX1%, y% + bby1%, leftX2%, y% + bby2%, 2)
    END IF
    RETURN collisionCol% > -1
}

FUNCTION clipSpeedY%(DIM gamemap%(0, 0), x%, y%, bbx1%, bby1%, bbx2%, bby2%, speedY%) {
    IF speedY% > 1 THEN BEGIN
        bottomY1% = y% + bby2% + 1
        bottomY2% = bottomY1% + speedY%
        floorCollisionRow% = ARRAY2DFINDROW(gamemap%, x% + bbx1%, bottomY1%, x% + bbx2%, bottomY2%, 1)
        IF floorCollisionRow% = -1 THEN clippedY% = 0 ELSE clippedY% = bottomY2% - floorCollisionRow%
        speedY% = speedY% - clippedY%
    ELSE BEGIN
        topY1% = MAX(0, y% + bby1% - 1)
        topY2% = MAX(0, topY1% + speedY%)
        ceilCollisionRow% = ARRAY2DFINDROW(gamemap%, x% + bbx1%, topY1%, x% + bbx2%, topY2%, 1)
        IF ceilCollisionRow% = -1 THEN clippedY% = 0 ELSE clippedY% = topY2% - ceilCollisionRow%
        speedY% = speedY% - clippedY%
    END IF
    RETURN speedY%
}

FUNCTION isKilledByHorizontalWall%(DIM gamemap%(0, 0), x%, y%, bbx1%, bby1%, bbx2%, bby2%, speedY%) {
    collisionRow% = -1
    IF speedY% > 1 THEN BEGIN
        bottomY1% = y% + bby2% + 1
        bottomY2% = bottomY1% + speedY%
        collisionRow% = ARRAY2DFINDROW(gamemap%, x% + bbx1%, bottomY1%, x% + bbx2%, bottomY2%, 2)
    ELSE BEGIN
        topY1% = MAX(0, y% + bby1% - 1)
        topY2% = MAX(0, topY1% + speedY%)
        collisionRow% = ARRAY2DFINDROW(gamemap%, x% + bbx1%, topY1%, x% + bbx2%, topY2%, 2)
    END IF
    RETURN collisionRow% > -1
}

MAPROOT$ = "samples/map/maps/"
IMAGEROOT$ = "samples/map/images/"
SOUNDROOT$ = "samples/map/sounds/"

' Background map
BGTILEH% = 64 : BGTILEW% = 64
BGNROWS% = 12 : BGNCOLS% = 80
DIM BGMAP%(BGNROWS%, BGNCOLS%)

' Game map
SPLIT% = 8
GAMEH% = BGTILEH% \ SPLIT% : GAMEW% = BGTILEW% \ SPLIT%
GAMENROWS% = BGNROWS% * SPLIT% : GAMENCOLS% = BGNCOLS% * SPLIT%
DIM GAMEMAP%(GAMENROWS%, GAMENCOLS%)

LOADWAV SOUNDROOT$ + "level1_bg1.wav", SOUNDLEVEL1BG1%
LOADWAV SOUNDROOT$ + "bullet1.wav", SOUNDBULLET1%
LOADWAV SOUNDROOT$ + "land1.wav", SOUNDLAND1%

' Background Sprites

DIM BGTILE0%(BGTILEW%, BGTILEH%)
DIM BGTILE1%(BGTILEW%, BGTILEH%)
DIM BGTILE2%(BGTILEW%, BGTILEH%)
DIM BGTILE3%(BGTILEW%, BGTILEH%)
DIM BGTILE4%(BGTILEW%, BGTILEH%)
DIM BGTILE5%(BGTILEW%, BGTILEH%)
DIM BGTILE6%(BGTILEW%, BGTILEH%)
DIM BGTILE7%(BGTILEW%, BGTILEH%)
DIM BGTILE8%(BGTILEW%, BGTILEH%)
DIM BGTILE9%(BGTILEW%, BGTILEH%)
DIM BGTILE10%(BGTILEW%, BGTILEH%)
DIM BGTILE11%(BGTILEW%, BGTILEH%)
DIM BGTILE12%(BGTILEW%, BGTILEH%)
DIM BGTILE13%(BGTILEW%, BGTILEH%)
DIM BGTILE14%(BGTILEW%, BGTILEH%)
DIM BGTILE15%(BGTILEW%, BGTILEH%)
DIM BGTILE16%(BGTILEW%, BGTILEH%)

' Background entities
numMapEntities% = 0
MaxBgEntities% = 64
BgEntityMaxNumSprites% = 4
BgEntityKindEmpty% = 0
BgEntityKindBlock% = 1
BgEntityKindEnemy% = 2
DIM BgEntityNumSprites%(MaxBgEntities%)
DIM BgEntitySpriteItr%(MaxBgEntities%)
DIM BgEntityKind%(MaxBgEntities%)
DIM BgEntitySprites%(MaxBgEntities%, BgEntityMaxNumSprites%)

' Lava Fall
LavaFallStateDormant% = 0
LavaFallStateDead% = 1

MaxLavaFall% = 32
startLavaFallIdx% = 0
numLavaFall% = 0
DIM LavaFallX%(MaxLavaFall%)
DIM LavaFallY%(MaxLavaFall%)
DIM LavaFallShootPosX%(MaxLavaFall%)
DIM LavaFallShootPosY%(MaxLavaFall%)
DIM LavaFallSpeedY%(MaxLavaFall%)
DIM LavaFallState%(MaxLavaFall%)
DIM LavaFallTimer@(MaxLavaFall%)
DIM LavaFallInterval%(MaxLavaFall%)
DIM LavaFallSprite%(MaxLavaFall%)

' Enemy bullet config
EnemyBulletStateEmpty% = 0
EnemyBulletStateFire% = 1
EnemyBulletStateMove% = 2

NumEnemyBullets% = 8
DIM EnemyBulletX%(NumEnemyBullets%)
DIM EnemyBulletY%(NumEnemyBullets%)
DIM EnemyBulletSpeedX%(NumEnemyBullets%)
DIM EnemyBulletSpeedY%(NumEnemyBullets%)
DIM EnemyBulletState%(NumEnemyBullets%)
DIM EnemyBulletEntity%(NumEnemyBullets%)
DIM EnemyBulletSprite%(NumEnemyBullets%)

LIST<DIM %> bgSprites
mapFile$ = MAPROOT$ + "game_level1.map"
GOSUB "LoadMaps"

' Player Sprites

PLAYERW% = 128 : PLAYERH% = 128

DIM PLAYERSTANDR1%(PLAYERH%, PLAYERW%)   ' 0
DIM PLAYERSTANDRUP1%(PLAYERH%, PLAYERW%) ' 1
DIM PLAYERRUNR1%(PLAYERH%, PLAYERW%)     ' 2
DIM PLAYERRUNR2%(PLAYERH%, PLAYERW%)     ' 3
DIM PLAYERRUNR3%(PLAYERH%, PLAYERW%)     ' 4
DIM PLAYERRUNR4%(PLAYERH%, PLAYERW%)     ' 5
DIM PLAYERRUNR5%(PLAYERH%, PLAYERW%)     ' 6
DIM PLAYERRUNR6%(PLAYERH%, PLAYERW%)     ' 7
DIM PLAYERJUMPR1%(PLAYERH%, PLAYERW%)    ' 8
DIM PLAYERJUMPR2%(PLAYERH%, PLAYERW%)    ' 9
DIM PLAYERSITR1%(PLAYERH%, PLAYERW%)     ' 10

DIM PLAYERSTANDL1%(PLAYERH%, PLAYERW%)   ' 11
DIM PLAYERSTANDLUP1%(PLAYERH%, PLAYERW%) ' 12
DIM PLAYERRUNL1%(PLAYERH%, PLAYERW%)     ' 13
DIM PLAYERRUNL2%(PLAYERH%, PLAYERW%)     ' 14
DIM PLAYERRUNL3%(PLAYERH%, PLAYERW%)     ' 15
DIM PLAYERRUNL4%(PLAYERH%, PLAYERW%)     ' 16
DIM PLAYERRUNL5%(PLAYERH%, PLAYERW%)     ' 17
DIM PLAYERRUNL6%(PLAYERH%, PLAYERW%)     ' 18
DIM PLAYERJUMPL1%(PLAYERH%, PLAYERW%)    ' 19
DIM PLAYERJUMPL2%(PLAYERH%, PLAYERW%)    ' 20
DIM PLAYERSITL1%(PLAYERH%, PLAYERW%)     ' 21

PLAYERNUMSPRITES% = 22

LIST<DIM %> playerSprites
loadAndAddSprite(IMAGEROOT$, "player/STAND_R_1.png", PLAYERSTANDR1%, playerSprites)
loadAndAddSprite(IMAGEROOT$, "player/STAND_R_UP_1.png", PLAYERSTANDRUP1%, playerSprites)
loadAndAddSprite(IMAGEROOT$, "player/RUN_R_1.png", PLAYERRUNR1%, playerSprites)
loadAndAddSprite(IMAGEROOT$, "player/RUN_R_2.png", PLAYERRUNR2%, playerSprites)
loadAndAddSprite(IMAGEROOT$, "player/RUN_R_3.png", PLAYERRUNR3%, playerSprites)
loadAndAddSprite(IMAGEROOT$, "player/RUN_R_4.png", PLAYERRUNR4%, playerSprites)
loadAndAddSprite(IMAGEROOT$, "player/RUN_R_5.png", PLAYERRUNR5%, playerSprites)
loadAndAddSprite(IMAGEROOT$, "player/RUN_R_6.png", PLAYERRUNR6%, playerSprites)
loadAndAddSprite(IMAGEROOT$, "player/JUMP_R_1.png", PLAYERJUMPR1%, playerSprites)
loadAndAddSprite(IMAGEROOT$, "player/JUMP_R_2.png", PLAYERJUMPR2%, playerSprites)
loadAndAddSprite(IMAGEROOT$, "player/SIT_R_1.png", PLAYERSITR1%, playerSprites)

loadAndAddSprite(IMAGEROOT$, "player/STAND_L_1.png", PLAYERSTANDL1%, playerSprites)
loadAndAddSprite(IMAGEROOT$, "player/STAND_L_UP_1.png", PLAYERSTANDLUP1%, playerSprites)
loadAndAddSprite(IMAGEROOT$, "player/RUN_L_1.png", PLAYERRUNL1%, playerSprites)
loadAndAddSprite(IMAGEROOT$, "player/RUN_L_2.png", PLAYERRUNL2%, playerSprites)
loadAndAddSprite(IMAGEROOT$, "player/RUN_L_3.png", PLAYERRUNL3%, playerSprites)
loadAndAddSprite(IMAGEROOT$, "player/RUN_L_4.png", PLAYERRUNL4%, playerSprites)
loadAndAddSprite(IMAGEROOT$, "player/RUN_L_5.png", PLAYERRUNL5%, playerSprites)
loadAndAddSprite(IMAGEROOT$, "player/RUN_L_6.png", PLAYERRUNL6%, playerSprites)
loadAndAddSprite(IMAGEROOT$, "player/JUMP_L_1.png", PLAYERJUMPL1%, playerSprites)
loadAndAddSprite(IMAGEROOT$, "player/JUMP_L_2.png", PLAYERJUMPL2%, playerSprites)
loadAndAddSprite(IMAGEROOT$, "player/SIT_L_1.png", PLAYERSITL1%, playerSprites)

' Load bullet sprites

LIST<DIM %> bulletSprites
LIST<DIM %> enemyBulletSprites
BULLETW% = 16 : BULLETH% = 16
DIM BULLET1%(BULLETH%, BULLETW%)         ' 0
DIM LavaFallEnemyBullet1%(BULLETH%, BULLETW%)  ' 0
loadAndAddSprite(IMAGEROOT$, "Bullet_1.png", BULLET1%, bulletSprites)
loadAndAddSprite(IMAGEROOT$, "LavaFallBullet1.png", LavaFallEnemyBullet1%, enemyBulletSprites)

' Player Config

StateStand% = 0
StateSit% = 1
StateRun% = 2
StateJump% = 3
StateFall% = 4
StateDead% = 5
StateNum% = 6

MoveDirRight% = 1
MoveDirLeft% = -1

MoveDirNumUR% = 0
MoveDirNumURR% = 1
MoveDirNumR% = 2
MoveDirNumDRR% = 3
MoveDirNumDR% = 4
MoveDirNumDL% = 5
MoveDirNumDLL% = 6
MoveDirNumL% = 7
MoveDirNumULL% = 8
MoveDirNumUL% = 9
MoveDirNum% = 10

MAXNUMFRAME% = 7
DIM AFRAME%(StateNum%, MoveDirNum%, MAXNUMFRAME%)

AFRAME%(StateStand%, MoveDirNumUR%, 0) = 1  : AFRAME%(StateStand%, MoveDirNumUR%, 1) = 1
AFRAME%(StateStand%, MoveDirNumURR%, 0) = 1 : AFRAME%(StateStand%, MoveDirNumURR%, 1) = 0
AFRAME%(StateStand%, MoveDirNumR%, 0) = 1   : AFRAME%(StateStand%, MoveDirNumR%, 1) = 0
AFRAME%(StateStand%, MoveDirNumDRR%, 0) = 1 : AFRAME%(StateStand%, MoveDirNumDRR%, 1) = 0
AFRAME%(StateStand%, MoveDirNumDR%, 0) = 1  : AFRAME%(StateStand%, MoveDirNumDR%, 1) = 0
AFRAME%(StateStand%, MoveDirNumDL%, 0) = 1  : AFRAME%(StateStand%, MoveDirNumDL%, 1) = 11
AFRAME%(StateStand%, MoveDirNumDLL%, 0) = 1 : AFRAME%(StateStand%, MoveDirNumDLL%, 1) = 11
AFRAME%(StateStand%, MoveDirNumL%, 0) = 1   : AFRAME%(StateStand%, MoveDirNumL%, 1) = 11
AFRAME%(StateStand%, MoveDirNumULL%, 0) = 1 : AFRAME%(StateStand%, MoveDirNumULL%, 1) = 11
AFRAME%(StateStand%, MoveDirNumUL%, 0) = 1  : AFRAME%(StateStand%, MoveDirNumUL%, 1) = 12

AFRAME%(StateSit%, MoveDirNumUR%, 0) = 1  : AFRAME%(StateSit%, MoveDirNumUR%, 1) = 10
AFRAME%(StateSit%, MoveDirNumURR%, 0) = 1 : AFRAME%(StateSit%, MoveDirNumURR%, 1) = 10
AFRAME%(StateSit%, MoveDirNumR%, 0) = 1   : AFRAME%(StateSit%, MoveDirNumR%, 1) = 10
AFRAME%(StateSit%, MoveDirNumDRR%, 0) = 1 : AFRAME%(StateSit%, MoveDirNumDRR%, 1) = 10
AFRAME%(StateSit%, MoveDirNumDR%, 0) = 1  : AFRAME%(StateSit%, MoveDirNumDR%, 1) = 10
AFRAME%(StateSit%, MoveDirNumDL%, 0) = 1  : AFRAME%(StateSit%, MoveDirNumDL%, 1) = 21
AFRAME%(StateSit%, MoveDirNumDLL%, 0) = 1 : AFRAME%(StateSit%, MoveDirNumDLL%, 1) = 21
AFRAME%(StateSit%, MoveDirNumL%, 0) = 1   : AFRAME%(StateSit%, MoveDirNumL%, 1) = 21
AFRAME%(StateSit%, MoveDirNumULL%, 0) = 1 : AFRAME%(StateSit%, MoveDirNumULL%, 1) = 21
AFRAME%(StateSit%, MoveDirNumUL%, 0) = 1  : AFRAME%(StateSit%, MoveDirNumUL%, 1) = 21

AFRAME%(StateRun%, MoveDirNumUR%, 0) = 6
    AFRAME%(StateRun%, MoveDirNumUR%, 1) = 2 : AFRAME%(StateRun%, MoveDirNumUR%, 2) = 3
    AFRAME%(StateRun%, MoveDirNumUR%, 3) = 4 : AFRAME%(StateRun%, MoveDirNumUR%, 4) = 5
    AFRAME%(StateRun%, MoveDirNumUR%, 5) = 6 : AFRAME%(StateRun%, MoveDirNumUR%, 6) = 7
AFRAME%(StateRun%, MoveDirNumURR%, 0) = 6
    AFRAME%(StateRun%, MoveDirNumURR%, 1) = 2 : AFRAME%(StateRun%, MoveDirNumURR%, 2) = 3
    AFRAME%(StateRun%, MoveDirNumURR%, 3) = 4 : AFRAME%(StateRun%, MoveDirNumURR%, 4) = 5
    AFRAME%(StateRun%, MoveDirNumURR%, 5) = 6 : AFRAME%(StateRun%, MoveDirNumURR%, 6) = 7
AFRAME%(StateRun%, MoveDirNumR%, 0) = 6
    AFRAME%(StateRun%, MoveDirNumR%, 1) = 2 : AFRAME%(StateRun%, MoveDirNumR%, 2) = 3
    AFRAME%(StateRun%, MoveDirNumR%, 3) = 4 : AFRAME%(StateRun%, MoveDirNumR%, 4) = 5
    AFRAME%(StateRun%, MoveDirNumR%, 5) = 6 : AFRAME%(StateRun%, MoveDirNumR%, 6) = 7
AFRAME%(StateRun%, MoveDirNumDRR%, 0) = 6
    AFRAME%(StateRun%, MoveDirNumDRR%, 1) = 2 : AFRAME%(StateRun%, MoveDirNumDRR%, 2) = 3
    AFRAME%(StateRun%, MoveDirNumDRR%, 3) = 4 : AFRAME%(StateRun%, MoveDirNumDRR%, 4) = 5
    AFRAME%(StateRun%, MoveDirNumDRR%, 5) = 6 : AFRAME%(StateRun%, MoveDirNumDRR%, 6) = 6
AFRAME%(StateRun%, MoveDirNumDR%, 0) = 6
    AFRAME%(StateRun%, MoveDirNumDR%, 1) = 2 : AFRAME%(StateRun%, MoveDirNumDR%, 2) = 3
    AFRAME%(StateRun%, MoveDirNumDR%, 3) = 4 : AFRAME%(StateRun%, MoveDirNumDR%, 4) = 5
    AFRAME%(StateRun%, MoveDirNumDR%, 5) = 6 : AFRAME%(StateRun%, MoveDirNumDR%, 6) = 7
AFRAME%(StateRun%, MoveDirNumDL%, 0) = 6
    AFRAME%(StateRun%, MoveDirNumDL%, 1) = 13 : AFRAME%(StateRun%, MoveDirNumDL%, 2) = 14
    AFRAME%(StateRun%, MoveDirNumDL%, 3) = 15 : AFRAME%(StateRun%, MoveDirNumDL%, 4) = 16
    AFRAME%(StateRun%, MoveDirNumDL%, 5) = 17 : AFRAME%(StateRun%, MoveDirNumDL%, 6) = 18
AFRAME%(StateRun%, MoveDirNumDLL%, 0) = 6
    AFRAME%(StateRun%, MoveDirNumDLL%, 1) = 13 : AFRAME%(StateRun%, MoveDirNumDLL%, 2) = 14
    AFRAME%(StateRun%, MoveDirNumDLL%, 3) = 15 : AFRAME%(StateRun%, MoveDirNumDLL%, 4) = 16
    AFRAME%(StateRun%, MoveDirNumDLL%, 5) = 17 : AFRAME%(StateRun%, MoveDirNumDLL%, 6) = 18
AFRAME%(StateRun%, MoveDirNumL%, 0) = 6
    AFRAME%(StateRun%, MoveDirNumL%, 1) = 13 : AFRAME%(StateRun%, MoveDirNumL%, 2) = 14
    AFRAME%(StateRun%, MoveDirNumL%, 3) = 15 : AFRAME%(StateRun%, MoveDirNumL%, 4) = 16
    AFRAME%(StateRun%, MoveDirNumL%, 5) = 17 : AFRAME%(StateRun%, MoveDirNumL%, 6) = 18
AFRAME%(StateRun%, MoveDirNumULL%, 0) = 6
    AFRAME%(StateRun%, MoveDirNumULL%, 1) = 13 : AFRAME%(StateRun%, MoveDirNumULL%, 2) = 14
    AFRAME%(StateRun%, MoveDirNumULL%, 3) = 15 : AFRAME%(StateRun%, MoveDirNumULL%, 4) = 16
    AFRAME%(StateRun%, MoveDirNumULL%, 5) = 17 : AFRAME%(StateRun%, MoveDirNumULL%, 6) = 18
AFRAME%(StateRun%, MoveDirNumUL%, 0) = 6
    AFRAME%(StateRun%, MoveDirNumUL%, 1) = 13 : AFRAME%(StateRun%, MoveDirNumUL%, 2) = 14
    AFRAME%(StateRun%, MoveDirNumUL%, 3) = 15 : AFRAME%(StateRun%, MoveDirNumUL%, 4) = 16
    AFRAME%(StateRun%, MoveDirNumUL%, 5) = 17 : AFRAME%(StateRun%, MoveDirNumUL%, 6) = 18

AFRAME%(StateJump%, MoveDirNumUR%, 0) = 2  : AFRAME%(StateJump%, MoveDirNumUR%, 1) = 8   : AFRAME%(StateJump%, MoveDirNumUR%, 2) = 9
AFRAME%(StateJump%, MoveDirNumURR%, 0) = 2 : AFRAME%(StateJump%, MoveDirNumURR%, 1) = 8  : AFRAME%(StateJump%, MoveDirNumURR%, 2) = 9
AFRAME%(StateJump%, MoveDirNumR%, 0) = 2   : AFRAME%(StateJump%, MoveDirNumR%, 1) = 8    : AFRAME%(StateJump%, MoveDirNumR%, 2) = 9
AFRAME%(StateJump%, MoveDirNumDRR%, 0) = 2 : AFRAME%(StateJump%, MoveDirNumDRR%, 1) = 8  : AFRAME%(StateJump%, MoveDirNumDRR%, 2) = 9
AFRAME%(StateJump%, MoveDirNumDR%, 0) = 2  : AFRAME%(StateJump%, MoveDirNumDR%, 1) = 8   : AFRAME%(StateJump%, MoveDirNumDR%, 2) = 9
AFRAME%(StateJump%, MoveDirNumDL%, 0) = 2  : AFRAME%(StateJump%, MoveDirNumDL%, 1) = 19  : AFRAME%(StateJump%, MoveDirNumDL%, 2) = 20
AFRAME%(StateJump%, MoveDirNumDLL%, 0) = 2 : AFRAME%(StateJump%, MoveDirNumDLL%, 1) = 19 : AFRAME%(StateJump%, MoveDirNumDLL%, 2) = 20
AFRAME%(StateJump%, MoveDirNumL%, 0) = 2   : AFRAME%(StateJump%, MoveDirNumL%, 1) = 19   : AFRAME%(StateJump%, MoveDirNumL%, 2) = 20
AFRAME%(StateJump%, MoveDirNumULL%, 0) = 2 : AFRAME%(StateJump%, MoveDirNumULL%, 1) = 19 : AFRAME%(StateJump%, MoveDirNumULL%, 2) = 20
AFRAME%(StateJump%, MoveDirNumUL%, 0) = 2  : AFRAME%(StateJump%, MoveDirNumUL%, 1) = 19  : AFRAME%(StateJump%, MoveDirNumUL%, 2) = 20

AFRAME%(StateFall%, MoveDirNumUR%, 0) = 1  : AFRAME%(StateFall%, MoveDirNumUR%, 1) = 7
AFRAME%(StateFall%, MoveDirNumURR%, 0) = 1 : AFRAME%(StateFall%, MoveDirNumURR%, 1) = 7
AFRAME%(StateFall%, MoveDirNumR%, 0) = 1   : AFRAME%(StateFall%, MoveDirNumR%, 1) = 7
AFRAME%(StateFall%, MoveDirNumDRR%, 0) = 1 : AFRAME%(StateFall%, MoveDirNumDRR%, 1) = 7
AFRAME%(StateFall%, MoveDirNumDR%, 0) = 1  : AFRAME%(StateFall%, MoveDirNumDR%, 1) = 7
AFRAME%(StateFall%, MoveDirNumDL%, 0) = 1  : AFRAME%(StateFall%, MoveDirNumDL%, 1) = 18
AFRAME%(StateFall%, MoveDirNumDLL%, 0) = 1 : AFRAME%(StateFall%, MoveDirNumDLL%, 1) = 18
AFRAME%(StateFall%, MoveDirNumL%, 0) = 1   : AFRAME%(StateFall%, MoveDirNumL%, 1) = 18
AFRAME%(StateFall%, MoveDirNumULL%, 0) = 1 : AFRAME%(StateFall%, MoveDirNumULL%, 1) = 18
AFRAME%(StateFall%, MoveDirNumUL%, 0) = 1  : AFRAME%(StateFall%, MoveDirNumUL%, 1) = 18

' Player Bounding box
DIM PLAYERBB%(PLAYERNUMSPRITES%, 4)
PLAYERBB%(0,0)=4 : PLAYERBB%(0,1)=0 : PLAYERBB%(0,2)=11 : PLAYERBB%(0,3)=15
PLAYERBB%(1,0)=4 : PLAYERBB%(1,1)=0 : PLAYERBB%(1,2)=11 : PLAYERBB%(1,3)=15
PLAYERBB%(2,0)=4 : PLAYERBB%(2,1)=0 : PLAYERBB%(2,2)=11 : PLAYERBB%(2,3)=15
PLAYERBB%(3,0)=4 : PLAYERBB%(3,1)=0 : PLAYERBB%(3,2)=11 : PLAYERBB%(3,3)=15
PLAYERBB%(4,0)=4 : PLAYERBB%(4,1)=0 : PLAYERBB%(4,2)=11 : PLAYERBB%(4,3)=15
PLAYERBB%(5,0)=4 : PLAYERBB%(5,1)=0 : PLAYERBB%(5,2)=11 : PLAYERBB%(5,3)=15
PLAYERBB%(6,0)=4 : PLAYERBB%(6,1)=0 : PLAYERBB%(6,2)=11 : PLAYERBB%(6,3)=15
PLAYERBB%(7,0)=4 : PLAYERBB%(7,1)=0 : PLAYERBB%(7,2)=11 : PLAYERBB%(7,3)=15
PLAYERBB%(8,0)=4 : PLAYERBB%(8,1)=0 : PLAYERBB%(8,2)=11 : PLAYERBB%(8,3)=15
PLAYERBB%(9,0)=4 : PLAYERBB%(9,1)=0 : PLAYERBB%(9,2)=11 : PLAYERBB%(9,3)=15
PLAYERBB%(10,0)=2 : PLAYERBB%(10,1)=12 : PLAYERBB%(10,2)=12 : PLAYERBB%(10,3)=15
PLAYERBB%(11,0)=5 : PLAYERBB%(11,1)=0 : PLAYERBB%(11,2)=12 : PLAYERBB%(11,3)=15
PLAYERBB%(12,0)=5 : PLAYERBB%(12,1)=0 : PLAYERBB%(12,2)=12 : PLAYERBB%(12,3)=15
PLAYERBB%(13,0)=5 : PLAYERBB%(13,1)=0 : PLAYERBB%(13,2)=12 : PLAYERBB%(13,3)=15
PLAYERBB%(14,0)=5 : PLAYERBB%(14,1)=0 : PLAYERBB%(14,2)=12 : PLAYERBB%(14,3)=15
PLAYERBB%(15,0)=5 : PLAYERBB%(15,1)=0 : PLAYERBB%(15,2)=12 : PLAYERBB%(15,3)=15
PLAYERBB%(16,0)=5 : PLAYERBB%(16,1)=0 : PLAYERBB%(16,2)=12 : PLAYERBB%(16,3)=15
PLAYERBB%(17,0)=5 : PLAYERBB%(17,1)=0 : PLAYERBB%(17,2)=12 : PLAYERBB%(17,3)=15
PLAYERBB%(18,0)=5 : PLAYERBB%(18,1)=0 : PLAYERBB%(18,2)=12 : PLAYERBB%(18,3)=15
PLAYERBB%(19,0)=5 : PLAYERBB%(19,1)=0 : PLAYERBB%(19,2)=12 : PLAYERBB%(19,3)=15
PLAYERBB%(20,0)=5 : PLAYERBB%(20,1)=0 : PLAYERBB%(20,2)=12 : PLAYERBB%(20,3)=15
PLAYERBB%(21,0)=4 : PLAYERBB%(21,1)=12 : PLAYERBB%(21,2)=14 : PLAYERBB%(21,3)=15

' Player Shoot Config (x, y, speedX, speedY)
BulletSpeedX% = 3
BulletSpeedY% = 3
DIM PLAYERSHOOT%(PLAYERNUMSPRITES%, 4)
' right
PLAYERSHOOT%(0,0)=12 : PLAYERSHOOT%(0,1)=4 : PLAYERSHOOT%(0,2)=BulletSpeedX% : PLAYERSHOOT%(0,3)=0
PLAYERSHOOT%(1,0)=10 : PLAYERSHOOT%(1,1)=0 : PLAYERSHOOT%(1,2)=0 : PLAYERSHOOT%(1,3)=-BulletSpeedY%
PLAYERSHOOT%(2,0)=12 : PLAYERSHOOT%(2,1)=4 : PLAYERSHOOT%(2,2)=BulletSpeedX% : PLAYERSHOOT%(2,3)=0
PLAYERSHOOT%(3,0)=12 : PLAYERSHOOT%(3,1)=4 : PLAYERSHOOT%(3,2)=BulletSpeedX% : PLAYERSHOOT%(3,3)=0
PLAYERSHOOT%(4,0)=12 : PLAYERSHOOT%(4,1)=4 : PLAYERSHOOT%(4,2)=BulletSpeedX% : PLAYERSHOOT%(4,3)=0
PLAYERSHOOT%(5,0)=12 : PLAYERSHOOT%(5,1)=4 : PLAYERSHOOT%(5,2)=BulletSpeedX% : PLAYERSHOOT%(5,3)=0
PLAYERSHOOT%(6,0)=12 : PLAYERSHOOT%(6,1)=4 : PLAYERSHOOT%(6,2)=BulletSpeedX% : PLAYERSHOOT%(6,3)=0
PLAYERSHOOT%(7,0)=12 : PLAYERSHOOT%(7,1)=4 : PLAYERSHOOT%(7,2)=BulletSpeedX% : PLAYERSHOOT%(7,3)=0
PLAYERSHOOT%(8,0)=12 : PLAYERSHOOT%(8,1)=4 : PLAYERSHOOT%(8,2)=BulletSpeedX% : PLAYERSHOOT%(8,3)=0
PLAYERSHOOT%(9,0)=12 : PLAYERSHOOT%(9,1)=4 : PLAYERSHOOT%(9,2)=BulletSpeedX% : PLAYERSHOOT%(9,3)=0
PLAYERSHOOT%(10,0)=14 : PLAYERSHOOT%(10,1)=14 : PLAYERSHOOT%(10,2)=BulletSpeedX% : PLAYERSHOOT%(10,3)=0
' left
PLAYERSHOOT%(11,0)=3 : PLAYERSHOOT%(11,1)=4 : PLAYERSHOOT%(11,2)=-BulletSpeedX% : PLAYERSHOOT%(11,3)=0
PLAYERSHOOT%(12,0)=5 : PLAYERSHOOT%(12,1)=0 : PLAYERSHOOT%(12,2)=0 : PLAYERSHOOT%(12,3)=-BulletSpeedY%
PLAYERSHOOT%(13,0)=3 : PLAYERSHOOT%(13,1)=4 : PLAYERSHOOT%(13,2)=-BulletSpeedX% : PLAYERSHOOT%(13,3)=0
PLAYERSHOOT%(14,0)=3 : PLAYERSHOOT%(14,1)=4 : PLAYERSHOOT%(14,2)=-BulletSpeedX% : PLAYERSHOOT%(14,3)=0
PLAYERSHOOT%(15,0)=3 : PLAYERSHOOT%(15,1)=4 : PLAYERSHOOT%(15,2)=-BulletSpeedX% : PLAYERSHOOT%(15,3)=0
PLAYERSHOOT%(16,0)=3 : PLAYERSHOOT%(16,1)=4 : PLAYERSHOOT%(16,2)=-BulletSpeedX% : PLAYERSHOOT%(16,3)=0
PLAYERSHOOT%(17,0)=3 : PLAYERSHOOT%(17,1)=4 : PLAYERSHOOT%(17,2)=-BulletSpeedX% : PLAYERSHOOT%(17,3)=0
PLAYERSHOOT%(18,0)=3 : PLAYERSHOOT%(18,1)=4 : PLAYERSHOOT%(18,2)=-BulletSpeedX% : PLAYERSHOOT%(18,3)=0
PLAYERSHOOT%(19,0)=3 : PLAYERSHOOT%(19,1)=4 : PLAYERSHOOT%(19,2)=-BulletSpeedX% : PLAYERSHOOT%(19,3)=0
PLAYERSHOOT%(20,0)=3 : PLAYERSHOOT%(20,1)=4 : PLAYERSHOOT%(20,2)=-BulletSpeedX% : PLAYERSHOOT%(20,3)=0
PLAYERSHOOT%(21,0)=1 : PLAYERSHOOT%(21,1)=14 : PLAYERSHOOT%(21,2)=-BulletSpeedX% : PLAYERSHOOT%(21,3)=0

' Bullet config
BulletStateEmpty% = 0
BulletStateFire% = 1
BulletStateMove% = 2

NumP1Bullets% = 8
DIM bullet1x%(NumP1Bullets%)
DIM bullet1y%(NumP1Bullets%)
DIM bullet1speedX%(NumP1Bullets%)
DIM bullet1speedY%(NumP1Bullets%)
DIM bullet1state%(NumP1Bullets%)
DIM bullet1entity%(NumP1Bullets%)
DIM bullet1sprite%(NumP1Bullets%)

' Key config

KeyUp$ = CHR$(0) + CHR$(38)
KeyRight$ = CHR$(0) + CHR$(39)
KeyDown$ = CHR$(0) + CHR$(40)
KeyLeft$ = CHR$(0) + CHR$(37)
KeyA$ = "x"
KeyB$ = "z"
KeySelect$ = CHR$(10)

' Create screen
BGSCRW% = 30 : BGSCRH% = 12
SCRW% = BGSCRW% * BGTILEW%
SCRH% = BGSCRH% * BGTILEH%
GAMESCRW% = BGSCRW% * SPLIT%
GAMESCRH% = BGSCRH% * SPLIT%
GAMESCRSCROLLW% = GAMESCRW% \ 2

createScreen(SCRW%, SCRH%, SCRW% + BGTILEW%, SCRH%)

' Draw background on BACK1
drawBg%(BGMAP%, BGSCRW%, BGSCRH%, BGTILEW%, BGTILEH%, 0, bgSprites, numMapEntities%, BgEntityNumSprites%, BgEntitySpriteItr%, BgEntitySprites%, 0)

' Player Jump Config (the player goes up faster than coming down)
MAXVERTSPEED% = 15
DIM VERTSPEED%(1 + MAXVERTSPEED%)

VERTSPEED%(0) = -12 : VERTSPEED%(1) = -8 : VERTSPEED%(2) = -2
VERTSPEED%(3) = -2 : VERTSPEED%(4) = -1 : VERTSPEED%(5) = -1
VERTSPEED%(6) = 0
VERTSPEED%(7) = 1 : VERTSPEED%(8) = 1 : VERTSPEED%(9) = 2
VERTSPEED%(10) = 1 : VERTSPEED%(11) = 2 : VERTSPEED%(12) = 2
VERTSPEED%(13) = 2 : VERTSPEED%(14) = 2 : VERTSPEED%(15) = 4

JUMPVERTSPEED% = 0
FALLVERTSPEED% = 7
STOPVERTSPEED% = 6

SpeedX% = 2
ScrollX% = 16
GameMapScrollX% = ScrollX% \ SPLIT%

xoffset% = 0
colstart% = 0
scrollx% = 0

STRUCT Player { moveLR%, moveDirNum%, x%, y%, vertSpeedIdx%, state%, numLives% }
Player player1 {}

player1.moveLR% = MoveDirRight%
player1.moveDirNum% = MoveDirNumR%
player1.x% = 1
player1.y% = 1
player1.vertSpeedIdx% = FALLVERTSPEED%
player1.state% = StateFall%
player1.numLives% = 10
p1numFrames% = AFRAME%(player1.state%, player1.moveDirNum%, 0)
p1currFrame% = 0

cycleNum% = 0
numCycleFrame% = 3

bgCycleNum% = 0
numBgCycleFrame% = 8

cycleSinceLastP1Jump% = 0
cycleSinceLastP1Shoot% = 0
P1ActionNumCycles% = 5

shiftX% = 0
shiftY% = 0

' Play bg sound
PLAYWAV SOUNDLEVEL1BG1%

timer0@ = TIMERMILLIS

' Game loop
WHILE player1.numLives% > 0

    p1x% = player1.x%
    p1y% = player1.y%
    p1speedX% = 0
    p1speedY% = 0
    p1state% = player1.state%
    nextp1state% = p1state%
    p1moveLR% = player1.moveLR%
    p1moveDirNum% = player1.moveDirNum%
    p1vertSpeedIdx% = player1.vertSpeedIdx%
    p1frame% = AFRAME%(p1state%, p1moveDirNum%, p1currFrame% + 1)
    p1bbx1% = PLAYERBB%(p1frame%, 0)
    p1bby1% = PLAYERBB%(p1frame%, 1)
    p1bbx2% = PLAYERBB%(p1frame%, 2)
    p1bby2% = PLAYERBB%(p1frame%, 3)
    p1scrX% = p1x% * GAMEW%
    p1scrY% = p1y% * GAMEH%

    '  Draw Player1
    AUTO p1sprite = playerSprites.get(p1frame%)
    PUT(p1scrX%, p1scrY%), p1sprite, "MIX"
    LINE (p1scrX% + p1bbx1% * GAMEW%, p1scrY% + p1bby1% * GAMEH%) - (p1scrX% + p1bbx2% * GAMEW%, p1scrY% + p1bby2% * GAMEH%), "B"

    ' Draw bullets
    FOR I% = 0 TO NumP1Bullets% - 1
        b1state% = bullet1state%(I%)
        IF b1state% = BulletStateMove% THEN BEGIN
            b1scrX% = bullet1x%(I%) * GAMEW%
            b1scrY% = bullet1y%(I%) * GAMEH%
            PUT(b1scrX%, b1scrY%), BULLET1%, "MIX"
        END IF
    NEXT I%

    ' Draw enemy bullets
    FOR I% = 0 TO NumEnemyBullets% - 1
        ebstate% = EnemyBulletState%(I%)
        IF ebstate% = EnemyBulletStateMove% THEN BEGIN
            ebscrX% = EnemyBulletX%(I%) * SPLIT%
            ebscrY% = EnemyBulletY%(I%) * SPLIT%
            PUT(ebscrX%, ebscrY%), LavaFallEnemyBullet1%, "MIX"
        END IF
    NEXT I%

    REPAINT
    timer1@ = TIMERMILLIS

    key$ = INKEY$
    kpU% = ISKEYPRESSED(KeyUp$)
    kpR% = ISKEYPRESSED(KeyRight$)
    kpD% = ISKEYPRESSED(KeyDown$)
    kpL% = ISKEYPRESSED(KeyLeft$)
    checkKey% = -1

    IF ISKEYPRESSED(KeyA$) OR key$ = KeyA$ THEN keyPressedA% = -1 ELSE keyPressedA% = 0
    IF ISKEYPRESSED(KeyB$) OR key$ = KeyB$ THEN keyPressedB% = -1 ELSE keyPressedB% = 0
    IF ISKEYPRESSED(KeySelect$) OR key$ = KeySelect$ THEN keyPressedSelect% = -1 ELSE keyPressedSelect% = 0

    IF p1moveLR% = MoveDirRight% THEN p1moveDirNum% = MoveDirNumR% ELSE p1moveDirNum% = MoveDirNumL%

    IF kpR% THEN BEGIN
        checkKey% = 0
        IF kpU% THEN p1moveDirNum%=MoveDirNumURR% ELSE IF kpD% THEN p1moveDirNum%=MoveDirNumDRR% ELSE p1moveDirNum%=MoveDirNumR%
        p1moveLR% = MoveDirRight%
        p1speedX% = SpeedX%
    END IF
    IF checkKey% AND kpL% THEN BEGIN
        checkKey% = 0
        IF kpU% THEN p1moveDirNum%=MoveDirNumULL% ELSE IF kpD% THEN p1moveDirNum%=MoveDirNumDLL% ELSE p1moveDirNum%=MoveDirNumL%
        p1moveLR% = MoveDirLeft%
        p1speedX% = -SpeedX%
    END IF
    IF checkKey% THEN BEGIN
        IF kpU% THEN IF p1moveLR%=MoveDirRight% THEN p1moveDirNum%=MoveDirNumUR% ELSE p1moveDirNum%=MoveDirNumUL%
        IF kpD% THEN IF p1moveLR%=MoveDirRight% THEN p1moveDirNum%=MoveDirNumDR% ELSE p1moveDirNum%=MoveDirNumDL%
        p1speedX% = 0
    END IF

    ' Check if player is on the floor
    floorCollision% = Array2DFindRow(GAMEMAP%, p1x% + p1bbx1%, p1y% + p1bby2% + 1, p1x% + p1bbx2%, p1y% + p1bby2% + 1, 1) > -1
    deadOnCollision% = Array2DFindRow(GAMEMAP%, p1x% + p1bbx1%, p1y% + p1bby2% + 1, p1x% + p1bbx2%, p1y% + p1bby2% + 1, 2) > -1

    ' Handle player state changes
    IF deadOnCollision% THEN nextp1state% = StateDead%
    IF p1state% = StateFall% THEN BEGIN
        IF floorCollision% THEN nextp1state% = StateStand%
    END IF
    IF p1state% = StateStand% THEN BEGIN
        IF floorCollision% = 0 THEN nextp1state% = StateFall%
        IF p1speedX% <> 0 THEN nextp1state% = StateRun% ELSE IF kpD% THEN nextp1state% = StateSit%
        IF keyPressedA% AND floorCollision% AND cycleSinceLastP1Jump% > P1ActionNumCycles% THEN nextp1state% = StateJump% : cycleSinceLastP1Jump% = 0
    END IF
    IF p1state% = StateSit% THEN BEGIN
        IF floorCollision% = 0 THEN nextp1state% = StateFall%
        IF kpD% = 0 THEN nextp1state% = StateStand%
    END IF
    IF p1state% = StateRun% THEN BEGIN
        IF floorCollision% = 0 THEN nextp1state% = StateFall% ELSE IF p1speedX% = 0 THEN nextp1state% = StateStand%
        IF keyPressedA% AND floorCollision% AND cycleSinceLastP1Jump% > P1ActionNumCycles% THEN nextp1state% = StateJump% : cycleSinceLastP1Jump% = 0
    END IF
    IF p1state% = StateJump% THEN BEGIN
        IF floorCollision% THEN nextp1state% = StateStand%
    END IF

    IF keyPressedB% AND cycleSinceLastP1Shoot% > P1ActionNumCycles% THEN BEGIN
        I% = 0
        slot% = -1
        WHILE slot%  = -1 AND I% < NumP1Bullets%
            IF bullet1state%(I%) = BulletStateEmpty% THEN slot% = I%
            I% = I% + 1
        WEND
        IF slot% > -1 THEN BEGIN
            cycleSinceLastP1Shoot% = 0
            bullet1state%(slot%) = BulletStateFire%
            bullet1entity%(slot%) = 0
            bullet1sprite%(slot%) = 0
            PLAYWAV SOUNDBULLET1%
        END IF
    END IF

    cycleSinceLastP1Shoot% = cycleSinceLastP1Shoot% + 1
    cycleSinceLastP1Jump% = cycleSinceLastP1Jump% + 1

    ' Handle Lava Fall
    nextStartLavaFallIdx% = startLavaFallIdx%
    FOR I% = startLavaFallIdx% TO numLavaFall% - 1
        state% = LavaFallState%(I%)
        IF state% <> LavaFallStateDead% THEN BEGIN
            x% = LavaFallX%(I%) + LavaFallShootPosX%(I%) + shiftX%
            y% = LavaFallY%(I%) + LavaFallShootPosY%(I%) + shiftY%
            IF x% >= 0 AND y% >=0 AND x% < GAMESCRW% AND y% < GAMESCRH% THEN BEGIN
                dt@ = timer1@ - LavaFallTimer@(I%)
                interval% = LavaFallInterval%(I%)
                IF dt@ > interval% THEN BEGIN
                    J% = 0
                    slot% = -1
                    WHILE slot%  = -1 AND J% < NumEnemyBullets%
                        IF EnemyBulletState%(J%) = EnemyBulletStateEmpty% THEN slot% = J%
                        J% = J% + 1
                    WEND
                    IF slot% > -1 THEN BEGIN
                        PRINT "FIRE Lava" : LavaFallTimer@(I%) = timer1@
                        EnemyBulletState%(slot%) = EnemyBulletStateFire%
                        EnemyBulletX%(slot%) = x%
                        EnemyBulletY%(slot%) = y%
                        EnemyBulletEntity%(slot%) = 0
                        EnemyBulletSprite%(slot%) = 0
                        PRINT I%, x%, y%, GAMESCRW%, GAMESCRH%, state%
                        'PLAYWAV SOUNDBULLET1%
                    END IF
                END IF
            END IF
            IF x% < 0 THEN LavaFallState%(I%) = LavaFallStateDead% : nextStartLavaFallIdx% = I% + 1
        END IF
    NEXT I%
    startLavaFallIdx% = nextStartLavaFallIdx%

    ' Handle vertical speed changes
    canChangeVertSpeed% = 0
    IF p1state% <> nextp1state% THEN BEGIN
        p1currFrame% = 0
        IF nextp1state%=StateFall% THEN p1vertSpeedIdx%=FALLVERTSPEED% ELSE IF nextp1state%=StateJump% THEN p1vertSpeedIdx%=JUMPVERTSPEED% ELSE p1vertSpeedIdx%=STOPVERTSPEED%
        IF (p1state%=StateJump% OR p1state%=StateFall%) AND nextp1state%=StateStand% THEN PLAYWAV SOUNDLAND1%
    ELSE BEGIN
        IF nextp1state%=StateFall% OR nextp1state%=StateJump% THEN p1vertSpeedIdx%=MIN(p1vertSpeedIdx% + 1, MAXVERTSPEED%)
    END IF

    IF isKilledByVerticalWall%(GAMEMAP%, p1x%, p1y%, p1bbx1%, p1bby1%, p1bbx2%, p1bby2%, p1speedX%) THEN nextp1state% = StateDead%
    p1speedX% = clipSpeedX%(GAMEMAP%, p1x%, p1y%, p1bbx1%, p1bby1%, p1bbx2%, p1bby2%, p1speedX%)

    p1speedY% = VERTSPEED%(p1vertSpeedIdx%)

    IF isKilledByHorizontalWall%(GAMEMAP%, p1x%, p1y%, p1bbx1%, p1bby1%, p1bbx2%, p1bby2%, p1speedY%) THEN nextp1state% = StateDead%
    p1speedY% = clipSpeedY%(GAMEMAP%, p1x%, p1y%, p1bbx1%, p1bby1%, p1bbx2%, p1bby2%, p1speedY%)

    IF nextp1state% <> StateDead% THEN BEGIN
        p1x% = MIN(MAX(0, p1x% + p1speedX%), GAMESCRW% - 16)
        p1y% = MIN(MAX(0, p1y% + p1speedY%), GAMESCRH% - 16)
        p1numFrames% = AFRAME%(nextp1state%, p1moveDirNum%, 0)
        cycleNum% = cycleNum% + 1
        IF cycleNum% = numCycleFrame% THEN p1currFrame% = (p1currFrame% + 1) MOD p1numFrames% : cycleNum% = 0
    ELSE BEGIN
        player1.numLives% = player1.numLives% - 1
        ' change player state
        ' find spawn position
        ' set new x and y
    END IF

    IF p1x% > GAMESCRSCROLLW% AND colstart% < BGNCOLS% - BGSCRW% THEN BEGIN
        scrollx% = ScrollX%
        p1x% = GAMESCRSCROLLW%
    ELSE BEGIN
        scrollx% = 0
    END IF

    player1.x% = p1x%
    player1.y% = p1y%
    player1.state% = nextp1state%
    player1.moveLR% = p1moveLR%
    player1.vertSpeedIdx% = p1vertSpeedIdx%
    player1.moveDirNum% = p1moveDirNum%

    xoffset% = xoffset% + scrollx%

    FOR I% = 0 TO NumP1Bullets% - 1
        b1state% = bullet1state%(I%)
        b1x% = bullet1x%(I%) - scrollx% \ SPLIT%
        b1y% = bullet1y%(I%)

        IF b1state% = BulletStateMove% THEN BEGIN
            b1x% = b1x% + bullet1speedX%(I%)
            b1y% = b1y% + bullet1speedY%(I%)
            IF b1x% < 0% OR b1x% > GAMESCRW% OR b1y% < 0 OR b1y% > GAMESCRH% THEN b1state% = BulletStateEmpty%
            bullet1state%(I%) = b1state%
        ELSE BEGIN
            IF b1state% = BulletStateFire% THEN BEGIN
                p1frame% = AFRAME%(nextp1state%, p1moveDirNum%, p1currFrame% + 1)
                bullet1state%(I%) = BulletStateMove%
                b1x% = p1x% + PLAYERSHOOT%(p1frame%, 0)
                b1y% = p1y% + PLAYERSHOOT%(p1frame%, 1)
                bullet1speedX%(I%) = PLAYERSHOOT%(p1frame%, 2)
                bullet1speedY%(I%) = PLAYERSHOOT%(p1frame%, 3)
            END IF
        END IF

        bullet1x%(I%) = b1x%
        bullet1y%(I%) = b1y%

    NEXT I%

    FOR I% = 0 TO NumEnemyBullets% - 1
        ebstate% = EnemyBulletState%(I%)
        ebx% = EnemyBulletX%(I%) - scrollx% \ SPLIT%
        eby% = EnemyBulletY%(I%)

        IF ebstate% = EnemyBulletStateMove% THEN BEGIN
            ebx% = ebx% + EnemyBulletSpeedX%(I%)
            eby% = eby% + EnemyBulletSpeedY%(I%)
            IF ebx% < 0% OR ebx% > GAMESCRW% OR eby% < 0 OR eby% > GAMESCRH% THEN ebstate% = EnemyBulletStateEmpty%
            ' Check collision with player
            r1x1% = p1x% + p1bbx1% : r1y1% = p1y% + p1bby1% : r1x2% = p1x% + p1bbx2% : r1y2% = p1y% + p1bby2%
            r2x1% = ebx% : r2y1% = eby% : r2x2% = ebx% +  + BULLETW% \ SPLIT% : r2y2% = eby% + BULLETH% \ SPLIT%
            collision% = r2x1% <= r1x2% AND r2y1% <= r1y2% AND r2x2% >= r1x1% AND r2y2% >= r1y1%
            IF collision% THEN BEGIN
                ebstate% = EnemyBulletStateEmpty%
                ' draw collision animation
                ' set player state to dead
                ' reduce players lives
                player1.numLives% = player1.numLives% - 1
                PRINT "Collision", collision%, r1x1%, r1y1%, r1x2%, r1y2%, r2x1%, r2y1%, r2x2%, r2y2%, player1.numLives%
            END IF
            EnemyBulletState%(I%) = ebstate%
        ELSE BEGIN
            IF ebstate% = EnemyBulletStateFire% THEN BEGIN
                PRINT "EnemyBullet"
                EnemyBulletState%(I%) = EnemyBulletStateMove%
                EnemyBulletSpeedX%(I%) = 0
                EnemyBulletSpeedY%(I%) = 1
            END IF
        END IF

        EnemyBulletX%(I%) = ebx%
        EnemyBulletY%(I%) = eby%

    NEXT I%

    IF scrollx% > 0 THEN ARRAY2DSHIFTHOR GAMEMAP%, -GameMapScrollX% : shiftX% = shiftX% - GameMapScrollX%
    ' Scroll the bg map and game map
    IF xoffset% = BGTILEW% THEN BEGIN
        ARRAY2DSHIFTHOR BGMAP%, -1
        xoffset% = 0
        colstart% = colstart% + 1
    END IF

    ' Redraw background
    IF bgCycleNum% = numBgCycleFrame% THEN incrBgAnimation% = 1 : bgCycleNum% = 0 ELSE incrBgAnimation% = 0 : bgCycleNum% = bgCycleNum% + 1
    drawBg%(BGMAP%, BGSCRW%, BGSCRH%, BGTILEW%, BGTILEH%, xoffset%, bgSprites, numMapEntities%, BgEntityNumSprites%, BgEntitySpriteItr%, BgEntitySprites%, incrBgAnimation%)

    sleepTime@ = MAX(0, 40 - (TIMERMILLIS - timer1@))
    IF sleepTime@ > 0 THEN SLEEP sleepTime@

WEND

END

LABEL "LoadMaps"

PRINT "Reading bg map file: ", mapFile$
OPEN "I", #1, mapFile$

' Read num sprites
LINE INPUT#1, mapline$ : AUTO tokens = SPLIT$(mapline$, ",") : numBgSprites% = VAL(tokens(0))

' Read sprites
I% = 0
IF I% < numBgSprites% THEN LINE INPUT#1, mapline$ : AUTO tokens = SPLIT$(mapline$, ",") : loadAndAddSprite(IMAGEROOT$, tokens(1), BGTILE0%, bgSprites) : I% = I% + 1
IF I% < numBgSprites% THEN LINE INPUT#1, mapline$ : AUTO tokens = SPLIT$(mapline$, ",") : loadAndAddSprite(IMAGEROOT$, tokens(1), BGTILE1%, bgSprites) : I% = I% + 1
IF I% < numBgSprites% THEN LINE INPUT#1, mapline$ : AUTO tokens = SPLIT$(mapline$, ",") : loadAndAddSprite(IMAGEROOT$, tokens(1), BGTILE2%, bgSprites) : I% = I% + 1
IF I% < numBgSprites% THEN LINE INPUT#1, mapline$ : AUTO tokens = SPLIT$(mapline$, ",") : loadAndAddSprite(IMAGEROOT$, tokens(1), BGTILE3%, bgSprites) : I% = I% + 1
IF I% < numBgSprites% THEN LINE INPUT#1, mapline$ : AUTO tokens = SPLIT$(mapline$, ",") : loadAndAddSprite(IMAGEROOT$, tokens(1), BGTILE4%, bgSprites) : I% = I% + 1
IF I% < numBgSprites% THEN LINE INPUT#1, mapline$ : AUTO tokens = SPLIT$(mapline$, ",") : loadAndAddSprite(IMAGEROOT$, tokens(1), BGTILE5%, bgSprites) : I% = I% + 1
IF I% < numBgSprites% THEN LINE INPUT#1, mapline$ : AUTO tokens = SPLIT$(mapline$, ",") : loadAndAddSprite(IMAGEROOT$, tokens(1), BGTILE6%, bgSprites) : I% = I% + 1
IF I% < numBgSprites% THEN LINE INPUT#1, mapline$ : AUTO tokens = SPLIT$(mapline$, ",") : loadAndAddSprite(IMAGEROOT$, tokens(1), BGTILE7%, bgSprites) : I% = I% + 1
IF I% < numBgSprites% THEN LINE INPUT#1, mapline$ : AUTO tokens = SPLIT$(mapline$, ",") : loadAndAddSprite(IMAGEROOT$, tokens(1), BGTILE8%, bgSprites) : I% = I% + 1
IF I% < numBgSprites% THEN LINE INPUT#1, mapline$ : AUTO tokens = SPLIT$(mapline$, ",") : loadAndAddSprite(IMAGEROOT$, tokens(1), BGTILE9%, bgSprites) : I% = I% + 1
IF I% < numBgSprites% THEN LINE INPUT#1, mapline$ : AUTO tokens = SPLIT$(mapline$, ",") : loadAndAddSprite(IMAGEROOT$, tokens(1), BGTILE10%, bgSprites) : I% = I% + 1
IF I% < numBgSprites% THEN LINE INPUT#1, mapline$ : AUTO tokens = SPLIT$(mapline$, ",") : loadAndAddSprite(IMAGEROOT$, tokens(1), BGTILE11%, bgSprites) : I% = I% + 1
IF I% < numBgSprites% THEN LINE INPUT#1, mapline$ : AUTO tokens = SPLIT$(mapline$, ",") : loadAndAddSprite(IMAGEROOT$, tokens(1), BGTILE12%, bgSprites) : I% = I% + 1
IF I% < numBgSprites% THEN LINE INPUT#1, mapline$ : AUTO tokens = SPLIT$(mapline$, ",") : loadAndAddSprite(IMAGEROOT$, tokens(1), BGTILE13%, bgSprites) : I% = I% + 1
IF I% < numBgSprites% THEN LINE INPUT#1, mapline$ : AUTO tokens = SPLIT$(mapline$, ",") : loadAndAddSprite(IMAGEROOT$, tokens(1), BGTILE14%, bgSprites) : I% = I% + 1
IF I% < numBgSprites% THEN LINE INPUT#1, mapline$ : AUTO tokens = SPLIT$(mapline$, ",") : loadAndAddSprite(IMAGEROOT$, tokens(1), BGTILE15%, bgSprites) : I% = I% + 1
IF I% < numBgSprites% THEN LINE INPUT#1, mapline$ : AUTO tokens = SPLIT$(mapline$, ",") : loadAndAddSprite(IMAGEROOT$, tokens(1), BGTILE16%, bgSprites) : I% = I% + 1

' Read num map entities
LINE INPUT#1, mapline$ : AUTO tokens = SPLIT$(mapline$, ",") : numMapEntities% = VAL(tokens(0))
FOR I% = 0 TO numMapEntities% - 1
    LINE INPUT#1, mapline$ : AUTO tokens = SPLIT$(mapline$, ",")
    J% = VAL(tokens(0))
    BgEntityKind%(J%) = VAL(tokens(1))
    numSprites% = VAL(tokens(2))
    BgEntityNumSprites%(J%) = numSprites%
    FOR K% = 0 TO numSprites% - 1
        BgEntitySprites%(J%, K%) = VAL(tokens(3 + K%))
    NEXT K%
NEXT I%

' Read ncols%
LINE INPUT#1, mapline$ : AUTO tokens = SPLIT$(mapline$, ",") : BGNCOLS% = VAL(tokens(0))

FOR I% = 0 TO BGNCOLS% - 1
    LINE INPUT#1, mapline$
    AUTO tokens = SPLIT$(mapline$, ",")
    maxtokenidx% = LEN(tokens) - 1
    maxtokenidx2% = SPLIT% * LEN(tokens) - 1
    FOR J% = 0 TO maxtokenidx%
        value% = VAL(tokens(J%))
        BGMAP%(maxtokenidx% - J%, I%) = value%
        entityKind% = BgEntityKind%(value%)
        FOR K% = 0 TO SPLIT% - 1
            FOR L% = 0 TO SPLIT% - 1
                GAMEMAP%(maxtokenidx2% - J%*SPLIT% + L%, I%*SPLIT% + K%) = entityKind%
            NEXT L%
        NEXT K%
    NEXT J%
NEXT I%

' Read num LAVA falls
LINE INPUT#1, mapline$ : AUTO tokens = SPLIT$(mapline$, ",") : numLavaFall% = VAL(tokens(0))
FOR I% = 0 TO numLavaFall% - 1
    LINE INPUT#1, mapline$ : AUTO tokens = SPLIT$(mapline$, ",")
    LavaFallX%(I%) = VAL(tokens(2)) * GAMEW%
    LavaFallY%(I%) = VAL(tokens(3)) * GAMEH%
    LavaFallShootPosX%(I%) = VAL(tokens(4)) \ SPLIT%
    LavaFallShootPosY%(I%) = VAL(tokens(5)) \ SPLIT%
    LavaFallInterval%(I%) = VAL(tokens(6)) * 1000
NEXT I%

CLOSE #1

RETURN
