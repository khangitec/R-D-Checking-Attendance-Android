package com.example.hoangdang.diemdanh;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.hoangdang.diemdanh.Fragments.AttendanceFragment;
import com.example.hoangdang.diemdanh.Fragments.ChooseCourseFragment;
import com.example.hoangdang.diemdanh.Fragments.ProfileFragment;
import com.example.hoangdang.diemdanh.Fragments.SendAbsenceRequestFragment;
import com.example.hoangdang.diemdanh.Fragments.SendFeedbackFragment;
import com.example.hoangdang.diemdanh.SupportClass.AppVariable;
import com.example.hoangdang.diemdanh.SupportClass.DatabaseHelper;
import com.example.hoangdang.diemdanh.SupportClass.Network;
import com.example.hoangdang.diemdanh.SupportClass.SecurePreferences;
import com.example.hoangdang.diemdanh.studentQuiz.PersonActivity;
import com.example.hoangdang.diemdanh.studentQuiz.UploadFaces2Activity;
import com.example.hoangdang.diemdanh.timeTable.TimeTableActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, ChooseCourseFragment.OnFragmentInteractionListener, AttendanceFragment.OnFragmentInteractionListener {

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.drawer_layout) DrawerLayout drawer;
    @BindView(R.id.nav_view) NavigationView navigationView;
    DatabaseHelper db;
    TextView _user_full_name_textView;

    protected String strUserFullName;
    protected ProgressDialog progressDialog;
    public static int user_role;

//    private Handler mHandler;

    @Override
    protected void onStart() {
        db = new DatabaseHelper(this);
        super.onStart();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!Network.isOnline(this)) {
            AppVariable.alert(this, "No Internet");
            return;
        }

        progressDialog = new ProgressDialog(MainActivity.this,
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setMessage("Retrieving data...");

        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        SharedPreferences pref = new SecurePreferences(this);
        user_role = pref.getInt(AppVariable.USER_ROLE, -1);
        strUserFullName = pref.getString(AppVariable.USER_NAME, "EMPTY");

        View header = navigationView.getHeaderView(0);

        //change menu for each role
        if (user_role == AppVariable.STUDENT_ROLE){
            Menu nav_Menu = navigationView.getMenu();
            nav_Menu.findItem(R.id.mnDiemDanh).setVisible(false);
            nav_Menu.findItem(R.id.mnAttendance).setVisible(true);
            nav_Menu.findItem(R.id.mnAbsenceRequest).setVisible(true);
        }

        _user_full_name_textView = (TextView) header.findViewById(R.id.user_name_textView);
        _user_full_name_textView.setText(strUserFullName);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.nav_drawer_open, R.string.nav_drawer_close);

        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);

        if (user_role == AppVariable.STUDENT_ROLE){
            showAttendanceFragment();
            pref.edit().putInt(AppVariable.CURRENT_ATTENDANCE, 1).apply();
        }
        else{
            progressDialog.show();
            new GetOpeningCourseTask().execute(
                    pref.getString(AppVariable.USER_TOKEN, null),
                    pref.getString(AppVariable.USER_ID, null));
        }
    }

    @Override
    public void onBackPressed() {
        if(drawer.isDrawerOpen(GravityCompat.START)){
            drawer.closeDrawer(GravityCompat.START);
        }
        else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.nav_drawer, menu);
        return true;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        switch (id){
            case R.id.mnAbsenceRequest:
                showSendAbsenceRequestFragment();
                overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
                break;
            case R.id.mnDiemDanh:
            case R.id.mnAttendance:
                if (user_role == AppVariable.TEACHER_ROLE){
                    showChooseCourseFragment();
                }
                else {
                    showAttendanceFragment();
                }
                overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
                break;
            case R.id.mnSendFeedback:
                showSendFeedbackFragment();
                overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
                break;
            case R.id.mnTimetable:
                startActivity(new Intent(this, TimeTableActivity.class));
                overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
                break;
            case R.id.mnUploadFaceTable: {
                SharedPreferences pref = new SecurePreferences(this);
                String studentId = pref.getString(AppVariable.USER_ID, "");
                String studentName = pref.getString(AppVariable.USER_NAME, "");
                String personId = pref.getString(AppVariable.USER_PERSON_ID, "");

                Intent intent = new Intent(this, PersonActivity.class);
                intent.putExtra("AddNewPerson", true);
                intent.putExtra("PersonGroupId", "hcmus-face");
                intent.putExtra("studentId", studentId);
                intent.putExtra("PersonName", studentName);
                intent.putExtra("person_id", personId);
                startActivity(intent);
                overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
                break;
            }
            case R.id.mnAccount:
                showProfileFragment();
                overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
                break;
            case R.id.mnAbout:
                startActivity(new Intent(this, AboutActivity.class));
                overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
                break;
            case R.id.mnLogout:
                overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
                logout();
                startActivity(new Intent(this, LoginActivity.class));
                finish();
                break;
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onFragmentInteraction(Uri uri) {}

    //UI blinding

    private void showChooseCourseFragment() {
        FragmentManager fragmentManager = this.getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        ChooseCourseFragment chooseCourseFragment = new ChooseCourseFragment();
        fragmentTransaction.replace(R.id.content_frame,chooseCourseFragment);

        fragmentTransaction.commit();
    }

    private void showSendFeedbackFragment() {
        FragmentManager fragmentManager = this.getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        SendFeedbackFragment sendFeedbackFragment = new SendFeedbackFragment();
        fragmentTransaction.replace(R.id.content_frame, sendFeedbackFragment);

        fragmentTransaction.commit();
    }

    private void showAttendanceFragment() {
        FragmentManager fragmentManager = this.getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        AttendanceFragment fragment = new AttendanceFragment();
        fragmentTransaction.replace(R.id.content_frame,fragment);

        fragmentTransaction.commit();
    }

    private void showSendAbsenceRequestFragment() {
        FragmentManager fragmentManager = this.getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        SendAbsenceRequestFragment fragment = new SendAbsenceRequestFragment();
        fragmentTransaction.replace(R.id.content_frame, fragment);

        fragmentTransaction.commit();
    }

    private void showProfileFragment() {
        FragmentManager fragmentManager = this.getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        ProfileFragment fragment = new ProfileFragment();
        fragmentTransaction.replace(R.id.content_frame, fragment);

        fragmentTransaction.commit();
    }

    //Function

    private void logout() {
        SharedPreferences prefs = new SecurePreferences(this);
        new LogoutTask().execute(prefs.getString(AppVariable.USER_TOKEN, null));
        db.cleanData();
        prefs.edit()
                .putString(AppVariable.USER_AUTHENTICATION, null)
                .putString(AppVariable.USER_TOKEN, null)
                .putInt(AppVariable.USER_ID, 0)
                .putInt(AppVariable.CURRENT_ATTENDANCE, 0)
                .putInt(AppVariable.CURRENT_CLASS_HAS_COURSE_ID, 0)
                .putInt(AppVariable.CURRENT_CLASS_ID, 0)
                .putInt(AppVariable.CURRENT_COURSE_ID, 0)
                .apply();
    }

    //Network task

    private class LogoutTask extends AsyncTask<String, Void, Integer> {
        @Override
        protected void onPreExecute(){}

        @Override
        protected Integer doInBackground(String... params) {
            try {
                URL url = new URL(Network.API_LOGOUT);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                try {
                    //prepare json data
                    JSONObject jsonUserData = new JSONObject();
                    jsonUserData.put("token", params[0]);

                    connection.setReadTimeout(10000);
                    connection.setConnectTimeout(15000);
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                    connection.setRequestProperty("Accept", "application/json");
                    connection.setDoInput(true);
                    connection.setDoOutput(true);

                    //write
                    OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
                    writer.write(jsonUserData.toString());
                    writer.flush();

                    return 0;
                }
                finally{
                    connection.disconnect();
                }
            }
            catch(Exception e) {
                return 0;
            }
        }

        @Override
        protected void onPostExecute(Integer status) {}

    }

    private class GetOpeningCourseTask extends AsyncTask<String, Void, Integer> {
        private String strJsonResponse;

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected Integer doInBackground(String... params) {
            try {
                URL url;
                //prepare json data
                JSONObject jsonUserData = new JSONObject();
                if(user_role == AppVariable.TEACHER_ROLE){
                    jsonUserData.put("token", params[0]);
                    jsonUserData.put("teacher_id", params[1]);
                    jsonUserData.put("isMobile", 1);
                    url = new URL(Network.API_RETRIEVE_OPENING_COURSE);
                }
                else {
                    jsonUserData.put("token", params[0]);
                    url = new URL(Network.API_RETRIEVE_STUDYING_OPENING_COURSE);
                }

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                try {
                    connection.setReadTimeout(10000);
                    connection.setConnectTimeout(15000);
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                    connection.setRequestProperty("Accept", "application/json");
                    connection.setDoInput(true);
                    connection.setDoOutput(true);

                    //write
                    OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
                    writer.write(jsonUserData.toString());
                    writer.flush();

                    //check http response code
                    int status = connection.getResponseCode();
                    switch (status){
                        case HttpURLConnection.HTTP_OK:
                            //read response
                            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

                            StringBuilder sb = new StringBuilder();
                            String line;

                            while ((line = bufferedReader.readLine()) != null) {
                                line = line + "\n";
                                sb.append(line);
                            }

                            bufferedReader.close();
                            strJsonResponse = sb.toString();

                            return HttpURLConnection.HTTP_OK;
                        default:
                            Toast.makeText(getParent(), connection.getResponseMessage(), Toast.LENGTH_LONG).show();
                            return 0;
                    }
                }
                finally{
                    connection.disconnect();
                }
            }
            catch(Exception e) {
                Toast.makeText(getParent(), e.getMessage(), Toast.LENGTH_LONG).show();
                return 0;
            }
        }

        @Override
        protected void onPostExecute(Integer status) {
            if (status != HttpURLConnection.HTTP_OK){
                progressDialog.dismiss();
                AppVariable.alert(MainActivity.this, "Cannot load opening course");
            }
            else {
                try{
                    JSONObject jsonObject = new JSONObject(strJsonResponse);
                    String result = jsonObject.getString("result");
                    if (result.equals("failure")){
                        progressDialog.dismiss();
                        AppVariable.alert(MainActivity.this, "Wrong Request");
                    }
                    else {
                        int length = Integer.valueOf(jsonObject.getString("length"));
                        JSONArray coursesJson = jsonObject.getJSONArray("opening_attendances");

                        ArrayList<String> courses = new ArrayList<>();
                        ArrayList<String> attendance = new ArrayList<>();
                        for (int i = 0; i < length; i++){
                            courses.add(coursesJson.getJSONObject(i).getString("class_has_course_id"));
                            attendance.add(coursesJson.getJSONObject(i).getString("attendance_id"));
                        }
                        db.updateOpeningCourse(db.getAllCourse(), courses, attendance, 1);
                        progressDialog.dismiss();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    progressDialog.dismiss();
                    AppVariable.alert(MainActivity.this, "Error when reading response in GetOpeningCourseTask");
                }
            }

            showChooseCourseFragment();
        }
    }
}
