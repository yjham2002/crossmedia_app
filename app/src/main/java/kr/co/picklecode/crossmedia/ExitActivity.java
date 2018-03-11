package kr.co.picklecode.crossmedia;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import bases.BaseActivity;

public class ExitActivity extends BaseActivity {

    private Button btn_exit;
    private Button btn_cancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exit);

        initView();
    }

    private void initView(){
        btn_exit = findViewById(R.id.exit_action);
        btn_cancel = findViewById(R.id.btn_cancel);

        setClick(btn_cancel, btn_exit);
    }

    @Override
    public void onClick(View v){
        switch (v.getId()){
            case R.id.exit_action:{
                System.exit(0);
            }
                break;
            case R.id.btn_cancel:{
                showToast("취소 버튼 선택됨.");
            }
            break;
            default: break;
        }
    }

}
