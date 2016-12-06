package iop.org.iop_contributors_app.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.Nullable;
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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import iop.org.iop_contributors_app.ApplicationController;
import iop.org.iop_contributors_app.R;
import iop.org.iop_contributors_app.core.iop_sdk.forum.ForumProfile;
import iop.org.iop_contributors_app.core.iop_sdk.forum.InvalidUserParametersException;
import iop.org.iop_contributors_app.ui.dialogs.DialogBuilder;
import iop.org.iop_contributors_app.wallet.WalletModule;

import static iop.org.iop_contributors_app.core.iop_sdk.utils.StringUtils.cleanString;

public class OnboardingWithCenterAnimationActivity extends AppCompatActivity {

    public static final int STARTUP_DELAY = 300;
    public static final int ANIM_ITEM_DURATION = 1000;
    public static final int ITEM_DELAY = 300;

    private boolean animationStarted = false;

    private static final String TAG = "StartActivity";

    private ApplicationController application;
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

        application = ApplicationController.getInstance();
        module = application.getWalletModule();

        if (module.isForumRegistered()) {
            startActivity(new Intent(this, MainActivity.class));
        }
        setContentView(R.layout.activity_onboarding_center);

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
                startActivity(new Intent(OnboardingWithCenterAnimationActivity.this, ProfileActivity.class));
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

                    progressBar.setVisibility(View.VISIBLE);

                    String username = txt_name.getText().toString();
                    String password = txt_password.getText().toString();

                    checkUsername(username);
                    checkPassword(password);

                    if (isUsernameCorrect) {
                        if (isPasswordCorrect) {
                            if (!module.isForumRegistered()) {
                                loginUser(username,password);
                            } else
                                Log.e(TAG, "IsRegistered true, error!!");
                        } else
                            Toast.makeText(OnboardingWithCenterAnimationActivity.this, "Error password is invalid", Toast.LENGTH_LONG).show();

                    } else {
                        Toast.makeText(OnboardingWithCenterAnimationActivity.this, "Username invalid, please add your nickname", Toast.LENGTH_LONG).show();
                    }
                    // invisible progress bar
                    progressBar.setVisibility(View.INVISIBLE);
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
                checkPassword(s.toString());
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
                checkUsername(s.toString());
            }
        });
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
    }

    private boolean checkPassword(String password) {
        boolean ret = false;
        if (password.length()>=8){
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
                    Log.d(TAG,"loginUser");
                    try {
                        if (module.connectToForum(username, password)) {

                            OnboardingWithCenterAnimationActivity.this.runOnUiThread(new Runnable() {
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
                                    Toast.makeText(OnboardingWithCenterAnimationActivity.this, "Error connection to the forum", Toast.LENGTH_LONG).show();
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
                    } catch (final Exception e){
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
        Intent intent = new Intent(this,MainActivity.class);
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

    private void buildFailDialog(String message) {
        DialogBuilder dialogBuilder = new DialogBuilder(this);
        dialogBuilder.setTitle("Error");
        dialogBuilder.setMessage(message);
        dialogBuilder.singleDismissButton(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent(OnboardingWithCenterAnimationActivity.this,MainActivity.class));
            }
        });

        dialogBuilder.show();
    }
}