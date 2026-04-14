/*
 * Copyright (c) 2017-2019 superblaubeere27, Sam Sun, MarcoMC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package me.superblaubeere27.jobf.utils.values;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import lombok.AccessLevel;
import lombok.Setter;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration 类，用于存储混淆器的配置信息。
 * 包含输入文件、输出文件、脚本和库文件等配置选项。
 */
@Getter
@Setter
public class Configuration {
    /**
     * 输入文件路径。
     * -- SETTER --
     *  设置输入文件路径。
     *
     * @param input 输入文件路径

     */
    private String input;
    
    /**
     * 输出文件路径。
     * -- SETTER --
     *  设置输出文件路径。
     *
     * @param output 输出文件路径

     */
    private String output;
    
    /**
     * 脚本路径。
     * -- SETTER --
     *  设置脚本路径。
     *
     * @param script 脚本路径
     */
    private String script;
    
    /**
     * 库文件路径列表。
     */
    @Setter(AccessLevel.NONE)
    @Getter
    private List<String> libraries;

    /**
     * 创建一个新的 Configuration 实例。
     * 
     * @param input 输入文件路径
     * @param output 输出文件路径
     * @param script 脚本路径
     * @param libraries 库文件路径列表
     */
    public Configuration(String input, String output, String script, List<String> libraries) {
        this.input = input;
        this.output = output;
        this.script = script;
        this.libraries = libraries;
    }

    /**
     * 从 JSON 对象创建 Configuration 实例。
     * 
     * @param obj 包含配置信息的 JSON 对象
     * @return 新的 Configuration 实例
     */
    /**
     * 从 JSON 对象创建 Configuration 实例。
     * <p>
     * 该方法从给定的 JSON 对象中提取配置信息，包括输入文件路径、输出文件路径、
     * 脚本路径和库文件路径列表。如果 JSON 对象中缺少某些字段，则使用默认值。
     * </p>
     * 
     * @param obj 包含配置信息的 JSON 对象
     * @return 新的 Configuration 实例，包含从 JSON 对象中提取的配置信息
     */
    static Configuration fromJsonObject(JsonObject obj) {
        // 初始化默认值
        String input = "";
        String output = "";
        String script = null;
        List<String> libraries = new ArrayList<>();

        // 从 JSON 对象中提取输入文件路径
        if (obj.has("input")) {
            input = obj.get("input").getAsString();
        }
        // 从 JSON 对象中提取输出文件路径
        if (obj.has("output")) {
            output = obj.get("output").getAsString();
        }
        // 从 JSON 对象中提取脚本路径
        if (obj.has("script")) {
            script = obj.get("script").getAsString();
        }
        // 从 JSON 对象中提取库文件路径列表
        if (obj.has("libraries")) {
            JsonArray jsonArray = obj.getAsJsonArray("libraries");

            for (JsonElement jsonElement : jsonArray) {
                libraries.add(jsonElement.getAsString());
            }
        }

        // 使用提取的配置信息创建并返回新的 Configuration 实例
        return new Configuration(input, output, script, libraries);
    }

    /**
     * 将配置信息添加到 JSON 对象中。
     * 
     * @param jsonObject 要添加配置信息的 JSON 对象
     */
    void addToJsonObject(JsonObject jsonObject) {
        jsonObject.addProperty("input", input);
        jsonObject.addProperty("output", output);
        jsonObject.addProperty("script", script);

        JsonArray array = new JsonArray();

        for (String library : libraries) {
            array.add(new JsonPrimitive(library));
        }

        jsonObject.add("libraries", array);
    }

}