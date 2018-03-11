package comm;

import android.util.Log;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by HP on 2018-01-03.
 */

public class Comm implements BaseCommunication{

    static{
        instance = new Comm();
        Log.i(Comm.class.getSimpleName(),String.format("Communication Service has been ignited with URL[%s].", BASE_URL));
    }

    private static Comm instance;

    private IComm service;
    private OkHttpClient httpClient;

    public static IComm call(){
        return Comm.instance.service;
    }

    private Comm(){
        this.httpClient = new OkHttpClient();

        final Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(httpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        this.service = retrofit.create(IComm.class);
    }

}
