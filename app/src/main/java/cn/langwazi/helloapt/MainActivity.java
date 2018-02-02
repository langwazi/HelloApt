package cn.langwazi.helloapt;

import android.annotation.SuppressLint;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import cn.langwazi.annotations.BindView;
import cn.langwazi.hello.Hello;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.tv1)
    TextView tvBind1;

    @BindView(R.id.tv2)
    TextView tvBind2;

    @BindView(R.id.tv3)
    TextView tvBind3;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Hello.bind(this);

        tvBind1.setText("hello bind1");
        tvBind2.setText("hello bind2");
        tvBind3.setText("hello bind3");

    }
}
