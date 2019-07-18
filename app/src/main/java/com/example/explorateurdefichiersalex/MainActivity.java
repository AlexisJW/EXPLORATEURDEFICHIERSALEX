package com.example.explorateurdefichiersalex;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout1);
    }

    class TextAdapter extends BaseAdapter{

        private List<String> donnees = new ArrayList<>();
        private boolean[] selection;

        public void setDonnees(List<String> donnees){
            if (donnees != null){
                this.donnees.clear();
                if (donnees.size() > 0){
                    this.donnees.addAll(donnees);
                }
                notifyDataSetChanged();
            }
        }

        void setSelection(boolean[] selection){
          if (selection != null){
              this.selection = new boolean[selection.length];
              for (int i = 0; i < selection.length; i++){
                  this.selection[i] = selection[i];
              }
              notifyDataSetChanged();
          }
        }

        @Override
        public int getCount() {
            return donnees.size();
        }

        @Override
        public String getItem(int position) {
            return donnees.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null){
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item, parent, false);
                convertView.setTag(new ViewHolder((TextView) convertView.findViewById(R.id.textItem)));
            }
            ViewHolder holder = (ViewHolder)convertView.getTag();
            final String item = getItem(position);
            holder.info.setText(item.substring(item.lastIndexOf('/')+1));
            if (selection != null){
                if (selection[position]){
                    holder.info.setBackgroundColor(Color.argb(100, 9,9,9));
                }else{
                    holder.info.setBackgroundColor(Color.WHITE);
                }
            }
            return convertView;
        }

        class ViewHolder{
            TextView info;
            ViewHolder(TextView info){
                this.info = info;
            }
        }
    }

    private static final int REQUEST_PERMISSIONS = 1234;
    private static final int PERMISSIONS_COUNT = 2;

    @SuppressLint("NewApi")
    private Boolean DeniedPermissions(){
            int permis = 0;
            while (permis < PERMISSIONS_COUNT){
                if (checkSelfPermission(PERMISSIONS[permis]) != PackageManager.PERMISSION_GRANTED){
                    return true;
                }
                permis++;
            }

        return false;
    }

    private static final String[] PERMISSIONS = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private boolean isFileManagerInitialized = false;
    private boolean[] selection;
    private File[] files;
    private List<String> filesList;
    private int filesFoundCount;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onResume(){
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && DeniedPermissions()){
           requestPermissions(PERMISSIONS, REQUEST_PERMISSIONS);
           return;
        }
        if(!isFileManagerInitialized){
            final String rootPath = String.valueOf(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS));
            //final String rootPathDoc = String.valueOf(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS));
            final File dir = new File(rootPath);
            //File dirDoc = new File(rootPathDoc);
            files = dir.listFiles();
            //final File[] filesDoc = dirDoc.listFiles();
            final TextView pathOutput = findViewById(R.id.pathOutPut);
            //final TextView pathOutputDoc = findViewById(R.id.pathOutPutDoc);
            pathOutput.setText(rootPath.substring(rootPath.lastIndexOf('/')+1));
            //pathOutputDoc.setText(rootPathDoc);
            filesFoundCount = files.length;
            final ListView listView = findViewById(R.id.listView);
            final TextAdapter textAdapter = new TextAdapter();
            listView.setAdapter(textAdapter);

            filesList = new ArrayList<>();
            for (int i = 0; i < filesFoundCount; i++){
                filesList.add(String.valueOf(files[i].getAbsolutePath()));
            }
            textAdapter.setDonnees(filesList);

            selection = new boolean[files.length];

            listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    selection[position] =! selection[position];
                    textAdapter.setSelection(selection);

                    boolean ifOneSelected = false;
                    for (boolean b : selection) {
                        if (b) {
                            ifOneSelected = true;
                            break;
                        }
                    }
                    if (ifOneSelected){
                        findViewById(R.id.bottomBar).setVisibility(View.VISIBLE);
                    }else{
                        findViewById(R.id.bottomBar).setVisibility(View.GONE);
                    }
                    return false;
                }
            });


            final Button bt1 = findViewById(R.id.bt1);
            final Button bt2 = findViewById(R.id.bt2);
            final Button bt3 = findViewById(R.id.bt3);
            final Button bt4 = findViewById(R.id.bt4);
            final Button bt5 = findViewById(R.id.bt5);

            bt1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final AlertDialog.Builder deleteDialog = new AlertDialog.Builder(MainActivity.this);
                    deleteDialog.setTitle("SUPPRIMER");
                    deleteDialog.setMessage("Voulez-vous vraiment le Supprimer???");
                    deleteDialog.setPositiveButton("OUI", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            for (int i = 0; i < files.length; i++){
                                if (selection[i]){
                                    deleteFileOrFolder(files[i]);
                                    selection[i] = false;
                                }
                            }
                            files = dir.listFiles();
                            filesFoundCount = files.length;
                            filesList.clear();
                            for (int i = 0; i < filesFoundCount; i++){
                                filesList.add(String.valueOf(files[i].getAbsolutePath()));
                            }
                            textAdapter.setDonnees(filesList);
                        }
                    });

                    deleteDialog.setNegativeButton("NON", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    deleteDialog.show();
                }
            });
            isFileManagerInitialized = true;
        }
    }

    private void deleteFileOrFolder(File fileOrFolder){
      if (fileOrFolder.isDirectory()){
          if (fileOrFolder.list().length == 0){
              fileOrFolder.delete();
          }else{
              String files[] = fileOrFolder.list();
              for (String temp:files){
                  File fileToDelete = new File(fileOrFolder,temp);
                  deleteFileOrFolder(fileToDelete);
              }

              if (fileOrFolder.list().length == 0){
                  fileOrFolder.delete();
              }
          }
      }else{
          fileOrFolder.delete();
      }
    }

    @SuppressLint("NewApi")
    @Override
    public void onRequestPermissionsResult(final int requestCode, final String[] permissions, final int[] grantResults){
       super.onRequestPermissionsResult(requestCode, permissions, grantResults);
       if (requestCode == REQUEST_PERMISSIONS && grantResults.length > 0){
           if (DeniedPermissions()){
               ((ActivityManager) Objects.requireNonNull(this.getSystemService(ACTIVITY_SERVICE))).clearApplicationUserData();
               recreate();
           }else{
               onResume();
           }
       }
    }
}
