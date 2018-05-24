package com.example.hoangdang.diemdanh.teacherQuiz;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.view.View;
import android.widget.TextView;

import com.example.hoangdang.diemdanh.Fragments.CheckedFragment;
import com.example.hoangdang.diemdanh.MainActivity;
import com.example.hoangdang.diemdanh.R;
import com.example.hoangdang.diemdanh.SupportClass.AppVariable;
import com.example.hoangdang.diemdanh.SupportClass.Network;
import com.example.hoangdang.diemdanh.SupportClass.SecurePreferences;
import com.example.hoangdang.diemdanh.SupportClass.Student;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class FinishStateActivity extends AppCompatActivity implements View.OnClickListener {

    private ViewPager mViewPager;
    private TabLayout mTabLayout;
    private TextView mTxtParticipatedStudents;
    private TextView mTxtTotalStudents;
    private AppCompatButton mBtnClose;

    private CheckedFragment mCheckedFragment;
    private CheckedFragment mUncheckedFragment;
    private CheckedFragment mNoAttendFragment;

    private ProgressDialog progressDialog;

    private String mQuizCode;
    private String class_has_course_id;

    private String mStrTotalStudent = "Total Student: ";
    private String mStrParticipantStudent = "Participated Students: ";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finish_state);

        mQuizCode = getIntent().getExtras().getString("quiz_code");
        class_has_course_id = getIntent().getExtras().getString("class_has_course_id");

        progressDialog = new ProgressDialog(this, R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setMessage("Loading...");

        mTxtParticipatedStudents = (TextView) findViewById(R.id.txt_participated_students);
        mTxtParticipatedStudents.setText(mStrParticipantStudent);

        mTxtTotalStudents = (TextView) findViewById(R.id.txt_total_students);
        mTxtTotalStudents.setText(mStrTotalStudent);

        mBtnClose = (AppCompatButton) findViewById(R.id.btn_close);
        mBtnClose.setOnClickListener(this);

        mViewPager = (ViewPager) findViewById(R.id.viewpager);

        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());

        // Add Fragments to adapter one by one
        mCheckedFragment = new CheckedFragment();
        mUncheckedFragment = new CheckedFragment();
        mNoAttendFragment = new CheckedFragment();

        adapter.addFragment(mCheckedFragment, "Checked");
        adapter.addFragment(mUncheckedFragment, "Unchecked");
        adapter.addFragment(mNoAttendFragment, "No Attend");
        mViewPager.setAdapter(adapter);

        mTabLayout = (TabLayout) findViewById(R.id.tabs);
        mTabLayout.setupWithViewPager(mViewPager);
    }

    @Override
    protected void onStart() {
        super.onStart();

        SharedPreferences pref = new SecurePreferences(FinishStateActivity.this);
        new GetQuizResultTask().execute(
                pref.getString(AppVariable.USER_TOKEN, null),
                mQuizCode,
                class_has_course_id);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_close:
                Intent intent = new Intent(FinishStateActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                break;
        }
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

    private ArrayList<Student> getStudentListFromJSONArray(JSONObject jsonObject, String key) {
        ArrayList<Student> studentList = new ArrayList<>();

        try {
            JSONArray participantArray = jsonObject.getJSONArray(key);
            for (int i = 0; i < participantArray.length(); i++) {
                JSONObject studentJSON = participantArray.getJSONObject(i);

                Student student = new Student();
                student.stud_id = studentJSON.getString("stud_id");
                student.first_name = studentJSON.getString("first_name");
                student.last_name = studentJSON.getString("last_name");

                studentList.add(student);
            }

        } catch (Exception e) {
            AppVariable.alert(FinishStateActivity.this, e.getMessage());
        }

        return studentList;
    }

    private class GetQuizResultTask extends AsyncTask<String, Void, Integer> {

        private Exception exception;
        private String strJsonResponse;

        @Override
        protected void onPreExecute() {
            progressDialog.show();
        }

        @Override
        protected Integer doInBackground(String... params) {
            int flag = 0;
            try {
                URL url = new URL(Network.API_QUIZ_RESULT);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                try {
                    //prepare json data
                    JSONObject jsonUserData = new JSONObject();
                    jsonUserData.put("token", params[0]);
                    jsonUserData.put("quiz_code", params[1]);
                    jsonUserData.put("class_has_course_id", params[2]);

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
                                sb.append(line).append("\n");
                            }

                            bufferedReader.close();
                            strJsonResponse = sb.toString();

                            flag = HttpURLConnection.HTTP_OK;
                        default:
                            exception = new Exception(connection.getResponseMessage());
                    }
                }
                finally{
                    connection.disconnect();
                }
            }
            catch(Exception e) {
                exception = e;
            }
            return flag;
        }

        @Override
        protected void onPostExecute(Integer status) {
            progressDialog.dismiss();

            if (status != HttpURLConnection.HTTP_OK){
                AppVariable.alert(FinishStateActivity.this, exception.getMessage());
            }
            else {
                try{
                    JSONObject jsonObject = new JSONObject(strJsonResponse);

                    String result = jsonObject.getString("result");
                    if (result.equals("failure")) {
                        progressDialog.dismiss();
                        AppVariable.alert(FinishStateActivity.this, null);
                        return;
                    }

                    ArrayList<Student> checkedStudentList = getStudentListFromJSONArray(jsonObject, "checked_student_list");
                    mCheckedFragment.setStudentList(checkedStudentList);

                    ArrayList<Student> unCheckedStudentList = getStudentListFromJSONArray(jsonObject, "unchecked_student_list");
                    mUncheckedFragment.setStudentList(unCheckedStudentList);

                    ArrayList<Student> noAttendStudentList = getStudentListFromJSONArray(jsonObject, "not_participate_list");
                    mNoAttendFragment.setStudentList(noAttendStudentList);

                    int participantCount = checkedStudentList.size() + unCheckedStudentList.size();
                    int total = participantCount + noAttendStudentList.size();

                    mTxtParticipatedStudents.setText(mStrParticipantStudent + participantCount);
                    mTxtTotalStudents.setText(mStrTotalStudent + total);

                    return;

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
