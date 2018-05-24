package com.example.hoangdang.diemdanh.teacherQuiz;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.hoangdang.diemdanh.Fragments.CheckedFragment;
import com.example.hoangdang.diemdanh.R;
import com.example.hoangdang.diemdanh.SelectImageActivity;
import com.example.hoangdang.diemdanh.SupportClass.AppVariable;
import com.example.hoangdang.diemdanh.SupportClass.Network;
import com.example.hoangdang.diemdanh.SupportClass.SecurePreferences;
import com.example.hoangdang.diemdanh.SupportClass.Student;
import com.example.hoangdang.diemdanh.imgurmodel.ImageResponse;
import com.example.hoangdang.diemdanh.imgurmodel.Upload;
import com.example.hoangdang.diemdanh.studentQuiz.PersonActivity;
import com.example.hoangdang.diemdanh.utils.DocumentHelper;
import com.example.hoangdang.diemdanh.utils.ImageHelper;
import com.example.hoangdang.diemdanh.utils.StorageHelper;
import com.example.hoangdang.diemdanh.utils.UploadService;
import com.microsoft.projectoxford.face.FaceServiceClient;
import com.microsoft.projectoxford.face.FaceServiceRestClient;
import com.microsoft.projectoxford.face.contract.Face;
import com.microsoft.projectoxford.face.contract.IdentifyResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class FaceDetectionActivity extends AppCompatActivity implements View.OnClickListener, CheckedFragment.CheckFragmentListener {

    private static final int REQUEST_SELECT_IMAGE = 0;

    private ImageView mImvBack;
    private Button mBtnSelectImage;
    private Button mBtnIdentify;
    private Button mBtnSubmit;
    private ProgressDialog progressDialog;
    private ViewPager mViewPager;
    private TabLayout mTabLayout;

    private ArrayList<Student> mStudentList;
//    private ArrayList<Uri> mImageUriList;
    private ArrayList<String> mImagePathList;
    private ArrayList<String> mImageUriRealPathList;

    private CheckedFragment mPresentFragment;
    private CheckedFragment mAbsentFragment;

    private Bitmap mBitmap;
    private FaceServiceClient mFaceServiceClient;
    private FaceListAdapter mFaceListAdapter;
//    private boolean detected;
    private String mPersonGroupId = "hcmus-face";
    private String mUserToken;
    private int mAttendanceId;
    private int mCount;
    private Upload upload; // Upload object containging image and meta data
    private Dialog mReplaceStudentDialog;

    private int mStudentIndexToReplace;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_detection);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]
                    {
                            Manifest.permission.CAMERA,
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                    }, 123);
        }

        mUserToken = getIntent().getExtras().getString("userToken");
        mAttendanceId = getIntent().getExtras().getInt("attendanceID");

        mStudentList = new ArrayList<>();
//        mImageUriList = new ArrayList<>();
        mImagePathList = new ArrayList<>();
        mImageUriRealPathList = new ArrayList<>();

//        detected = false;

        mFaceServiceClient = new FaceServiceRestClient("https://westcentralus.api.cognitive.microsoft.com/face/v1.0/","18db52d47bc5483f92d687a957c40c98");

        progressDialog = new ProgressDialog(this, R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setMessage("Loading...");

        mImvBack = (ImageView) findViewById(R.id.imv_back);
        mImvBack.setOnClickListener(this);

        mBtnSelectImage = (Button) findViewById(R.id.btn_select_image);
        mBtnSelectImage.setOnClickListener(this);

        mBtnIdentify = (Button) findViewById(R.id.identify);
        mBtnIdentify.setOnClickListener(this);

        mBtnSubmit = (Button) findViewById(R.id.done_and_save);
        mBtnSubmit.setOnClickListener(this);

        mFaceListAdapter = new FaceListAdapter();

        initCheckViewPager();

        new GetStudentTask().execute(mUserToken, "" + mAttendanceId);
    }

    private void initCheckViewPager() {
        mPresentFragment = new CheckedFragment();
        mPresentFragment.mListener = this;

        mAbsentFragment = new CheckedFragment();

        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());

        adapter.addFragment(mPresentFragment, "Present");
        adapter.addFragment(mAbsentFragment, "Absent");

        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        mViewPager.setAdapter(adapter);

        mTabLayout = (TabLayout) findViewById(R.id.tabs);
        mTabLayout.setupWithViewPager(mViewPager);
    }

    @Override
    public void onItemCheckClick(int position) {
        mStudentIndexToReplace = position;
        showRepleceStudentDialog();
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imv_back:
                finish();
                break;

            case R.id.btn_select_image:
                Intent intent = new Intent(this, SelectImageActivity.class);
                startActivityForResult(intent, REQUEST_SELECT_IMAGE);
                break;

            case R.id.identify:
                identify();
                break;

            case R.id.done_and_save:
                clickSubmit();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SELECT_IMAGE) {
            if(resultCode == RESULT_OK) {
//                detected = true;

                mBitmap = ImageHelper.loadSizeLimitedBitmapFromUri(data.getData(), getContentResolver());
                if (mBitmap != null) {
//                    Uri uriImagePicked = data.getData();
//                    mImageUriList.add(uriImagePicked);
                    mImageUriRealPathList.add(getRealPathFromURI(data.getData()));

                    View originalFaces = findViewById(R.id.all_faces);
                    originalFaces.setVisibility(View.VISIBLE);

                    // Show the result of face grouping.
//                    ListView groupedFaces = (ListView) findViewById(R.id.grouped_faces);
//                    FaceGroupsAdapter faceGroupsAdapter = new FaceGroupsAdapter(null);
//                    groupedFaces.setAdapter(faceGroupsAdapter);

                    // Put the image into an input stream for detection.
                    ByteArrayOutputStream output = new ByteArrayOutputStream();
                    mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, output);
                    ByteArrayInputStream inputStream = new ByteArrayInputStream(output.toByteArray());

                    // Start a background task to detect faces in the image.
                    new DetectionTask().execute(inputStream);
                }
            }
        }
    }

    private String getRealPathFromURI(Uri contentUri) {

        // can post image
        String [] proj={MediaStore.Images.Media.DATA};
        Cursor cursor = managedQuery( contentUri,
                proj, // Which columns to return
                null,       // WHERE clause; which rows to return (all rows)
                null,       // WHERE clause selection arguments (none)
                null); // Order-by clause (ascending by name)
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();

        return cursor.getString(column_index);
    }

    void setUiAfterDetection(Face[] result) {
        progressDialog.dismiss();


        if (result != null) {
            // Show the detailed list of original faces.
            mFaceListAdapter.addFaces(result);
            GridView listView = (GridView) findViewById(R.id.all_faces);
            listView.setAdapter(mFaceListAdapter);

            TextView textView = (TextView) findViewById(R.id.text_all_faces);
            textView.setText(String.format(
                    "%d face%s in total",
                    mFaceListAdapter.faces.size(),
                    mFaceListAdapter.faces.size() != 1 ? "s" : ""));
        } else {
            AppVariable.alert(FaceDetectionActivity.this, "No face to detect");
            return;
        }
    }

    // Called when the "Detect" button is clicked.
    private void identify() {
        // Start detection task only if the image to detect is selected.
        if (mPersonGroupId != null) {
            // Start a background task to identify faces in the image.
            List<UUID> faceIds = new ArrayList<>();
            for (Face face:  mFaceListAdapter.faces) {
                faceIds.add(face.faceId);
            }

            new IdentificationTask(mPersonGroupId).execute(
                    faceIds.toArray(new UUID[faceIds.size()]));
        } else {
            // Not detected or person group exists.
            AppVariable.alert(FaceDetectionActivity.this, "Please select an image and create a person group first.");
        }
    }

    // Background task for face detection
    class DetectionTask extends AsyncTask<InputStream, String, Face[]> {
        private boolean mSucceed = true;
        private String mError = "";
        @Override
        protected Face[] doInBackground(InputStream... params) {
            try {
                publishProgress("Detecting...");

                // Start detection.
                return mFaceServiceClient.detect(
                        params[0],  /* Input stream of image to detect */
                        true,       /* Whether to return face ID */
                        false,       /* Whether to return face landmarks */
                        /* Which face attributes to analyze, currently we support:
                           age,gender,headPose,smile,facialHair */
                        null);
            }  catch (Exception e) {
                mSucceed = false;
                mError = e.toString();

                return null;
            }
        }

        @Override
        protected void onPreExecute() {
            progressDialog.show();
        }

        @Override
        protected void onProgressUpdate(String... values) {
            progressDialog.setMessage(values[0]);
        }

        @Override
        protected void onPostExecute(Face[] result) {
            if (!mSucceed) {
                AppVariable.alert(FaceDetectionActivity.this, mError);
                return;
            }

            setUiAfterDetection(result);
        }
    }

    private class GetStudentTask extends AsyncTask<String, Void, Integer> {

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
                URL url = new URL(Network.API_RETRIEVE_STUDENT);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                try {
                    //prepare json data
                    JSONObject jsonUserData = new JSONObject();
                    jsonUserData.put("token", params[0]);
                    jsonUserData.put("attendance_id", params[1]);

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
                AppVariable.alert(FaceDetectionActivity.this, exception.getMessage());
            }
            else {
                try{
                    JSONObject jsonObject = new JSONObject(strJsonResponse);
                    String result = jsonObject.getString("result");
                    if (result.equals("failure")){
                        progressDialog.dismiss();
                        AppVariable.alert(FaceDetectionActivity.this, result.toString());
                        return;
                    }

                    int length = Integer.valueOf(jsonObject.getString("length"));
                    JSONArray studentsJson = jsonObject.getJSONArray("check_attendance_list");

                    mStudentList.removeAll(mStudentList);

                    for (int i = 0; i < length; i++){
                        JSONObject s = studentsJson.getJSONObject(i);

                        Student student = new Student();
                        student.strCode = s.getString("code");
                        student.status = s.getInt("status");
                        student.name = s.getString("name");
                        student.student_id = s.getString("student_id");
                        student.attendance_id = s.getString("attendance_id");
                        student.person_id = s.getString("person_id");
                        student.avatar = s.getString("avatar");

                        mStudentList.add(student);
                    }
                    mAbsentFragment.setStudentList(mStudentList);

                    return;

                } catch (JSONException e) {
                    AppVariable.alert(FaceDetectionActivity.this, e.toString());
                }
            }
        }
    }

    private class FaceListAdapter extends BaseAdapter {
        // The detected faces.
        List<Face> faces;

        // The thumbnails of detected faces.
        List<Bitmap> faceThumbnails;

        Map<UUID, Bitmap> faceIdThumbnailMap;

        FaceListAdapter() {
            faces = new ArrayList<>();
            faceThumbnails = new ArrayList<>();
            faceIdThumbnailMap = new HashMap<>();
        }

        public void addFaces(Face[] detectionResult) {
            if (detectionResult != null) {
                List<Face> detectedFaces = Arrays.asList(detectionResult);
                for (Face face: detectedFaces) {
                    faces.add(face);
                    try {
                        Bitmap faceThumbnail = ImageHelper.generateFaceThumbnail(mBitmap, face.faceRectangle);
                        faceThumbnails.add(faceThumbnail);
                        faceIdThumbnailMap.put(face.faceId, faceThumbnail);
                    } catch (IOException e) {
                        // Show the exception when generating face thumbnail fails.
                        TextView textView = (TextView)findViewById(R.id.info);
                        textView.setText(e.getMessage());
                    }
                }
            }
        }

        @Override
        public int getCount() {
            return faces.size();
        }

        @Override
        public Object getItem(int position) {
            return faces.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater layoutInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = layoutInflater.inflate(R.layout.item_face, parent, false);
            }
            convertView.setId(position);

            // Show the face thumbnail.
            ((ImageView)convertView.findViewById(R.id.image_face)).setImageBitmap(faceThumbnails.get(position));

            return convertView;
        }
    }

    // Background task of face identification.
    private class IdentificationTask extends AsyncTask<UUID, String, IdentifyResult[]> {
        private boolean mSucceed = true;
        String mPersonGroupId;
        IdentificationTask(String personGroupId) {
            this.mPersonGroupId = personGroupId;
        }

        @Override
        protected IdentifyResult[] doInBackground(UUID... params) {
            try{
                // Start identification.
                return mFaceServiceClient.identityInLargePersonGroup(
                        this.mPersonGroupId,   /* personGroupId */
                        params,                  /* faceIds */
                        1);                      /* maxNumOfCandidatesReturned */
            }  catch (Exception e) {
                mSucceed = false;
                publishProgress(e.getMessage());
                return null;
            }
        }

        @Override
        protected void onPreExecute() {
            progressDialog.show();
        }

        @Override
        protected void onProgressUpdate(String... values) {
            progressDialog.setMessage(values[0]);
        }

        @Override
        protected void onPostExecute(IdentifyResult[] result) {
            // Show the result on screen when detection is done.
            setUiAfterIdentification(result, mSucceed);
        }
    }

    // Show the result on screen when detection is done.
    private void setUiAfterIdentification(IdentifyResult[] result, boolean succeed) {
        progressDialog.dismiss();

//        setAllButtonsEnabledStatus(true);
        setIdentifyButtonEnabledStatus(false);

        //TODO

        if (succeed) {
            if (result != null) {
                ArrayList<Student> checkedList = new ArrayList<>();
                ArrayList<Student> unCheckedList = new ArrayList<>();

                unCheckedList.addAll(mStudentList);

                for (int i = 0; i < mStudentList.size(); i++) {
                    Student student = mStudentList.get(i);

                    for (IdentifyResult identifyResult: result) {
                        String faceId = identifyResult.faceId.toString();

                        if (identifyResult.candidates != null) {
                            for (int j = 0; j < identifyResult.candidates.size(); j++) {
                                String personId = identifyResult.candidates.get(0).personId.toString();

                                if (isPersonIdExistInList(unCheckedList, personId) &&
                                        personId.equals(student.person_id)) {

                                    student.thumbnail = mFaceListAdapter.faceIdThumbnailMap.get(identifyResult.faceId);

                                    checkedList.add(student);
                                    unCheckedList.remove(student);
                                }
                            }
                        }

                    }
                }

                Log.d("sdfdsf", "dfdfdf");
                mPresentFragment.setStudentList(checkedList);
                mAbsentFragment.setStudentList(unCheckedList);

//                mFaceListAdapter.setIdentificationResult(result);
//
//                String logString = "Response: Success. ";
//                for (IdentifyResult identifyResult: result) {
//                    logString += "Face " + identifyResult.faceId.toString() + " is identified as "
//                            + (identifyResult.candidates.size() > 0
//                            ? identifyResult.candidates.get(0).personId.toString()
//                            : "Unknown Person")
//                            + ". ";
//                }
//                addLog(logString);
//
//                // Show the detailed list of detected faces.
//                ListView listView = (ListView) findViewById(R.id.list_identified_faces);
//                listView.setAdapter(mFaceListAdapter);
            }
        }
    }

    private boolean isPersonIdExistInList(ArrayList<Student> students, String personId) {
        for (int i = 0; i < students.size(); i++) {
            Student student = students.get(i);
            if (student.person_id != null && student.person_id.equals(personId)) {
                return true;
            }
        }

        return false;
    }

    // Set the group button is enabled or not.
    private void setIdentifyButtonEnabledStatus(boolean isEnabled) {
        Button button = (Button) findViewById(R.id.identify);
        button.setEnabled(isEnabled);
    }

    private void clickSubmit() {
        mCount = 0;
        uploadFaceToServer();
    }

    private void createUpload(File image, String faceId) {
        upload = new Upload();

        upload.image = image;
        upload.title = faceId;
        upload.description = "";
    }

    private void uploadFaceToServer() {
        if (mCount < mImageUriRealPathList.size()) {
            String filePath = mImageUriRealPathList.get(mCount);
            if (filePath == null || filePath.isEmpty()) return;
            File file = new File(filePath);

            createUpload(file, "" + mCount);

            progressDialog.setMessage("Uploading ...");
            progressDialog.show();
            new UploadService(this).Execute(upload, new FaceDetectionActivity.UiCallback());

//            String faceId = faceGridViewAdapter.faceIdList.get(mCount);
//            String parentFaceUriStr = StorageHelper.getParentFaceUri(faceId, this);
//            Uri parentFaceUri = Uri.parse(parentFaceUriStr);
//            String filePath = DocumentHelper.getPath(this, parentFaceUri);
//            if (filePath == null || filePath.isEmpty()) return;
//            File file = new File(filePath);
//
//            createUpload(file, faceId);
//
//            progressDialog.show();
//            new UploadService(this).Execute(upload, new PersonActivity.UiCallback());
        } else {
            SharedPreferences pref = new SecurePreferences(FaceDetectionActivity.this);

            new SubmitTask().execute(pref.getString(AppVariable.USER_TOKEN, null));
        }
    }

    private class UiCallback implements Callback<ImageResponse> {

        @Override
        public void success(ImageResponse imageResponse, Response response) {
            progressDialog.hide();
            SharedPreferences pref = new SecurePreferences(FaceDetectionActivity.this);

            String link = imageResponse.data.link;
            mImagePathList.add(link);

            mCount++;
            uploadFaceToServer();

//            new PersonActivity.UploadFaceForStudentTask().execute(
//                    pref.getString(AppVariable.USER_TOKEN, null),
//                    personId,
//                    faceId,
//                    link);
        }

        @Override
        public void failure(RetrofitError error) {
            progressDialog.hide();
            //Assume we have no connection, since error is null
            if (error == null) {
                AppVariable.alert(FaceDetectionActivity.this,"No internet connection");
            }
        }
    }

    private class SubmitTask extends AsyncTask<String, Void, Integer> {

        private Exception exception;
        private String strJsonResponse;

        @Override
        protected void onPreExecute() {
            progressDialog.setMessage("Loading...");
            progressDialog.show();
        }

        @Override
        protected Integer doInBackground(String... params) {
            int flag = 0;
            try {
                URL url = new URL(Network.API_SUBMIT);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                try {
                    JSONArray studentArray = new JSONArray(mPresentFragment.getStudentIdList());
                    JSONArray imagePathArray = new JSONArray(mImagePathList);

                    //prepare json data
                    JSONObject jsonUserData = new JSONObject();
                    jsonUserData.put("token", params[0]);
                    jsonUserData.put("students", studentArray);
                    jsonUserData.put("attendance_id", mAttendanceId);
                    jsonUserData.put("attendance_type", 4);
                    jsonUserData.put("attendance_img", imagePathArray);

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
                AppVariable.alert(FaceDetectionActivity.this, exception.getMessage());
                return;
            }

//            AppVariable.alert(FaceDetectionActivity.this, "Submit successfully");
            finish();
        }
    }

    //TODO
    private void showRepleceStudentDialog() {
        if (mReplaceStudentDialog != null) {
            mReplaceStudentDialog.cancel();
            mReplaceStudentDialog = null;
        }
        mReplaceStudentDialog = new Dialog(this);
        mReplaceStudentDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mReplaceStudentDialog.setContentView(R.layout.fragment_checked);
        mReplaceStudentDialog.setCanceledOnTouchOutside(true);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(mReplaceStudentDialog.getWindow().getAttributes());
        lp.width = width * 2 / 3;
        lp.height = height * 2 / 3;
        mReplaceStudentDialog.getWindow().setAttributes(lp);

        RecyclerView recyclerView = (RecyclerView) mReplaceStudentDialog.findViewById(R.id.recycler_students);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new StudentAdapter());

        mReplaceStudentDialog.show();
    }

    private class StudentAdapter extends RecyclerView.Adapter<StudentHolder> {

        @Override
        public StudentHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_student,parent, false);

            return new StudentHolder(v);
        }

        @Override
        public void onBindViewHolder(StudentHolder holder, int position) {

            final Student student = mStudentList.get(position);

            if (student.stud_id != null && !student.stud_id.equals(""))
                holder.txtStudentCode.setText(student.stud_id);

            if (student.student_id != null && !student.student_id.equals(""))
                holder.txtStudentCode.setText(student.student_id);

            if (student.first_name != null && !student.first_name.equals(""))
                holder.txtStudentName.setText(mStudentList.get(position).first_name + " " + mStudentList.get(position).last_name);

            if (student.name != null && !student.name.equals("")) {
                holder.txtStudentName.setText(student.name);
            }

            if (student.avatar != null && !student.avatar.equals("")) {
                Glide.with(FaceDetectionActivity.this).load(student.avatar).into(holder.imvIconStudent);
            }

            holder.relMain.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ArrayList<String> absentStudentIdList = mAbsentFragment.getStudentIdList();
                    int indexAbsent = getIndexInStudentListWithId(student.student_id, absentStudentIdList);

                    if (indexAbsent == -1) {
                        AppVariable.alert(FaceDetectionActivity.this, "No find student on absent");
                        return;
                    }

                    //Present
                    ArrayList<Student> presentList = mPresentFragment.getStudentList();
                    Student presentStudent = presentList.get(mStudentIndexToReplace);

                    ArrayList<Student> absentList = mAbsentFragment.getStudentList();
                    Student absentStudent = absentList.get(indexAbsent);

                    absentStudent.thumbnail = presentStudent.thumbnail.copy(presentStudent.thumbnail.getConfig(), true);
                    presentStudent.thumbnail = null;

                    absentList.set(indexAbsent, presentStudent);
                    mAbsentFragment.setStudentList(absentList);

                    presentList.set(mStudentIndexToReplace, absentStudent);
                    mPresentFragment.setStudentList(presentList);

                    if (mReplaceStudentDialog != null) {
                        mReplaceStudentDialog.cancel();
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return mStudentList.size();
        }
    }

    private int getIndexInStudentListWithId(String studentId, ArrayList<String> studentIdList) {
        for (int i = 0; i < studentIdList.size(); i++) {
            String id = studentIdList.get(i);
            if (studentId.equals(id)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * A Simple ViewHolder for the RecyclerView
     */
    public static class StudentHolder extends RecyclerView.ViewHolder{

        RelativeLayout relMain;
        TextView txtStudentCode;
        TextView txtStudentName;
        ImageView imvIconStudent;

        public StudentHolder(View itemView) {
            super(itemView);

            relMain        = (RelativeLayout) itemView.findViewById(R.id.rel_main);
            txtStudentCode = (TextView) itemView.findViewById(R.id.txt_student_code);
            txtStudentName = (TextView) itemView.findViewById(R.id.txt_student_name);
            imvIconStudent = (ImageView) itemView.findViewById(R.id.imv_ic_student);
        }
    }
}
