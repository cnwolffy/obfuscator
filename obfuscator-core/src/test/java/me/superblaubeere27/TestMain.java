/*
 * www.matou.tech Inc.
 * Copyright © 2020-2022 CODE INVESTMENT TECHNOLOGY . All rights reserved.
 */
package me.superblaubeere27;

/*
 * 修订记录:
 * turalyon@matou.tech 2025-02-26 18:44 创建
 *
 */


import me.superblaubeere27.jobf.JObf;

/**
 * 说明：
 * <p>
 *     参数
 * --cp
 * D:\SourceCode\Java\Matou\smart-procure\ydzb-intelligence-procure-client-builder\release\common\libs
 * -cp
 * D:\SourceCode\Java\Matou\smart-procure\ydzb-intelligence-procure-client\intelligence-procure-client-common\target\poi-common-1.12.2.jar
 * -cp
 * D:\SourceCode\repository\com\baomidou\mybatis-plus-core\3.5.7\mybatis-plus-core-3.5.7.jar
 * --threads
 * 1
 * --config
 * D:\SourceCode\Java\github\obfuscator\test-dir\poi-ext.jocfg
 * -jarIn
 * D:\SourceCode\Java\github\obfuscator\test-dir\test.jar
 * --jarOut
 * D:\SourceCode\Java\github\obfuscator\test-dir\test-out.jar
 * @author turalyon@matou.tech
 */
public class TestMain {
    public static void main(String[] args) throws Exception {

        if (args==null || args.length==0) {
            args = new String[10];
            args[0] = "--cp";
            args[1] = "D:\\SourceCode\\Java\\Matou\\smart-procure\\ydzb-intelligence-procure-client-builder\\release\\common\\libs;D:\\SourceCode\\Java\\Matou\\smart-procure\\ydzb-intelligence-procure-client\\intelligence-procure-client-common\\target\\poi-common-1.12.2.jar";
            args[2] = "--threads";
            args[3] = "1";
            args[4] = "--config";
            args[5] = "D:\\SourceCode\\Java\\github\\obfuscator\\test-dir\\poi-ext.jocfg";
            args[6] = "--jarIn";
            args[7] = "D:\\SourceCode\\Java\\github\\obfuscator\\test-dir\\test.jar";
            args[8] = "--jarOut";
            args[9] = "D:\\SourceCode\\Java\\github\\obfuscator\\test-dir\\test-out.jar";
        }

        JObf.main(args);
    }
}
