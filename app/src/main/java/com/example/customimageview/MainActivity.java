package com.example.customimageview;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.pureimageview.PureImageView;
import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        PureImageView pureImageView = findViewById(R.id.test_image);
        Picasso.with(this).load("https://ss0.bdstatic.com/70cFuHSh_Q1YnxGkpoWK1HF6hhy/it/u=1819216937,2118754409&fm=26&gp=0.jpg")
                .into(pureImageView);
    }
}