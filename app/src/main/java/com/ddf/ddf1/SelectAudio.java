package com.ddf.ddf1;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.IOException;
import java.util.List;

public class SelectAudio extends Activity {
    private DBHelper dbHelper;
    private int selectedIndex = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_audio);

        init();
    }

    private void init(){
        dbHelper = new DBHelper(this);
        Intent intent = getIntent();
        String pressedAudioName = intent.getStringExtra("audio_name");
        // 将所有查询出来的音频文件显示在linearlayout中，要可滑动
        final List<ContentValues>list = dbHelper.queryAll();
        final LinearLayout window = findViewById(R.id.window);
        // clear
        window.removeAllViews();
        window.setBackgroundColor(ContextCompat.getColor(SelectAudio.this, R.color.unfocused));
        // 动态增加单元
        for (int i = 0; i < list.size(); i++){
            LinearLayout smallWindow = new LinearLayout(this);
            // 设置linearlayout
            smallWindow.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, Change.dp2px(this, 60.0f));
            lp.gravity = Gravity.LEFT; // 内部组件向左靠拢
            lp.setMargins(20,10,10,10);// 边缘分隔
            smallWindow.setLayoutParams(lp);
            final String audioName = (String) list.get(i).get("audio_name");// 音频文件名
            if(audioName.equals(pressedAudioName)){
                // 选中在上一个activity中长按的音频，使其高亮
                selectedIndex = i;
                smallWindow.setBackgroundColor(ContextCompat.getColor(SelectAudio.this, R.color.focused));
            }
            final int j = i;
            smallWindow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 改变背景色
                    window.getChildAt(selectedIndex).setBackgroundColor(ContextCompat.getColor(SelectAudio.this, R.color.unfocused));
                    selectedIndex = j;
                    window.getChildAt(selectedIndex).setBackgroundColor(ContextCompat.getColor(SelectAudio.this, R.color.focused));
                    // 播放音频文件
                    try {
                        AssetFileDescriptor fd = getAssets().openFd(audioName);
                        MediaPlayer mediaPlayer = new MediaPlayer();
                        mediaPlayer.setDataSource(fd.getFileDescriptor(), fd.getStartOffset(), fd.getLength());
                        // 音乐播放结束后释放资源
                        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                            @Override
                            public void onCompletion(MediaPlayer mp) {
                                if (mp != null) {
                                    mp.stop();
                                    mp.reset();
                                    mp.release();
                                    mp = null;
                                }
                            }
                        });
                        mediaPlayer.prepare();
                        // 播放
                        mediaPlayer.start();
                    }
                    catch (IOException e){
                        e.printStackTrace();
                    }
                }
            });
            // imageview
            ImageView im = new ImageView(this);
            Drawable drawable = ContextCompat.getDrawable(this, (int)list.get(i).get("image_id"));
            LinearLayout.LayoutParams im_pm = new LinearLayout.LayoutParams(Change.dp2px(this, 60.0f), LinearLayout.LayoutParams.MATCH_PARENT);
            im.setLayoutParams(im_pm);
            im.setImageDrawable(drawable);
            // textview
            TextView tv = new TextView(this);
            tv.setBackgroundColor(ContextCompat.getColor(SelectAudio.this, R.color.unfocused));

            // 获取屏幕宽度(dp)
            WindowManager wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
            DisplayMetrics dm = new DisplayMetrics();
            wm.getDefaultDisplay().getMetrics(dm);
            int width = dm.widthPixels;// 屏幕宽度（像素）
            float density = dm.density;//屏幕密度（0.75 / 1.0 / 1.5）
            float screenWidth = width/density;//屏幕宽度(dp)

            LinearLayout.LayoutParams tv_pm = new LinearLayout.LayoutParams(Change.dp2px(this, screenWidth-90),LinearLayout.LayoutParams.MATCH_PARENT);
            tv_pm.setMargins(10,10,10,10);
            tv.setGravity(Gravity.CENTER_VERTICAL); // 文字垂直居中
            tv.setLayoutParams(tv_pm);
            tv.setText((String)list.get(i).get("audio_description"));
            // 组合
            smallWindow.addView(im);
            smallWindow.addView(tv);
            window.addView(smallWindow);
        }

        findViewById(R.id.confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 确认后跳转回发声界面
                Intent intent = new Intent();
                intent.putExtra("audio_name", (String)list.get(selectedIndex).get("audio_name"));
                intent.putExtra("audio_description", (String)list.get(selectedIndex).get("audio_description"));
                intent.putExtra("image_id", (int)list.get(selectedIndex).get("image_id"));
                setResult(11022, intent);
                finish();
            }
        });
    }
}
