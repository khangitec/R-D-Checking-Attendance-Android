package com.example.hoangdang.diemdanh.studentQuiz;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.hoangdang.diemdanh.R;
import com.example.hoangdang.diemdanh.SupportClass.AppVariable;
import com.example.hoangdang.diemdanh.SupportClass.Network;
import com.example.hoangdang.diemdanh.SupportClass.Question;
import com.example.hoangdang.diemdanh.SupportClass.SecurePreferences;

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

public class StudentQuizActivity extends AppCompatActivity {

    @BindView(R.id.quiz_ll)
    LinearLayout layout;
    int quiz_id;
    public SharedPreferences prefs;
    ProgressDialog progressDialog;
    ArrayList<Question> questions;
    int user_id;
    String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_quiz);

        prefs = new SecurePreferences(this);

        quiz_id = prefs.getInt(AppVariable.CURRENT_QUIZ_ID, 0);
        user_id = prefs.getInt(AppVariable.USER_ID, 0);
        token = prefs.getString(AppVariable.USER_TOKEN, null);

        ButterKnife.bind(this);

        this.setTitle("QUIZ");

        // prepare spinner
        progressDialog = new ProgressDialog(this, R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setCanceledOnTouchOutside(false);

        new GetQuizTask().execute(prefs.getString(AppVariable.USER_TOKEN, null), String.valueOf(quiz_id));
    }

    //UI

    private void buildUI() {
        LinearLayout.LayoutParams layoutParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);

        for (Question q: questions)
        {
            TextView textView = new TextView(this);
            textView.setText(q.text);
            textView.setTextSize(20);
            textView.setTextColor(ContextCompat.getColor(this, R.color.black));
            layout.addView(textView);

            TextInputLayout titleWrapper = new TextInputLayout(this);
            titleWrapper.setLayoutParams(layoutParams);
            titleWrapper.setHint("Answer");
            layout.addView(titleWrapper);

            EditText title = new EditText(this);
            title.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_MULTI_LINE);
            title.setLayoutParams(layoutParams);
            title.setTextColor(ContextCompat.getColor(this, R.color.black));
            titleWrapper.addView(title);
        }

        //set the properties for button
        Button btnTag = new Button(this);
        btnTag.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        btnTag.setText("Submit");
        btnTag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean done = validate();
                if (done){
                    InputMethodManager inputManager = (InputMethodManager)
                            getSystemService(Context.INPUT_METHOD_SERVICE);

                    inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                            InputMethodManager.HIDE_NOT_ALWAYS);
                    new SubmitQuizTask().execute(token, String.valueOf(user_id));
                }
            }
        });
        //add button to the layout
        layout.addView(btnTag);
    }

    private void displayToast(String toast) {
        if(toast != null) {
            Toast.makeText(this, toast, Toast.LENGTH_LONG).show();
        }
    }

    //Functions

    private EditText traverseEditTexts(ViewGroup v) {
        EditText invalid = null;
        int IQ = 0;
        for (int i = 0; i < v.getChildCount(); i++)
        {
            Object child = v.getChildAt(i);
            if (child instanceof TextInputLayout)
            {
                TextInputLayout e = (TextInputLayout)child;
                Object d = e.getChildAt(0);
                if (d instanceof FrameLayout){
                    FrameLayout x = (FrameLayout)d;
                    Object f = x.getChildAt(0);
                    if (f instanceof EditText){
                        EditText answerInput = (EditText)f;
                        if(answerInput.getText().length() == 0) // Whatever logic here to determine if valid.
                        {
                            invalid = answerInput;
                            return invalid;   // Stops at first invalid one. But you could add this to a list.
                        }
                        Question q = questions.get(IQ);
                        q.answer = answerInput.getText().toString();
                        questions.set(IQ++, q);
                    }
                }
            }
        }
        return invalid;
    }

    private boolean validate() {
        EditText emptyText = traverseEditTexts(layout);
        if(emptyText != null)
        {
            displayToast("You have to answer all questions");
            emptyText.requestFocus();
        }
        return emptyText == null;
    }

    //Network task

    private class GetQuizTask extends AsyncTask<String, Void, Integer> {

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
                URL url = new URL(Network.API_GET_QUIZ);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                try {
                    //prepare json data
                    JSONObject jsonUserData = new JSONObject();
                    jsonUserData.put("token", params[0]);
                    jsonUserData.put("quiz_id", params[1]);

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
               displayToast(exception.getMessage());
            }
            else {
                try{
                    JSONObject jsonObject = new JSONObject(strJsonResponse);
                    String result = jsonObject.getString("result");

                    if (result.equals("failure")){
                        String message = jsonObject.getString("message");
                        progressDialog.dismiss();
                        displayToast(message);
                        finish();
                        return;
                    }

                    int length = Integer.valueOf(jsonObject.getString("total_item"));
                    JSONObject quizJson = jsonObject.getJSONObject("quiz");
                    JSONArray questionsJson = quizJson.getJSONArray("questions");

                    questions = new ArrayList<>();
                    for (int i = 0; i < length; i++){
                        JSONObject q = questionsJson.getJSONObject(i);
                        questions.add(new Question(q.getInt("id"), q.getString("text")));
                    }

                    buildUI();

                    progressDialog.dismiss();
                    return;

                } catch (JSONException e) {
                    e.printStackTrace();
                    displayToast(e.getMessage());
                }
            }

            progressDialog.dismiss();
        }
    }

    private class SubmitQuizTask extends AsyncTask<String, Void, Integer> {

        private Exception exception;
        private String strJsonResponse;

        @Override
        protected void onPreExecute() {
            progressDialog.setMessage("Sending...");
            progressDialog.show();
        }

        @Override
        protected Integer doInBackground(String... params) {
            int flag = 0;
            try {
                URL url = new URL(Network.API_SUBMIT_QUIZ);

                //prepare json data
                JSONObject jsonUserData = new JSONObject();
                jsonUserData.put("token", params[0]);
                jsonUserData.put("student_id", params[1]);

                JSONArray questionArray = new JSONArray();
                for(Question q: questions){
                    JSONObject qJson = new JSONObject();
                    qJson.put("id", q.id);
                    qJson.put("answer", q.answer);
                    questionArray.put(qJson);
                }

                JSONObject quiz = new JSONObject();
                quiz.put("questions", questionArray);
                quiz.put("id", quiz_id);
                jsonUserData.put("quiz", quiz);

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
                displayToast(exception.getMessage());
            }
            else {
                try{
                    JSONObject jsonObject = new JSONObject(strJsonResponse);
                    String result = jsonObject.getString("result");

                    if (result.equals("failure")){
                        String message = jsonObject.getString("message");
                        progressDialog.dismiss();
                        displayToast(message);
                        return;
                    }

                    progressDialog.dismiss();
                    displayToast("Successfully");
                    finish();
                    return;

                } catch (JSONException e) {
                    e.printStackTrace();
                    displayToast(e.getMessage());
                }
            }
            progressDialog.dismiss();
        }
    }

    //Socket
}
