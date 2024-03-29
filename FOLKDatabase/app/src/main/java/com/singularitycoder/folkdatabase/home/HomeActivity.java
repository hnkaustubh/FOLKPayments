package com.singularitycoder.folkdatabase.home;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.singularitycoder.folkdatabase.R;
import com.singularitycoder.folkdatabase.auth.MainActivity;
import com.singularitycoder.folkdatabase.database.ContactItem;
import com.singularitycoder.folkdatabase.database.DatabaseActivity;
import com.singularitycoder.folkdatabase.database.NotificationAdapter;
import com.singularitycoder.folkdatabase.helper.CustomEditText;
import com.singularitycoder.folkdatabase.helper.Helper;
import com.singularitycoder.folkdatabase.profile.ProfileActivity;

import java.util.ArrayList;
import java.util.Objects;

public class HomeActivity extends AppCompatActivity {

    private static final String TAG = "HomeActivity";

    Toolbar toolbar;

    private ArrayList<HomeItem> homeList;
    private RecyclerView recyclerView;
    private HomeAdapter homeAdapter;
    private String strOldPassword;
    private ProgressDialog loadingBar;

    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;

    // this listener is called when there is change in firebase fireUser session
    FirebaseAuth.AuthStateListener authListener = firebaseAuth -> {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null) {
            // fireUser fireAuth state is changed - fireUser is null launch login activity
            startActivity(new Intent(this, MainActivity.class));
            Objects.requireNonNull(this).finish();
        } else {
            Toast.makeText(this, "AuthUserItem: " + user.getEmail(), Toast.LENGTH_SHORT).show();
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        authListener = firebaseAuth -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user == null) {
                // fireUser fireAuth state is changed - fireUser is null launch login activity
                startActivity(new Intent(this, MainActivity.class));
                Objects.requireNonNull(this).finish();
            }
        };

        loadingBar = new ProgressDialog(this);

        setUpStatusBar();
        setContentView(R.layout.activity_home);
        initToolBar();
        setUpRecyclerView();
    }


    private void setUpStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = this.getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);  // clear FLAG_TRANSLUCENT_STATUS flag:
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);  // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));   // change the color
        }
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN, WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }


    private void initToolBar() {
        toolbar = findViewById(R.id.toolbar_home);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("FOLK Home v0.0");
        }
        toolbar.setTitleTextColor(getResources().getColor(R.color.colorWhite));
        toolbar.setOverflowIcon(getResources().getDrawable(R.drawable.ic_more_vert_black_24dp));
    }


    private void setUpRecyclerView() {
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if (position == 0) {
                    return 2;
                } else {
                    return 1;
                }
            }
        });
        recyclerView = findViewById(R.id.recycler_home);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(20);
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);

        homeList = new ArrayList<>();
        SharedPreferences sp = getSharedPreferences("authItem", Context.MODE_PRIVATE);

        String imgUrl = sp.getString("profileImage", "");
        String fullName = sp.getString("firstName", "") + " " + sp.getString("lastName", "");

        homeList.add(new HomeItem(imgUrl, fullName));
        homeList.add(new HomeItem(R.drawable.ic_accomodation3, "Accomodation", ""));
        homeList.add(new HomeItem(R.drawable.ic_database3, "Database", ""));
        homeList.add(new HomeItem(R.drawable.ic_posting3, "Posting", ""));
        homeList.add(new HomeItem(R.drawable.ic_service3, "Service", ""));
        homeList.add(new HomeItem(R.drawable.ic_payments3, "Payments", ""));
        homeList.add(new HomeItem(R.drawable.ic_prasadum3, "Prasadam Coupon", ""));

        homeAdapter = new HomeAdapter(homeList, this);
        homeAdapter.setHasStableIds(true);
        homeAdapter.setOnItemClickListener(new HomeAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if (position == 0) {
                    Helper.comingSoonDialog(HomeActivity.this);
                }

                if (position == 1) {
                    Helper.comingSoonDialog(HomeActivity.this);
                }

                if (position == 2) {
                    Intent intent = new Intent(getApplicationContext(), DatabaseActivity.class);
                    startActivity(intent);
                }

                if (position == 3) {
                    Helper.comingSoonDialog(HomeActivity.this);
                }

                if (position == 4) {
                    Helper.comingSoonDialog(HomeActivity.this);
                }

                if (position == 5) {
                    Helper.comingSoonDialog(HomeActivity.this);
                }

                if (position == 6) {
                    Helper.comingSoonDialog(HomeActivity.this);
                }
            }
        });
        recyclerView.setAdapter(homeAdapter);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_notifications:
                dialogNotifications(this);
                return true;
            case R.id.action_my_profile:
                Intent intent = new Intent(this, ProfileActivity.class);
                intent.putExtra("profileKey", "AUTHUSER");
                startActivity(intent);
                return true;
            case R.id.action_about:
                aboutDialog(this);
                return true;
            case R.id.action_change_password:
                dialogChangePassword(this);
                return true;
            case R.id.action_delete_account:
                AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(this));
                builder.setTitle("Are you sure?");
                builder.setMessage("You cannot undo this!");

                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteAccount();
                        dialog.dismiss();
                    }
                });

                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                AlertDialog alertDialog = builder.create();
                alertDialog.show();
                return true;
            case R.id.action_log_out:
                AlertDialog.Builder builderLogOut = new AlertDialog.Builder(Objects.requireNonNull(this));
                builderLogOut.setMessage("Do you want to Log Out?");

                builderLogOut.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        logOut();
                        dialog.dismiss();
                    }
                });

                builderLogOut.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                AlertDialog alertDialogLogOut = builderLogOut.create();
                alertDialogLogOut.show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void aboutDialog(Activity activity) {
        final Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.dialog_about);

        Rect displayRectangle = new Rect();
        Window window = activity.getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(displayRectangle);
        dialog.getWindow().setLayout((int) (displayRectangle.width() * 0.8f), dialog.getWindow().getAttributes().height);

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT)); // for rounded corners

        TextView tvContactUs = dialog.findViewById(R.id.tv_contact_us);
        tvContactUs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", "name@emailaddress.com", null));
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Contact Us");
                emailIntent.putExtra(Intent.EXTRA_TEXT, "Feedback, Help, Report Bugs etc.");
                activity.startActivity(Intent.createChooser(emailIntent, "Send email..."));
            }
        });
        TextView tvRateUs = dialog.findViewById(R.id.tv_rate_us);
        tvRateUs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=org.srilaprabhupadalila&hl=en")));
            }
        });
        TextView tvVolunteer = dialog.findViewById(R.id.tv_volunteer_appdev);
        tvVolunteer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://forms.gle/WCBV2q4b1ZBgDf3B9")));
            }
        });
        TextView tvDedicated = dialog.findViewById(R.id.tv_dedicated_to);
        tvDedicated.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.srilaprabhupadalila.org/who-is-srila-prabhupada")));
            }
        });
        TextView tvShareApk = dialog.findViewById(R.id.tv_share_app_apk);
        tvShareApk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        TextView tvShareLink = dialog.findViewById(R.id.tv_share_app_link);
        tvShareLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Title Of The Post");
                shareIntent.putExtra(Intent.EXTRA_TEXT, "https://www.singularitycoder.com");
                shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                activity.startActivity(Intent.createChooser(shareIntent, "Share to"));
            }
        });
        dialog.show();
    }


    private void dialogNotifications(Activity activity) {
        final Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.dialog_notifications);

        Rect displayRectangle = new Rect();
        Window window = activity.getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(displayRectangle);
        Objects.requireNonNull(dialog.getWindow()).setLayout((int) (displayRectangle.width() * 0.8f), dialog.getWindow().getAttributes().height);

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        // if today's date matches birthday - show notif badge
        // Show all birthday's in the notifications

        ImageView imgCloseBtn = dialog.findViewById(R.id.img_dialog_close);
        imgCloseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        ArrayList<ContactItem> notificationList = new ArrayList<>();
        notificationList.add(new ContactItem("Michael Marvin", "", "Team Gauranga sold 8 million books today! Hari Bol!", "19/2/20"));
        notificationList.add(new ContactItem("Michael Marvin", "", "Team Gauranga sold 8 million books today! Hari Bol!", "19/2/20"));
        notificationList.add(new ContactItem("Michael Marvin", "", "Team Gauranga sold 8 million books today! Hari Bol!", "19/2/20"));
        notificationList.add(new ContactItem("Michael Marvin", "", "Team Gauranga sold 8 million books today! Hari Bol!", "19/2/20"));
        notificationList.add(new ContactItem("Michael Marvin", "", "Team Gauranga sold 8 million books today! Hari Bol!", "19/2/20"));

        LinearLayoutManager commentLayoutManager = new LinearLayoutManager(activity, RecyclerView.VERTICAL, false);

        RecyclerView notificationsRecycler = dialog.findViewById(R.id.dialog_notif_recycler);
        notificationsRecycler.setLayoutManager(commentLayoutManager);
        notificationsRecycler.setHasFixedSize(true);
        notificationsRecycler.setItemViewCacheSize(20);
        notificationsRecycler.setDrawingCacheEnabled(true);
        notificationsRecycler.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);

        NotificationAdapter notificationAdapter = new NotificationAdapter(notificationList, activity);
        notificationAdapter.setHasStableIds(true);

        notificationsRecycler.setAdapter(notificationAdapter);

        dialog.show();
    }

    private void dialogChangePassword(Activity activity) {
        final Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.dialog_change_password);

        Rect displayRectangle = new Rect();
        Window window = activity.getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(displayRectangle);
        Objects.requireNonNull(dialog.getWindow()).setLayout((int) (displayRectangle.width() * 0.8f), dialog.getWindow().getAttributes().height);

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        ImageView imgClose = dialog.findViewById(R.id.img_close);
        CustomEditText etOldPassword = dialog.findViewById(R.id.et_old_password);
        CustomEditText etNewPassword = dialog.findViewById(R.id.et_new_password);
        CustomEditText etNewPasswordAgain = dialog.findViewById(R.id.et_new_password_again);
        Button btnReset = dialog.findViewById(R.id.btn_change_password);

        imgClose.setOnClickListener(view -> dialog.dismiss());

        btnReset.setOnClickListener(view -> {
            if (hasValidInput(etOldPassword, etNewPassword, etNewPasswordAgain)) {
                // 1. Check old password matches or not
                // 2. Update in firestore new password value
                // 3. Update in auth new password value
                changePassword(etNewPassword.getText().toString().trim());
            }
        });

        dialog.show();
    }

    private boolean hasValidInput(CustomEditText etOldPassword, CustomEditText etNewPassword, CustomEditText etNewPasswordAgain) {
        if (etOldPassword.getText().toString().trim().equals("")) {
            etOldPassword.setError("Password is Required!");
            etOldPassword.requestFocus();
            return false;
        }

        if (!Helper.hasValidPassword(etOldPassword.getText().toString().trim())) {
            etOldPassword.setError("Password must have at least 8 characters with One Uppercase and One lower case. These Special Characters are allwoed .,#@-_+!?;':*");
            etOldPassword.requestFocus();
            return false;
        }

//            if (!etOldPassword.getText().toString().trim().equals("fewea")) {
//                etOldPassword.setError("Wrong old password!");
//                etOldPassword.requestFocus();
//                return false;
//            }

        if (etNewPassword.getText().toString().trim().equals("")) {
            etNewPassword.setError("Password is Required!");
            etNewPassword.requestFocus();
            return false;
        }

        if (!Helper.hasValidPassword(etNewPassword.getText().toString().trim())) {
            etNewPassword.setError("Password must have at least 8 characters with One Uppercase and One lower case. These Special Characters are allwoed .,#@-_+!?;':*");
            etNewPassword.requestFocus();
            return false;
        }

        if (etNewPasswordAgain.getText().toString().trim().equals("")) {
            etNewPasswordAgain.setError("Password is Required!");
            etNewPasswordAgain.requestFocus();
            return false;
        }

        if (!Helper.hasValidPassword(etNewPasswordAgain.getText().toString().trim())) {
            etNewPasswordAgain.setError("Password must have at least 8 characters with One Uppercase and One lower case. These Special Characters are allwoed .,#@-_+!?;':*");
            etNewPasswordAgain.requestFocus();
            return false;
        }

        if (!etNewPassword.getText().toString().trim().equals(etNewPasswordAgain.getText().toString().trim())) {
            etNewPassword.setError("Password is not matching!");
            etNewPasswordAgain.setError("Password is not matching!");
            etNewPassword.requestFocus();
            etNewPasswordAgain.requestFocus();
        }

        return true;
    }

    private String oldPassword() {
        FirebaseFirestore
                .getInstance()
                .collection("FolkPeople")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            strOldPassword = queryDocumentSnapshots.getDocuments().get(0).getString("password");
                            // store user doc id, name, pasword all basic details in shared prefs
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: got hit");
                    }
                });
        return strOldPassword;
    }

    private void changePassword(String newPassword) {
        loadingBar.setMessage("Please wait...");
        loadingBar.setCanceledOnTouchOutside(false);
        loadingBar.show();
        if (firebaseUser != null) {
            firebaseUser.updatePassword(newPassword)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(HomeActivity.this, "Password is updated! LogIn with new password!", Toast.LENGTH_SHORT).show();
                                logOut();
                                loadingBar.dismiss();
                            } else {
                                Toast.makeText(HomeActivity.this, "Failed to update Password!", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }
                        }
                    });
        }
    }


    private void deleteAccount() {
        loadingBar.setMessage("Please wait...");
        loadingBar.setCanceledOnTouchOutside(false);
        loadingBar.show();
        if (firebaseUser != null) {
            firebaseUser.delete()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(HomeActivity.this, "Your profile is deleted :( Create an account now!", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(HomeActivity.this, MainActivity.class));
                                Objects.requireNonNull(HomeActivity.this).finish();
                                loadingBar.dismiss();
                            } else {
                                Toast.makeText(HomeActivity.this, "Failed to delete your account!", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }
                        }
                    });
        }
    }


    private void logOut() {
        firebaseAuth.signOut();
        // this listener will be called when there is change in firebase fireUser session
        FirebaseAuth.AuthStateListener authListener = firebaseAuth -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user == null) {
                // fireUser fireAuth state is changed - fireUser is null launch login activity
                startActivity(new Intent(HomeActivity.this, MainActivity.class));
                Objects.requireNonNull(HomeActivity.this).finish();
            }
        };
    }


    @Override
    public void onStart() {
        super.onStart();
        if (authListener != null) {
            firebaseAuth.addAuthStateListener(authListener);
        }
    }


    @Override
    public void onStop() {
        super.onStop();
        if (authListener != null) {
            firebaseAuth.removeAuthStateListener(authListener);
        }
    }
}
