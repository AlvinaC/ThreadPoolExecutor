package threadpoolexec.com.threadpoolexecutor.DI

import dagger.Component
import threadpoolexec.com.threadpoolexecutor.services.DownloaderService
import javax.inject.Singleton

@Singleton
@Component(modules = [NetworksModule::class])
interface NetworkComponent {
    fun inject(service: DownloaderService)
}

