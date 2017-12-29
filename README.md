# ThreadPoolExecutor
Demonstrates use of ThreadpoolExecutors , Dagger , Retrofit , Service

Scenario

1) Need to download multiple files parallelly in the background
2) Show progress of download in the notification tray

Used Components

1) Dagger2 for dependancy injection(helps create and maintain the same retrofit object for all the dowloads)
2) ThreadPoolExecutors (best for the scenario, when there are lots of files to download)
3) Service component which assigns the job to threads in the pool(any other component also can be used for the downloads, but service offers the advantage of starting itself again when the app process is killed)

Out of scope

1) When the app process is killed the download starts again, if any download is running the status regarding that is not saved in this project, but the code can be extended for the same
2) Not checking the internet connection- but code can be extended for the same



