package com.example.hoangdang.diemdanh.studentQuiz;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.hoangdang.diemdanh.R;
import com.example.hoangdang.diemdanh.SupportClass.AppVariable;
import com.example.hoangdang.diemdanh.SupportClass.SecurePreferences;

import org.json.JSONObject;

import butterknife.ButterKnife;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class StudentQuizActivity extends AppCompatActivity implements View.OnClickListener {

    private LinearLayout mLilAnswer;
    private TextView mTxtWaiting;
    private TextView mTxtA;
    private TextView mTxtB;
    private TextView mTxtC;
    private TextView mTxtD;

    int quiz_id;
    public SharedPreferences prefs;
    ProgressDialog progressDialog;
    int user_id;
    int mQuestionIndex;
    int mQuizId;
    String token;
    Socket socket;

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

        //Layout
        mLilAnswer = (LinearLayout) findViewById(R.id.lil_answer);

        mTxtWaiting = (TextView) findViewById(R.id.txt_waiting);
        mTxtA = (TextView) findViewById(R.id.txt_a);
        mTxtB = (TextView) findViewById(R.id.txt_b);
        mTxtC = (TextView) findViewById(R.id.txt_c);
        mTxtD = (TextView) findViewById(R.id.txt_d);

        mTxtA.setOnClickListener(this);
        mTxtB.setOnClickListener(this);
        mTxtC.setOnClickListener(this);
        mTxtD.setOnClickListener(this);

        showQuestionsOrNot(false);

        openSocket();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        socket.disconnect();
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
                            showQuestionsOrNot(false);
                            progressDialog.show();
                        }
                    });
                }

            }).on("quizQuestionReady", new Emitter.Listener() {

                @Override
                public void call(Object... args) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.show();
                        }
                    });
                }

            }).on("quizQuestionLoaded", new Emitter.Listener() {

                @Override
                public void call(final Object... args) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.hide();

                            JSONObject obj = (JSONObject)args[0];

                            try {
                                mQuestionIndex = obj.getInt("question_index");
                                mQuizId = obj.getInt("quiz_code");
                            } catch (Exception e) {
                                Log.e("danh", e.toString());
                            }

                            showQuestionsOrNot(true);
                        }
                    });
                }

            }).on("quizQuestionEnded", new Emitter.Listener() {

                @Override
                public void call(Object... args) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.hide();
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

    private void showQuestionsOrNot(boolean isShow) {
        if (isShow) {
            mTxtWaiting.setText("Question " + mQuestionIndex + 1);
            mLilAnswer.setVisibility(View.VISIBLE);

        } else {
            mTxtWaiting.setText("You joined the quiz.\n Wait for teacher start the quiz");
            mLilAnswer.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.txt_a:
                didAnswerWithOption("a");
                break;

            case R.id.txt_b:
                didAnswerWithOption("b");
                break;

            case R.id.txt_c:
                didAnswerWithOption("c");
                break;

            case R.id.txt_d:
                didAnswerWithOption("d");
                break;
        }
    }

    private void didAnswerWithOption(String answer) {
        try {
            JSONObject obj = new JSONObject();
            obj.put("quiz_code", mQuizId);
            obj.put("question_index", mQuestionIndex);
            obj.put("option", answer);
            obj.put("student_id", user_id);

            socket.emit("answeredQuiz", obj);

        } catch (Exception e) {
            Toast.makeText(StudentQuizActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
        }

    }
}
