package com.example.hoangdang.diemdanh.SupportClass;

public class Student {
    public int iID;
    public String strCode;
    public int iClass;
    public String strLastName;
    public String strFirstName;
    public String strName;
    public int status;

    public Student(int iID, String strCode, String strName, int status){
        this.iID = iID;
        this.strCode = strCode;
        this.strName = strName;
        this.status = status;
    }

    public Student(){}
}
