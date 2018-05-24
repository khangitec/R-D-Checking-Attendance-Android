package com.example.hoangdang.diemdanh.SupportClass;

import android.graphics.Bitmap;

public class Student {
    public int iID;
    public String strCode;
    public int iClass;
    public String stud_id;
    public String first_name;
    public String last_name;
    public String strName;
    public String name;
    public String face_image;
    public String person_id;
    public String attendance_id;
    public String student_id;
    public String avatar;
    public Bitmap thumbnail;
    public int status;

    public Student(int iID, String strCode, String strName, int status){
        this.iID = iID;
        this.strCode = strCode;
        this.strName = strName;
        this.status = status;
    }

    public Student(){}
}
