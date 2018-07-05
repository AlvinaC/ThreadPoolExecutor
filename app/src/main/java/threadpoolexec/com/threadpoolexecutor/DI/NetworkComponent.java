package threadpoolexec.com.threadpoolexecutor.DI;

import javax.inject.Singleton;

import dagger.Component;
import threadpoolexec.com.threadpoolexecutor.services.DownloaderService;

@Singleton
@Component(modules = {NetworksModule.class})
public interface NetworkComponent {
    public void inject(DownloaderService service);
}
