package com.example.hoangdang.diemdanh.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.hoangdang.diemdanh.R;
import com.example.hoangdang.diemdanh.SupportClass.Student;

import java.util.List;

public class JoinedStudentsAdapter extends RecyclerView.Adapter<JoinedStudentsAdapter.MyViewHolder> {

    private List<Student> studentList;

    public class MyViewHolder extends RecyclerView.ViewHolder {

        public TextView txtStudentCode, txtStudentName;

        public MyViewHolder(View view) {
            super(view);
            txtStudentCode = (TextView) view.findViewById(R.id.txt_student_code);
            txtStudentName = (TextView) view.findViewById(R.id.txt_student_name);
        }
    }

    public JoinedStudentsAdapter(List<Student> studentList) {
        this.studentList = studentList;
    }

    public void setStudentList(List<Student> studentList) {
        this.studentList = studentList;
        notifyDataSetChanged();
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_joined_student, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Student student = studentList.get(position);

        holder.txtStudentCode.setText(student.strCode);
        holder.txtStudentName.setText(student.strName);
    }

    @Override
    public int getItemCount() {
        return studentList.size();
    }
}
