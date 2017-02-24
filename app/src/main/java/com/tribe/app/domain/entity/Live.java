package com.tribe.app.domain.entity;

import java.util.List;

/**
 * Created by tiago on 23/02/2017.
 */

public class Live {

  private String id;
  private String displayName;
  private String picture;
  private List<String> membersPics;
  private boolean isGroup;
  private String sessionId;

  private Live(Builder builder) {
    this.id = builder.id;
    this.displayName = builder.displayName;
    this.picture = builder.picture;
    this.membersPics = builder.membersPics;
    this.isGroup = builder.isGroup;
    this.sessionId = builder.sessionId;
  }

  public static class Builder {

    private String id;
    private String displayName;
    private String picture;
    private List<String> membersPics;
    private boolean isGroup;
    private String sessionId;
    private int color;

    public Builder(String id) {
      this.id = id;
    }

    public Builder displayName(String displayName) {
      this.displayName = displayName;
      return this;
    }

    public Builder picture(String picture) {
      this.picture = picture;
      return this;
    }

    public Builder membersPics(List<String> membersPics) {
      this.membersPics = membersPics;
      return this;
    }

    public Builder isGroup(boolean isGroup) {
      this.isGroup = isGroup;
      return this;
    }

    public Builder sessionId(String sessionId) {
      this.sessionId = sessionId;
      return this;
    }

    public Live build() {
      return new Live(this);
    }
  }
}
