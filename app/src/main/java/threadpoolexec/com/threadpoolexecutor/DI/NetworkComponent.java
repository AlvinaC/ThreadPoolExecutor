package threadpoolexec.com.threadpoolexecutor.DI;

/**
 * Created by 10986 on 12/5/2017.
 */

import javax.inject.Singleton;

import dagger.Component;
import threadpoolexec.com.threadpoolexecutor.services.DownloaderService;

@Singleton
@Component(modules = {NetworksModule.class})
public interface NetworkComponent {
    public void inject(DownloaderService service);
}
