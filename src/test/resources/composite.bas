' Test struct

STRUCT struct1 { A% , B% }
struct1 s1 {}
struct1 s2 {}

PRINT s1.A%, s2.A%

s1.A% = 2
s2.A% = 10
PRINT s1.A%, s2.A%

s1.A% = s2.A%
PRINT s1.A%, s2.A%

s1.A% = 11
s2.A% = 12
s2 = s1
PRINT s1.A%, s2.A%

STRUCT struct2 { C% , struct1 child }
struct2 s3 {}
struct2 s4 {}

s3.child.A% = 100
s3.C% = 50
PRINT s3.child.A%, s3.C%
