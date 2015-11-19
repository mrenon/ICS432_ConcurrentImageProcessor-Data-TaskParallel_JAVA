ran with 16 images

******* 1 Thread *******
rrrrrrrrprwprwprwprwprwprwprwprTime Spent Reading: 1.665 sec.
wpwpwpwpwpwpwpwpTime Spent Processing: 26.482 sec.
wTime Spent Writing: 0.778 sec.

Overall Execution Time: 26.515 sec.

******* 2 Thread *******
rrrrrrrrrprprwwprwpwrprprwwprTime Spent Reading: 2.522 sec.
pwwppwwpwpwpwpwpTime Spent Processing: 17.51 sec.
wpTime Spent Processing: 1.428056152026E9 sec.
wTime Spent Writing: 2.764 sec.

Overall Execution Time: 17.691 sec.

******* 4 Thread *******
rrrrrrrrrrrprpprwwrpwrwprTime Spent Reading: 3.053 sec.
pwpwpwwpwpwppwwpTime Spent Processing: 12.027 sec.
wpTime Spent Processing: 1.428056255397E9 sec.
pTime Spent Processing: 1.428056257612E9 sec.
pTime Spent Processing: 2.856112500986E9 sec.
wwwTime Spent Writing: 0.997 sec.

Overall Execution Time: 12.204 sec.

******* 8 Thread *******
rrrrrrrrrrrrrrrprTime Spent Reading: 2.815 sec.
wpwpwpwppwpwpwwpTime Spent Processing: 10.86 sec.
wpTime Spent Processing: 1.428056305358E9 sec.
pTime Spent Processing: 1.428056309623E9 sec.
pTime Spent Processing: 2.856112604148E9 sec.
wpTime Spent Processing: 2.856112608438E9 sec.
wpTime Spent Processing: 4.284168903011E9 sec.
wpTime Spent Processing: 4.284168907364E9 sec.
wpTime Spent Processing: 5.712225201943E9 sec.
wwwTime Spent Writing: 1.019 sec.

Overall Execution Time: 11.227 sec.


The performance factor with 2, 4, 8 threads were significantly faster than when 1 thread was ran. The performance factor using 2 threads compared to 1 made the runtime cut down by almost half. and when 4 threads were introduced it was 3-4 times as fast. By this observation we can assume there is a correlation in which the number of threads makes the code that much times faster. 




*******LARGGE-PHOTO*******

*****task-parallel******
rTime Spent Reading: 0.87 sec.
pTime Spent Processing: 18.28 sec.
wTime Spent Writing: 0.383 sec.


***data-parallel*******
rTime Spent Reading: 0.627 sec.
pTime Spent Processing: 2.778 sec.
wTime Spent Writing: 0.285 sec.

Overall Execution Time: 3.064 sec.


there is a significant boost between processing time because more threads work on the image instead of one thread. Whether the load balancing is fair between the threads in data parallel is another story, but as of right now we don’t care because it is still faster than using one thread all together,