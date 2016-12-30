//package iop.org.iop_contributors_app.ui;
//
//import android.Manifest;
//import android.content.DialogInterface;
//import android.content.Intent;
//import android.content.pm.PackageManager;
//import android.graphics.Color;
//import android.graphics.PorterDuff;
//import android.os.Build;
//import android.os.Bundle;
//import android.support.v4.app.ActivityCompat;
//import android.support.v4.content.ContextCompat;
//import android.support.v7.app.AppCompatActivity;
//import android.text.Editable;
//import android.text.TextWatcher;
//import android.util.Log;
//import android.view.View;
//import android.widget.EditText;
//import android.widget.ImageView;
//import android.widget.ProgressBar;
//import android.widget.Toast;
//
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//import java.util.concurrent.atomic.AtomicBoolean;
//
//import iop.org.contributors_app.ApplicationController;
//import iop.org.iop_contributors_app.ConnectionRefusedException;
//import iop.org.iop_contributors_app.R;
//import iop.org.iop_contributors_app.core.iop_sdk.forum.ForumProfile;
//import iop.org.iop_contributors_app.core.iop_sdk.forum.InvalidUserParametersException;
//import iop.org.furszy_lib.dialogs.DialogBuilder;
//import iop.org.iop_contributors_app.ui.settings.DevActivity;
//import org.iop.WalletModule;
//
//import static android.content.pm.PackageManager.PERMISSION_GRANTED;
//import static iop_sdk.governance.utils.StringUtils.cleanString;
//
///**
// * Created by mati on 28/11/16.
// */
//
//public class StartActivity extends AppCompatActivity {
//
//    private static final String TAG = "StartActivity";
//
//    private ApplicationController application;
//    private WalletModule module;
//
//
//    private EditText txt_name;
//    private EditText txt_password;
//    private ProgressBar progressBar;
//    private ImageView check_password;
//    private ImageView img_check_username;
//
//
//    private ExecutorService executor;
//
//    private AtomicBoolean lock = new AtomicBoolean(false);
//
//    private boolean isPasswordCorrect;
//    private boolean isUsernameCorrect;
//
//
//
//    @Override
//    public final void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
////        checkPermissions();
//
//        application = ApplicationController.getInstance();
//        module = application.getModule();
//
//
//        // Get intent, action and MIME type
//        Intent intent = getIntent();
//        String action = intent.getAction();
//        String type = intent.getType();
//
//        if (Intent.ACTION_VIEW.equals(action)){
//
//            String data = intent.getData().toString();
//            Log.d(TAG,"Actio view...");
//
//        }
//
//
//        setContentView(R.layout.start_main);
//        txt_name = (EditText) findViewById(R.id.txt_name);
//        txt_password = (EditText) findViewById(R.id.txt_password);
//        check_password = (ImageView) findViewById(R.id.img_check_password);
//        img_check_username = (ImageView) findViewById(R.id.img_check_username);
//
//        ForumProfile forumProfile = module.getForumProfile();
//        if (forumProfile!=null){
//            if (checkPassword(forumProfile.getPassword()) && checkUsername(forumProfile.getUsername())) {
//                txt_name.setText(forumProfile.getUsername());
//                txt_password.setText(forumProfile.getPassword());
//            }
//
//
//        }
//
//        progressBar = (ProgressBar) findViewById(R.id.progressBar);
//
//        progressBar = (ProgressBar) findViewById(R.id.progressBar);
//        progressBar.getIndeterminateDrawable().setColorFilter(Color.BLUE, PorterDuff.Mode.MULTIPLY);
//        progressBar.setVisibility(View.GONE);
//
//        findViewById(R.id.txt_sign_up).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                startActivity(new Intent(StartActivity.this, ProfileActivity.class));
//            }
//        });
//
//        findViewById(R.id.txt_forget_password).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                // todo: hacer que vaya a la wabview para recuperar password
//            }
//        });
//
//        findViewById(R.id.btn_login).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                try {
//
//                    progressBar.setVisibility(View.VISIBLE);
//
//                    String username = txt_name.getText().toString();
//                    String password = txt_password.getText().toString();
//
//                    checkUsername(username);
//                    checkPassword(password);
//
//                    if (isUsernameCorrect) {
//                        if (isPasswordCorrect) {
//                            if (!module.isForumRegistered()) {
//                                loginUser(username,password);
//                            } else
//                                Log.e(TAG, "IsRegistered true, error!!");
//                        } else
//                            Toast.makeText(StartActivity.this, "Error password is invalid", Toast.LENGTH_LONG).show();
//
//                    } else {
//                        Toast.makeText(StartActivity.this, "Username invalid, please add your nickname", Toast.LENGTH_LONG).show();
//                    }
//                    // invisible progress bar
//                    progressBar.setVisibility(View.INVISIBLE);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        });
//
//
//        txt_password.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int init, int count, int after) {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int init, int before, int count) {
//
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//                checkPassword(s.toString());
//            }
//        });
//
//        txt_name.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int init, int count, int after) {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int init, int before, int count) {
//
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//                checkUsername(s.toString());
//            }
//        });
//
//
//        findViewById(R.id.img_logo).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                startActivity(new Intent(v.getContext(),DevActivity.class));
//            }
//        });
//
//    }
//
//
//
//    private boolean checkPassword(String password) {
//        boolean ret = false;
//        if (password.length()>=8){
//            isPasswordCorrect = true;
//            check_password.setVisibility(View.VISIBLE);
//            check_password.setImageResource(R.drawable.ic_check_profile);
//            ret = true;
//        }else {
//            isPasswordCorrect = false;
//            check_password.setVisibility(View.VISIBLE);
//            check_password.setImageResource(R.drawable.ic_xroja_profile);
//            ret = false;
//        }
//        return ret;
//    }
//
//    private boolean checkUsername(String username){
//        boolean ret = false;
//        if (username.toString().length()>=3){
//            isUsernameCorrect = true;
//            img_check_username.setVisibility(View.VISIBLE);
//            img_check_username.setImageResource(R.drawable.ic_check_profile);
//            ret = true;
//        }else {
//            isUsernameCorrect = false;
//            img_check_username.setVisibility(View.VISIBLE);
//            img_check_username.setImageResource(R.drawable.ic_xroja_profile);
//            ret = false;
//        }
//        return ret;
//    }
//
//
//    private void loginUser(final String username, final String password){
//        if (!lock.getAndSet(true)) {
//            execute(new Runnable() {
//                @Override
//                public void run() {
//                    try {
//                        if (module.connectToForum(username, password)) {
//
//                            StartActivity.this.runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    progressBar.setVisibility(View.GONE);
//                                    goHome();
//                                }
//                            });
//                        } else {
//                            runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    Toast.makeText(StartActivity.this, "Error connection to the forum", Toast.LENGTH_LONG).show();
//                                    progressBar.setVisibility(View.GONE);
//                                }
//                            });
//                        }
//                    } catch (final InvalidUserParametersException e) {
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                buildFailDialog(cleanString(e.getMessage()));
//                                progressBar.setVisibility(View.GONE);
//                            }
//                        });
//                    } catch (ConnectionRefusedException e) {
//                        e.printStackTrace();
//                    }
//                    lock.set(false);
//                }
//            });
//        }
//    }
//
//    private void goHome(){
//        Intent intent = new Intent(this,MainActivity.class);
//        startActivity(intent);
//    }
//
//    /**
//     * Execute
//     *
//     * @param runnable
//     */
//    private void execute(Runnable runnable){
//        executor = Executors.newSingleThreadExecutor();
//        executor.execute(runnable);
//        executor.shutdown();
//    }
//
//    private void buildFailDialog(String message) {
//        DialogBuilder dialogBuilder = new DialogBuilder(this);
//        dialogBuilder.setTitle("Error");
//        dialogBuilder.setMessage(message);
//        dialogBuilder.singleDismissButton(new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                startActivity(new Intent(StartActivity.this,MainActivity.class));
//            }
//        });
//
//        dialogBuilder.show();
//    }
//
//
//}
