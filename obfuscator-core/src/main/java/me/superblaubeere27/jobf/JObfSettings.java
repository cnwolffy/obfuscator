/*
 * Copyright (c) 2017-2019 superblaubeere27, Sam Sun, MarcoMC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package me.superblaubeere27.jobf;

import me.superblaubeere27.jobf.utils.values.BooleanValue;
import me.superblaubeere27.jobf.utils.values.DeprecationLevel;
import me.superblaubeere27.jobf.utils.values.FilePathValue;
import me.superblaubeere27.jobf.utils.values.StringValue;

/**
 * JObfSettings 类，用于存储混淆器的一般设置。
 * 包含生成混淆名称的字符、字典设置和压缩方式等配置选项。
 */
@lombok.Getter
public class JObfSettings {
    /**
     * 处理器名称，用于标识设置所属的类别。
     */
    private static final String PROCESSOR_NAME = "General Settings";

    /**
     * 生成混淆名称时使用的字符。
     */
    private StringValue generatorChars = new StringValue(PROCESSOR_NAME, "Generator characters", DeprecationLevel.GOOD, "Il");

    /**
     * 是否使用自定义字典。
     */
    private BooleanValue useCustomDictionary = new BooleanValue(PROCESSOR_NAME, "Custom dictionary", DeprecationLevel.GOOD, false);

    /**
     * 类名字典文件路径。
     */
    private FilePathValue classNameDictionary = new FilePathValue(PROCESSOR_NAME, "Class Name dictionary", DeprecationLevel.GOOD, "");

    /**
     * 名称字典文件路径。
     */
    private FilePathValue nameDictionary = new FilePathValue(PROCESSOR_NAME, "Name dictionary", DeprecationLevel.GOOD, "");

    /**
     * 是否使用 STORE 而不是 DEFLATE 压缩（例如用于 SpringBoot）。
     */
    private BooleanValue useStore = new BooleanValue(PROCESSOR_NAME, "Use STORE instead of DEFLATE (For e.g. SpringBoot)", DeprecationLevel.GOOD, false);

}

