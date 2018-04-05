package com.example.hoangdang.diemdanh.Fragments;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.hoangdang.diemdanh.R;
import com.example.hoangdang.diemdanh.SupportClass.AppVariable;
import com.example.hoangdang.diemdanh.SupportClass.DatabaseHelper;
import com.example.hoangdang.diemdanh.SupportClass.Network;
import com.example.hoangdang.diemdanh.SupportClass.SecurePreferences;
import com.example.hoangdang.diemdanh.SupportClass.User;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ProfileFragment extends Fragment {
    @BindView(R.id.first_name_tv)
    TextView _first_name;
    @BindView(R.id.last_name_tv)
    TextView _last_name;

    @BindView(R.id.stud_id_tv)
    TextView _stud_id;

    @BindView(R.id.role_tv)
    TextView _role;

    @BindView(R.id.email_tv)
    TextView _email;

    @BindView(R.id.phone_tv)
    TextView _phone;

    @BindView(R.id.btn_changePassword)
    Button _changePassword;

    @BindView(R.id.stud_id_ll)
    LinearLayout stud_id_ll;

    DatabaseHelper db;

    ProgressDialog progressDialog;

    private OnFragmentInteractionListener mListener;

    public ProfileFragment() {}

    public static ProfileFragment newInstance(String param1, String param2) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().setTitle("ACCOUNT");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        ButterKnife.bind(this, view);

        progressDialog = new ProgressDialog(getActivity(),
                R.style.AppTheme_Dark_Dialog);

        db = new DatabaseHelper(getActivity());
        User user = db.getUser();

        _first_name.setText(user.getFirstName());
        _last_name.setText(user.getLastName());
        if(user.getRole() == AppVariable.STUDENT_ROLE){
            _role.setText("Student");
            stud_id_ll.setVisibility(View.VISIBLE);
            _stud_id.setText(user.getEmail().substring(0,7));
        }
        else {
            _role.setText("Teacher");
        }
        _email.setText(user.getEmail());
        _phone.setText(user.getPhone());

        _changePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showChangePasswordDialog();
            }
        });

        return view;
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private void showChangePasswordDialog() {
        LayoutInflater li = LayoutInflater.from(getContext());
        View promptsView = li.inflate(R.layout.change_password, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                getContext());

        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(promptsView);

        final EditText currentPwd = (EditText) promptsView
                .findViewById(R.id.current_password_editText);


        final EditText newPwd = (EditText) promptsView
                .findViewById(R.id.new_password_editText);

        final CheckBox showPwd = (CheckBox) promptsView
                .findViewById(R.id.showPassword_chxbox);

        showPwd.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    currentPwd.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    newPwd.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                }
                else {
                    currentPwd.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    newPwd.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
                currentPwd.setSelection(currentPwd.getText().length());
                newPwd.setSelection(newPwd.getText().length());
            }
        });

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("Change",
                        new DialogInterface.OnClickListener() {
                            SharedPreferences pref = new SecurePreferences(getActivity());

                            public void onClick(DialogInterface dialog,int id) {
                                new UpdatePasswordTask().execute(
                                        pref.getString(AppVariable.USER_TOKEN, null),
                                        pref.getString(AppVariable.USER_ID, null),
                                        currentPwd.getText().toString(),
                                        newPwd.getText().toString());
                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                dialog.cancel();
                            }
                        });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

    private class UpdatePasswordTask extends AsyncTask<String, Void, Integer> {

        private Exception exception;
        private String strJsonResponse;

        @Override
        protected void onPreExecute() {
            progressDialog.setIndeterminate(true);
            progressDialog.setMessage("Changing...");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();
        }

        @Override
        protected Integer doInBackground(String... params) {
            try {
                URL url = new URL(Network.API_USER_CHANGE_PASSWORD);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                try {
                    JSONObject jsonUserData = new JSONObject();
                    jsonUserData.put("token", params[0]);
                    jsonUserData.put("confirm_password", params[3]);
                    jsonUserData.put("current_password", params[2]);
                    jsonUserData.put("new_password", params[3]);

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
                            exception = new Exception(connection.getResponseMessage());
                            return 0;
                    }
                }
                finally{
                    connection.disconnect();
                }
            }
            catch(Exception e) {
                exception = e;
                return 0;
            }
        }

        @Override
        protected void onPostExecute(Integer status) {
            if (status != HttpURLConnection.HTTP_OK){
                onChangePasswordFail(exception.getMessage());
            }
            else {
                try{
                    JSONObject jsonObject = new JSONObject(strJsonResponse);
                    String result = jsonObject.getString("result");
                    if (result.equals("failure")){
                        progressDialog.dismiss();
                        onChangePasswordFail("Wrong old password");
                        return;
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    onChangePasswordFail(e.getMessage());
                }
            }
            progressDialog.dismiss();
        }
    }

    public void onChangePasswordFail(String message){
        new AlertDialog.Builder(getActivity())
                .setTitle("Alert")
                .setMessage("Fail to change password: " + message)
                .setNegativeButton("close", null).show();
    }
}
