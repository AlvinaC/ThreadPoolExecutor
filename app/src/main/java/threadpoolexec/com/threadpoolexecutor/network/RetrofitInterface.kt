package threadpoolexec.com.threadpoolexecutor.network

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Streaming
import retrofit2.http.Url

/**
 * Retrofit Interface
 */
interface RetrofitInterface {

    @GET
    @Streaming
    fun downloadFile(@Url url: String): Call<ResponseBody>
}

