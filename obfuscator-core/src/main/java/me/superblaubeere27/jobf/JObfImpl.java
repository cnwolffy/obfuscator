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

import com.google.common.io.ByteStreams;
import lombok.extern.slf4j.Slf4j;
import me.superblaubeere27.jobf.processors.*;
import me.superblaubeere27.jobf.processors.flowObfuscation.FlowObfuscator;
import me.superblaubeere27.jobf.processors.name.ClassWrapper;
import me.superblaubeere27.jobf.processors.name.INameObfuscationProcessor;
import me.superblaubeere27.jobf.processors.name.InnerClassRemover;
import me.superblaubeere27.jobf.processors.name.NameObfuscation;
import me.superblaubeere27.jobf.processors.optimizer.Optimizer;
import me.superblaubeere27.jobf.processors.packager.Packager;
import me.superblaubeere27.jobf.utils.*;
import me.superblaubeere27.jobf.utils.scheduler.ScheduledRunnable;
import me.superblaubeere27.jobf.utils.scheduler.Scheduler;
import me.superblaubeere27.jobf.utils.script.JObfScript;
import me.superblaubeere27.jobf.utils.values.Configuration;
import me.superblaubeere27.jobf.utils.values.ValueManager;
import org.apache.commons.lang3.StringUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ModifiedClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FrameNode;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.zip.*;

/**
 * JObfImpl 类是混淆器的核心实现类，负责协调整个混淆过程。
 * 它管理类的加载、转换和输出，以及各种混淆处理器的协调。
 */
@Slf4j(topic = "obfuscator")
public class JObfImpl {
    /**
     * JObfImpl 的单例实例。
     */
    public static final JObfImpl INSTANCE = new JObfImpl();

    /**
     * 类转换器列表，包含所有要应用的混淆处理器。
     */
    public static List<IClassTransformer> processors;

    /**
     * 存储加载的类节点，键为类名，值为对应的 ClassNode。
     */
    public static HashMap<String, ClassNode> classes = new HashMap<>();

    /**
     * 存储非类文件，键为文件名，值为文件内容的字节数组。
     */
    public static HashMap<String, byte[]> files = new HashMap<>();

    /**
     * 预处理转换器列表。
     */
    private static List<IPreClassTransformer> preProcessors;

    /**
     * 混淆脚本，用于控制混淆行为。
     */
    public JObfScript script;

    /**
     * 主类是否被更改的标志。
     */
    private boolean mainClassChanged;

    /**
     * 名称混淆处理器列表。
     */
    private final List<INameObfuscationProcessor> nameObfuscationProcessors = new ArrayList<>();

    /**
     * 主类名称。
     */
    private String mainClass;

    /**
     * 类路径映射，键为类名，值为对应的 ClassWrapper。
     */
    private Map<String, ClassWrapper> classPath = new HashMap<>();

    /**
     * 类层次结构映射，键为类名，值为对应的 ClassTree。
     */
    private Map<String, ClassTree> hierarchy = new HashMap<>();

    /**
     * 库类节点集合。
     */
    private Set<ClassWrapper> libraryClassNodes = new HashSet<>();

    /**
     * 库文件列表。
     */
    private List<File> libraryFiles;

    /**
     * 计算模式，用于 ASM 类写入器。
     */
    private int computeMode;

    /**
     * 是否使用 invokedynamic 指令。
     */
    private boolean invokeDynamic;

    /**
     * 混淆器设置。
     */
    private final JObfSettings settings = new JObfSettings();

    /**
     * 线程数，默认为可用处理器数量的最大值，最少为1。
     */
    private int threadCount = Math.max(1, Runtime.getRuntime().availableProcessors());


    public JObfImpl() {
        processors = new ArrayList<>();

        ValueManager.registerClass(settings);

        addProcessors();
    }

    public static HashMap<String, ClassNode> getClasses() {
        return classes;
    }

    public String getMainClass() {
        return mainClass;
    }

    private void setMainClass(String mainClass) {
        this.mainClass = mainClass;
    }

    public ClassTree getTree(String ref) {
        if (!hierarchy.containsKey(ref)) {
            ClassWrapper wrapper = classPath.get(ref);

            if (wrapper == null)
                return null;

            buildHierarchy(wrapper, null, false);
        }

        return hierarchy.get(ref);
    }

    /**
     * 构建类的层次结构。
     * 递归地构建类的继承层次结构，包括父类和接口，并处理缺失类的情况。
     *
     * @param classWrapper       要构建层次结构的类包装器
     * @param sub                子类包装器，如果当前类是作为子类的父类被处理
     * @param acceptMissingClass 是否接受缺失的类
     * @throws MissingClassException 如果缺少必要的类且 acceptMissingClass 为 false
     */
    public void buildHierarchy(ClassWrapper classWrapper, ClassWrapper sub, boolean acceptMissingClass) {
        // 如果该类的层次结构尚未构建
        if (hierarchy.get(classWrapper.classNode.name) == null) {
            // 创建新的类树节点
            ClassTree tree = new ClassTree(classWrapper);

            // 处理父类
            if (classWrapper.classNode.superName != null) {
                tree.parentClasses.add(classWrapper.classNode.superName);
                ClassWrapper superClass = classPath.get(classWrapper.classNode.superName);
                boolean isJdkClass = false;
                if (superClass==null){
                    isJdkClass= JdkClassUtils.isJdkClass(classWrapper.classNode.superName) || JdkClassUtils.isSpringClass(classWrapper.classNode.superName);
                }
                // 检查父类是否存在
                if (superClass == null && !acceptMissingClass)
                    if (!isJdkClass){
                        throw new MissingClassException(classWrapper.classNode.superName + " (referenced in " + classWrapper.classNode.name + ") is missing in the classPath.");
                    }
                else if (superClass == null) {
                    // 标记缺少父类
                    tree.missingSuperClass = true;
                    log.warn("Missing class: " + classWrapper.classNode.superName + " (No methods of subclasses will be remapped)");
                } else {
                    // 递归构建父类的层次结构
                    buildHierarchy(superClass, classWrapper, acceptMissingClass);

                    // 继承缺少父类的状态
                    if (hierarchy.get(classWrapper.classNode.superName).missingSuperClass) {
                        tree.missingSuperClass = true;
                    }
                }
            }

            // 处理接口
            if (classWrapper.classNode.interfaces != null && !classWrapper.classNode.interfaces.isEmpty()) {
                for (String s : classWrapper.classNode.interfaces) {
                    tree.parentClasses.add(s);
                    ClassWrapper interfaceClass = classPath.get(s);
                    boolean isJdkInterface = false;
                    // 忽略java jdk自带的接口，如Runnable、Callable等
                    if (interfaceClass==null){
                        // 判断是否是java jdk自带的接口
                        isJdkInterface = JdkClassUtils.isJdkClass(s) || JdkClassUtils.isSpringClass(s);
                    }

                    // 检查接口是否存在
                    if (interfaceClass == null && !acceptMissingClass)
                        if (!isJdkInterface) {
                            throw new MissingClassException(s + " (referenced in " + classWrapper.classNode.name + ") is missing in the classPath.");
                        }
                    else if (interfaceClass == null) {
                        // 标记缺少父类（接口）
                        tree.missingSuperClass = true;
                        log.warn("Missing interface class: " + s + " (No methods of subclasses will be remapped)");
                    } else {
                        // 递归构建接口的层次结构
                        buildHierarchy(interfaceClass, classWrapper, acceptMissingClass);

                        // 继承缺少父类的状态
                        if (hierarchy.get(s).missingSuperClass) {
                            tree.missingSuperClass = true;
                        }
                    }
                }
            }

            // 将构建好的层次结构添加到映射中
            hierarchy.put(classWrapper.classNode.name, tree);
        }

        // 如果存在子类，将子类添加到当前类的子类列表中
        if (sub != null) {
            hierarchy.get(classWrapper.classNode.name).subClasses.add(sub.classNode.name);
        }
    }

    //    private Map<String, ClassWrapper> loadClasspathFile(File file) throws IOException {
    //        Map<String, ClassWrapper> map = new HashMap<>();
    //
    //        ZipFile zipIn = new ZipFile(file);
    //        Enumeration<? extends ZipEntry> entries = zipIn.entries();
    //        while (entries.hasMoreElements()) {
    //            ZipEntry ent = entries.nextElement();
    //            if (ent.getName().endsWith(".class")) {
    //                byte[] bytes = ByteStreams.toByteArray(zipIn.getInputStream(ent));
    //
    //                ClassReader reader = new ClassReader(bytes);
    //                ClassNode node = new ClassNode();
    //                reader.accept(node, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
    //                map.put(node.name, new ClassWrapper(node, true, bytes));
    //            }
    //        }
    //        zipIn.close();
    //
    //        return map;
    //    }
    private List<byte[]> loadClasspathFile(File file) throws IOException {
        ZipFile zipIn = new ZipFile(file);
        Enumeration<? extends ZipEntry> entries = zipIn.entries();

        boolean isJmod = file.getName().endsWith(".jmod");

        List<byte[]> byteList = new ArrayList<>(zipIn.size());

        while (entries.hasMoreElements()) {
            ZipEntry ent = entries.nextElement();
            if (ent.getName().endsWith(".class") && (!isJmod || !ent.getName().endsWith("module-info.class") && ent.getName().startsWith("classes/"))) {
                byteList.add(ByteStreams.toByteArray(zipIn.getInputStream(ent)));
            }
        }
        zipIn.close();

        return byteList;
    }

    private void loadClasspath() throws IOException {
        if (libraryFiles != null) {
            int i = 0;

            LinkedList<byte[]> byteList = new LinkedList<>();

            for (File file : libraryFiles) {
                if (file.isFile()) {
                    log.info("Loading " + file.getAbsolutePath() + " (" + (i++ * 100 / libraryFiles.size()) + "%)");
                    byteList.addAll(loadClasspathFile(file));
                    //                    classPath.putAll(loadClasspathFile(file));
                } else {
                    Files.walk(file.toPath()).map(Path::toFile).filter(f -> f.getName().endsWith(".jar") || f.getName().endsWith(".zip") || f.getName().endsWith(".jmod")).forEach(f -> {
                        log.info("Loading " + f.getName() + " (from " + file.getAbsolutePath() + ") to memory");
                        try {
                            byteList.addAll(loadClasspathFile(f));
                            //                            classPath.putAll(loadClasspathFile(f));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                }
            }

            log.info("Read " + byteList.size() + " class files to memory");
            log.info("Parsing class files...");

            ScheduledRunnable runnable = () -> {
                Map<String, ClassWrapper> map = new HashMap<>();

                while (true) {
                    byte[] bytes;

                    synchronized (byteList) {
                        bytes = byteList.poll();
                    }

                    if (bytes == null)
                        break;

                    ClassReader reader = new ClassReader(bytes);
                    ClassNode node = new ClassNode();
                    reader.accept(node, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
                    map.put(node.name, new ClassWrapper(node, true, bytes));
                }

                synchronized (classPath) {
                    classPath.putAll(map);
                }

                return true;
            };

            Scheduler scheduler = new Scheduler(runnable);

            scheduler.run(threadCount);
            scheduler.waitFor();

        }

        libraryClassNodes.addAll(classPath.values());
    }

    public Map<String, ClassWrapper> getClassPath() {
        return classPath;
    }

    public boolean isLibrary(ClassNode classNode) {
        return libraryClassNodes.stream().anyMatch(e -> e.classNode.name.equals(classNode.name));
    }

    public boolean isLoadedCode(ClassNode classNode) {
        return classes.containsKey(classNode.name);
    }

    private void addProcessors() {
        processors.add(new StaticInitializionTransformer(this));

        processors.add(new HWIDProtection(this));
        processors.add(new Optimizer());
        processors.add(new InlineTransformer(this));
        processors.add(new InvokeDynamic());

        processors.add(new StringEncryptionTransformer(this));
        processors.add(new NumberObfuscationTransformer(this));
        processors.add(new FlowObfuscator(this));
        processors.add(new HideMembers(this));
        processors.add(new LineNumberRemover(this));
        processors.add(new ShuffleMembersTransformer(this));


        nameObfuscationProcessors.add(new NameObfuscation());
        nameObfuscationProcessors.add(new InnerClassRemover());
        processors.add(new CrasherTransformer(this));
        processors.add(new ReferenceProxy(this));

        preProcessors = new ArrayList<>();

        for (IClassTransformer processor : processors) {
            ValueManager.registerClass(processor);
        }
        for (IPreClassTransformer processor : preProcessors) {
            ValueManager.registerClass(processor);
        }
        for (INameObfuscationProcessor processor : nameObfuscationProcessors) {
            ValueManager.registerClass(processor);
        }
    }

    public void setScript(JObfScript script) {
        this.script = script;
    }

    public void processJar(Configuration config) throws IOException {
        ZipInputStream inJar = null;
        ZipOutputStream outJar = null;

        boolean stored = settings.getUseStore().getObject();

        libraryFiles = new ArrayList<>();

        classes = new HashMap<>();
        libraryClassNodes = new HashSet<>();
        classPath = new HashMap<>();
        files = new HashMap<>();
        hierarchy = new HashMap<>();

        NameUtils.applySettings(settings);
        NameUtils.setup();

        try {
            script = StringUtils.isBlank(config.getScript()) ? null : new JObfScript(config.getScript());
        } catch (Exception e) {
            log.error("Failed to load script", e);
            e.printStackTrace();
            return;
        }

        for (String s : config.getLibraries())
            libraryFiles.add(new File(s));

        long startTime = System.currentTimeMillis();

        try {
            log.info("Loading classpath...");
            loadClasspath();
            try {
                inJar = new ZipInputStream(new BufferedInputStream(new FileInputStream(config.getInput())));
            } catch (FileNotFoundException e) {
                throw new FileNotFoundException("Could not open input file: " + e.getMessage());
            }

            try {
                OutputStream out = (config.getOutput() == null ? new ByteArrayOutputStream() : new FileOutputStream(config.getOutput()));
                outJar = new ZipOutputStream(new BufferedOutputStream(out));
                outJar.setMethod(stored ? ZipOutputStream.STORED : ZipOutputStream.DEFLATED);

                if (stored) {
                    outJar.setLevel(Deflater.NO_COMPRESSION);
                }
            } catch (FileNotFoundException e) {
                throw new FileNotFoundException("Could not open output file: " + e.getMessage());
            }
            setMainClass(null);

            log.info("... Finished after " + Utils.formatTime(System.currentTimeMillis() - startTime));

            startTime = System.currentTimeMillis();

            log.info("Reading input...");

            HashMap<String, byte[]> classDataMap = new HashMap<>();

            while (true) {
                ZipEntry entry = inJar.getNextEntry();

                if (entry == null) {
                    break;
                }

                if (entry.isDirectory()) {
                    outJar.putNextEntry(entry);
                    continue;
                }

                byte[] data = new byte[8192];
                ByteArrayOutputStream entryBuffer = new ByteArrayOutputStream();

                int len;
                do {
                    len = inJar.read(data);
                    if (len > 0) {
                        entryBuffer.write(data, 0, len);
                    }
                } while (len != -1);

                byte[] entryData = entryBuffer.toByteArray();

                String entryName = entry.getName();

                if (entryName.endsWith(".class")) {
                    try {
                        ClassReader cr = new ClassReader(entryData);
                        ClassNode cn = new ClassNode();

                        cr.accept(cn, 0);
                        classes.put(entryName, cn);
                        classDataMap.put(entryName, entryData);
                    } catch (Exception e) {
                        log.warn("Failed to read class " + entryName);
                        e.printStackTrace();
                        files.put(entryName, entryData);
                    }

                } else {
                    if (entryName.equals("META-INF/MANIFEST.MF")) {
                        setMainClass(Utils.getMainClass(new String(entryData, StandardCharsets.UTF_8)));
                    }

                    files.put(entryName, entryData);
                }
            }

            for (Map.Entry<String, ClassNode> stringClassNodeEntry : classes.entrySet()) {
                classPath.put(stringClassNodeEntry.getKey().replace(".class", ""), new ClassWrapper(stringClassNodeEntry.getValue(), false, classDataMap.get(stringClassNodeEntry.getKey())));
            }
            for (ClassNode value : classes.values()) {
                libraryClassNodes.add(new ClassWrapper(value, false, null));
            }

            //            if (nameobf) {
            for (INameObfuscationProcessor nameObfuscationProcessor : nameObfuscationProcessors) {
                nameObfuscationProcessor.transformPost(this, classes);
            }
            for (IPreClassTransformer preProcessor : preProcessors) {
                preProcessor.process(classes.values());
            }
            //            }

            AtomicInteger processed = new AtomicInteger();

            if (Packager.INSTANCE.isEnabled()) {
                Packager.INSTANCE.init();
            }

            log.info("... Finished after " + Utils.formatTime(System.currentTimeMillis() - startTime));

            startTime = System.currentTimeMillis();

            log.info("Transforming with " + threadCount + " threads...");

            final LinkedList<Map.Entry<String, ClassNode>> classQueue = new LinkedList<>(classes.entrySet());

            HashMap<String, byte[]> toWrite = new HashMap<>();

            List<Thread> threads = new ArrayList<>();

            if (threadCount > 1) {
                for (int i = 0; i < threadCount; i++) {
                    //                ZipOutputStream finalOutJar = outJar;
                    Thread t = new Thread(() -> {
                        try {
                            doObfuscate(classQueue, toWrite, processed);
                        } finally {
                            synchronized (threads) {
                                threads.remove(Thread.currentThread());
                            }
                        }
                    });

                    t.setName("Thread-" + i);
                    t.setContextClassLoader(ObfuscatorClassLoader.INSTANCE);

                    t.start();

                    synchronized (threads) {
                        threads.add(t);
                    }
                }


                while (true) {
                    synchronized (threads) {
                        if (threads.isEmpty()) {
                            break;
                        }
                        threads.stream().filter(thread -> thread == null || !thread.isAlive()).collect(Collectors.toList()).forEach(threads::remove);
                    }

                    Thread.sleep(100);
                }
            } else {
                doObfuscate(classQueue, toWrite, processed);
            }
            log.info("... Finished after " + Utils.formatTime(System.currentTimeMillis() - startTime));

            startTime = System.currentTimeMillis();

            log.info("Writing classes...");

            for (Map.Entry<String, byte[]> stringEntry : toWrite.entrySet()) {
                writeEntry(outJar, stringEntry.getKey(), stringEntry.getValue(), stored);
            }

            log.info("... Finished after " + Utils.formatTime(System.currentTimeMillis() - startTime));

            startTime = System.currentTimeMillis();

            log.info("Writing resources...");

            for (Map.Entry<String, byte[]> stringEntry : files.entrySet()) {
                String entryName = stringEntry.getKey();
                byte[] entryData = stringEntry.getValue();

                if (entryName.equals("META-INF/MANIFEST.MF")) {
                    // 处理 MANIFEST.MF 文件
                    if (Packager.INSTANCE.isEnabled()) {
                        // 如果启用了打包器，将 MANIFEST.MF 中的主类替换为打包器的解密类名
                        entryData = Utils.replaceMainClass(new String(entryData, StandardCharsets.UTF_8), Packager.INSTANCE.getDecryptionClassName()).getBytes(StandardCharsets.UTF_8);
                    } else if (mainClassChanged) {
                        // 如果主类被更改，更新 MANIFEST.MF 中的主类名
                        entryData = Utils.replaceMainClass(new String(entryData, StandardCharsets.UTF_8), mainClass).getBytes(StandardCharsets.UTF_8);
                        log.info("Replaced Main-Class with " + mainClass);
                    }

                    // 记录 MANIFEST.MF 文件处理完成
                    log.info("Processed MANIFEST.MF");
                }
                log.info("Copying " + entryName);


                writeEntry(outJar, entryName, entryData, stored);
            }

            log.info("... Finished after " + Utils.formatTime(System.currentTimeMillis() - startTime));

            startTime = System.currentTimeMillis();

            if (Packager.INSTANCE.isEnabled()) {
                log.info("Packaging...");
                writeEntry(outJar, Packager.INSTANCE.getDecryptionClassName() + ".class", Packager.INSTANCE.generateEncryptionClass(), stored);
                outJar.closeEntry();
                log.info("... Finished after " + Utils.formatTime(System.currentTimeMillis() - startTime));
            }
        } catch (InterruptedException ignored) {
        } finally {
            classPath.clear();
            classes.clear();
            libraryFiles.clear();
            libraryClassNodes.clear();
            files.clear();
            hierarchy.clear();

            NameUtils.cleanUp();

            System.gc();

            if (outJar != null) {
                try {
                    log.info("Finishing...");
                    outJar.flush();
                    outJar.close();
                    log.info(">>> Processing completed. If you found a bug / if the output is invalid please open an issue at https://github.com/superblaubeere27/obfuscator/issues");
                } catch (Exception e) {
                    // ignore
                }
            }

            if (inJar != null) {
                try {
                    inJar.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
    }

    private void doObfuscate(LinkedList<Map.Entry<String, ClassNode>> classQueue, HashMap<String, byte[]> toWrite, AtomicInteger processed) {
        while (true) {
            Map.Entry<String, ClassNode> stringClassNodeEntry;

            synchronized (classQueue) {
                stringClassNodeEntry = classQueue.poll();
            }

            if (stringClassNodeEntry == null)
                break;

            ProcessorCallback callback = new ProcessorCallback();

            String entryName = stringClassNodeEntry.getKey();
            byte[] entryData;
            ClassNode cn = stringClassNodeEntry.getValue();

            try {
                try {

                    computeMode = ModifiedClassWriter.COMPUTE_MAXS;


                    if (script == null || script.isObfuscatorEnabled(cn)) {
                        log.info(String.format("[%s] (%s/%s), Processing %s", Thread.currentThread().getName(), processed, classes.size(), entryName));

                        for (IClassTransformer proc : processors) {
                            try {
                                proc.process(callback, cn);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    } else {
                        log.info(String.format("[%s] (%s/%s), Skipping %s", Thread.currentThread().getName(), processed, classes.size(), entryName));
                    }

                    if (callback.isForceComputeFrames())
                        cn.methods.forEach(method -> Arrays.stream(method.instructions.toArray())
                                                           .filter(abstractInsnNode -> abstractInsnNode instanceof FrameNode)
                                                           .forEach(abstractInsnNode -> method.instructions.remove(abstractInsnNode)));


                    int mode = computeMode
                            | (callback.isForceComputeFrames() ? ModifiedClassWriter.COMPUTE_FRAMES : 0);

                    log.info(String.format("[%s] (%s/%s), Writing (computeMode = %s) %s", Thread.currentThread().getName(), processed, classes.size(), mode, entryName));

                    ModifiedClassWriter writer = new ModifiedClassWriter(
                            mode
                            //                                            ModifiedClassWriter.COMPUTE_MAXS |
                            //                                            ModifiedClassWriter.COMPUTE_FRAMES
                    );
                    cn.accept(writer);

                    entryData = writer.toByteArray();
                } catch (Throwable e) {
                    System.err.println("Error while writing " + entryName);
                    e.printStackTrace();
                    //                                    if (e instanceof) {
                    //
                    //                                    }
                    ModifiedClassWriter writer = new ModifiedClassWriter(ModifiedClassWriter.COMPUTE_MAXS
                            //                            | ModifiedClassWriter.COMPUTE_FRAMES
                    );
                    cn.accept(writer);


                    entryData = writer.toByteArray();
                }
                try {
                    if (Packager.INSTANCE.isEnabled()) {
                        entryName = Packager.INSTANCE.encryptName(entryName.replace(".class", ""));
                        entryData = Packager.INSTANCE.encryptClass(entryData);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }


                //                                synchronized (finalOutJar) {
                //                                    ZipEntry newEntry = new ZipEntry(entryName);
                //                                    finalOutJar.putNextEntry(newEntry);
                //                                    finalOutJar.write(entryData);
                //                                }

                synchronized (toWrite) {
                    toWrite.put(entryName, entryData);
                }
                //                    JObfImpl.log.log(Level.FINE, String.format("Processed %s (+%.2f KB)", entryName, (entryData.length - entryBuffer.size()) / 1024.0));
            } catch (Exception e) {
                e.printStackTrace();
            }
            //                JObfImpl.log.log(Level.FINE, "Processed " + entryBuffer.size() + " -> " + entryData.length);
            processed.getAndIncrement();
        }
    }

    public void writeEntry(ZipOutputStream outJar, String name, byte[] value, boolean stored) throws IOException {
        ZipEntry newEntry = new ZipEntry(name);


        if (stored) {
            CRC32 crc = new CRC32();
            crc.update(value);

            newEntry.setSize(value.length);
            newEntry.setCrc(crc.getValue());
        }


        outJar.putNextEntry(newEntry);
        outJar.write(value);
    }

    public void setWorkDone() {
        boolean workDone = true;
    }


    public void setThreadCount(int threadCount) {
        this.threadCount = threadCount;
    }

}