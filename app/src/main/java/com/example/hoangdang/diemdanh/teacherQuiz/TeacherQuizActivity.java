package com.example.hoangdang.diemdanh.teacherQuiz;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.hoangdang.diemdanh.R;
import com.example.hoangdang.diemdanh.SupportClass.AppVariable;
import com.example.hoangdang.diemdanh.SupportClass.Network;
import com.example.hoangdang.diemdanh.SupportClass.Question;
import com.example.hoangdang.diemdanh.SupportClass.QuizModel;
import com.example.hoangdang.diemdanh.SupportClass.SecurePreferences;
import com.example.hoangdang.diemdanh.SupportClass.Student;
import com.example.hoangdang.diemdanh.adapters.JoinedStudentsAdapter;

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

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

@RequiresApi(api = Build.VERSION_CODES.M)
public class TeacherQuizActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView mImvBack;
    private TextView mTxtTitle;
    private TextView mTxtQuizType;
    private TextView mTxtQuizCode;
    private RecyclerView mRecyclerStudents;
    private AppCompatButton mBtnStart;
    private ProgressDialog progressDialog;

    private List<Student> mStudentList;

    private JoinedStudentsAdapter mAdapter;
    private Socket socket;
    private QuizModel mQuizModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_quiz);

        mQuizModel = new QuizModel();

        progressDialog = new ProgressDialog(this, R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setMessage("Loading...");

        mImvBack = (ImageView) findViewById(R.id.imv_back);
        mImvBack.setOnClickListener(this);

        mTxtQuizType = (TextView) findViewById(R.id.txt_quiztype);

        mTxtQuizCode = (TextView) findViewById(R.id.txt_quizcode);
        mTxtQuizCode.setText("");

        mTxtTitle = (TextView) findViewById(R.id.txt_title);

        mBtnStart = (AppCompatButton) findViewById(R.id.btn_start);
        mBtnStart.setVisibility(View.GONE);
        mBtnStart.setOnClickListener(this);

        setDataForQuizType("");
        initJoinedStudentList();

        openSocket();
    }

    private void setDataForQuizType(String quizType) {
        SpannableStringBuilder builder = new SpannableStringBuilder();

        String black = "Quiz Code - ";
        SpannableString blackSpannable= new SpannableString(black);
        blackSpannable.setSpan(new ForegroundColorSpan(Color.BLACK), 0, black.length(), 0);
        blackSpannable.setSpan(new StyleSpan(Typeface.BOLD), 0, black.length(), 0);
        builder.append(blackSpannable);

        String blue = "Quiz Type";
        if (!quizType.equals("")) {
            blue = quizType;
        }
        SpannableString blueSpannable= new SpannableString(blue);
        blueSpannable.setSpan(new ForegroundColorSpan(getColor(R.color.primary_dark)), 0, blue.length(), 0);
        blueSpannable.setSpan(new StyleSpan(Typeface.BOLD), 0, blue.length(), 0);
        builder.append(blueSpannable);

        mTxtQuizType.setText(builder, TextView.BufferType.SPANNABLE);
    }

    private void initJoinedStudentList() {
        mStudentList = new ArrayList<>();

        mRecyclerStudents = (RecyclerView) findViewById(R.id.recyclerView_students);

        mAdapter = new JoinedStudentsAdapter(mStudentList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        mRecyclerStudents.setLayoutManager(mLayoutManager);
        mRecyclerStudents.setItemAnimator(new DefaultItemAnimator());
        mRecyclerStudents.setAdapter(mAdapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        socket.disconnect();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imv_back:
                finish();
                break;

            case R.id.btn_start:
                emitStartQuiz();
                moveToTeacherWaitingScreen();
                break;
        }
    }

    private void emitStartQuiz() {
        JSONObject object = new JSONObject();
        try {
            object.put("quiz_code", mQuizModel.code);
        } catch (JSONException e) {
            AppVariable.alert(TeacherQuizActivity.this, e.getMessage());
        }

        socket.emit("mobileStartedQuiz", object);
    }

    private void moveToTeacherWaitingScreen() {
        startActivity(new Intent(TeacherQuizActivity.this, TeacherQuizWaitingActivity.class));
    }

    private void openSocket() {
        try {
            socket = IO.socket("https://iteccyle8.herokuapp.com/");
            socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {

                @Override
                public void call(Object... args) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.show();
                        }
                    });
                }

            }).on("webPublishedQuiz", new Emitter.Listener() {

                @Override
                public void call(final Object... args) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            JSONObject obj = (JSONObject) args[0];

                            try {
                                String quizCode = obj.getString("quiz_code");

                                SharedPreferences pref = new SecurePreferences(TeacherQuizActivity.this);

                                progressDialog.show();
                                new GetPublishQuizTask().execute(
                                        pref.getString(AppVariable.USER_TOKEN, null),
                                        quizCode);

                            } catch (Exception e) {
                                AppVariable.alert(TeacherQuizActivity.this, e.getMessage());
                            }
                        }
                    });
                }

            }).on("joinedQuiz", new Emitter.Listener() {

                @Override
                public void call(final Object... args) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.hide();

                            JSONObject obj = (JSONObject) args[0];

                            try {
                                String quizCode = obj.getString("quiz_code");

                                SharedPreferences pref = new SecurePreferences(TeacherQuizActivity.this);

                                progressDialog.show();
                                new GetPublishQuizTask().execute(
                                        pref.getString(AppVariable.USER_TOKEN, null),
                                        quizCode);

                            } catch (Exception e) {
                                AppVariable.alert(TeacherQuizActivity.this, e.getMessage());
                            }
                        }
                    });
                }

            }).on("portalStartedQuiz", new Emitter.Listener() {

                @Override
                public void call(final Object... args) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.hide();

                            JSONObject obj = (JSONObject) args[0];

                            try {
                                String quizCode = obj.getString("quiz_code");

                                SharedPreferences pref = new SecurePreferences(TeacherQuizActivity.this);

                                if (quizCode.equals(mQuizModel.code)) {
                                    new StartQuizTask().execute(
                                            pref.getString(AppVariable.USER_TOKEN, null),
                                            quizCode);
                                }

                            } catch (Exception e) {
                                AppVariable.alert(TeacherQuizActivity.this, e.getMessage());
                            }
                        }
                    });
                }

            }).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {

                @Override
                public void call(Object... args) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.hide();
                        }
                    });
                }

            });
            socket.connect();

        } catch (Exception e) {
            System.out.print(e);
        }
    }

    private class GetPublishQuizTask extends AsyncTask<String, Void, Integer> {

        private Exception exception;
        private String strJsonResponse;

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected Integer doInBackground(String... params) {
            int flag = 0;
            try {
                URL url = new URL(Network.API_GET_PUBLISH_QUIZ);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                try {
                    //prepare json data
                    JSONObject jsonUserData = new JSONObject();
                    jsonUserData.put("token", params[0]);
                    jsonUserData.put("quiz_code", params[1]);

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
            if (status != HttpURLConnection.HTTP_OK){
                AppVariable.alert(TeacherQuizActivity.this, exception.getMessage());
            }
            else {
                try{
                    JSONObject jsonObject = new JSONObject(strJsonResponse);
                    String result = jsonObject.getString("result");
                    if (result.equals("failure")) {
                        progressDialog.dismiss();
                        AppVariable.alert(TeacherQuizActivity.this, null);
                        return;
                    }

                    JSONObject quizJSON = jsonObject.getJSONObject("quiz");

                    mQuizModel.class_has_course_id = quizJSON.getInt("class_has_course_id");
                    mQuizModel.code = quizJSON.getString("code");
                    mQuizModel.required_correct_answers = quizJSON.getString("required_correct_answers");
                    mQuizModel.title = quizJSON.getString("title");
                    mQuizModel.type = quizJSON.getString("type");

                    mQuizModel.participants = new ArrayList<>();

                    JSONArray participantArray = quizJSON.getJSONArray("participants");
                    for (int i = 0; i < participantArray.length(); i++) {
                        JSONObject studentJSON = participantArray.getJSONObject(i);

                        Student student = new Student();
                        student.iID = studentJSON.getInt("id");
                        student.strCode = studentJSON.getString("code");
                        student.strName = studentJSON.getString("name");

                        mQuizModel.participants.add(student);
                    }
                    mStudentList = mQuizModel.participants;
                    mAdapter.setStudentList(mStudentList);

                    mQuizModel.questions = new ArrayList<>();

                    JSONArray questionArray = quizJSON.getJSONArray("questions");
                    for (int i = 0; i < questionArray.length(); i++) {
                        JSONObject questionJSON = questionArray.getJSONObject(i);

                        Question question = new Question();
                        question.text = questionJSON.getString("text");
                        question.option_a = questionJSON.getString("option_a");
                        question.option_b = questionJSON.getString("option_b");
                        question.option_c = questionJSON.getString("option_c");
                        question.option_d = questionJSON.getString("option_d");
                        question.correct_option = questionJSON.getString("correct_option");
                        question.timer = questionJSON.getInt("timer");

                        mQuizModel.questions.add(question);
                    }

                    // Update for layout
                    mTxtQuizCode.setText(mQuizModel.code);

                    String quizType = (mQuizModel.type.equals("0")) ? "Academic" : "Miscellaneous";
                    setDataForQuizType(quizType);

                    if (mQuizModel.participants.size() > 0) {
                        mBtnStart.setVisibility(View.VISIBLE);
                        progressDialog.hide();
                    }

                    return;

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class StartQuizTask extends AsyncTask<String, Void, Integer> {

        private Exception exception;
        private String strJsonResponse;

        @Override
        protected void onPreExecute() {
            progressDialog.setMessage("Loading...");
        }

        @Override
        protected Integer doInBackground(String... params) {
            int flag = 0;
            try {
                URL url = new URL(Network.API_QUIZ_START);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                try {
                    //prepare json data
                    JSONObject jsonUserData = new JSONObject();
                    jsonUserData.put("token", params[0]);
                    jsonUserData.put("quiz_code", params[1]);

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
                AppVariable.alert(TeacherQuizActivity.this, exception.getMessage());
                return;
            } else {
                try{
                    JSONObject jsonObject = new JSONObject(strJsonResponse);
                    String result = jsonObject.getString("result");
                    if (result.equals("failure")) {
                        progressDialog.dismiss();
                        AppVariable.alert(TeacherQuizActivity.this, "server failure");
                        return;
                    }

                    moveToTeacherWaitingScreen();

                } catch (JSONException e) {
                    AppVariable.alert(TeacherQuizActivity.this, e.toString());
                }
            }
        }
    }
}
