package com.example.hoangdang.diemdanh.SupportClass;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class Network {

    /**
     * List of API
     */

    //public static final String HOST = "http://192.168.21.22:3000/"; //May minh tret
    //public static final String HOST = "http://192.168.22.126:3000/"; //May Nghia
    //public static final String HOST = "http://192.168.22.22:3000/"; //May minh lau 1
    //public static final String HOST = "http://192.168.1.106:3000/"; //Hai Thanh
    //public static final String HOST = "http://192.168.1.162:3000/"; //ct
	//public static final String HOST = "http://192.168.1.114:3000/"; //test
//    public static final String HOST = "http://192.168.2.213:3000/"; //heroku
    public static final String HOST = "https://iteccyle8.herokuapp.com/";

    public static final String API_LOGIN = HOST + "authenticate/login";
    public static final String API_LOGOUT = HOST + "authenticate/logout";

    public static final String API_RETRIEVE_STUDENT = HOST + "api/attendance/check-attendance";
    public static final String API_RETRIEVE_STUDENT_FACE = HOST + "api/attendance/check-attendance-face-recognition";
    public static final String API_SUBMIT_CHECK_ATTENDANCE = HOST + "api/check-attendance/verify-face";

    public static final String API_RETRIEVE_COURSE_LIST = HOST + "api/course/teaching";
    public static final String API_RETRIEVE_STUDYING_COURSE_LIST = HOST + "api/course/studying";
    public static final String API_RETRIEVE_OPENING_COURSE = HOST + "api/attendance/opening-by-teacher";
    public static final String API_RETRIEVE_STUDYING_OPENING_COURSE = HOST + "api/attendance/opening-for-student";

    public static final String API_SEND_STUDENT_ATTENDANCE_DATA = HOST + "api/attendance/update-attendance";

    public static final String API_CREATE_ATTEND = HOST + "api/attendance/create";
    public static final String API_CANCEL_ATTEND = HOST + "api/attendance/delete";
    public static final String API_DELETE_ATTEND = HOST + "api/attendance/delete";
    public static final String API_FINISH_ATTEND = HOST + "api/attendance/close";

    public static final String API_ATTENDANCE_QR_CODE = HOST + "api/check-attendance/qr-code/";
    public static final String API_ATTENDANCE_FACER = HOST + "api/check-attendance/face";
    public static final String API_ATTENDANCE_CHECKLIST = HOST + "api/check-attendance/check-list";

    public static final String API_SEND_FEEDBACK = HOST + "api/feedback/send";
    public static final String API_GET_SENT_FEEDBACK_LIST = HOST + "api/feedback/history";

    public static final String API_SEND_ABSENCE_REQUEST = HOST + "api/absence-request/create";
    public static final String API_GET_SENT_ABSENCE_REQUEST_LIST = HOST + "api/absence-request/by-student";

    public static final String API_CHECK_DELEGATE_CODE = HOST + "api/attendance/check-delegate-code";
    public static final String API_GET_DELEGATE_CODE = HOST + "api/attendance/generate-delegate-code";

    public static final String API_CHECK_QUIZ_CODE = HOST + "api/quiz/join";
    public static final String API_GET_PUBLISH_QUIZ = HOST + "api/quiz/published";
    public static final String API_SUBMIT_QUIZ = HOST + "api/quiz/submit";
    public static final String API_QUIZ_LIST = HOST + "api/quiz/list";
    public static final String API_QUIZ_START = HOST + "api/quiz/start";
    public static final String API_QUIZ_RESULT = HOST + "api/quiz/quizMobileResults";
    public static final String API_UPLOAD_FACE_TO_STUDENT = HOST + "api/student/uploadFace";
    public static final String API_STUDENT_BY_ID = HOST + "api/student/detail/";

    public static final String API_USER_CHANGE_PASSWORD = HOST + "api/user/change-password";
    public static final String API_USER_RESET_PASSWORD = HOST + "authenticate/forgot-password";
    public static final String API_GET_PERSON_ID = HOST + "api/student/student-by-id";
    public static final String API_SUBMIT = HOST + "api/check-attendance/verify-face";
    public static final String API_SYNC = "https://";

    /**
     * A function to check internet connection.
     * @param c classActivity.this
     * @return boolean
     */
    public static boolean isOnline(Context c) {
        try {
            ConnectivityManager cm = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            return netInfo != null && netInfo.isConnectedOrConnecting();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}
