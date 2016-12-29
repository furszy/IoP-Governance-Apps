package iop.org.voting_app.ui;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.iop.AppController;
import org.iop.WalletModule;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import iop.org.furszy_lib.AsteriskPasswordTransformationMethod;
import iop.org.furszy_lib.dialogs.DialogBuilder;
import iop.org.iop_contributors_app.R;
import iop.org.iop_contributors_app.ui.ProfileActivity;
import iop.org.iop_contributors_app.ui.settings.DevActivity;
import iop_sdk.forum.ForumProfile;
import iop_sdk.forum.InvalidUserParametersException;
import iop_sdk.global.exceptions.ConnectionRefusedException;

import static iop_sdk.governance.utils.StringUtils.cleanString;

public class VotingStartActivity extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1000;

    public static final int STARTUP_DELAY = 300;
    public static final int ANIM_ITEM_DURATION = 1000;
    public static final int ITEM_DELAY = 300;

    private boolean animationStarted = false;

    private static final String TAG = "StartActivity";

    private AppController application;
    private WalletModule module;

    private EditText txt_name;
    private EditText txt_password;
    private ProgressBar progressBar;
    private ImageView check_password;
    private ImageView img_check_username;


    private ExecutorService executor;

    private AtomicBoolean lock = new AtomicBoolean(false);

    private boolean isPasswordCorrect;
    private boolean isUsernameCorrect;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        super.onCreate(savedInstanceState);

        checkPermissions();

        application = (AppController) getApplication();
        module = application.getModule();

        if (module.isForumRegistered()) {
            startActivity(new Intent(this, VotingProposalsActivity.class));
        }
        setContentView(R.layout.start_activity_main);

        txt_name = (EditText) findViewById(R.id.txt_name);
        txt_password = (EditText) findViewById(R.id.txt_password);
        check_password = (ImageView) findViewById(R.id.img_check_password);
        img_check_username = (ImageView) findViewById(R.id.img_check_username);

        ForumProfile forumProfile = module.getForumProfile();
        if (forumProfile!=null){
            if (checkPassword(forumProfile.getPassword()) && checkUsername(forumProfile.getUsername())) {
                txt_name.setText(forumProfile.getUsername());
                txt_password.setText(forumProfile.getPassword());
            }


        }


        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.getIndeterminateDrawable().setColorFilter(Color.BLUE, PorterDuff.Mode.MULTIPLY);

        findViewById(R.id.txt_sign_up).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(VotingStartActivity.this, ProfileActivity.class));
            }
        });

        findViewById(R.id.txt_forget_password).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // todo: hacer que vaya a la wabview para recuperar password
            }
        });

        findViewById(R.id.btn_login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {

                    if (isOnline()) {

                        progressBar.setVisibility(View.VISIBLE);

                        String username = txt_name.getText().toString();
                        String password = txt_password.getText().toString();

                        checkUsername(username);
                        checkPassword(password);

                        if (isUsernameCorrect) {
                            if (isPasswordCorrect) {
                                if (!module.isForumRegistered()) {
                                    loginUser(username, password);
                                } else
                                    Log.e(TAG, "IsRegistered true, error!!");
                            } else {
                                Toast.makeText(v.getContext(), "Error password is invalid", Toast.LENGTH_LONG).show();
                                progressBar.setVisibility(View.INVISIBLE);
                            }

                        } else {
                            Toast.makeText(v.getContext(), "Username invalid, please add your nickname", Toast.LENGTH_LONG).show();
                            progressBar.setVisibility(View.INVISIBLE);
                        }
                    }else
                        Toast.makeText(v.getContext(),"No internet connection available\nplease retry again later",Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });


        txt_password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().equals("")){isPasswordCorrect=false; return;}
                checkPassword(s.toString());
            }
        });

        txt_password.setTransformationMethod(new AsteriskPasswordTransformationMethod());

        txt_name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                checkUsername(s.toString());
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (module.isForumRegistered()) {
            onBackPressed();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {

        if (!hasFocus || animationStarted) {
            return;
        }

        animate();

        super.onWindowFocusChanged(hasFocus);
    }

    private void animate() {
        ImageView logoImageView = (ImageView) findViewById(R.id.img_logo);
        ViewGroup container = (ViewGroup) findViewById(R.id.container);

        ViewCompat.animate(logoImageView)
                .translationY(-250)
                .setStartDelay(STARTUP_DELAY)
                .setDuration(ANIM_ITEM_DURATION).setInterpolator(
                new DecelerateInterpolator(1.2f)).start();

        for (int i = 0; i < container.getChildCount(); i++) {
            View v = container.getChildAt(i);
            ViewPropertyAnimatorCompat viewAnimator;

            if (!(v instanceof Button)) {
                viewAnimator = ViewCompat.animate(v)
                        .translationY(50).alpha(1)
                        .setStartDelay((ITEM_DELAY * i) + 500)
                        .setDuration(1000);
            } else {
                viewAnimator = ViewCompat.animate(v)
                        .scaleY(1).scaleX(1)
                        .setStartDelay((ITEM_DELAY * i) + 500)
                        .setDuration(500);
            }

            viewAnimator.setInterpolator(new DecelerateInterpolator()).start();
        }

        logoImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(v.getContext(), DevActivity.class));
            }
        });
    }

    private boolean checkPassword(String password) {
        boolean ret = false;
        if (password.length()>=10){
            isPasswordCorrect = true;
            check_password.setVisibility(View.VISIBLE);
            check_password.setImageResource(R.drawable.ic_check_profile);
            ret = true;
        }else {
            isPasswordCorrect = false;
            check_password.setVisibility(View.VISIBLE);
            check_password.setImageResource(R.drawable.ic_xroja_profile);
            ret = false;
        }
        return ret;
    }

    private boolean checkUsername(String username){
        boolean ret = false;
        if (username.toString().length()>=3){
            isUsernameCorrect = true;
            img_check_username.setVisibility(View.VISIBLE);
            img_check_username.setImageResource(R.drawable.ic_check_profile);
            ret = true;
        }else {
            isUsernameCorrect = false;
            img_check_username.setVisibility(View.VISIBLE);
            img_check_username.setImageResource(R.drawable.ic_xroja_profile);
            ret = false;
        }
        return ret;
    }


    private void loginUser(final String username, final String password){
        if (!lock.getAndSet(true)) {
            execute(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "loginUser");
                    try {
                        if (module.connectToForum(username, password)) {

                            VotingStartActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    progressBar.setVisibility(View.GONE);
                                    goHome();
                                }
                            });
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(VotingStartActivity.this, "Fail connection to the forum", Toast.LENGTH_LONG).show();
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
                    }catch (final ConnectionRefusedException e) {
                        e.printStackTrace();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                buildFailDialog("Cant connect\n"+e.getMessage());
                                progressBar.setVisibility(View.GONE);
                            }
                        });
                    }catch (final Exception e){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                buildFailDialog("Cant connect\n"+e.getMessage());
                                progressBar.setVisibility(View.GONE);
                            }
                        });
                    }
                    lock.set(false);
                }
            });
        }else
            Log.e(TAG,"Login tocado varias veces..");
    }

    private void goHome(){
        Intent intent = new Intent(this,VotingProposalsActivity.class);
        startActivity(intent);
    }

    /**
     * Execute
     *
     * @param runnable
     */
    private void execute(Runnable runnable){
        executor = Executors.newSingleThreadExecutor();
        Future future = executor.submit(runnable);
        executor.shutdown();
    }

    /**
     * Check internet connectivity
     *
     * @return
     */
    private boolean isOnline(){
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return networkInfo!= null &&  networkInfo.isConnected();

    }

    private void buildFailDialog(String message) {
        DialogBuilder dialogBuilder = new DialogBuilder(this);
        dialogBuilder.setTitle("Error");
        dialogBuilder.setMessage(message);
        dialogBuilder.show();
    }


    private void checkPermissions() {
        // Assume thisActivity is the current activity
        if (Build.VERSION.SDK_INT > 22) {

            int permissionCheck = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE);

            // Here, thisActivity is the current activity
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {

                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.READ_CONTACTS)) {

                    // Show an expanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.

                } else {

                    // No explanation needed, we can request the permission.

                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);

                    // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                    // app-defined int constant. The callback method gets the
                    // result of the request.
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this, "Permission denied to read your External storage", Toast.LENGTH_SHORT).show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
}