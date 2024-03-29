// Generated by view binder compiler. Do not edit!
package com.example.profiscooter.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import com.example.profiscooter.R;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;

public final class ActivityRegisterUserBinding implements ViewBinding {
  @NonNull
  private final ConstraintLayout rootView;

  @NonNull
  public final EditText age;

  @NonNull
  public final TextView banner;

  @NonNull
  public final TextView bannerDescription;

  @NonNull
  public final EditText email;

  @NonNull
  public final EditText nick;

  @NonNull
  public final EditText password;

  @NonNull
  public final ProgressBar progressBar;

  @NonNull
  public final Button registerUser;

  private ActivityRegisterUserBinding(@NonNull ConstraintLayout rootView, @NonNull EditText age,
      @NonNull TextView banner, @NonNull TextView bannerDescription, @NonNull EditText email,
      @NonNull EditText nick, @NonNull EditText password, @NonNull ProgressBar progressBar,
      @NonNull Button registerUser) {
    this.rootView = rootView;
    this.age = age;
    this.banner = banner;
    this.bannerDescription = bannerDescription;
    this.email = email;
    this.nick = nick;
    this.password = password;
    this.progressBar = progressBar;
    this.registerUser = registerUser;
  }

  @Override
  @NonNull
  public ConstraintLayout getRoot() {
    return rootView;
  }

  @NonNull
  public static ActivityRegisterUserBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static ActivityRegisterUserBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.activity_register_user, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static ActivityRegisterUserBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.age;
      EditText age = ViewBindings.findChildViewById(rootView, id);
      if (age == null) {
        break missingId;
      }

      id = R.id.banner;
      TextView banner = ViewBindings.findChildViewById(rootView, id);
      if (banner == null) {
        break missingId;
      }

      id = R.id.bannerDescription;
      TextView bannerDescription = ViewBindings.findChildViewById(rootView, id);
      if (bannerDescription == null) {
        break missingId;
      }

      id = R.id.email;
      EditText email = ViewBindings.findChildViewById(rootView, id);
      if (email == null) {
        break missingId;
      }

      id = R.id.nick;
      EditText nick = ViewBindings.findChildViewById(rootView, id);
      if (nick == null) {
        break missingId;
      }

      id = R.id.password;
      EditText password = ViewBindings.findChildViewById(rootView, id);
      if (password == null) {
        break missingId;
      }

      id = R.id.progressBar;
      ProgressBar progressBar = ViewBindings.findChildViewById(rootView, id);
      if (progressBar == null) {
        break missingId;
      }

      id = R.id.registerUser;
      Button registerUser = ViewBindings.findChildViewById(rootView, id);
      if (registerUser == null) {
        break missingId;
      }

      return new ActivityRegisterUserBinding((ConstraintLayout) rootView, age, banner,
          bannerDescription, email, nick, password, progressBar, registerUser);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}
