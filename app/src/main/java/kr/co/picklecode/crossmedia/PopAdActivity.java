package kr.co.picklecode.crossmedia;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import bases.BaseActivity;

public class PopAdActivity extends BaseActivity {

    private Button btn_close;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pop_ad);

        init();
    }

    private void init(){
        btn_close = findViewById(R.id.closeBtn);
        setClick(btn_close);
    }

    @Override
    public void onClick(View v){
        switch(v.getId()){
            case R.id.closeBtn:
                finish();
                break;
            default: break;
        }
    }

}
