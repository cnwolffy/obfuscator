/*
 * Copyright (c) 2017-2019 superblaubeere27, Sam Sun, MarcoMC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package me.superblaubeere27.jobf.processors.flowObfuscation;

import me.superblaubeere27.jobf.processors.NumberObfuscationTransformer;
import me.superblaubeere27.jobf.utils.VariableProvider;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.lang.reflect.Modifier;
import java.util.List;

/**
 * 开关语句混淆器，用于将 switch 语句转换为一系列 if-else 语句，增加代码复杂度。
 */
class SwitchMangler {

    /**
     * 混淆方法中的 switch 语句。
     * 将 TableSwitchInsnNode 和 LookupSwitchInsnNode 转换为一系列的 if-else 语句。
     * @param node 要处理的方法节点
     */
    static void mangleSwitches(MethodNode node) {
        // 跳过抽象方法和本地方法
        if (Modifier.isAbstract(node.access) || Modifier.isNative(node.access))
            return;

        // 创建变量提供者并分配一个变量槽位
        VariableProvider provider = new VariableProvider(node);
        int resultSlot = provider.allocateVar();

        // 遍历方法中的所有指令
        for (AbstractInsnNode abstractInsnNode : node.instructions.toArray()) {
            // 处理 TableSwitchInsnNode 类型的 switch 语句
            if (abstractInsnNode instanceof TableSwitchInsnNode) {
                TableSwitchInsnNode switchInsnNode = (TableSwitchInsnNode) abstractInsnNode;

                // 创建新的指令列表
                InsnList insnList = new InsnList();
                // 存储 switch 表达式的结果到变量槽位
                insnList.add(new VarInsnNode(Opcodes.ISTORE, resultSlot));

                int j = 0;

                // 为每个 case 创建一个 if 语句
                for (int i = switchInsnNode.min; i <= switchInsnNode.max; i++) {
                    // 加载存储的 switch 表达式结果
                    insnList.add(new VarInsnNode(Opcodes.ILOAD, resultSlot));
                    // 生成 case 值的指令（可能经过混淆）
                    insnList.add(NumberObfuscationTransformer.getInstructions(i));
                    // 如果相等，跳转到对应的标签
                    insnList.add(new JumpInsnNode(Opcodes.IF_ICMPEQ, switchInsnNode.labels.get(j)));

                    j++;
                }
                // 添加默认分支的跳转
                insnList.add(new JumpInsnNode(Opcodes.GOTO, switchInsnNode.dflt));

                // 插入新的指令列表并移除原有的 switch 指令
                node.instructions.insert(abstractInsnNode, insnList);
                node.instructions.remove(abstractInsnNode);
            }
            // 处理 LookupSwitchInsnNode 类型的 switch 语句
            if (abstractInsnNode instanceof LookupSwitchInsnNode) {
                LookupSwitchInsnNode switchInsnNode = (LookupSwitchInsnNode) abstractInsnNode;

                // 创建新的指令列表
                InsnList insnList = new InsnList();
                // 存储 switch 表达式的结果到变量槽位
                insnList.add(new VarInsnNode(Opcodes.ISTORE, resultSlot));

                // 获取 switch 的所有 case 值
                List<Integer> keys = switchInsnNode.keys;
                // 为每个 case 创建一个 if 语句
                for (int i = 0; i < keys.size(); i++) {
                    Integer key = keys.get(i);
                    // 加载存储的 switch 表达式结果
                    insnList.add(new VarInsnNode(Opcodes.ILOAD, resultSlot));
                    // 生成 case 值的指令（可能经过混淆）
                    insnList.add(NumberObfuscationTransformer.getInstructions(key));
                    // 如果相等，跳转到对应的标签
                    insnList.add(new JumpInsnNode(Opcodes.IF_ICMPEQ, switchInsnNode.labels.get(i)));

                }

                // 添加默认分支的跳转
                insnList.add(new JumpInsnNode(Opcodes.GOTO, switchInsnNode.dflt));

                // 插入新的指令列表并移除原有的 switch 指令
                node.instructions.insert(abstractInsnNode, insnList);
                node.instructions.remove(abstractInsnNode);
            }
        }
    }

}