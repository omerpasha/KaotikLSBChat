// Generated by view binder compiler. Do not edit!
package com.omer.stegochat.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import com.omer.stegochat.R;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;

public final class ActivityRegisterBinding implements ViewBinding {
  @NonNull
  private final LinearLayout rootView;

  @NonNull
  public final Button btnLogin;

  @NonNull
  public final Button btnRegister;

  @NonNull
  public final EditText etConfirmPassword;

  @NonNull
  public final EditText etEmail;

  @NonNull
  public final EditText etName;

  @NonNull
  public final EditText etPassword;

  private ActivityRegisterBinding(@NonNull LinearLayout rootView, @NonNull Button btnLogin,
      @NonNull Button btnRegister, @NonNull EditText etConfirmPassword, @NonNull EditText etEmail,
      @NonNull EditText etName, @NonNull EditText etPassword) {
    this.rootView = rootView;
    this.btnLogin = btnLogin;
    this.btnRegister = btnRegister;
    this.etConfirmPassword = etConfirmPassword;
    this.etEmail = etEmail;
    this.etName = etName;
    this.etPassword = etPassword;
  }

  @Override
  @NonNull
  public LinearLayout getRoot() {
    return rootView;
  }

  @NonNull
  public static ActivityRegisterBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static ActivityRegisterBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.activity_register, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static ActivityRegisterBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.btnLogin;
      Button btnLogin = ViewBindings.findChildViewById(rootView, id);
      if (btnLogin == null) {
        break missingId;
      }

      id = R.id.btnRegister;
      Button btnRegister = ViewBindings.findChildViewById(rootView, id);
      if (btnRegister == null) {
        break missingId;
      }

      id = R.id.etConfirmPassword;
      EditText etConfirmPassword = ViewBindings.findChildViewById(rootView, id);
      if (etConfirmPassword == null) {
        break missingId;
      }

      id = R.id.etEmail;
      EditText etEmail = ViewBindings.findChildViewById(rootView, id);
      if (etEmail == null) {
        break missingId;
      }

      id = R.id.etName;
      EditText etName = ViewBindings.findChildViewById(rootView, id);
      if (etName == null) {
        break missingId;
      }

      id = R.id.etPassword;
      EditText etPassword = ViewBindings.findChildViewById(rootView, id);
      if (etPassword == null) {
        break missingId;
      }

      return new ActivityRegisterBinding((LinearLayout) rootView, btnLogin, btnRegister,
          etConfirmPassword, etEmail, etName, etPassword);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}
