package com.mnd.healthhub.common;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.MD5;

/**
 * 工具类：主要的方法是将用户名作为盐值加密密码。
 */
public class PasswordEncryptor {

    private PasswordEncryptor(){}

    /**
     * 使用用户名作为盐值生成加密密码。
     * @param username 用户名
     * @param rawPassword 明文密码
     * @return 密文密码。
     */
    public static String encryptWithUsernameSalt(String username, String rawPassword){
        MD5 md5 = MD5.create();
        // 1.对用户名进行哈希
        String usernameMd5 = md5.digestHex(username);
        // 2.取出前6位
        String saltPrefix = StrUtil.subWithLength(usernameMd5, 0, 6);
        // 3.取出后3位
        String saltSuffix = StrUtil.subSuf(usernameMd5, usernameMd5.length() - 3);
        // 4.拼接：盐前缀 + 明文密码 + 盐后缀
        String saltedPassword = saltPrefix + rawPassword + saltSuffix;
        // 5.加密
        return md5.digestHex(saltedPassword).toUpperCase();
    }

    public static void main(String[] args) {
        String s = PasswordEncryptor.encryptWithUsernameSalt("admin", "new123456");
        System.out.println(s);
    }
}

















