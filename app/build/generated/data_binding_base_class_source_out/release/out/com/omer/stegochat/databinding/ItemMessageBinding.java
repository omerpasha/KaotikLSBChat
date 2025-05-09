// Generated by view binder compiler. Do not edit!
package com.omer.stegochat.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import com.omer.stegochat.R;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;

public final class ItemMessageBinding implements ViewBinding {
  @NonNull
  private final LinearLayout rootView;

  @NonNull
  public final ImageView imageViewMessage;

  @NonNull
  public final TextView textViewMessage;

  private ItemMessageBinding(@NonNull LinearLayout rootView, @NonNull ImageView imageViewMessage,
      @NonNull TextView textViewMessage) {
    this.rootView = rootView;
    this.imageViewMessage = imageViewMessage;
    this.textViewMessage = textViewMessage;
  }

  @Override
  @NonNull
  public LinearLayout getRoot() {
    return rootView;
  }

  @NonNull
  public static ItemMessageBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static ItemMessageBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.item_message, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static ItemMessageBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.imageViewMessage;
      ImageView imageViewMessage = ViewBindings.findChildViewById(rootView, id);
      if (imageViewMessage == null) {
        break missingId;
      }

      id = R.id.textViewMessage;
      TextView textViewMessage = ViewBindings.findChildViewById(rootView, id);
      if (textViewMessage == null) {
        break missingId;
      }

      return new ItemMessageBinding((LinearLayout) rootView, imageViewMessage, textViewMessage);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}
