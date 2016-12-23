package iop.org.iop_contributors_app.ui;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.hdodenhof.circleimageview.CircleImageView;
import iop.org.iop_contributors_app.ApplicationController;
import iop.org.iop_contributors_app.R;
import iop.org.iop_contributors_app.core.iop_sdk.forum.InvalidUserParametersException;
import iop.org.iop_contributors_app.core.iop_sdk.forum.flarum.FlarumClientInvalidDataException;
import iop.org.iop_contributors_app.core.iop_sdk.forum.ForumProfile;
import iop.org.iop_contributors_app.ui.base.BaseActivity;
import iop.org.iop_contributors_app.ui.dialogs.DialogBuilder;
import iop.org.iop_contributors_app.wallet.WalletModule;

import static iop.org.iop_contributors_app.core.iop_sdk.utils.StringUtils.cleanString;

/**
 * Created by mati on 07/11/16.
 */

public class ProfileActivity extends ContributorBaseActivity implements View.OnClickListener{


    private static final String TAG = "ProfileActivity";

    public static final String INTENT_LOGIN = "login";

    private static final int RESULT_LOAD_IMAGE = 100;
    private static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL = 500;

    private WalletModule module;

    // UI
    private CircleImageView imgProfile;
    private EditText txt_name;
    private EditText txt_password;
    private EditText txt_email;
    private Button btn_create;
    private ProgressBar progressBar;
    private ImageView check_profile;
    private ImageView check_password;
    private ImageView check_email;


    private ForumProfile forumProfile;

    byte[] profImgData;

    private boolean isPasswordCorrect;
    private boolean isUsernameCorrect;
    private boolean isEmailCorrect;

    private AtomicBoolean lock = new AtomicBoolean(false);

    private boolean isRegistered;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (isRegistered)
            return super.onCreateOptionsMenu(menu);
        else
            return true;
    }

    @Override
    protected void onCreateView(ViewGroup container, Bundle savedInstance) {

        getLayoutInflater().inflate(R.layout.profile_main,container);

        module = ApplicationController.getInstance().getWalletModule();

        isRegistered = module.isForumRegistered();

        imgProfile = (CircleImageView) findViewById(R.id.profile_image);
        txt_name = (EditText) findViewById(R.id.txt_name);
        txt_password = (EditText) findViewById(R.id.txt_password);
        txt_email = (EditText) findViewById(R.id.txt_mail);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.getIndeterminateDrawable().setColorFilter(Color.BLUE, PorterDuff.Mode.MULTIPLY);
        progressBar.setVisibility(View.GONE);

        check_profile = (ImageView) findViewById(R.id.img_check_profile);
        check_email = (ImageView) findViewById(R.id.img_check_email);
        check_password = (ImageView) findViewById(R.id.img_check_password);

        btn_create = (Button) findViewById(R.id.btn_create);

        imgProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, RESULT_LOAD_IMAGE);
            }
        });

        if (isRegistered){
            btn_create.setText("Back");
            btn_create.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });

        }else {
            btn_create.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        progressBar.setVisibility(View.VISIBLE);
                        if (isUsernameCorrect){
                            if (isPasswordCorrect){
                                if (isEmailCorrect){
                                    if (!module.isForumRegistered()) {
                                        final String username = txt_name.getText().toString();
                                        final String password = txt_password.getText().toString();
                                        final String email = txt_email.getText().toString();

                                        registerUser(username, password, email);

                                    } else
                                        Log.e(TAG, "IsRegistered true, error!!");
                                }else {
                                    Toast.makeText(ProfileActivity.this, "Error email is invalid", Toast.LENGTH_LONG).show();
                                }

                            }else {
                                Toast.makeText(ProfileActivity.this, "Error password is invalid", Toast.LENGTH_LONG).show();
                            }
                        }else {
                            Toast.makeText(ProfileActivity.this, "Username invalid, please add your nickname", Toast.LENGTH_LONG).show();
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            });

        }
        try {
            File imgFile = module.getUserImageFile();
            if (imgFile.exists())
                Picasso.with(this).load(imgFile).into(imgProfile);
        }catch (Exception e){
            e.printStackTrace();
        }

        init();
    }

    @Override
    protected boolean onBroadcastReceive(Bundle data) {
        return false;
    }

    @Override
    protected boolean hasDrawer() {
        return isRegistered;
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
                if (s.toString().length()>=10){
                    isPasswordCorrect = true;
                    check_password.setVisibility(View.VISIBLE);
                    check_password.setImageResource(R.drawable.ic_check_profile);
                }else {
                    isPasswordCorrect = false;
                    check_password.setVisibility(View.VISIBLE);
                    check_password.setImageResource(R.drawable.ic_xroja_profile);
                }
            }
        };

        txt_password.addTextChangedListener(passwordWatcher);

        txt_email.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (validateMail(s.toString())){
                    isEmailCorrect = true;
                    check_email.setVisibility(View.VISIBLE);
                    check_email.setImageResource(R.drawable.ic_check_profile);
                }else {
                    isEmailCorrect = false;
                    check_email.setVisibility(View.VISIBLE);
                    check_email.setImageResource(R.drawable.ic_xroja_profile);
                }
            }
        });

        txt_name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().length()>3){
                    isUsernameCorrect = true;
                    check_profile.setVisibility(View.VISIBLE);
                    check_profile.setImageResource(R.drawable.ic_check_profile);
                }else {
                    isUsernameCorrect = false;
                    check_profile.setVisibility(View.VISIBLE);
                    check_profile.setImageResource(R.drawable.ic_xroja_profile);
                }
            }
        });

    }

    private boolean validateMail(CharSequence email){
        String regExpn =
                "^(([\\w-]+\\.)+[\\w-]+|([a-zA-Z]{1}|[\\w-]{2,}))@"
                        +"((([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                        +"[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\."
                        +"([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                        +"[0-9]{1,2}|25[0-5]|2[0-4][0-9])){1}|"
                        +"([a-zA-Z]+[\\w-]+\\.)+[a-zA-Z]{2,4})$";

        Pattern pattern = Pattern.compile(regExpn, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);

        if(matcher.matches())
            return true;
        else
            return false;
    }

    private void registerUser(final String username, final String password, final String email){

        if (!lock.getAndSet(true)) {
            execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (module.registerForumUser(username, password, email)) {

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    progressBar.setVisibility(View.GONE);
                                    buildDialog();
                                }
                            });
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(ProfileActivity.this, "Error connection to the forum", Toast.LENGTH_LONG).show();
                                    progressBar.setVisibility(View.GONE);
                                }
                            });
                        }
                    } catch (final InvalidUserParametersException e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                buildFailDialog(cleanString(e.getMessage()));
                                progressBar.setVisibility(View.GONE);
                            }
                        });
                    }

                    lock.set(false);
                }
            });
        }
    }


    /**
     * Execute
     *
     * @param runnable
     */
    private void execute(Runnable runnable){
        executor.execute(runnable);
    }

    private void goHome(){
        Intent intent = new Intent(this,MainActivity.class);
        startActivity(intent);
    }

    private void buildFailDialog(String message) {
        DialogBuilder dialogBuilder = new DialogBuilder(ProfileActivity.this);
        dialogBuilder.setTitle("Error");
        dialogBuilder.setMessage(message);

        dialogBuilder.show();
    }

    private void buildDialog(){
        DialogBuilder dialogBuilder = new DialogBuilder(ProfileActivity.this);
        dialogBuilder.setTitle("Great!");
        dialogBuilder.setMessage("Register completed");//, please check your email to verify the account
        dialogBuilder.singleDismissButton(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent(ProfileActivity.this,OnboardingWithCenterAnimationActivity.class));
            }
        });

        dialogBuilder.show();
    }

    private void sendUpdateProfileRequest(final byte[] version, final String name, final byte[] img){


//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                Log.d(TAG,"Sending work to profile service");
//                try {
//                    //sendWorkToProfileService();
//                    module.updateProfile(version,name,img);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        }).init();



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

            // scale image
            imgProfile.setImageBitmap(Bitmap.createScaledBitmap(bitmap, 1024, 1024, false));
//            imgProfile.setImageBitmap(bitmap);

            if( ContextCompat.checkSelfPermission(ProfileActivity.this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.READ_CONTACTS)) {

                    // Show an expanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.

                } else {

                    // No explanation needed, we can request the permission.

                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL);

                }
            }

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

            if (isRegistered){
                btn_create.setText("Save");
                btn_create.setOnClickListener(this);
            }
        }
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_create){
            module.updateUser(null,null,null,profImgData);
            Toast.makeText(this,"Saved",Toast.LENGTH_LONG).show();
        }
    }
}
