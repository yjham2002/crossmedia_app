package comm;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.GET;

/**
 * Created by HP on 2018-01-03.
 */

public interface IComm {

    @GET("/help/json")
    Call<List<Map>> getHelp();

    @GET("/info/article")
    Call<List<Map>> getArticles();

}
