package iop.org.iop_contributors_app.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.hdodenhof.circleimageview.CircleImageView;
import iop.org.iop_contributors_app.ApplicationController;
import iop.org.iop_contributors_app.configurations.ProfileServerConfigurations;
import iop.org.iop_contributors_app.R;
import iop.org.iop_contributors_app.core.iop_sdk.forum.FlarumClient;
import iop.org.iop_contributors_app.core.iop_sdk.forum.FlarumClientInvalidDataException;
import iop.org.iop_contributors_app.core.iop_sdk.forum.ForumProfile;
import iop.org.iop_contributors_app.profile_server.ModuleProfileServer;
import iop.org.iop_contributors_app.ui.base.BaseActivity;
import iop.org.iop_contributors_app.ui.dialogs.DialogBuilder;
import iop.org.iop_contributors_app.wallet.WalletModule;

/**
 * Created by mati on 07/11/16.
 */

public class ProfileActivity extends BaseActivity {


    private static final String TAG = "ProfileActivity";

    public static final String INTENT_LOGIN = "login";

    private static final int RESULT_LOAD_IMAGE = 100;

    private WalletModule module;

    // UI
    private CircleImageView imgProfile;
    private EditText txt_name;
    private EditText txt_password;
    private EditText txt_email;
    private Button btn_create;
    private ProgressBar progressBar;

    private ForumProfile forumProfile;

    byte[] profImgData;

    private boolean isLogin = false;

    @Override
    protected void onCreateView(ViewGroup container, Bundle savedInstance) {

        if (getIntent().getExtras()!=null) {
            isLogin = getIntent().getExtras().getBoolean(INTENT_LOGIN, false);
        }

        getLayoutInflater().inflate(R.layout.profile_main,container);

        module = ApplicationController.getInstance().getWalletModule();

        imgProfile = (CircleImageView) findViewById(R.id.profile_image);
        txt_name = (EditText) findViewById(R.id.txt_name);
        txt_password = (EditText) findViewById(R.id.txt_password);
        txt_email = (EditText) findViewById(R.id.txt_mail);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.getIndeterminateDrawable().setColorFilter(Color.BLUE, PorterDuff.Mode.MULTIPLY);
        progressBar.setVisibility(View.GONE);


        btn_create = (Button) findViewById(R.id.btn_create);

        imgProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, RESULT_LOAD_IMAGE);
            }
        });

        if (module.isForumRegistered()){
            btn_create.setText("Back");
            btn_create.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(v.getContext(),MainActivity.class));
                }
            });
        }else {

            if (isLogin) {
                txt_email.setVisibility(View.GONE);
                btn_create.setText("LOGIN");
                findViewById(R.id.container_mail).setVisibility(View.GONE);
            }

            btn_create.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {

                        progressBar.setVisibility(View.VISIBLE);


                        final String username = txt_name.getText().toString();
                        final String password = txt_password.getText().toString();
                        final String email = txt_email.getText().toString();

                        if (username != null && !username.equals("")) {

                            if (password != null && !password.equals("")) {

                                if (!module.isForumRegistered()) {

                                    if (isLogin) {
                                        loginUser(username, password);
                                    } else if (email != null && !email.equals("")) {
                                        registerUser(username, password, email);
                                    }

                                } else
                                    Log.e(TAG, "IsRegistered true, error!!");
                            } else
                                Toast.makeText(ProfileActivity.this, "Error password is invalid", Toast.LENGTH_LONG).show();

                        } else {
                            Toast.makeText(ProfileActivity.this, "Username invalid, please add your nickname", Toast.LENGTH_LONG).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            });

        }
        init();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // init profile
        forumProfile = module.getForumProfile();
        if (forumProfile!=null) {
            txt_name.setText(forumProfile.getUsername());
            txt_password.setText(forumProfile.getPassword());
            txt_email.setText(forumProfile.getEmail());
        }
    }

    private void init(){


        TextWatcher passwordWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().length()<8){
                    Toast.makeText(ProfileActivity.this,"Password debe tener 8 caracteres minimo",Toast.LENGTH_SHORT).show();
                }
            }
        };

        txt_password.addTextChangedListener(passwordWatcher);

    }

    private void registerUser(final String username, final String password, final String email){

        execute(new Runnable() {
            @Override
            public void run() {
                try {
                    if (module.registerForumUser(username, password, email)) {

                        ProfileActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressBar.setVisibility(View.GONE);
                                buildDialog();
                            }
                        });
                    } else {
                        Toast.makeText(ProfileActivity.this, "Error connection to the forum", Toast.LENGTH_LONG).show();
                    }
                } catch (FlarumClientInvalidDataException e) {
                    buildFailDialog(e.getMessage());
                }
            }
        });


    }

    private void loginUser(final String username, final String password){
        execute(new Runnable() {
            @Override
            public void run() {
                try {
                    if (module.connectToForum(username, password)) {

                        ProfileActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressBar.setVisibility(View.GONE);
                                goHome();
                            }
                        });
                    } else {
                        Toast.makeText(ProfileActivity.this, "Error connection to the forum", Toast.LENGTH_LONG).show();
                    }
                } catch (FlarumClientInvalidDataException e) {
                    buildFailDialog(e.getMessage());
                }
            }
        });
    }

    /**
     * Execute
     *
     * @param runnable
     */
    private void execute(Runnable runnable){
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(runnable);
        executorService.shutdownNow();
    }

    private void goHome(){
        Intent intent = new Intent(this,MainActivity.class);
        startActivity(intent);
    }

    private void buildFailDialog(String message) {
        DialogBuilder dialogBuilder = new DialogBuilder(ProfileActivity.this);
        dialogBuilder.setTitle("Error");
        dialogBuilder.setMessage(message);
        dialogBuilder.singleDismissButton(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent(ProfileActivity.this,MainActivity.class));
            }
        });

        dialogBuilder.show();
    }

    private void buildDialog(){
        DialogBuilder dialogBuilder = new DialogBuilder(ProfileActivity.this);
        dialogBuilder.setTitle("Great!");
        dialogBuilder.setMessage("Register completed, please check the email to verify the account");
        dialogBuilder.singleDismissButton(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent(ProfileActivity.this,MainActivity.class));
            }
        });

        dialogBuilder.show();
    }

    private void sendUpdateProfileRequest(final byte[] version, final String name, final byte[] img){


        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG,"Sending work to profile service");
                try {
                    //sendWorkToProfileService();
                    module.updateProfile(version,name,img);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();



//        Bundle bundle = new Bundle();
//        bundle.putByteArray(ProfileServerConfigurations.PREFS_USER_VERSION,version);
//        bundle.putString(ProfileServerConfigurations.PREFS_USER_NAME,name);
//        bundle.putByteArray(ProfileServerConfigurations.PREFS_USER_IMAGE,img);
//
//        sendWorkToProfileService(bundle);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };
            Cursor cursor = getContentResolver().query(selectedImage,filePathColumn, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();
            Bitmap bitmap = BitmapFactory.decodeFile(picturePath);
            imgProfile.setImageBitmap(bitmap);

            // compress and do it array
            ByteArrayOutputStream out = null;
            try {
                out = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                profImgData = out.toByteArray();
            }catch (Exception e){
                e.printStackTrace();
            }finally {
                try {
                    if (out != null) {
                        out.close();
                    }
                } catch (IOException e) {
                    // nothing
                }
            }
        }
    }



}
