package co.martinbrown.example.mobdb;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;
import co.martinbrown.example.mongodb.R;

public class MongoDbExampleActivity extends Activity {

    // YOU NEED TO SETUP YOUR OWN MONGODB DATABASE ON MONGOLAB.COM
    // PUT YOUR API KEY IN STRING RESOURCES
    private String USER_BASE_URL = "https://api.mongolab.com/api/1/databases/my-db/collections/my-coll?apiKey=";

    // YOU NEED TO SETUP A TABLE IN MONGOLAB WITH SOME STRUCTURE, LIKE THIS
    public final static String USER_FIRST_NAME = "firstname";
    public final static String USER_LAST_NAME = "lastname";
    public final static String USER_PASSWORD = "password";
    public final static String USER_EMAIL = "email";
    public final static String USER_SUBSCRIPTIONS = "subscriptions";

    EditText mEditFirstName;
    EditText mEditLastName;
    EditText mEditEmail;
    EditText mEditPassword;

    CheckBox mCheckSendSpam;
    CheckBox mCheckSellInfo;

    Button mButtonCancel;
    Button mButtonSubmit;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        USER_BASE_URL += getResources().getString(R.string.mongolab_api_key);

        mEditFirstName = (EditText) findViewById(R.id.editFirstName);
        mEditLastName = (EditText) findViewById(R.id.editLastName);
        mEditEmail = (EditText) findViewById(R.id.editEmail);
        mEditPassword = (EditText) findViewById(R.id.editPassword);

        mCheckSendSpam = (CheckBox) findViewById(R.id.checkSendSpam);
        mCheckSellInfo = (CheckBox) findViewById(R.id.checkSellInfo);

        mButtonCancel = (Button) findViewById(R.id.buttonCancel);
        mButtonSubmit = (Button) findViewById(R.id.buttonSubmit);

        mButtonSubmit.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                new Thread(new Runnable() {

                    @Override
                    public void run() {

                        if(emailExists(mEditEmail.getText().toString())) {
                            runOnUiThread(new Runnable() {

                                @Override
                                public void run() {
                                    mEditEmail.setError(mEditEmail.getText().toString() + " already in use");
                                }
                            });

                            return;
                        }

                        JSONObject user = new JSONObject();

                        try {
                            user.put(USER_FIRST_NAME, mEditFirstName.getText().toString());
                            user.put(USER_LAST_NAME, mEditLastName.getText().toString());
                            user.put(USER_EMAIL, mEditEmail.getText().toString());
                            user.put(USER_PASSWORD, mEditPassword.getText().toString());

                            JSONArray subscriptions = new JSONArray();
                            if(mCheckSendSpam.isChecked())
                                subscriptions.put(0);

                            if(mCheckSellInfo.isChecked())
                                subscriptions.put(1);

                            user.put(USER_SUBSCRIPTIONS, subscriptions);

                            if(postData(user)) {
                                clearFields();
                            }

                        }
                        catch(JSONException e) {

                        }
                    }
                }).start();
            }
        });

        mEditFirstName.setText("Jane");
        mEditLastName.setText("Doe");
        mEditEmail.setText("jane@gmail.com");
        mEditPassword.setText("321");
        mCheckSendSpam.setChecked(true);
        mCheckSellInfo.setChecked(true);
    }

    public void clearFields() {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                mEditFirstName.setText("");
                mEditLastName.setText("");
                mEditEmail.setText("");
                mEditPassword.setText("");
            }
        });
    }

    public String getJSONStringFromUrl(final String url) {

        final StringBuffer sb = new StringBuffer("");

        try {
            BufferedReader in = null;

            HttpClient client = new DefaultHttpClient();
            HttpGet request = new HttpGet(url);
            HttpResponse response = client.execute(request);

            final int statusCode = response.getStatusLine().getStatusCode();

            if(statusCode == 200) {

                in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));


                String line = "";
                String NL = System.getProperty("line.separator");

                while ((line = in.readLine()) != null) {
                    sb.append(line + NL);
                }

                in.close();
            }
        }
        catch(Exception e) {}

        return sb.toString();
    }

    public boolean emailExists(String email) {
        try {
            JSONArray users = new JSONArray(getJSONStringFromUrl(USER_BASE_URL));

            String uname;

            for(int i = 0; i < users.length(); i++) {
                uname = users.getJSONObject(i).getString(USER_EMAIL);
                if(uname.trim().equals(email.trim()))
                    return true;
            }
        }
        catch(JSONException e) {
            Toast.makeText(this, "There was an error", Toast.LENGTH_LONG).show();
        }

        return false;
    }

    public boolean postData(final JSONObject data) {

        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(USER_BASE_URL);

        boolean posted = false;

        try {
            HttpResponse response;

            StringEntity params = new StringEntity(data.toString());

            httppost.setHeader("Accept", "application/json");
            httppost.setHeader("Content-type", "application/json");
            httppost.setEntity(params);

            response = httpclient.execute(httppost);
            @SuppressWarnings("unused")
            String k = response.getStatusLine().toString();

            posted = true;

        } catch (ClientProtocolException e) {
            Toast.makeText(getApplicationContext(), "There was an error", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "There was an error", Toast.LENGTH_LONG).show();
        }

        return posted;
    }
}