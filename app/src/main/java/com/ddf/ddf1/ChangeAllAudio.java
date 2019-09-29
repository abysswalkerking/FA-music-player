package com.ddf.ddf1;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class ChangeAllAudio extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_audio);

        init();
    }

    private void init(){
        findViewById(R.id.confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 确认后跳转回发声界面
                Intent intent = new Intent();
//                intent.putExtra("audio_name", (String)list.get(selectedIndex).get("audio_name"));
//                intent.putExtra("audio_description", (String)list.get(selectedIndex).get("audio_description"));
//                intent.putExtra("image_id", (int)list.get(selectedIndex).get("image_id"));
                setResult(11023, intent);
                finish();
            }
        });
    }
}
