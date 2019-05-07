package info.androidhive.androidcamera;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FilesActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.files_activity);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Table Scanner");
        setSupportActionBar(toolbar);
        Bundle bundle = getIntent().getExtras();
        String path = bundle.getString("path");
        Log.i("", "onCreate: path="+path);

        File file=new File(path);
        String arr[]=file.list();
        ArrayList l=new ArrayList<>();
        for(String i:arr){
            if(i.endsWith(".csv")){
                l.add(i);
                Log.i("File Name", "onCreate: ="+i);
            }
        }


        RecyclerView files = findViewById(R.id.ListOfFiles);
        LinearLayoutManager llm = new LinearLayoutManager(getApplicationContext());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        files.setLayoutManager(llm);
        FilesAdapter adapter = new FilesAdapter(getApplicationContext(),l,path);
        files.setAdapter(adapter);
    }
}
