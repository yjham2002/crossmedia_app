package bases;

import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

/**
 * Base class for activities
 * @author EuiJin.Ham
 * @version 1.0.0
 */
public abstract class BaseActivity extends AppCompatActivity implements View.OnClickListener{

    protected void showToast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onClick(View view){
        Log.e(this.getClass().getSimpleName(), "Override onClick method in BaseActivity to use View.OnClickListener");
    }

    /**
     * Bind itself as a OnClickListener on parameters
     * @param views view objects to bind a listener
     */
    protected void setClick(View... views){
        for(View view : views) view.setOnClickListener(this);
    }

}
