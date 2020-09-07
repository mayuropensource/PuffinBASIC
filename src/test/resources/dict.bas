DICT<$,%> dict1

dict1.put("a", 65)
dict1.put("b", 66)
PRINT LEN(dict1)

PRINT dict1.getOrDefault("a", -1)
PRINT dict1.getOrDefault("c", -1)
PRINT LEN(dict1)

PRINT dict1.remove("a")
PRINT dict1.getOrDefault("a", -1)
PRINT LEN(dict1)

dict1.clear()
PRINT LEN(dict1)
