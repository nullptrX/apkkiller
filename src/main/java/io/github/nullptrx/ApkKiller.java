package io.github.nullptrx;

import bin.signer.ApkSigner;
import bin.signer.key.KeystoreKey;
import bin.util.StreamUtil;
import bin.xml.decode.AXmlDecoder;
import bin.xml.decode.AXmlResourceParser;
import bin.zip.ZipEntry;
import bin.zip.ZipFile;
import bin.zip.ZipOutputStream;
import io.github.nullptrx.editor.ParserChunkUtils;
import io.github.nullptrx.editor.XmlEditor;
import org.apache.commons.cli.*;
import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.dexbacked.DexBackedClassDef;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.writer.builder.DexBuilder;
import org.jf.dexlib2.writer.io.MemoryDataStore;
import org.jf.smali.Smali;
import org.jf.smali.SmaliOptions;
import sun.security.pkcs.PKCS7;

import java.io.*;
import java.security.cert.Certificate;
import java.util.*;

public class ApkKiller {
    private static final String targetApplicationName = "io.github.nullptrx.AppKiller";
    private static final String confName = "config.properties";
    private static boolean customApplication = false;
    private static String customApplicationName;
    private static String packageName;
    private static byte[] signatures;

    public static void main(String[] args) throws Exception {
        int length = args.length;
        Properties properties = new Properties();
        if (length == 0) {
            File file = new File(confName);
            if (file.exists()) {
                try (FileInputStream fis = new FileInputStream(file)) {
                    properties.load(fis);
                }
            } else {
                loadDefaultProperties(properties);
            }
        } else {
            loadDefaultProperties(properties);
            CommandLineParser parser = new DefaultParser();
            Options options = new Options();
            options.addRequiredOption("i", "input", true, "input file");
            options.addOption("o", "output", true, "output file");
            CommandLine commandLine = parser.parse(options, args);
            String outputPath;
            if (commandLine.hasOption("o")) {
                outputPath = commandLine.getOptionValue("i");
            } else {
                outputPath = "out.apk";
            }
            String inputPath = commandLine.getOptionValue("i");
            properties.setProperty("apk.src", inputPath);
            properties.setProperty("apk.signed", inputPath);
            properties.setProperty("apk.out", outputPath);
        }

        process(properties);
    }

    private static void loadDefaultProperties(Properties properties) throws IOException {
        ClassLoader classLoader = ApkKiller.class.getClassLoader();
        try (InputStream fis = classLoader.getResourceAsStream("resources/" + confName)) {
            properties.load(fis);
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static void process(Properties properties) throws Exception {

        File srcApk = new File(properties.getProperty("apk.src"));
        File signApk = new File(properties.getProperty("apk.signed"));
        File outApk = new File(properties.getProperty("apk.out"));
        boolean signEnable = properties.getProperty("sign.enable").equalsIgnoreCase("true");
        String signFile = properties.getProperty("sign.file");
        String signPassword = properties.getProperty("sign.password");
        String signAlias = properties.getProperty("sign.alias");
        String signAliasPassword = properties.getProperty("sign.aliasPassword");

        if (!srcApk.exists()) {
            System.out.println("源文件未定义或不存在");
            return;
        }
        if (signEnable && !signApk.exists()) {
            System.out.println("签名文件未定义或不存在");
            return;
        }


        System.out.println("正在读取签名：" + signApk.getPath());
        signatures = getApkSignatureData(signApk);
        byte[] manifestData;
        byte[] dexData;

        System.out.println("\n正在读取APK：" + srcApk.getPath());

        try (ZipFile zipFile = new ZipFile(srcApk)) {
            System.out.println("  --正在处理AndroidManifest.xml");
            ZipEntry manifestEntry = zipFile.getEntry("AndroidManifest.xml");
            // manifestData = parseManifest(zipFile.getInputStream(manifestEntry));
            manifestData = processManifest(zipFile.getInputStream(manifestEntry));

            ZipEntry dexEntry = zipFile.getEntry("classes.dex");
            DexBackedDexFile dex = DexBackedDexFile.fromInputStream(Opcodes.getDefault(),
                    new BufferedInputStream(zipFile.getInputStream(dexEntry)));

            System.out.println("  --正在处理classes.dex");
            dexData = processDex(dex);

            System.out.println("\n正在写出APK：" + outApk.getPath());
            try (ZipOutputStream zos = new ZipOutputStream(outApk)) {
                zos.putNextEntry("AndroidManifest.xml");
                zos.write(manifestData);
                zos.closeEntry();

                zos.putNextEntry("classes.dex");
                zos.write(dexData);
                zos.closeEntry();

                Enumeration<ZipEntry> enumeration = zipFile.getEntries();
                while (enumeration.hasMoreElements()) {
                    ZipEntry ze = enumeration.nextElement();
                    if (ze.getName().equals("AndroidManifest.xml")
                            || ze.getName().equals("classes.dex")
                            || ze.getName().startsWith("META-INF/"))
                        continue;
                    zos.copyZipEntry(ze, zipFile);
                }
            }
            if (signEnable) {
                System.out.println("\n正在签名APK：" + outApk.getPath());
                KeystoreKey keystoreKey = new KeystoreKey(signFile, signPassword, signAlias, signAliasPassword);
                File temp = new File(outApk.getPath() + ".tmp");
                ApkSigner.signApk(outApk, temp, keystoreKey, null);
                outApk.delete();
                temp.renameTo(outApk);
            }

            System.out.println("\n处理完成");
        }
    }

    private static byte[] processDex(DexBackedDexFile dex) throws Exception {
        DexBuilder dexBuilder = new DexBuilder(Opcodes.getDefault());
        try (InputStream fis = ApkKiller.class.getResourceAsStream("App.smali")) {
            String src = new String(StreamUtil.readBytes(fis), "utf-8");
            if (customApplication) {
                if (customApplicationName.startsWith(".")) {
                    if (packageName == null)
                        throw new NullPointerException("Package name is null.");
                    customApplicationName = packageName + customApplicationName;
                }
                customApplicationName = "L" + customApplicationName.replace('.', '/') + ";";
                src = src.replace("Landroid/app/Application;", customApplicationName);
            }
            if (signatures == null)
                throw new NullPointerException("Signatures is null");
            src = src.replace("### Signatures Data ###", Base64.getEncoder().encodeToString(signatures));
            ClassDef classDef = Smali.assembleSmaliFile(src, dexBuilder, new SmaliOptions());
            if (classDef == null)
                throw new Exception("Parse smali failed");
            for (DexBackedClassDef dexBackedClassDef : dex.getClasses()) {
                dexBuilder.internClassDef(dexBackedClassDef);
            }
        }
        MemoryDataStore store = new MemoryDataStore();
        dexBuilder.writeTo(store);
        return Arrays.copyOf(store.getBufferData(), store.getSize());
    }

    private static byte[] processManifest(InputStream is) throws IOException {
        AXmlDecoder axml = AXmlDecoder.decode(is);
        AXmlResourceParser parser = new AXmlResourceParser();
        parser.open(new ByteArrayInputStream(axml.getData()), axml.mTableStrings);
        ArrayList<String> list = new ArrayList<>(axml.mTableStrings.getSize());
        axml.mTableStrings.getStrings(list);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        axml.write(list, baos);
        ParserChunkUtils.xmlStruct.byteSrc = baos.toByteArray();
        if (parser.findResourceID(0x01010003) != -1) {
            customApplication = true;
            XmlEditor.modifyAttr("application", "", "name", targetApplicationName);
        } else {
            XmlEditor.addAttr("application", "", "name", targetApplicationName);
        }

        if (parser.findResourceID(0x0101000f) != -1) {
            XmlEditor.modifyAttr("application", "", "debuggable", "true");
        } else {
            XmlEditor.addAttr("application", "", "debuggable", "true");
        }
        return ParserChunkUtils.xmlStruct.byteSrc;
    }

    private static byte[] getApkSignatureData(File apkFile) throws Exception {
        ZipFile zipFile = new ZipFile(apkFile);
        Enumeration<ZipEntry> entries = zipFile.getEntries();
        while (entries.hasMoreElements()) {
            ZipEntry ze = entries.nextElement();
            String name = ze.getName().toUpperCase();
            if (name.startsWith("META-INF/") && (name.endsWith(".RSA") || name.endsWith(".DSA"))) {
                PKCS7 pkcs7 = new PKCS7(StreamUtil.readBytes(zipFile.getInputStream(ze)));
                Certificate[] certs = pkcs7.getCertificates();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                DataOutputStream dos = new DataOutputStream(baos);
                dos.write(certs.length);
                for (int i = 0; i < certs.length; i++) {
                    byte[] data = certs[i].getEncoded();
                    System.out.printf("  --SignatureHash[%d]: %08x\n", i, Arrays.hashCode(data));
                    dos.writeInt(data.length);
                    dos.write(data);
                }
                return baos.toByteArray();
            }
        }
        throw new Exception("META-INF/XXX.RSA (DSA) file not found.");
    }

}
