/*
 * Copyright (c) 2017-2019 superblaubeere27, Sam Sun, MarcoMC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package me.superblaubeere27.jobf.processors.encryption.string;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * XOR 字符串加密算法实现类，实现了 IStringEncryptionAlgorithm 接口。
 * 使用 XOR 运算对字符串进行加密和解密，并使用 Base64 编码确保加密结果可安全传输。
 */
public class XOREncryptionAlgorithm implements IStringEncryptionAlgorithm {

    /**
     * 解密字符串。
     * 首先对输入字符串进行 Base64 解码，然后使用 XOR 运算与密钥进行解密。
     * @param obj 要解密的 Base64 编码字符串
     * @param key 解密密钥
     * @return 解密后的原始字符串
     */
    public static String decrypt(String obj, String key) {
        // 对输入字符串进行 Base64 解码
        obj = new String(Base64.getDecoder().decode(obj.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
        StringBuilder sb = new StringBuilder();
        char[] keyChars = key.toCharArray();
        // 初始化密钥索引计数器
        int i = 0;
        // 遍历字符串中的每个字符进行 XOR 解密
        for (char c : obj.toCharArray()) {
            // 使用密钥字符进行 XOR 运算，密钥循环使用
            sb.append((char) (c ^ keyChars[i % keyChars.length]));
            // 递增密钥索引
            i++;
        }
        return sb.toString();
    }

    /**
     * 加密字符串。
     * 使用 XOR 运算与密钥对字符串进行加密，然后对结果进行 Base64 编码。
     * @param obj 要加密的原始字符串
     * @param key 加密密钥
     * @return 加密后经过 Base64 编码的字符串
     */
    @Override
    public String encrypt(String obj, String key) {
        StringBuilder sb = new StringBuilder();
        char[] keyChars = key.toCharArray();
        // 初始化密钥索引计数器
        int i = 0;
        // 遍历字符串中的每个字符进行 XOR 加密
        for (char c : obj.toCharArray()) {
            // 使用密钥字符进行 XOR 运算，密钥循环使用
            sb.append((char) (c ^ keyChars[i % keyChars.length]));
            // 递增密钥索引
            i++;
        }
        // 对加密结果进行 Base64 编码
        return new String(Base64.getEncoder().encode(sb.toString().getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
    }
}