package com.wacked.smarttourist.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.wacked.smarttourist.R;


//Задумка для дальнейшей разработки, выбор маршрутов из предложенных
public class RouteChoose extends AppCompatActivity {

    Button route1;
    Button route2;
    Button route3;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.route_choose_activity);

        route1 = findViewById(R.id.route1);
        route1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RouteChoose.this, MainMapsActivity.class));
            }
        });


        route2 = findViewById(R.id.route2);
        route2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RouteChoose.this, MainMapsActivity.class));
            }
        });


        route3 = findViewById(R.id.route3);
        route3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RouteChoose.this, MainMapsActivity.class));
            }
        });
    }
}