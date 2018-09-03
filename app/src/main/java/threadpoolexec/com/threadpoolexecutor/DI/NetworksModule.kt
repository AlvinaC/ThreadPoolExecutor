package threadpoolexec.com.threadpoolexecutor.DI

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.Executors


@Module
class NetworksModule(private val urlPath: String) {

    @Provides
    fun provideGson(): Gson = GsonBuilder().create()

    @Provides
    fun provideRetrofit(gson: Gson): Retrofit =
            Retrofit.Builder()
                    .baseUrl(urlPath)
                    .callbackExecutor(Executors.newSingleThreadExecutor())
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build()
}