PRINT "SET of STRINGs"

SET<$> set1

set1.add("a")
set1.add("a")
PRINT LEN(set1)

PRINT set1.contains("a")

set1.remove("a")
PRINT LEN(set1)

set1.clear()
PRINT LEN(set1)
