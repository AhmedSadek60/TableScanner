package info.androidhive.androidcamera;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import static android.content.ContentValues.TAG;

public class FilesAdapter extends RecyclerView.Adapter<FilesAdapter.ViewHolder> {
    Context context;
    ArrayList<String> allfiles = new ArrayList<String>();
    String folderPath;

    public FilesAdapter(Context context, ArrayList<String> arrayList, String path) {
        this.context = context;
        allfiles = arrayList;
        folderPath = path;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.file_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {

        final String name = allfiles.get(position);
        String pureName = name.substring(0, (name.length() - 4));
        holder.textView.setText(pureName);
        //  Drawable logo = Resources.getSystem().getDrawable(R.drawable.logo);
        // holder.imageView.setImageDrawable(logo);
        // String value = (new ArrayList<String>(myDataSet.values())).get(position);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                File parentFolder = new File(folderPath);
                File file = new File(parentFolder, name);
                Uri uri = FileProvider.getUriForFile(context, "com.tablescanner.app.provider", file);
                String mime = context.getContentResolver().getType(uri);

                // Open file with user selected app
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.setDataAndType(uri, mime);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                //startActivity(intent);
                //Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + "Your Phone_number"));
                view.getContext().startActivity(intent);
            }


        });

       /* holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                File dir = new File(folderPath);
                File file = new File(dir, name);
                file.delete();
                Toast.makeText(context,name+" deleted successfully!",Toast.LENGTH_LONG).show();
                return false;
            }
        });*/
    }

    @Override
    public int getItemCount() {
        return allfiles.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageView;
        public TextView textView;

        public ViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.Brand_Picture);
            textView = itemView.findViewById(R.id.Name);
        }
    }

}
