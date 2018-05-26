package com.example.hoangdang.diemdanh.Fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.hoangdang.diemdanh.R;
import com.example.hoangdang.diemdanh.SupportClass.Student;

import java.util.ArrayList;
import java.util.List;

public class CheckedFragment extends Fragment {

    private RecyclerView mRecyclerStudents;

    public CheckFragmentListener mListener;

    private ArrayList<Student> mStudentList;
    private StudentAdapter mStudentAdapter;
    private boolean isShowImvClose;

    public interface CheckFragmentListener {
        void onItemCheckClick(int position);
        void onItemCloseClick(int position);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mStudentList = new ArrayList<>();

        mStudentAdapter = new StudentAdapter();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_checked, container, false);

        mRecyclerStudents = (RecyclerView) view.findViewById(R.id.recycler_students);

        mRecyclerStudents.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerStudents.setAdapter(mStudentAdapter);

        return view;
    }

    public void setIsShowImvClose(boolean isShowImvClose) {
        this.isShowImvClose = isShowImvClose;
    }

    public void setStudentList(ArrayList<Student> studentList) {
        mStudentList = studentList;
        if (mStudentAdapter != null)
            mStudentAdapter.notifyDataSetChanged();
    }

    public ArrayList<Student> getStudentList() {
        return mStudentList;
    }

    public ArrayList<String> getStudentIdList() {
        ArrayList<String> result = new ArrayList<>();

        for (int i = 0; i < mStudentList.size(); i++) {
            Student student = mStudentList.get(i);
            result.add(student.student_id);
        }
        return result;
    }

    private class StudentAdapter extends RecyclerView.Adapter<StudentHolder> {

        @Override
        public StudentHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_student,parent, false);

            return new StudentHolder(v);
        }

        @Override
        public void onBindViewHolder(StudentHolder holder, final int position) {

            Student student = mStudentList.get(position);

            if (student.stud_id != null && !student.stud_id.equals(""))
                holder.txtStudentCode.setText(student.stud_id);

            if (student.student_id != null && !student.student_id.equals(""))
                holder.txtStudentCode.setText(student.student_id);

            if (student.first_name != null && !student.first_name.equals(""))
                holder.txtStudentName.setText(mStudentList.get(position).first_name + " " + mStudentList.get(position).last_name);

            if (student.name != null && !student.name.equals("")) {
                holder.txtStudentName.setText(student.name);
            }

            if (student.thumbnail != null) {
                holder.imvIconStudent.setImageBitmap(student.thumbnail);
            }

//            if (student.avatar != null && !student.avatar.equals("")) {
//                Glide.with(getActivity()).load(student.avatar).into(holder.imvIconStudent);
//            }

            if (isShowImvClose) {
                holder.imvClose.setVisibility(View.VISIBLE);
                holder.imvClose.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mListener != null) {
                            mListener.onItemCloseClick(position);
                        }
                    }
                });
            } else {
                holder.imvClose.setVisibility(View.GONE);
            }

            holder.relMain.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        mListener.onItemCheckClick(position);
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return mStudentList.size();
        }
    }

    /**
     * A Simple ViewHolder for the RecyclerView
     */
    public static class StudentHolder extends RecyclerView.ViewHolder{

        RelativeLayout relMain;
        TextView txtStudentCode;
        TextView txtStudentName;
        ImageView imvIconStudent;
        ImageView imvClose;

        public StudentHolder(View itemView) {
            super(itemView);

            relMain = (RelativeLayout) itemView.findViewById(R.id.rel_main);
            txtStudentCode = (TextView) itemView.findViewById(R.id.txt_student_code);
            txtStudentName = (TextView) itemView.findViewById(R.id.txt_student_name);
            imvIconStudent = (ImageView) itemView.findViewById(R.id.imv_ic_student);
            imvClose = (ImageView) itemView.findViewById(R.id.imv_close);
        }
    }
}
