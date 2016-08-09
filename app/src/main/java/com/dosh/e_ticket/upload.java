package com.dosh.e_ticket;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

public class upload extends AppCompatActivity implements View.OnClickListener {

    private static final String SERVER_ADDRESS="http://192.168.56.1/";

    EditText etName, etAge, etUsername, etPassword;
    Button bRegister;

    ImageView imageToUpload, downloadedImage;
    Button bUploadImage, bDownloadImage;
    EditText uploadImageName, downloadImageName;
    private static final int Result_Load_Image = 1 ;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.upload);

        imageToUpload = (ImageView)findViewById(R.id.imageToUpload);
        downloadedImage = (ImageView) findViewById(R.id.downloadedImage);

        bUploadImage =(Button) findViewById(R.id.bUploadImage);
        bDownloadImage = (Button) findViewById(R.id. bDownloadImage);

        uploadImageName = (EditText) findViewById(R.id.etUploadName);
        downloadImageName = (EditText) findViewById(R.id.etDownloadName);



        etName = (EditText) findViewById(R.id.etName);
        etAge = (EditText) findViewById(R.id.etAge);
        etUsername = (EditText) findViewById(R.id.etUsername);
        etPassword = (EditText) findViewById(R.id.etPassword);
        bRegister = (Button) findViewById(R.id.bRegister);


        imageToUpload.setOnClickListener(this);
        bUploadImage.setOnClickListener(this);
      //  bDownloadImage.setOnClickListener(this);
        bRegister.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {

        switch(v.getId()){
            case R.id.imageToUpload:
                Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent,Result_Load_Image);
                break;
            case R.id.bUploadImage:
                Bitmap image = ((BitmapDrawable)imageToUpload.getDrawable()).getBitmap();
                new UploadImage(image,uploadImageName.getText().toString()).execute();
                break;
            case R.id.bDownloadImage:
new DownloadImage(downloadImageName.getText().toString()).execute();

                break;


                case R.id.bRegister:
                    String name = etName.getText().toString();
                    String username = etUsername.getText().toString();
                    String password = etPassword.getText().toString();
                    int age = Integer.parseInt(etAge.getText().toString());

                    User user = new User(name, age, username, password);
                    registerUser(user);
                    break;
            }
        }

    private void registerUser(User user) {
        ServerRequests serverRequest = new ServerRequests(this);
        serverRequest.storeUserDataInBackground(user, new GetUserCallback() {
            @Override
            public void done(User returnedUser) {
                Intent loginIntent = new Intent(upload.this, Login.class);
                startActivity(loginIntent);
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Result_Load_Image && resultCode == RESULT_OK && data !=null);
        Uri selectedImage =data.getData();
        imageToUpload.setImageURI(selectedImage);
    }
    private class UploadImage extends AsyncTask<Void, Void, Void>{
        Bitmap image;
        String name;

     public UploadImage(Bitmap image, String name){
         this.image =image;
         this.name =name;
     }
        @Override
        protected Void doInBackground(Void... params) {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            image.compress(Bitmap.CompressFormat.JPEG,100,byteArrayOutputStream);
            String encodedImage = Base64.encodeToString(byteArrayOutputStream.toByteArray(),Base64.DEFAULT);

            ArrayList<NameValuePair> dataToSend = new ArrayList<>();
            dataToSend.add(new BasicNameValuePair("image", encodedImage));
            dataToSend.add(new BasicNameValuePair("name",name));

            HttpParams httpRequestParams = getHttpRequestParams();

            HttpClient client = new DefaultHttpClient(httpRequestParams);
            HttpPost post = new HttpPost(SERVER_ADDRESS + "SavePicture.php");
            try{
                post.setEntity(new UrlEncodedFormEntity(dataToSend));
                client.execute(post);

            }catch (Exception e){
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Toast.makeText(getApplicationContext(), "Image Uploaded", Toast.LENGTH_SHORT).show();
        }
    }
    private class DownloadImage extends AsyncTask<Void, Void, Bitmap>{
        String name;

        public DownloadImage (String name){
            this.name = name;
        }

        @Override
        protected Bitmap doInBackground(Void... params) {
            String url =SERVER_ADDRESS + "pictures/"+name+".jpg";
            try{
                URLConnection connection =new URL(url).openConnection();
                connection.setConnectTimeout (1000 * 30);
                connection.setReadTimeout(1000 * 30);

                return BitmapFactory.decodeStream((InputStream)connection.getContent(), null, null);
            }catch(Exception e){
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            if (bitmap !=null){
                downloadedImage.setImageBitmap(bitmap);
            }
        }
    }


    private HttpParams getHttpRequestParams(){
        HttpParams httpRequestParams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpRequestParams, 1000 * 30);
        HttpConnectionParams.setSoTimeout(httpRequestParams, 1000 * 30);
        return httpRequestParams;
    }
}
