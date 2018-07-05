# ThreadPoolExecutor
Demonstrates use of ThreadpoolExecutors , Dagger , Retrofit , Service

Scenario

![ScreenShot](https://raw.githubusercontent.com/AlvinaC/ThreadPoolExecutor/master/screenshot/scenario.png)


1) Need to download multiple files parallelly in the background
2) Show progress of download in the notification tray

Used Components

1) Dagger2 for dependancy injection(helps create and maintain the same retrofit object for all the dowloads)
2) ThreadPoolExecutors (best for the scenario, when there are lots of files to download)
3) Service component which assigns the job to threads in the pool
4) When the task is removed from recents, the downloads continue as the service is upgraded to run in foreground.

Out of scope
1) Not checking the internet connection- but code can be extended for the same



