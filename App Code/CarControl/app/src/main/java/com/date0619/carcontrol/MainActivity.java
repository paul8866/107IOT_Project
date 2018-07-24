package com.date0619.carcontrol;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;


public class MainActivity extends AppCompatActivity {

    private Context context;
    private Switch btSwitch;
    private ListView btListView;
    private BluetoothAdapter btAdapter;
    private static final int Resquest_Enable_BT = 2;
    private Set<BluetoothDevice> allBTDevices;
    private ArrayList<String> btDeviceList;
    private ArrayAdapter<String> adapter;
    private String itemData;
    private Intent intent;
    private boolean receiverFlag = false;
    private final int Permission_Request_code=100;
    private int permissionCheck;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        context = this;

        //檢查是否取得語音權限
        permissionCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO);
        //當沒有權限時跳出詢問視窗
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    1);
        }

        btSwitch = (Switch) findViewById(R.id.btSwitch);
        btListView = (ListView) findViewById(R.id.listViewID);
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (btAdapter == null) {
            finish();
        } else if (!btAdapter.isEnabled()) {
            Intent btIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(btIntent, Resquest_Enable_BT);
        }

        btSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    allBTDevices = btAdapter.getBondedDevices();      //取得曾經配對過的藍牙裝置
                    btDeviceList = new ArrayList<String>();
                    if (allBTDevices.size() > 0) {
                        for (BluetoothDevice device : allBTDevices) {
                            btDeviceList.add("Paired :  " + device.getName() + "\n" + device.getAddress());
                        }
                        adapter = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, btDeviceList);
                        btListView.setAdapter(adapter);
                    }
                } else {
                    if (adapter != null) {
                        adapter.clear();
                        adapter.notifyDataSetChanged();     //重新刷新藍牙列表
                    }
                }
            }
        });

        btListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                //如果第一次拒絕語音權限，則出現Dialog視窗再次詢問
                if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                        Manifest.permission.RECORD_AUDIO)) {
                    //創建Dialog解釋視窗
                    new AlertDialog.Builder(MainActivity.this)
                            .setMessage("如不允許將無法使用語音辨識功能！")
                            .setPositiveButton("再次確認權限", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    ActivityCompat.requestPermissions(MainActivity.this,
                                            new String[]{Manifest.permission.RECORD_AUDIO},
                                            1);

                                }
                            })
                            .setNegativeButton("關閉程式", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    finish();
                                }
                            })
                            .show();
                } else {
                    //如果已取得語音權限則進入下一頁
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.RECORD_AUDIO},
                            1);

                    itemData = parent.getItemAtPosition(position).toString();
                    Toast.makeText(context, itemData, Toast.LENGTH_SHORT).show();
                    intent = new Intent(context, modeActivity.class);
                    intent.putExtra("btData", itemData);
                    startActivity(intent);
                }
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode){
            //詢問是否打開藍牙，如不允許則關閉程式
            case Resquest_Enable_BT:
                if (resultCode==RESULT_CANCELED){
                    Toast.makeText(context,"Deny BT enable.",Toast.LENGTH_SHORT).show();
                    finish();
                }else if (resultCode==RESULT_OK){
                    Toast.makeText(context,"Turn on BT.",Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.main_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            //搜尋附近的藍芽裝置
            case R.id.bt_search:
                btAdapter.startDiscovery();
                IntentFilter filter=new IntentFilter(BluetoothDevice.ACTION_FOUND);
                receiverFlag=true;
                registerReceiver(mRecaiver,filter);
                Toast.makeText(context,"Start to scan BT device.",Toast.LENGTH_SHORT).show();
                break;

            //讓自身藍牙裝置可被其他裝置發現
            case R.id.bt_discovery:
                btAdapter.cancelDiscovery();
                Intent intent=new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION,100);
                startActivity(intent);
                Toast.makeText(context,"Start to be found.",Toast.LENGTH_SHORT).show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    BroadcastReceiver mRecaiver=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action=intent.getAction();

            if (action.equals(BluetoothDevice.ACTION_FOUND)){
                BluetoothDevice device=intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (adapter==null){
                    Toast.makeText(context,"Please switch on.",Toast.LENGTH_SHORT).show();
                }else {
                    adapter.add("Found :   "+device.getName()+"\n"+device.getAddress());
                }
            }
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case Permission_Request_code:
                if (grantResults[0]==PERMISSION_GRANTED){
                }else {
                }
                return;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0); //關閉應用程式

//        btAdapter.cancelDiscovery();
//        if (mRecaiver!=null){
//            if (receiverFlag){
//                unregisterReceiver(mRecaiver);
//                receiverFlag=false;
//            }
//        }
    }
}
