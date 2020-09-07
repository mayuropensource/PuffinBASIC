PRINT "DICT of STRING to INT32"

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

PRINT dict1.containsKey("a")
PRINT dict1.containsKey("b")

dict1.clear()
PRINT LEN(dict1)

PRINT "DICT of INT32 to STRING"

DICT<%,$> dict2

dict2.put(1, "a")
dict2.put(2, "b")
PRINT LEN(dict2)

PRINT dict2.getOrDefault(1, "")
PRINT dict2.getOrDefault(2, "")
PRINT LEN(dict2)

PRINT dict2.remove(1)
PRINT dict2.getOrDefault(1, "")
PRINT LEN(dict2)

PRINT dict2.containsKey(1)
PRINT dict2.containsKey(2)

dict2.clear()
PRINT LEN(dict2)
