package com.example.freak.ltx;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    RadioGroup g1;
    RadioButton spd;
    Button send;
    EditText out,in;
    static final int CAMERA_REQUEST=50;
    boolean Camperm;
    Handler handler;
    Runnable runnable;
    String fin_data,temp_data;
    int in_count,flash_status;
    Integer speed;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        g1= findViewById(R.id.speed);
        send=findViewById(R.id.button);
        in=findViewById(R.id.editText);
        out=findViewById(R.id.editText2);
        Camperm=ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;
        in_count=0;
        flash_status=0;
        temp_data="";
        handler= new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
             if(in_count<fin_data.length()){
             char ch=fin_data.charAt(in_count);
             if(ch=='1'&& flash_status==0){
                 flashLightOn();
                 flash_status=1;}
             else if(ch=='0'){
                 flash_status=0;
                 flashLightOff();}
             in_count++;
             temp_data=temp_data+ch;
             out.setText(temp_data);
             // DEBUG PURPOSE 50ms is considered as 1sec to visuvalize
             if(speed==50)
                 speed=1000;
             handler.postDelayed(runnable,speed);
             }
             else
                 send.setEnabled(true);

            }
        };
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!Camperm) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST);
                }

                else {

                    int id = g1.getCheckedRadioButtonId();
                    spd = (RadioButton) findViewById(id);
                    String temp= spd.getText().toString();
                    // Getting tx speed
                    if(temp.length()>3)
                    speed=Integer.valueOf(temp.substring(0,2));
                    else
                        speed=Integer.valueOf(temp.substring(0,1));
                    if (in.getText().toString().isEmpty()) {
                        Toast.makeText(MainActivity.this, "Enter Data", Toast.LENGTH_SHORT).show();
                    } else
                        transmit();
                }
            }
        });

    }
    protected void flashLightOn() {
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

        try {
            String cameraId = cameraManager.getCameraIdList()[0];
            cameraManager.setTorchMode(cameraId, true);
        }
        catch (CameraAccessException e) {
        }
    }
    protected void flashLightOff() {
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

        try {
            String cameraId = cameraManager.getCameraIdList()[0];
            cameraManager.setTorchMode(cameraId, false);
        }
        catch (CameraAccessException e) {
        }
    }

        protected void transmit(){
        send.setEnabled(false);
        in_count=0;
        temp_data="";
        fin_data="";
        // Preping Data:
        String data=in.getText().toString();
        for(int i=0;i<data.length();i++){
            Integer dat= Integer.valueOf((int)data.charAt(i));
            String dat_byte=Integer.toBinaryString(dat);
            //Padding to make 8bit numbers
            while(dat_byte.length()<7){
                dat_byte="0"+dat_byte;
            }
            //Adding Start and end bit
            dat_byte="1"+dat_byte+"0";
            fin_data=fin_data+dat_byte;
        }
        handler.postDelayed(runnable,0);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,  int[] grantResults) {
        switch(requestCode) {
            case CAMERA_REQUEST :
                if (grantResults.length > 0  &&  grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    send.setEnabled(true);
                    Camperm=true;
                } else {
                    Toast.makeText(MainActivity.this, "Permission Denied for the Camera", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

}
