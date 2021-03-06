package com.kcrason.kselectimages;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.kcrason.kselectimages.ui.ReleaseImageActivity;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.btn_select_image)
    void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_select_image:
                startActivity(new Intent(this, ReleaseImageActivity.class));
                break;
        }
    }
}
