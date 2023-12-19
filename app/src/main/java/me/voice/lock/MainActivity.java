package me.voice.lock;

import android.Manifest;
import android.annotation.SuppressLint;

import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import android.app.Activity;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.media.AudioManager;
import android.content.Context;
import android.content.Intent;
import android.widget.Button;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends Activity {
    boolean isRefuse;
    boolean volumeLock = false;
    int originVolume = 0;
    int maxVolume = 0;
    int setVolume = 0;
    @SuppressLint({"SetTextI18n", "StringFormatMatches"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 先判断有没有权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !isRefuse) {// android 11  且 不是已经被拒绝
            if (!Environment.isExternalStorageManager()) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, 1024);
            }
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, 1);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, 1);
        }
        //去掉标题栏
        setContentView(R.layout.activity_main);
        //防止重新加载
        if (!this.isTaskRoot()) {
            Intent mainIntent = getIntent();
            String action = mainIntent.getAction();
            if (mainIntent.hasCategory(Intent.CATEGORY_LAUNCHER) && action.equals(Intent.ACTION_MAIN)) {
                finish();
                return;
            }}
        //获取音频服务
        AudioManager am = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        originVolume = am.getStreamVolume(AudioManager.STREAM_RING);
        maxVolume = am.getStreamMaxVolume(AudioManager.STREAM_RING);
        TextView tV = findViewById(R.id.textVoice);
        TextView vV = findViewById(R.id.voiceView);
        EditText eV = findViewById(R.id.editVoice);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            tV.setText(String.format(this.getString(R.string.VoiceLabel), am.getStreamMinVolume(AudioManager.STREAM_RING), maxVolume));
        }
        //启动线程循环设置音量
        new Thread(() -> {
            //这儿是耗时操作，完成之后更新UI；
            while(true){
                setVolume = volumeLock ? maxVolume : originVolume;
                runOnUiThread(() -> {
                    am.setStreamVolume(AudioManager.STREAM_RING, setVolume, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                    //更新UI
                    vV.setText(String.format(this.getString(R.string.VoiceNow), setVolume));
                });
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ignored) {}
            }
        }).start();
        Button bt = findViewById(R.id.ButtonSetting);
        bt.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                String StrOrigin = eV.getText().toString();
                if (!StrOrigin.equals("")){
                    originVolume = Integer.parseInt(StrOrigin);
                }
                eV.setText("");
            }
        });
        Button vi = findViewById(R.id.ButtonLock);
        vi.setOnClickListener(new View.OnClickListener(){
             public void onClick(View v) {
                volumeLock = !volumeLock;
                if (volumeLock) {
                    vi.setText(R.string.VoiceLocked);
                } else {
                    vi.setText(R.string.VoiceUnlock);
                }
            }
        });
    }

    @Override
    protected void onRestart(){
        super.onRestart();
    }

    // 带回授权结果
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1024 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // 检查是否有权限
            isRefuse = !Environment.isExternalStorageManager();
        }
    }
//    public static void saveLog(String data){
//        String path = Environment.getExternalStorageState() + "/voice.txt";
//        try {
//            FileOutputStream fos = new FileOutputStream(path);
//            fos.write(data.getBytes());
//            fos.flush();
//            fos.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
}


