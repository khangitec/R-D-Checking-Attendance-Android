package com.example.hoangdang.diemdanh.teacherQuiz;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.example.hoangdang.diemdanh.R;
import com.example.hoangdang.diemdanh.SupportClass.AppVariable;
import com.example.hoangdang.diemdanh.SupportClass.SecurePreferences;

import org.json.JSONObject;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class TeacherQuizWaitingActivity extends AppCompatActivity {

    private ProgressDialog progressDialog;

    private Socket socket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_quiz_waiting);

        progressDialog = new ProgressDialog(this, R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setCanceledOnTouchOutside(false);

        openSocket();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        socket.disconnect();
    }

    private void endQuiz(String quizCode, String class_has_course_id) {
        Bundle bundle = new Bundle();
        bundle.putString("quiz_code", quizCode);
        bundle.putString("class_has_course_id", class_has_course_id);

        Intent intent = new Intent(TeacherQuizWaitingActivity.this, FinishStateActivity.class);
        intent.putExtras(bundle);
        startActivity(intent);
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

            }).on("quizCompletedMobile", new Emitter.Listener() {

                @Override
                public void call(final Object... args) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.hide();

                            JSONObject obj = (JSONObject) args[0];

                            try {
                                String quizCode = obj.getString("quiz_code");
                                String class_has_course_id = obj.getString("class_has_course_id");

                                endQuiz(quizCode, class_has_course_id);


                            } catch (Exception e) {
                                AppVariable.alert(TeacherQuizWaitingActivity.this, e.getMessage());
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
}
