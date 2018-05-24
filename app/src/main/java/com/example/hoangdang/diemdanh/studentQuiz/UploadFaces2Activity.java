package com.example.hoangdang.diemdanh.studentQuiz;

import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import android.app.*;
import android.content.*;
import android.net.*;
import android.os.*;
import android.view.*;
import android.graphics.*;
import android.widget.*;

import com.example.hoangdang.diemdanh.SelectImageActivity;
import com.example.hoangdang.diemdanh.SupportClass.AppVariable;
import com.example.hoangdang.diemdanh.SupportClass.SecurePreferences;
import com.example.hoangdang.diemdanh.utils.ImageHelper;
import com.microsoft.projectoxford.face.*;
import com.microsoft.projectoxford.face.contract.*;

import com.example.hoangdang.diemdanh.R;

public class UploadFaces2Activity extends AppCompatActivity implements View.OnClickListener {

    private static final int REQUEST_SELECT_IMAGE = 0;

    private TextView mTxtStudentName;
    private GridView mGridView;
    private Button mBtnAddFace;
    private Button mBtnSubmit;

    // The URI of the image selected to detect.
    private Uri mImageUri;
    // The image selected to detect.
    private Bitmap mBitmap;
    private ProgressDialog mProgressDialog;

    private ArrayList<Bitmap> mFaceImageList;
    private FaceListAdapter mFaceListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_faces);

        SharedPreferences pref = new SecurePreferences(this);
        String name = pref.getString(AppVariable.USER_NAME, "");

        mProgressDialog = new ProgressDialog(this, R.style.AppTheme_Dark_Dialog);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setMessage("Loading...");

        mTxtStudentName = (TextView) findViewById(R.id.txt_student_name);
        mTxtStudentName.setText(name);

        mBtnAddFace = (Button) findViewById(R.id.btn_add_face);
        mBtnAddFace.setOnClickListener(this);

        mBtnSubmit = (Button) findViewById(R.id.btn_submit);
        mBtnSubmit.setOnClickListener(this);

        initFaceImageList();
    }

    private void initFaceImageList() {
        mFaceImageList = new ArrayList<>();
        mFaceListAdapter = new FaceListAdapter();

        mGridView = (GridView) findViewById(R.id.all_faces);
        mGridView.setAdapter(mFaceListAdapter);

        mGridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                mFaceListAdapter.faces.remove(position);
                mFaceListAdapter.faceThumbnails.remove(position);
                mFaceListAdapter.faceIdThumbnailMap.remove(position);
                mFaceListAdapter. notifyDataSetChanged();

                return false;
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_add_face:
                Intent intent = new Intent(this, SelectImageActivity.class);
                startActivityForResult(intent, REQUEST_SELECT_IMAGE);
                break;

            case R.id.btn_submit:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SELECT_IMAGE) {
            if(resultCode == RESULT_OK) {
                mBitmap = ImageHelper.loadSizeLimitedBitmapFromUri(data.getData(), getContentResolver());
                if (mBitmap != null) {
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

    void setUiAfterDetection(Face[] result) {
        mProgressDialog.dismiss();

        if (result != null) {
            // Show the detailed list of original faces.
            mFaceListAdapter.addFaces(result);
            mFaceListAdapter.notifyDataSetChanged();
        }
    }

    private class DetectionTask extends AsyncTask<InputStream, String, Face[]> {

        @Override
        protected Face[] doInBackground(InputStream... params) {
            // Get an instance of face service client to detect faces in image.
            FaceServiceClient faceServiceClient = new FaceServiceRestClient("https://westcentralus.api.cognitive.microsoft.com/face/v1.0/","18db52d47bc5483f92d687a957c40c98");
            try {
                publishProgress("Detecting...");

                // Start detection.
                return faceServiceClient.detect(
                        params[0],  /* Input stream of image to detect */
                        true,       /* Whether to return face ID */
                        true,       /* Whether to return face landmarks */
                        /* Which face attributes to analyze, currently we support:
                           age,gender,headPose,smile,facialHair */
                        new FaceServiceClient.FaceAttributeType[] {
                                FaceServiceClient.FaceAttributeType.Age,
                                FaceServiceClient.FaceAttributeType.Gender,
                                FaceServiceClient.FaceAttributeType.Glasses,
                                FaceServiceClient.FaceAttributeType.Smile,
                                FaceServiceClient.FaceAttributeType.HeadPose
                        });
            } catch (Exception e) {
                publishProgress(e.getMessage());
                return null;
            }
        }

        @Override
        protected void onPreExecute() {
            mProgressDialog.show();
        }

        @Override
        protected void onProgressUpdate(String... progress) {
            mProgressDialog.setMessage(progress[0]);
//            setInfo(progress[0]);
        }

        @Override
        protected void onPostExecute(Face[] result) {
            // Show the result on screen when detection is done.
            setUiAfterDetection(result);
        }
    }

    // The adapter of the GridView which contains the thumbnails of the detected faces.
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
                        Bitmap faceThumbnail = ImageHelper.generateFaceThumbnail(
                                mBitmap, face.faceRectangle);
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
}
