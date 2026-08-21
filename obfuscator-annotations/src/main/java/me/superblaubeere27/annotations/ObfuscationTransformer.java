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

/**
 * 混淆转换器枚举，定义了各种代码混淆的类型。
 * 这些转换器用于在代码混淆过程中应用不同的混淆策略。
 */
public enum ObfuscationTransformer {
    /**
     * 控制流混淆 - 通过修改代码的控制流结构增加代码复杂度
     */
    FLOW_OBFUSCATION,
    /**
     * 行号移除 - 移除代码中的行号信息，使调试更加困难
     */
    LINE_NUMBER_REMOVER,
    /**
     * 数字混淆 - 对代码中的数字常量进行混淆处理
     */
    NUMBER_OBFUSCATION,
    /**
     * 字符串加密 - 对代码中的字符串进行加密处理
     */
    STRING_ENCRYPTION,
    /**
     * HWID 保护 - 添加硬件 ID 验证保护
     */
    HWID_PROTECTION,
    /**
     * 窥孔优化器 - 应用窥孔优化技术
     */
    PEEPHOLE_OPTIMIZER,
    /**
     * 崩溃器 - 可能用于添加反调试或反分析机制
     */
    CRASHER,
    /**
     * 动态调用 - 使用 invokedynamic 指令
     */
    INVOKE_DYNAMIC,
    /**
     * 引用代理 - 通过代理方式处理引用
     */
    REFERENCE_PROXY,
    /**
     * 成员重排 - 重新排列类成员的顺序
     */
    SHUFFLE_MEMBERS,
    /**
     * 内部类移除 - 移除内部类信息
     */
    INNER_CLASS_REMOVER,
    /**
     * 名称混淆 - 混淆类、方法和字段的名称
     */
    NAME_OBFUSCATION,
    /**
     * 字段名称混淆 - 混淆私有字段的名称
     */
    FIELD_OBFUSCATION,
    /**
     * 成员隐藏 - 隐藏类成员
     */
    HIDE_MEMBERS,
    /**
     * 内联 - 内联方法调用
     */
    INLINING
}