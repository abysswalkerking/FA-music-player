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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

public class FaPlayer extends Activity {
    public class FaUnit
    {
        public View view;
        public String audioNameL;
        public String audioNameR;
        public FaUnit(View view, String audioNameL, String audioNameR)
        {
            this.view = view;
            this.audioNameL = audioNameL;
            this.audioNameR = audioNameR;
        }
    }
    private int faUnitCount = 4;
    private ArrayList<FaUnit> faList = new ArrayList<>();
    private DBHelper dbHelper;
    private SQLiteDatabase mDatabase;
    private Spinner spinner = null;
    private MediaPlayer mediaPlayer = null;
    private int pressedColumn = 0;
    private int pressedRow = 0;
    private ContentValues folder2image = null;
    private LinearLayout layoutFaUnits;
    private static final String DEFAULT_AUDIO = "乐器/【乐器】搓X电音.mp3";
    private EditText countInput;
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
        layoutFaUnits = findViewById(R.id.layout_fa_units);
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

        // 初始化每个按键的资源与描述
        resetAudio();
        countInput = findViewById(R.id.countInput);
        // 点击重置每个按键的资源与描述
        findViewById(R.id.resetAudio).setOnClickListener(v -> resetAudio());
        // 一键播放所有单元中的音频文件按钮
        findViewById(R.id.playAllAudio).setOnClickListener(v -> {
            // 根据延时
            spinner = (Spinner) findViewById(R.id.spinner);
            System.out.println(spinner.getSelectedItem());
             //播放fa_audioName中所有音乐文件
            long delayTime = Long.parseLong((String)spinner.getSelectedItem());
            long counter = 1;
            for (int i = 0; i < faList.size(); i++)
            {
                final String audioNameL = faList.get(i).audioNameL;
                new Handler().postDelayed(() -> playAudio(audioNameL), delayTime * counter);    //延时1s执行
                counter++;
                final String audioNameR = faList.get(i).audioNameR;
                new Handler().postDelayed(() -> playAudio(audioNameR), delayTime * counter);    //延时1s执行
                counter++;
            }
        });

        // 一键更改当前所有单元中音乐
        findViewById(R.id.changeAllAudio).setOnClickListener(v -> {
            Intent intent = new Intent(FaPlayer.this,SelectAudio.class);
            // 跳转
            startActivityForResult(intent, 11023);
        });
        findViewById(R.id.modifyCount).setOnClickListener(v ->
        {
            int count = Integer.parseInt(countInput.getText().toString());
            if (faUnitCount != count)
            {
                faUnitCount = count;
                resetAudio();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        switch (requestCode) {
            case 11022: {
                String audioName = intent.getStringExtra("audio_name");
                String audioDescription = intent.getStringExtra("audio_description");
                int imageId = intent.getIntExtra("image_id", -1);
                FaUnit faUnit = faList.get(pressedRow);

                LinearLayout linearLayout;
                if (pressedColumn == 0)
                {
                    linearLayout = faUnit.view.findViewById(R.id.L1);
                    faUnit.audioNameL = audioName;
                }
                else
                {
                    linearLayout = faUnit.view.findViewById(R.id.R1);
                    faUnit.audioNameR = audioName;
                }
                ImageView im = getUnitImage(pressedRow, pressedColumn);
                TextView tv = getUnitText(pressedRow, pressedColumn);
                setImage(im, imageId); // 图片
                setText(tv, audioDescription); // 文本
                setAudio(linearLayout, audioName); // 单击事件
                setLongClick(pressedRow);
                break;
            }
            case 11023: {
                System.out.println("ddf");
                // 获取传回的值
                String audioName = intent.getStringExtra("audio_name");
                String audioDescription = intent.getStringExtra("audio_description");
                int imageId = intent.getIntExtra("image_id", -1);
                // 给每个单元分配初始图片资源、音乐资源、音乐描述
                for (int i = 0; i < faList.size(); i++) {
                    for (int j = 0; j < 2; j++) { // row
                        if (j == 0)
                        {
                            faList.get(i).audioNameL = audioName;
                            setAudio((LinearLayout) faList.get(i).view.findViewById(R.id.L1), audioName);
                        }
                        else
                        {
                            faList.get(i).audioNameR = audioName;
                            setAudio((LinearLayout) faList.get(i).view.findViewById(R.id.R1), audioName);
                        }
                        // 修改图片
                        setImage(getUnitImage(i, j), imageId);
                        // 修改描述
                        setText(getUnitText(i, j), audioDescription);
                        // 长按事件
                        setLongClick(i);
                    }
                }
                break;
            }
            default:break;
        }
    }

    private void resetAudio(){
        // 给每个单元分配初始图片资源、音乐资源、音乐描述
        faList.clear();
        layoutFaUnits.removeAllViewsInLayout();
        for (int i = 0; i < faUnitCount; i++)
        {
            View view = View.inflate(this, R.layout.fa_line_layout, null);
            layoutFaUnits.addView(view);
            faList.add(new FaUnit(view, DEFAULT_AUDIO, DEFAULT_AUDIO));
        }
        for (int i = 0; i < faList.size(); i++) {
            for (int j = 0; j < 2; j++) { // row
                if (j == 0)
                {
                    faList.get(i).audioNameL = DEFAULT_AUDIO;
                    setAudio((LinearLayout) faList.get(i).view.findViewById(R.id.L1), DEFAULT_AUDIO);
                }
                else
                {
                    faList.get(i).audioNameR = DEFAULT_AUDIO;
                    setAudio((LinearLayout) faList.get(i).view.findViewById(R.id.R1), DEFAULT_AUDIO);
                }
                ContentValues cv = dbHelper.queryByFileName(DEFAULT_AUDIO);
                String audioDescription = (String) cv.get("audio_description");
                int imageId = (int)cv.get("image_id");
                // 修改图片
                setImage(getUnitImage(i, j), imageId);
                // 修改描述
                setText(getUnitText(i, j), audioDescription);
                // 长按事件
                setLongClick(i);
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

    private void setLongClick(final int index){
        LinearLayout linearLayoutL = faList.get(index).view.findViewById(R.id.L1);
        LinearLayout linearLayoutR = faList.get(index).view.findViewById(R.id.R1);
        final String audioNameL = faList.get(index).audioNameL;
        final String audioNameR = faList.get(index).audioNameR;
        // 长按跳转到音频选择界面
        linearLayoutL.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                pressedColumn = 0;
                pressedRow = index;
                Intent intent = new Intent(FaPlayer.this,SelectAudio.class);
                // 传值 跳转
                intent.putExtra("audio_name", audioNameL);//高亮这个文件名对应的音频单元
                startActivityForResult(intent, 11022);
                return true;
            }
        });
        linearLayoutR.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                pressedColumn = 1;
                pressedRow = index;
                Intent intent = new Intent(FaPlayer.this,SelectAudio.class);
                // 传值 跳转
                intent.putExtra("audio_name", audioNameR);//高亮这个文件名对应的音频单元
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
    private ImageView getUnitImage(int unitRow, int unitColumn){
        ImageView imageView = null;
        switch (unitColumn){
            case 0:
                imageView = (ImageView)((LinearLayout) faList.get(unitRow).view.findViewById(R.id.L1)).getChildAt(1);
                break;
            case 1:
                imageView = (ImageView)((LinearLayout) faList.get(unitRow).view.findViewById(R.id.R1)).getChildAt(0);
                break;
            default:break;
        }
        return imageView;
    }

    // 根据行列获得TextView对象，根据每一列的小单元的内部布局而定
    private TextView getUnitText(int unitRow, int unitColumn){
        TextView textView = null;
        switch (unitColumn){
            case 0:
                textView = (TextView)((LinearLayout) faList.get(unitRow).view.findViewById(R.id.L1)).getChildAt(0);
                break;
            case 1:
                textView = (TextView)((LinearLayout) faList.get(unitRow).view.findViewById(R.id.R1)).getChildAt(1);
                break;
            default:break;
        }
        return textView;
    }
}
