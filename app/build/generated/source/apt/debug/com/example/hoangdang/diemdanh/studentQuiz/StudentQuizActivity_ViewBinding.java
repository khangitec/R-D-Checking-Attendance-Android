// Generated code from Butter Knife. Do not modify!
package com.example.hoangdang.diemdanh.studentQuiz;

import android.support.annotation.CallSuper;
import android.support.annotation.UiThread;
import android.view.View;
import android.widget.LinearLayout;
import butterknife.Unbinder;
import butterknife.internal.Utils;
import com.example.hoangdang.diemdanh.R;
import java.lang.IllegalStateException;
import java.lang.Override;

public class StudentQuizActivity_ViewBinding implements Unbinder {
  private StudentQuizActivity target;

  @UiThread
  public StudentQuizActivity_ViewBinding(StudentQuizActivity target) {
    this(target, target.getWindow().getDecorView());
  }

  @UiThread
  public StudentQuizActivity_ViewBinding(StudentQuizActivity target, View source) {
    this.target = target;

    target.layout = Utils.findRequiredViewAsType(source, R.id.quiz_ll, "field 'layout'", LinearLayout.class);
  }

  @Override
  @CallSuper
  public void unbind() {
    StudentQuizActivity target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");
    this.target = null;

    target.layout = null;
  }
}
