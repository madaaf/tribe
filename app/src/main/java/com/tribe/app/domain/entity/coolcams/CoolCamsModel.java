package com.tribe.app.domain.entity.coolcams;

import com.tribe.app.R;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;
import javax.annotation.Resource;

/**
 * Created by tiago on 03/15/2018.
 */

public class CoolCamsModel {

  public enum CoolCamsFeatureEnum {
    NO_FACE("noFace"), HAS_SMILE("hasSmile"), HAS_ANGLE("hasAngle"), ONE_EYE_CLOSED(
        "oneEyeClosed"), TWO_EYES_CLOSED("twoEyesClosed");

    private String feature;

    CoolCamsFeatureEnum(String feature) {
      this.feature = feature;
    }
  }

  public enum CoolCamsStepsEnum {
    A("a"), B("b"), C("c"), D("d"), E("e"), F("f"), G("g"), H("h"), I("i"), J("j"), K("k");

    private String step;

    CoolCamsStepsEnum(String step) {
      this.step = step;
    }

    public @Resource int getIcon() {
      if (this.equals(A)) {
        return R.drawable.picto_coolcams_emoji_0;
      } else if (this.equals(B)) {
        return R.drawable.picto_coolcams_emoji_1;
      } else if (this.equals(C)) {
        return R.drawable.picto_coolcams_emoji_7;
      } else if (this.equals(D)) {
        return R.drawable.picto_coolcams_emoji_3;
      } else if (this.equals(E)) {
        return R.drawable.picto_coolcams_emoji_2;
      } else if (this.equals(F)) {
        return R.drawable.picto_coolcams_emoji_4;
      } else if (this.equals(G)) {
        return R.drawable.picto_coolcams_emoji_5;
      } else if (this.equals(H)) {
        return R.drawable.picto_coolcams_emoji_6;
      } else if (this.equals(I)) {
        return R.drawable.picto_coolcams_emoji_8;
      } else if (this.equals(J)) {
        return R.drawable.picto_coolcams_emoji_9;
      } else {
        return R.drawable.picto_coolcams_emoji_10;
      }
    }

    public String getStep() {
      return step;
    }

    public static List<CoolCamsStepsEnum> getSteps() {
      return new ArrayList<>(EnumSet.allOf(CoolCamsStepsEnum.class));
    }

    public static CoolCamsStepsEnum randomStep(CoolCamsStepsEnum lastStep) {
      List<CoolCamsStepsEnum> steps = getSteps();
      CoolCamsStepsEnum step = null;
      Random random = new Random();

      while (step == null || (lastStep != null && step == lastStep)) {
        step = steps.get(random.nextInt(steps.size()));
      }

      return step;
    }

    public List<CoolCamsFeatureEnum> getFeatures() {
      List<CoolCamsFeatureEnum> features = new ArrayList<>();

      if (this.equals(A)) {
        features.add(CoolCamsFeatureEnum.HAS_SMILE);
      } else if (this.equals(B)) {
        features.add(CoolCamsFeatureEnum.HAS_SMILE);
        features.add(CoolCamsFeatureEnum.TWO_EYES_CLOSED);
      } else if (this.equals(C)) {
        features.add(CoolCamsFeatureEnum.ONE_EYE_CLOSED);
      } else if (this.equals(D)) {
        features.add(CoolCamsFeatureEnum.TWO_EYES_CLOSED);
      } else if (this.equals(E)) {
        features.add(CoolCamsFeatureEnum.HAS_SMILE);
      } else if (this.equals(F)) {
        features.add(CoolCamsFeatureEnum.HAS_SMILE);
      } else if (this.equals(G)) {
        features.add(CoolCamsFeatureEnum.HAS_SMILE);
      } else if (this.equals(H)) {
        features.add(CoolCamsFeatureEnum.HAS_SMILE);
        features.add(CoolCamsFeatureEnum.TWO_EYES_CLOSED);
      } else if (this.equals(I)) {
        features.add(CoolCamsFeatureEnum.HAS_SMILE);
        features.add(CoolCamsFeatureEnum.ONE_EYE_CLOSED);
      } else if (this.equals(J)) {
        features.add(CoolCamsFeatureEnum.HAS_ANGLE);
      } else if (this.equals(K)) {
        features.add(CoolCamsFeatureEnum.NO_FACE);
      }

      return features;
    }

    public static List<CoolCamsStepsEnum> generateGame(int nbSteps) {
      List<CoolCamsStepsEnum> steps = new ArrayList<>();
      for (int i = 0; i < nbSteps; i++) {
        steps.add(
            CoolCamsStepsEnum.randomStep(steps.size() > 0 ? steps.get(steps.size() - 1) : null));
      }
      return steps;
    }
  }

  public enum CoolCamsStatus {
    NONE, STEP
  }
}

