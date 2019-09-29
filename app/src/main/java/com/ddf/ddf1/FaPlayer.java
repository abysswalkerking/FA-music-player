package com.ddf.ddf1;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.IOException;
import java.util.Map;

public class FaPlayer extends Activity {
    private DBHelper dbHelper;
    private SQLiteDatabase mDatabase;
    private Spinner spinner = null;
    private MediaPlayer mediaPlayer = null;
    private int pressedColumn = 0;
    private int pressedRow = 0;
    private int[][] fa_unit = {{R.id.L1, R.id.L2, R.id.L3, R.id.L4}, {R.id.R1, R.id.R2, R.id.R3, R.id.R4} };//第1维是列，第2维是行
    private String[][] fa_audioName = {{"乐器/【乐器】击臀声.mp3","乐器/【乐器】关铁门.mp3","乐器/【乐器】牛叫.mp3","乐器/【乐器】兄贵喘息声.mp3"},{"乐器/【乐器】啪~.mp3","乐器/【乐器】嗷啊~.mp3","乐器/【乐器】enenen~~.mp3","乐器/【乐器】搓X电音.mp3"}};
    private String[][] fa_ori_audioName = {{"乐器/【乐器】击臀声.mp3","乐器/【乐器】关铁门.mp3","乐器/【乐器】牛叫.mp3","乐器/【乐器】兄贵喘息声.mp3"},{"乐器/【乐器】啪~.mp3","乐器/【乐器】嗷啊~.mp3","乐器/【乐器】enenen~~.mp3","乐器/【乐器】搓X电音.mp3"}};
    private ContentValues folder2image = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fa_player);

        init();
    }

    // 初始化函数
    private void init(){
        dbHelper = new DBHelper(this);
        mDatabase = dbHelper.getWritableDatabase();

        // 文件夹中音乐对应的图片
        folder2image = new ContentValues();
        folder2image.put("BILLY", R.drawable.billy1);
        folder2image.put("VAN", R.drawable.van1);
        folder2image.put("乐器", R.drawable.van3);
        folder2image.put("处刑", R.drawable.condemn);
        folder2image.put("木吉", R.drawable.kazuya);
        folder2image.put("杂项", R.drawable.other2);
        folder2image.put("魔男", R.drawable.cantonese);

        // 向数据库中写入assets各目录下的音频文件信息
        // 遍历所有音频文件目录
        for (Map.Entry<String, Object> item : folder2image.valueSet())
        {
            String dir = item.getKey();
            try {
                // 遍历某一目录下所有音频文件
                String[] audioNames = getAssets().list(dir);
                for(String name: audioNames){
                    String description = name.substring(0, name.lastIndexOf("."));
                    dbHelper.insertData(dir +"/"+ name, description, folder2image.getAsInteger(dir));
                }
            }
            catch (IOException e){
                e.printStackTrace();
            }
        }

        for (int i = 0; i < fa_unit.length; i++){ // column
            for (int j = 0; j < fa_unit[i].length; j++) { // row
                setLongClick(i, j);
            }
        }

        // 初始化每个按键的资源与描述
        resetAudio();
        // 点击重置每个按键的资源与描述
        findViewById(R.id.resetAudio).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetAudio();
            }
        });
        // 一键播放所有单元中的音频文件按钮
        findViewById(R.id.playAllAudio).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 根据延时
                spinner = (Spinner) findViewById(R.id.spinner);
                System.out.println(spinner.getSelectedItem());
                 //播放fa_audioName中所有音乐文件
                long delayTime = Long.parseLong((String)spinner.getSelectedItem());
                long counter = 1;
                for (int i = 0; i < fa_audioName.length; i++){ // column
                    for (int j = 0; j < fa_audioName[i].length; j++) { // row
                        final String audioName = fa_audioName[i][j];
                        //playAudio(audioName);
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                playAudio(audioName);
                            }
                        }, delayTime * counter);    //延时1s执行
                        counter++;
                    }
                }
            }
        });

        // 一键更改当前所有单元中音乐
        findViewById(R.id.changeAllAudio).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FaPlayer.this,ChangeAllAudio.class);
                // 跳转
                startActivityForResult(intent, 11023);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        switch (resultCode) {
            case 11022:
                String audioName = intent.getStringExtra("audio_name");
                String audioDescription = intent.getStringExtra("audio_description");
                int imageId = intent.getIntExtra("image_id", -1);
                LinearLayout linearLayout = findViewById(fa_unit[pressedColumn][pressedRow]);
                ImageView im = getUnitImage(pressedColumn, pressedRow);
                TextView tv = getUnitText(pressedColumn, pressedRow);
                setImage(im, imageId); // 图片
                setText(tv, audioDescription); // 文本
                fa_audioName[pressedColumn][pressedRow] = audioName; // 记录
                setAudio(linearLayout, audioName); // 单击事件
                setLongClick(pressedColumn, pressedRow);
                break;
            case 11023:
                System.out.println("ddf");
                // 获取传回的值
//                String audioName = intent.getStringExtra("audio_name");
//                String audioDescription = intent.getStringExtra("audio_description");
//                int imageId = intent.getIntExtra("image_id", -1);
                audioName = "乐器/【乐器】搓X电音.mp3";
                audioDescription = "【乐器】搓X电音.mp3";
                imageId = (int)dbHelper.queryByFileName(audioName).get("image_id");
                // 给每个单元分配初始图片资源、音乐资源、音乐描述
                for (int i = 0; i < fa_unit.length; i++){ // column
                    for (int j = 0; j < fa_unit[i].length; j++){ // row
                        fa_audioName[i][j] = audioName;
                        // 修改图片
                        setImage(getUnitImage(i, j), imageId);
                        // 修改描述
                        setText(getUnitText(i, j), audioDescription);
                        // 点击播放音乐
                        setAudio((LinearLayout) findViewById(fa_unit[i][j]), audioName);
                        // 长按事件
                        setLongClick(i, j);
                    }
                }
                break;
            default:break;
        }
    }

    private void resetAudio(){
        // 给每个单元分配初始图片资源、音乐资源、音乐描述
        for (int i = 0; i < fa_audioName.length; i++){ // column
            for (int j = 0; j < fa_audioName[i].length; j++){ // row
                fa_audioName[i][j] = fa_ori_audioName[i][j];
                // 查询
                String audioName = fa_audioName[i][j];
                ContentValues cv = dbHelper.queryByFileName(audioName);
                String audioDescription = (String) cv.get("audio_description");
                int imageId = (int)cv.get("image_id");
                // 修改图片
                setImage(getUnitImage(i, j), imageId);
                // 修改描述
                setText(getUnitText(i, j), audioDescription);
                // 点击播放音乐
                setAudio((LinearLayout) findViewById(fa_unit[i][j]), audioName);
                // 长按事件
                setLongClick(i, j);
            }
        }
    }

    private void setImage(ImageView imageView, int imageId){
        // 设置图片文件
        Drawable drawable = ContextCompat.getDrawable(FaPlayer.this, imageId);
        imageView.setImageDrawable(drawable);
    }

    private void setText(TextView textView, String content){
        // 设置描述文本
        textView.setText(content);
    }

    private void setAudio(LinearLayout linearLayout, final String file){
        // 设置点击后要播放的音频文件
        linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playAudio(file);
            }
        });
    }

    private void setLongClick(final int column, final int row){
        LinearLayout linearLayout = findViewById(fa_unit[column][row]);
        final String audioName = fa_audioName[column][row];
        // 长按跳转到音频选择界面
        linearLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                pressedColumn = column;
                pressedRow = row;
                Intent intent = new Intent(FaPlayer.this,SelectAudio.class);
                // 传值 跳转
                intent.putExtra("audio_name", audioName);//高亮这个文件名对应的音频单元
                startActivityForResult(intent, 11022);
                return true;
            }
        });
    }

    private void playAudio(String file){
        // 播放音频文件
        try {
            AssetFileDescriptor fd = getAssets().openFd(file);
            mediaPlayer = new MediaPlayer();
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

    // 根据行列获得ImageView对象，根据每一列的小单元的内部布局而定
    private ImageView getUnitImage(int unitColumn, int unitRow){
        ImageView imageView = null;
        switch (unitColumn){
            case 0:
                imageView = (ImageView)((LinearLayout) findViewById(fa_unit[unitColumn][unitRow])).getChildAt(1);
                break;
            case 1:
                imageView = (ImageView)((LinearLayout) findViewById(fa_unit[unitColumn][unitRow])).getChildAt(0);
                break;
            default:break;
        }
        return imageView;
    }

    // 根据行列获得TextView对象，根据每一列的小单元的内部布局而定
    private TextView getUnitText(int unitColumn, int unitRow){
        TextView textView = null;
        switch (unitColumn){
            case 0:
                textView = (TextView) ((LinearLayout) findViewById(fa_unit[unitColumn][unitRow])).getChildAt(0);
                break;
            case 1:
                textView = (TextView)((LinearLayout) findViewById(fa_unit[unitColumn][unitRow])).getChildAt(1);
                break;
            default:break;
        }
        return textView;
    }
}
