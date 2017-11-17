package com.tribe.app.presentation.view.utils;

import android.view.ViewTreeObserver;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.view.widget.TextViewFont;
import java.util.List;

/**
 * Created by tiago on 17/10/2017.
 */

public class TextViewUtils {

  public static void constraintTextInto(TextViewFont txt, List<User> userList) {
    txt.getViewTreeObserver()
        .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
          @Override public void onGlobalLayout() {
            txt.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            int measureWidth = txt.getMeasuredWidth() - (int) txt.getPaint().measureText("... +x");
            boolean shouldEllipsize = false;

            int totalWidth = 0, count = 0;
            StringBuffer buffer = new StringBuffer();
            for (int i = 0; i < userList.size(); i++) {
              User user = userList.get(i);
              String name = user.getDisplayName();
              float txtWidth = txt.getPaint().measureText(name);
              if (totalWidth + txtWidth < measureWidth) {
                totalWidth += txtWidth;
                buffer.append(name);
                if (count < userList.size() - 1) buffer.append(", ");
                count++;
              } else {
                if (buffer.length() >= 2) {
                  buffer.replace(buffer.length() - 2, buffer.length() - 1, "");
                  shouldEllipsize = true;
                }
                break;
              }
            }

            if (shouldEllipsize) {
              buffer.append("... +" + (userList.size() - count));
            }

            txt.setText(buffer.toString());
          }
        });
  }
}
