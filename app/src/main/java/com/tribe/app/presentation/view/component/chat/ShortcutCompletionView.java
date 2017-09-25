package com.tribe.app.presentation.view.component.chat;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import butterknife.ButterKnife;
import com.tokenautocomplete.TokenCompleteTextView;
import com.tribe.app.R;
import com.tribe.app.domain.entity.Shortcut;

public class ShortcutCompletionView extends TokenCompleteTextView<Shortcut> {

  public ShortcutCompletionView(Context context) {
    super(context);
  }

  public ShortcutCompletionView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public ShortcutCompletionView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
  }

  @Override protected View getViewForObject(Shortcut shortcut) {
    LayoutInflater l =
        (LayoutInflater) getContext().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
    View parent = l.inflate(R.layout.view_shortcut_completion, (ViewGroup) getParent(), false);
    ShortcutCompletionTokenView token =
        ButterKnife.findById(parent, R.id.viewShortcutCompletionToken);
    token.setShortcut(shortcut);
    return parent;
  }

  @Override protected Shortcut defaultObject(String completionText) {
    return new Shortcut(completionText);
  }
}
