/*
 * Copyright (c) 2017-2019 superblaubeere27, Sam Sun, MarcoMC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package me.superblaubeere27.annotations;

import static me.superblaubeere27.annotations.ObfuscationTransformer.*;

/**
 * 混淆规则注解，用于指定对特定元素的混淆行为。
 * 该注解可以应用于类、方法或字段上，定义是否允许特定的混淆处理器对其进行处理。
 * @author superblaubeere27
 */
public @interface Rule {
    /**
     * 返回混淆动作，指定是允许还是禁止混淆。
     *
     * @return 混淆动作（ALLOW 或 DISALLOW）
     */
    Action value();

    /**
     * 返回应用此规则的混淆处理器数组。
     * 默认情况下，包含所有可用的混淆处理器。
     *
     * @return 混淆处理器数组
     */
    ObfuscationTransformer[] processors() default {
            FLOW_OBFUSCATION, // 控制流混淆
            LINE_NUMBER_REMOVER, // 行号移除
            NUMBER_OBFUSCATION, // 数字混淆
            STRING_ENCRYPTION, // 字符串加密
            HWID_PROTECTION, // HWID 保护
            PEEPHOLE_OPTIMIZER, // 窥孔优化器
            CRASHER, // 崩溃器
            INVOKE_DYNAMIC, // 动态调用
            REFERENCE_PROXY, // 引用代理
            SHUFFLE_MEMBERS, // 成员重排
            INNER_CLASS_REMOVER, // 内部类移除
            NAME_OBFUSCATION, // 名称混淆
            HIDE_MEMBERS, // 隐藏成员
            INLINING // 内联优化
    };

    /**
     * 混淆动作枚举，定义了允许或禁止混淆的行为。
     */
    enum Action {
        /**
         * 允许混淆
         */
        ALLOW,
        /**
         * 禁止混淆
         */
        DISALLOW
    }
}