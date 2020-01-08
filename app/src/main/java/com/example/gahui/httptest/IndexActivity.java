package com.example.gahui.httptest;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class IndexActivity extends AppCompatActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.index);
        System.out.println("进来");
    }
}
