PRINT "LIST of STRINGs"

LIST<$> list1
PRINT len(list1)

list1.append("a")
list1.append("b")
PRINT list1.get(0)
PRINT list1.get(1)
PRINT len(list1)

list1.insert(0, "c")
PRINT list1.get(0)
PRINT len(list1)

list1.clear()
PRINT len(list1)
