package com.smart.mvc.util;

import java.util.Base64;
//import org.apache.tomcat.util.codec.binary.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import com.google.gson.Gson;
import com.smart.mvc.constant.JwtConstant;
import com.smart.mvc.model.Item;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

public class JwtUtil {

  /**
   * 由字符串生成加密key
   *
   * @return
   */
  public SecretKey generalKey() {
    String stringKey = JwtConstant.JWT_SECRET;

    // 本地的密码解码
    byte[] encodedKey = Base64.getDecoder().decode(stringKey);

    // 根据给定的字节数组使用AES加密算法构造一个密钥
    SecretKey key = new SecretKeySpec(encodedKey, 0, encodedKey.length, "AES");

    return key;
  }

  /**
   * 创建jwt
   * 
   * @param id UUID生成的随机字符串
   * @param issuer 签发者
   * @param uid 用户ID
   * @param subject 自定义字符串
   * @param ttlMillis 超期时长（毫秒）
   * @return Base64字符串
   * @throws Exception
   */
  public String createJWT(String id, String issuer, String uid, String subject, long ttlMillis)
      throws Exception {

    // 指定签名的时候使用的签名算法，也就是header那部分，jjwt已经将这部分内容封装好了。
    SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;

    // 生成JWT的时间
    long nowMillis = System.currentTimeMillis();
    Date now = new Date(nowMillis);
//    Date exp = new Date(nowMillis + ttlMillis);

    // 创建payload的私有声明（根据特定的业务需要添加，如果要拿这个做验证，一般是需要和jwt的接收方提前沟通好验证方式的）
    Map<String, Object> claims = new HashMap<>();
    claims.put("uid", uid);
//    claims.put("user_name", "admin");
//    claims.put("nick_name", "X-rapido");

    // 生成签名的时候使用的秘钥secret，切记这个秘钥不能外露哦。它就是你服务端的私钥，在任何场景都不应该流露出去。
    // 一旦客户端得知这个secret, 那就意味着客户端是可以自我签发jwt了。
    SecretKey key = generalKey();

    // 下面就是在为payload添加各种标准声明和私有声明了
    JwtBuilder builder = Jwts.builder() // 这里其实就是new一个JwtBuilder，设置jwt的body
        .setClaims(claims) // 如果有私有声明，一定要先设置这个自己创建的私有的声明，这个是给builder的claim赋值，一旦写在标准的声明赋值之后，就是覆盖了那些标准的声明的
        .setId(id) // 设置jti(JWT ID)：是JWT的唯一标识，根据业务需要，这个可以设置为一个不重复的值，主要用来作为一次性token,从而回避重放攻击。
        .setIssuedAt(now) // iat: jwt的签发时间
        .setIssuer(issuer) // issuer：jwt签发人
        .setSubject(subject) // sub(Subject)：代表这个JWT的主体，即它的所有人，这个是一个json格式的字符串，可以存放什么userid，roldid之类的，作为什么用户的唯一标志。
        .signWith(signatureAlgorithm, key); // 设置签名使用的签名算法和签名使用的秘钥

    // 设置过期时间
    if (ttlMillis >= 0) {
      long expMillis = nowMillis + ttlMillis;
      Date exp = new Date(expMillis);
      builder.setExpiration(exp);
    }
    return builder.compact();
  }

  /**
   * 解密jwt
   *
   * @param jwt
   * @return
   * @throws Exception
   */
  public Claims parseJWT(String jwt) throws Exception {
    SecretKey key = generalKey(); // 签名秘钥，和生成的签名的秘钥一模一样
    Claims claims = Jwts.parser() // 得到DefaultJwtParser
        .setSigningKey(key) // 设置签名的秘钥
        .parseClaimsJws(jwt).getBody(); // 设置需要解析的jwt
    return claims;
  }

  public static void main(String[] args) {

    Item user = Item.create("tingfeng", "bulingbuling");
    String subject = new Gson().toJson(user);

    try {
      JwtUtil util = new JwtUtil();
      String jwt = util.createJWT(JwtConstant.JWT_ID, "smart-sso", user.getLabel(), null, JwtConstant.JWT_TTL);
      System.out.println("JWT：" + jwt);

      System.out.println("\n解密\n");

      Claims c = util.parseJWT(jwt);
      System.out.println(c.getId());
      System.out.println(c.getIssuedAt());
      System.out.println(c.getExpiration());
      System.out.println(c.getIssuer());
      System.out.println(c.get("uid", String.class));
      System.out.println(c.getSubject());
    } catch (Exception e) {
      e.printStackTrace();
    }

  }
}
