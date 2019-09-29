package com.ddf.ddf1;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class Welcome extends Activity {
    private int counter = 0;
    final private int threshold = 5;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome);

        Button btn_unlock = findViewById(R.id.unlock);
        btn_unlock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { // 按一定次数才跳转，防止误点
                counter++;
                if (counter >= threshold){
                    // TODO Auto-generated method stub
                    counter = 0;
                    Intent intent = new Intent(Welcome.this,FaPlayer.class);
                    startActivity(intent);
                }
            }
        });
    }
}
