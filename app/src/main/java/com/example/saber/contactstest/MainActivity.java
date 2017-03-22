package com.example.saber.contactstest;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private List<String> contacts = new ArrayList<>();
    private ListView lvContacts;
    private ArrayAdapter<String> adapter;
    private String number;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lvContacts = (ListView) findViewById(R.id.lv_contacts);

        adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,contacts);
        lvContacts.setAdapter(adapter);

        //运行时权限
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{ Manifest.permission.READ_CONTACTS},1);
        }else {
            readContacts();
        }

        /**
         * 打电话
         */
        lvContacts.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                number = contacts.get(position).substring(contacts.get(position).indexOf("\n"));

                //运行时权限
                if(ContextCompat.checkSelfPermission(MainActivity.this,Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.CALL_PHONE},2);
                }else{
                    call(number);
                }
            }
        });

    }

    /**
     * 拨打电话
     * @param number
     */
    private void call(String number) {

        try{
            Intent intent = new Intent(Intent.ACTION_CALL);
            intent.setData(Uri.parse("tel:"+number));
            startActivity(intent);
        }catch (SecurityException e){
            Log.e("info", "call:SecurityException"+e.toString() );
        }
    }

    /**
     * 读取联系人数据
     */
    private void readContacts() {
        Cursor c = null;
        ContentResolver contentResolver = this.getContentResolver();
        try {
            c = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,null,null,null);
            if(c != null){
                while(c.moveToNext()){
                    //获取联系人姓名
                    String name = c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                    //获取手机号
                    String number = c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                    contacts.add(name+"\n"+number);
                }
                adapter.notifyDataSetChanged();
            }
        }catch ( Exception e){
            Log.e("info", "readContactsException:"+e.toString() );
        }finally {
            if(c != null) {
                c.close();
            }
        }

    }


    /**
     * 运行时权限的返回消息
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode){
            case 1:
                if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    readContacts();
                }else {
                    Toast.makeText(this, "You denied the permission", Toast.LENGTH_SHORT).show();
                }
                break;
            case 2:
                if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    call(number);
                }else{
                    Toast.makeText(this, "You denied the permission", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
}
