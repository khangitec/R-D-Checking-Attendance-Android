package com.example.hoangdang.diemdanh.SupportClass;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.hoangdang.diemdanh.timeTable.LessonInfo;
import com.example.hoangdang.diemdanh.timeTable.TimeTable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * usage DatabaseHelper db = new DatabaseHelper(this);
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    // database config
    private static final String DATABASE_NAME = "QLDD";
    private static final int DATABASE_VERSION = 1;
    private static final String COLUMN_ID  = "id";

    //user table
    private static final String TABLE_USER = "user";
    private static final String COLUMN_USER_LAST_NAME = "last_name";
    private static final String COLUMN_USER_FIRST_NAME = "first_name";
    private static final String COLUMN_USER_EMAIL = "email";
    private static final String COLUMN_USER_PHONE = "phone";
    private static final String COLUMN_USER_ROLE = "role_id";

    // courses table
    private static final String TABLE_COURSES = "courses";
    private static final String COLUMN_COURSE_CODE = "code";
    private static final String COLUMN_COURSE_NAME = "name";
    private static final String COLUMN_COURSE_CLASS_ID = "class_id";
    private static final String COLUMN_COURSE_CLASS_NAME = "class_name";
    private static final String COLUMN_COURSE_CLASS_HAS_COURSE_ID = "class_has_course_id";
    private static final String COLUMN_COURSE_TOTAL_STUD = "total_stud";
    private static final String COLUMN_COURSE_OPEN = "open";
    private static final String COLUMN_COURSE_ATTENDANCE_ID = "attendance_id";
    private static final String COLUMN_COURSE_SCHEDULE = "schedule";
    private static final String COLUMN_COURSE_OFFICE_HOUR = "office_hour";
    private static final String COLUMN_COURSE_NOTE = "note";

    //attendance detail table
    private static final String TABLE_ATTENDANCE_DETAIL = "attendance";
    private static final String COLUMN_ATTENDANCE_STUDENT_ID = "student_id";
    private static final String COLUMN_ATTENDANCE_STUDENT_NAME = "student_name";
    private static final String COLUMN_ATTENDANCE_STUDENT_CODE = "student_code";
    private static final String COLUMN_ATTENDANCE_ATTENDANCE_ID = "attendance_id";
    private static final String COLUMN_ATTENDANCE_CLASS_HAS_COURSE_ID = "class_has_course_id";
    private static final String COLUMN_ATTENDANCE_STATUS = "status";

    //-----------------------------------------------------------------------
    // TODO: tao moi moi xoa detail attendance de khi office dung cai dd cu~
    // classes
    private static final String TABLE_CLASSES  = "classes";
    private static final String COLUMN_CLASS_NAME  = "class_name";
    // students table
    private static final String TABLE_STUDENTS = "students";
    private static final String COLUMN_STUDENTS_CODE = "stud_id";
    private static final String COLUMN_STUDENTS_CLASS_ID = "class_id";
    private static final String COLUMN_STUDENTS_LAST_NAME = "last_name";
    private static final String COLUMN_STUDENTS_FIRST_NAME = "first_name";
    // class_has_course
    private static final String TABLE_CLASS_HAS_COURSE = "class_has_course";
    private static final String COLUMN_CLASS_HAS_COURSE_CLASS_ID = "class_id";
    private static final String COLUMN_CLASS_HAS_COURSE_COURSE_ID = "course_id";
    private static final String COLUMN_CLASS_HAS_COURSE_TOTAL_STUD = "total_stud";
    //sync table
    private static final String TABLE_SYNC = "sync";
    private static final String COLUMN_SYNC_TASK_NAME = "name";
    private static final String COLUMN_SYNC_OBJECT_ID = "object_id";
    private static final String COLUMN_SYNC_ACTION = "action";
    private static final String COLUMN_SYNC_CREATED = "created_at";

    private static final int[] matrix = {6,12,18,24,7,13,19,25,8,14,20,26,9,15,21,27,10,16,22,28,11,17,23,29};

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_USER_TABLE = "create table "
                + TABLE_USER + "(" + COLUMN_ID + " integer primary key, "
                + COLUMN_USER_ROLE + " integer, "
                + COLUMN_USER_EMAIL + " string, "
                + COLUMN_USER_LAST_NAME + " string, "
                + COLUMN_USER_FIRST_NAME + " string, "
                + COLUMN_USER_PHONE + " string)";

        String CREATE_COURSE_TABLE = "create table "
                + TABLE_COURSES + "(" + COLUMN_ID + " integer, "
                + COLUMN_COURSE_CODE + " string, "
                + COLUMN_COURSE_NAME + " string, "
                + COLUMN_COURSE_CLASS_ID + " integer, "
                + COLUMN_COURSE_CLASS_NAME + " string, "
                + COLUMN_COURSE_CLASS_HAS_COURSE_ID + " integer primary key, "
                + COLUMN_COURSE_TOTAL_STUD + " integer, "
                + COLUMN_COURSE_OPEN + " integer, "
                + COLUMN_COURSE_ATTENDANCE_ID + " integer,"
                + COLUMN_COURSE_SCHEDULE + " string, "
                + COLUMN_COURSE_OFFICE_HOUR + " string, "
                + COLUMN_COURSE_NOTE + " string)";

        String CREATE_ATTENDANCE_TABLE = "create table "
                + TABLE_ATTENDANCE_DETAIL + "("
                + COLUMN_ATTENDANCE_STUDENT_ID + " integer, "
                + COLUMN_ATTENDANCE_STUDENT_NAME + " string, "
                + COLUMN_ATTENDANCE_STUDENT_CODE + " integer, "
                + COLUMN_ATTENDANCE_ATTENDANCE_ID + " integer, "
                + COLUMN_ATTENDANCE_CLASS_HAS_COURSE_ID + " integer, "
                + COLUMN_ATTENDANCE_STATUS + " integer, "
                + "PRIMARY KEY(" + COLUMN_ATTENDANCE_ATTENDANCE_ID + "," + COLUMN_ATTENDANCE_STUDENT_ID + "))";

        //--------------------------------------------------------------

        String CREATE_SYNC_TABLE = "create table "
                + TABLE_SYNC + "(" + COLUMN_ID + " integer primary key autoincrement, "
                + COLUMN_SYNC_TASK_NAME + " string, "
                + COLUMN_SYNC_OBJECT_ID + " integer, "
                + COLUMN_SYNC_ACTION + " integer, "
                + COLUMN_SYNC_CREATED + "string)";

        String CREATE_CLASSES_TABLE = "create table "
                + TABLE_CLASSES + "(" + COLUMN_ID + " integer primary key , "
                + COLUMN_CLASS_NAME + " string)";

        String CREATE_STUDENT_TABLE = "create table "
                + TABLE_STUDENTS + "(" + COLUMN_ID + " integer primary key , "
                + COLUMN_STUDENTS_CODE + " integer, "
                + COLUMN_STUDENTS_CLASS_ID + " integer, "
                + COLUMN_STUDENTS_LAST_NAME + " string, "
                + COLUMN_STUDENTS_FIRST_NAME + " string)";


        String CREATE_CLASS_HAS_COURSE_TABLE = "create table "
                + TABLE_CLASS_HAS_COURSE + "(" + COLUMN_ID + " integer primary key, "
                + COLUMN_CLASS_HAS_COURSE_CLASS_ID + " integer, "
                + COLUMN_CLASS_HAS_COURSE_COURSE_ID + " integer, "
                + COLUMN_CLASS_HAS_COURSE_TOTAL_STUD + "integer)";

        //--------------------------------------
        db.execSQL(CREATE_USER_TABLE);
        db.execSQL(CREATE_COURSE_TABLE);
        db.execSQL(CREATE_ATTENDANCE_TABLE);
        //--------------------------------------
        db.execSQL(CREATE_CLASSES_TABLE);
        db.execSQL(CREATE_STUDENT_TABLE);

        db.execSQL(CREATE_SYNC_TABLE);
        db.execSQL(CREATE_CLASS_HAS_COURSE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        /*if (oldVersion < 2) {
            db.execSQL(DATABASE_ALTER_TEAM_1);
        }
        if (oldVersion < 3) {
            db.execSQL(DATABASE_ALTER_TEAM_2);
        }*/
    }

    public void cleanData(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_USER, null, null);
        db.delete(TABLE_COURSES, null, null);
        db.delete(TABLE_ATTENDANCE_DETAIL, null, null);
        //------------------------------------------------
        db.delete(TABLE_STUDENTS, null, null);
        db.delete(TABLE_SYNC, null, null);
        db.delete(TABLE_CLASS_HAS_COURSE, null, null);
        db.delete(TABLE_CLASSES, null, null);
        db.close();
    }

    public void addUser(User user){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ID, user.getID());
        values.put(COLUMN_USER_ROLE, user.getRole());
        values.put(COLUMN_USER_EMAIL, user.getEmail());
        values.put(COLUMN_USER_LAST_NAME, user.getLastName());
        values.put(COLUMN_USER_FIRST_NAME, user.getFirstName());
        values.put(COLUMN_USER_PHONE, user.getPhone());

        db.insert(TABLE_USER, null, values);
        db.close();
    }

    public User getUser(){
        User user = new User();

        String selectQuery = "SELECT * "
                + "FROM " + TABLE_USER
                + " LIMIT 1";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            user.setID(cursor.getInt(0));
            user.setRole(cursor.getInt(1));
            user.setEmail(cursor.getString(2));
            user.setLastName(cursor.getString(3));
            user.setFirstName(cursor.getString(4));
            user.setPhone(cursor.getString(5));
            cursor.close();
        }

        db.close();
        return user;
    }

    public void addStudent(ArrayList<Student> students, int attendance_id, int class_has_course_id){
        SQLiteDatabase db = this.getWritableDatabase();

        db.delete(TABLE_ATTENDANCE_DETAIL, COLUMN_ATTENDANCE_CLASS_HAS_COURSE_ID + " = ?", new String[] {String.valueOf(class_has_course_id)});

        ContentValues values = new ContentValues();

        for (Student s: students) {
            values.put(COLUMN_ATTENDANCE_STUDENT_ID, s.iID);
            values.put(COLUMN_ATTENDANCE_STUDENT_NAME, s.strName);
            values.put(COLUMN_ATTENDANCE_STUDENT_CODE, s.strCode);
            values.put(COLUMN_ATTENDANCE_ATTENDANCE_ID, attendance_id);
            values.put(COLUMN_ATTENDANCE_CLASS_HAS_COURSE_ID, class_has_course_id);
            values.put(COLUMN_ATTENDANCE_STATUS, s.status);

            db.insert(TABLE_ATTENDANCE_DETAIL, null, values);
        }

        db.close();
    }

    public boolean addCourse(ArrayList<Course> courses){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_COURSES, null, null);

        ContentValues values = new ContentValues();

        for (Course c: courses) {
            values.put(COLUMN_ID, c.iID);
            values.put(COLUMN_COURSE_CODE, c.strCode);
            values.put(COLUMN_COURSE_NAME, c.strName);
            values.put(COLUMN_COURSE_CLASS_ID, c.classID);
            values.put(COLUMN_COURSE_CLASS_NAME, c.classname);
            values.put(COLUMN_COURSE_CLASS_HAS_COURSE_ID, c.chcid);
            values.put(COLUMN_COURSE_TOTAL_STUD, c.total_stud);
            values.put(COLUMN_COURSE_OPEN, 0);
            values.put(COLUMN_COURSE_ATTENDANCE_ID, 0);
            values.put(COLUMN_COURSE_SCHEDULE, c.schedule);
            values.put(COLUMN_COURSE_OFFICE_HOUR, c.office_hour);
            values.put(COLUMN_COURSE_NOTE, c.note);

            db.insert(TABLE_COURSES, null, values);
        }

        db.close();
        return true;
    }

    public void removeAttendanceData(int attendanceID){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_ATTENDANCE_DETAIL, COLUMN_ATTENDANCE_ATTENDANCE_ID + " = ?", new String[] {String.valueOf(attendanceID)});
        db.close();
    }

    public ArrayList<Course> getAllCourse() {
        ArrayList<Course> courseList = new ArrayList<>();
        String selectQuery = "SELECT "
                + COLUMN_ID + ", "
                + COLUMN_COURSE_CODE + ", "
                + COLUMN_COURSE_NAME + ", "
                + COLUMN_COURSE_CLASS_ID + ", "
                + COLUMN_COURSE_CLASS_NAME + ", "
                + COLUMN_COURSE_CLASS_HAS_COURSE_ID + ", "
                + COLUMN_COURSE_OPEN + ", "
                + COLUMN_COURSE_ATTENDANCE_ID + ", "
                + COLUMN_COURSE_TOTAL_STUD
                + " FROM " + TABLE_COURSES;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Course course = new Course();
                course.iID = Integer.parseInt(cursor.getString(0));
                course.strCode = cursor.getString(1);
                course.strName = cursor.getString(2);
                course.classID = cursor.getInt(3);
                course.classname = cursor.getString(4);
                course.chcid = cursor.getInt(5);
                course.open = cursor.getInt(6);
                course.attendid = cursor.getInt(7);
                course.total_stud = cursor.getInt(8);

                courseList.add(course);
            } while (cursor.moveToNext());

            cursor.close();
        }

        db.close();
        return courseList;
    }

    public ArrayList<Student> getStudentByAttendanceStatus(int attendanceID, int status) {
        ArrayList<Student> studentList = new ArrayList<>();
        String selectQuery = "SELECT "
                + COLUMN_ATTENDANCE_STUDENT_ID + ", "
                + COLUMN_ATTENDANCE_STUDENT_NAME + ", "
                + COLUMN_ATTENDANCE_STUDENT_CODE + " "
                + "FROM " + TABLE_ATTENDANCE_DETAIL + " "
                + "WHERE "
                + COLUMN_ATTENDANCE_ATTENDANCE_ID + "=" + String.valueOf(attendanceID) + " AND "
                + COLUMN_ATTENDANCE_STATUS + "=" + String.valueOf(status);

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Student student = new Student();
                student.iID = Integer.parseInt(cursor.getString(0));
                student.strCode = cursor.getString(1);
                student.strName = cursor.getString(2);

                studentList.add(student);
            } while (cursor.moveToNext());

            cursor.close();
        }

        return studentList;
    }

    public int getTotalStudentOfClass(int classHasCourseID) {
        int result = 0;
        String selectQuery = "SELECT " + COLUMN_COURSE_TOTAL_STUD
                + " FROM " + TABLE_COURSES
                + " WHERE " + COLUMN_COURSE_CLASS_HAS_COURSE_ID + "=" + String.valueOf(classHasCourseID)
                + " LIMIT 1";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            result = cursor.getInt(0);
        }

        cursor.close();
        return result;
    }

    public int getNumberStudentOfClassByAttendance(int attendanceID, int status) {
        int result = 0;
        String selectQuery = "SELECT COUNT(" + COLUMN_ATTENDANCE_STUDENT_ID + ")"
                + " FROM " + TABLE_ATTENDANCE_DETAIL
                + " WHERE "
                + COLUMN_ATTENDANCE_ATTENDANCE_ID + "=" + String.valueOf(attendanceID) + " AND "
                + COLUMN_ATTENDANCE_STATUS + "=" + String.valueOf(status);

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            result = Integer.parseInt(cursor.getString(0));
        }

        cursor.close();
        return result;
    }

    public void cleanOldData(){
        SQLiteDatabase db = this.getWritableDatabase();
        // TODO: if synced then delete local attend
        db.delete(TABLE_SYNC, null, null);
        db.delete(TABLE_ATTENDANCE_DETAIL, null, null);
        db.close();
    }

    public void addSyncTask(SyncTask task){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_SYNC_TASK_NAME, task.name);
        values.put(COLUMN_SYNC_OBJECT_ID, task.object);
        values.put(COLUMN_SYNC_ACTION, task.action);
        values.put(COLUMN_SYNC_CREATED, task.created_at);

        db.insert(TABLE_SYNC, null, values);
        db.close();
    }

    public void removeSyncTask(int id){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_SYNC, COLUMN_ID + " = ?", new String[] {String.valueOf(id)});
        db.close();
    }

    public int countSyncTask(){
        int result = 0;
        String selectQuery = "SELECT COUNT("
                + COLUMN_ID + ") "
                + "FROM " + TABLE_SYNC;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            result = Integer.parseInt(cursor.getString(0));
            cursor.close();
        }
        return result;
    }

    public SyncTask popSyncTask(){
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_SYNC, new String[] {
                COLUMN_ID,
                COLUMN_SYNC_TASK_NAME,
                COLUMN_SYNC_OBJECT_ID,
                COLUMN_SYNC_ACTION,
                COLUMN_SYNC_CREATED
        }, null, null, null, null, null, "1");

        if (cursor != null){
            cursor.moveToFirst();
        }
        else {
            return null;
        }

        try{
            SyncTask t = new SyncTask();

            t.ID = (Integer.parseInt(cursor.getString(0)));
            t.name = cursor.getString(1);
            t.object = (Integer.parseInt(cursor.getString(2)));
            t.action = (Integer.parseInt(cursor.getString(3)));
            t.created_at = (cursor.getString(4));
            cursor.close();
            return t;
        }
        catch (CursorIndexOutOfBoundsException e){
            return null;
        }
    }

    public boolean isTaskExist(int object, int action) {
        boolean result = false;

        String selectQuery = "SELECT COUNT("
                + COLUMN_ID + ") "
                + "FROM " + TABLE_SYNC
                + " WHERE " + COLUMN_SYNC_OBJECT_ID + '=' + String.valueOf(object)
                + " AND " + COLUMN_SYNC_ACTION + '=' + String.valueOf(action)
                + " LIMIT 1";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            result = cursor.getInt(0) == 1;
            cursor.close();
        }

        return result;
    }

    public void updateTask(int id, int action, String time) {
        String selectQuery = "UPDATE " + TABLE_SYNC + " SET "
                + COLUMN_SYNC_ACTION + "=" + String.valueOf(action) + " AND "
                + COLUMN_SYNC_CREATED + "=" + time
                + " WHERE " + COLUMN_SYNC_OBJECT_ID + "=" + String.valueOf(id);

        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL(selectQuery);
        db.close();
    }

    public JSONArray getAttendanceDataInJSON(int attendanceID){

        String selectQuery = "SELECT " + COLUMN_ATTENDANCE_STUDENT_ID + ", " + COLUMN_ATTENDANCE_STATUS
                + " FROM " + TABLE_ATTENDANCE_DETAIL
                + " WHERE " + COLUMN_ATTENDANCE_ATTENDANCE_ID + "=" + String.valueOf(attendanceID);

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        JSONArray array = new JSONArray();

        if (cursor.moveToFirst()) {
            do {
                JSONObject data = new JSONObject();

                try {
                    data.put(COLUMN_ATTENDANCE_STUDENT_ID, cursor.getString(0));
                    data.put(COLUMN_ATTENDANCE_STATUS, cursor.getString(1));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                array.put(data);
            } while (cursor.moveToNext());

            cursor.close();
        }

        return array;
    }

    public boolean hasTask() {
        boolean result = false;

        String selectQuery = "SELECT COUNT("
                + COLUMN_ID + ") "
                + "FROM " + TABLE_SYNC
                + " LIMIT 1";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            result = cursor.getInt(0) == 1;
            cursor.close();
        }

        return result;
    }

    public void updateOpeningCourse(ArrayList<Course> list, ArrayList<String> courses, ArrayList<String> attendance, int status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues data = new ContentValues();
        int length = courses.size();
        for (Course c: list){
            String class_has_course_id = String.valueOf(c.chcid);
            boolean flag = false;
            for (int i = 0; i < length; i++) {
                if (courses.get(i).equals(class_has_course_id)){
                    flag = true;
                    c.attendid = i;
                    break;
                }
            }

            if(flag){
                c.open = 1;
            }
            else {
                c.open = 0;
            }
        }

        for (Course c: list) {
            if (c.open == 1){
                data.put(COLUMN_COURSE_OPEN, 1);
                data.put(COLUMN_COURSE_ATTENDANCE_ID, attendance.get(c.attendid));
            }
            else {
                data.put(COLUMN_COURSE_OPEN, 0);
                data.put(COLUMN_COURSE_ATTENDANCE_ID, 0);
            }
            db.update(TABLE_COURSES, data, COLUMN_COURSE_CLASS_HAS_COURSE_ID + "=" + c.chcid, null);
        }

        db.close();
    }

    public void changeAttendanceStatus(int studentID, int attendanceID, int status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues data = new ContentValues();

        data.put(COLUMN_ATTENDANCE_STATUS, status);
        String condition = COLUMN_ATTENDANCE_ATTENDANCE_ID + "=" + String.valueOf(attendanceID) + " AND " +
                COLUMN_ATTENDANCE_STUDENT_ID + "=" + String.valueOf(studentID);
        db.update(TABLE_ATTENDANCE_DETAIL, data, condition, null);

        db.close();
    }

    public TimeTable getTimetable() {
        TimeTable tt = new TimeTable();

        ArrayList<Course> courseList = new ArrayList<>();
        String selectQuery = "SELECT "
                + COLUMN_ID + ", "
                + COLUMN_COURSE_CODE + ", "
                + COLUMN_COURSE_NAME + ", "
                + COLUMN_COURSE_CLASS_NAME + ", "
                + COLUMN_COURSE_SCHEDULE + ", "
                + COLUMN_COURSE_OFFICE_HOUR + ", "
                + COLUMN_COURSE_NOTE
                + " FROM " + TABLE_COURSES;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Course course = new Course();
                course.iID = Integer.parseInt(cursor.getString(0));
                course.strCode = cursor.getString(1);
                course.strName = cursor.getString(2);
                course.classname = cursor.getString(3);
                course.schedule = cursor.getString(4);
                course.office_hour = cursor.getString(5);
                course.note = cursor.getString(6);

                courseList.add(course);
            } while (cursor.moveToNext());

            cursor.close();
        }
        db.close();
        //'1-I23-LT;5-I23-LT;12-I11C-TH;12-I44-LT'
        for (Course c: courseList) {
            String schedules = c.schedule;
            StringTokenizer tokens = new StringTokenizer(schedules, ";");

            int length = tokens.countTokens();

            for(int i = 0; i < length; i++) {
                String schedule = tokens.nextToken();
                StringTokenizer tmp = new StringTokenizer(schedule, "-");
                int index = Integer.valueOf(tmp.nextToken());
                index = matrix[index];
                tt.index[index] = 1;
                LessonInfo l = new LessonInfo();
                l.code = c.strCode;
                l.classname = c.classname;
                l.name = c.strName;

                if (c.office_hour.equals("null")){
                    l.office_hour = "";
                }
                else {
                    l.office_hour = "Office Hour: " + c.office_hour;
                }

                if (c.note.equals("null")){
                    l.note = "";
                }
                else {
                    l.note = "Note: " + c.note;
                }

                l.content = "Room: " + tmp.nextToken();

                if (tmp.nextToken().equals("LT")){
                    l.content =  l.content + " - Ly thuyet";
                }
                else {
                    l.content =  l.content + " - Thuc Hanh";
                    l.underline = 1;
                }

                tt.lessonInfos[index].add(l);
            }
        }

        return tt;
    }

    public void updateCourseStatus(String course, int attendance,int status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues data = new ContentValues();

        data.put(COLUMN_COURSE_OPEN, status);
        data.put(COLUMN_COURSE_ATTENDANCE_ID, attendance);
        db.update(TABLE_COURSES, data, COLUMN_COURSE_CLASS_HAS_COURSE_ID + "=" + course, null);

        db.close();
    }

    public Course getCourse(int course_id, int class_id) {
        String selectQuery = "SELECT "
                + COLUMN_COURSE_CLASS_HAS_COURSE_ID + ", "
                + COLUMN_COURSE_NAME
                + " FROM " + TABLE_COURSES
                + " WHERE " + COLUMN_COURSE_CLASS_ID + "=" + course_id
                + " AND " + COLUMN_COURSE_CLASS_ID + "=" + class_id
                + " LIMIT 1";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        Course result = new Course();
        if (cursor.moveToFirst()) {
            do {
                result.chcid = cursor.getInt(0);
                result.strName = cursor.getString(1);
            } while (cursor.moveToNext());

            cursor.close();
        }

        db.close();
        return result;
    }
}
