package me.voice.lock;

import android.annotation.SuppressLint;

import android.os.Bundle;

import android.app.Activity;
import android.view.KeyEvent;
import android.os.Build;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.media.AudioManager;
import android.content.Context;
import android.content.Intent;
import android.view.Window;
import android.widget.Button;

public class MainActivity extends Activity {
    public AudioManager am;
    private boolean volumeLock = false;
    private int originVolume = 0;
    private int maxVolume = 0;
    TextView vV;
    //需要点击次数满足才会退出
    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //去掉标题栏
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        //防止重新加载
        if (!this.isTaskRoot()) {
            Intent mainIntent = getIntent();
            String action = mainIntent.getAction();
            if (mainIntent.hasCategory(Intent.CATEGORY_LAUNCHER) && action.equals(Intent.ACTION_MAIN)) {
                finish();return;
            }}
        //获取音频服务
        am = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        originVolume = am.getStreamVolume(AudioManager.STREAM_RING);
        maxVolume = am.getStreamMaxVolume(AudioManager.STREAM_RING);
        vV = findViewById(R.id.voiceView);

        TextView tV = findViewById(R.id.textVoice);
        tV.setText("输入一个介于 " + am.getStreamMinVolume(AudioManager.STREAM_RING) + " - " + maxVolume + " 原始音量值");
        EditText eV = findViewById(R.id.editVoice);

        //启动线程循环设置音量
        new Thread(() -> {
            //这儿是耗时操作，完成之后更新UI；
            while(true){
                final int m = volumeLock ? maxVolume : originVolume;
                runOnUiThread(() -> {
                    //更新UI
                    am.setStreamVolume(AudioManager.STREAM_RING, m, AudioManager.FLAG_PLAY_SOUND);
                    vV.setText("当前音量：" + am.getStreamVolume(AudioManager.STREAM_RING));
                });

                try {
                    Thread.sleep(500);
                } catch (InterruptedException ignored) {}
            }
        }).start();
        final Button bt = findViewById(R.id.activitymainButton);
        bt.setOnClickListener(p1 -> {
            if (!eV.getText().toString().equals("")){
                originVolume = Integer.parseInt(eV.getText().toString());
            }
            eV.setText("");
        });
        final Button vi = findViewById(R.id.activitymainVoice);
        vi.setOnClickListener(p1 -> {
            volumeLock = !volumeLock;
            if(volumeLock){
                vi.setText("解锁音量");
            }
            else{
                vi.setText("锁定音量");
            }
        });
    }

    @Override
    protected void onRestart(){
        super.onRestart();
    }

}


