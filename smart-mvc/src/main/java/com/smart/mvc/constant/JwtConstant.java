package com.smart.mvc.constant;

import java.util.UUID;

public class JwtConstant {
  public static final String JWT_ID = UUID.randomUUID().toString().replaceAll("-", "");
  /**
   * 加密密文
   */
  public static final String JWT_SECRET = "0abc54b8f26911eab443a45e60efbf2d";
  public static final int JWT_TTL = 60 * 60 * 1000; // 一个小时，单位millisecond
}
