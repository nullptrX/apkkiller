package io.github.nullptrx;

import at.favre.tools.apksigner.SignTool;
import at.favre.tools.apksigner.signing.SigningConfig;
import at.favre.tools.apksigner.signing.SigningConfigGen;
import at.favre.tools.apksigner.util.FileUtil;
import bin.signer.ApkSigner;
import bin.signer.key.KeystoreKey;
import bin.util.StreamUtil;
import bin.xml.decode.AXmlDecoder;
import bin.xml.decode.AXmlResourceParser;
import bin.xml.decode.XmlPullParser;
import bin.zip.ZipEntry;
import bin.zip.ZipFile;
import bin.zip.ZipOutputStream;
import io.github.nullptrx.editor.*;
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
    private static final String targetApplicationName = "io.github.nullptrx.App";
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
            if (commandLine.hasOption("o")) {
                String outputPath = commandLine.getOptionValue("i");
                properties.setProperty("apk.out", outputPath);
            }
            String inputPath = commandLine.getOptionValue("i");
            properties.setProperty("apk.src", inputPath);
            properties.setProperty("apk.signed", inputPath);
        }

        process(properties);
    }

    private static void loadDefaultProperties(Properties properties) throws IOException {
        ClassLoader classLoader = ApkKiller.class.getClassLoader();
        try (InputStream fis = classLoader.getResourceAsStream(confName)) {
            properties.load(fis);
        } catch (Exception e) {
            try (InputStream fis = classLoader.getResourceAsStream(confName)) {
                properties.load(fis);
            }
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static void process(Properties properties) throws Exception {

        File srcApk = new File(properties.getProperty("apk.src"));
        File signApk = new File(properties.getProperty("apk.signed"));
        String outputPath = properties.getProperty("apk.out");
        if (outputPath == null || outputPath.isEmpty()) {
            outputPath = FileUtil.getFileNameWithoutExtension(srcApk) + "-modified." + FileUtil.getFileExtension(srcApk);
        }
        File outApk = new File(outputPath);
        boolean signEnable = properties.getProperty("sign.enable").equalsIgnoreCase("true");
        // String signFilePath = properties.getProperty("sign.file");
        // String signPassword = properties.getProperty("sign.password");
        // String signAlias = properties.getProperty("sign.alias");
        // String signAliasPassword = properties.getProperty("sign.aliasPassword");

        if (!srcApk.exists()) {
            System.out.println("??????????????????????????????");
            return;
        }
        if (signEnable && !signApk.exists()) {
            System.out.println("?????????????????????????????????");
            return;
        }


        System.out.println("?????????????????????" + signApk.getPath());
        signatures = getApkSignatureData(signApk);
        byte[] manifestData;
        byte[] dexData;

        System.out.println("\n????????????APK???" + srcApk.getPath());

        try (ZipFile zipFile = new ZipFile(srcApk)) {
            System.out.println("  --????????????AndroidManifest.xml");
            ZipEntry manifestEntry = zipFile.getEntry("AndroidManifest.xml");
            manifestData = parseManifest(zipFile.getInputStream(manifestEntry));
            // manifestData = processManifest(zipFile.getInputStream(manifestEntry));

            ZipEntry dexEntry = zipFile.getEntry("classes.dex");
            DexBackedDexFile dex = DexBackedDexFile.fromInputStream(Opcodes.getDefault(),
                    new BufferedInputStream(zipFile.getInputStream(dexEntry)));

            System.out.println("  --????????????classes.dex");
            dexData = processDex(dex);

            System.out.println("\n????????????APK???" + outApk.getPath());
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
                System.out.println("\n????????????APK???" + outApk.getPath());
                SignTool.mainExecute(new String[]{
                        "-a", outApk.getPath(),
                        "--allowResign",
                        "--overwrite",
                });

                // File temp = new File(outApk.getPath() + ".tmp");
                // KeystoreKey keystoreKey = new KeystoreKey(signFile, signPassword, signAlias, signAliasPassword);
                // File temp = new File(outApk.getPath() + ".tmp");
                // ApkSigner.signApk(outApk, temp, keystoreKey, null);
                // outApk.delete();
                // temp.renameTo(outApk);
            }

            System.out.println("\n????????????");
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

    private static byte[] parseManifest(InputStream is) throws IOException {
        AXmlDecoder axml = AXmlDecoder.decode(is);
        AXmlResourceParser parser = new AXmlResourceParser();
        parser.open(new ByteArrayInputStream(axml.getData()), axml.mTableStrings);
        boolean success = false;
        int type;
        while ((type = parser.next()) != XmlPullParser.END_DOCUMENT) {
            if (type != XmlPullParser.START_TAG)
                continue;
            if (parser.getName().equals("manifest")) {
                int size = parser.getAttributeCount();
                for (int i = 0; i < size; ++i) {
                    if (parser.getAttributeName(i).equals("package")) {
                        packageName = parser.getAttributeValue(i);
                    }
                }
            } else if (parser.getName().equals("application")) {
                int size = parser.getAttributeCount();
                for (int i = 0; i < size; ++i) {
                    int resource = parser.getAttributeNameResource(i);
                    if (resource == 0x01010003) {
                        customApplication = true;
                        customApplicationName = parser.getAttributeValue(i);
                        int index = axml.mTableStrings.getSize();
                        byte[] data = axml.getData();
                        int off = parser.currentAttributeStart + 20 * i;
                        off += 8;
                        writeInt(data, off, index);
                        off += 8;
                        writeInt(data, off, index);
                    }
                }

                if (!customApplication) {
                    int off = parser.currentAttributeStart;
                    byte[] data = axml.getData();
                    byte[] newData = new byte[data.length + 20];
                    System.arraycopy(data, 0, newData, 0, off);
                    System.arraycopy(data, off, newData, off + 20, data.length - off);

                    // chunkSize
                    int chunkSize = readInt(newData, off - 32);
                    writeInt(newData, off - 32, chunkSize + 20);
                    // attributeCount
                    writeInt(newData, off - 8, size + 1);

                    int idIndex = parser.findResourceID(0x01010003);
                    if (idIndex == -1)
                        throw new IOException("idIndex == -1");

                    boolean isMax = true;
                    for (int i = 0; i < size; ++i) {
                        int id = parser.getAttributeNameResource(i);
                        if (id > 0x01010003) {
                            isMax = false;
                            if (i != 0) {
                                System.arraycopy(newData, off + 20, newData, off, 20 * i);
                                off += 20 * i;
                            }
                            break;
                        }
                    }
                    if (isMax) {
                        System.arraycopy(newData, off + 20, newData, off, 20 * size);
                        off += 20 * size;
                    }

                    writeInt(newData, off, axml.mTableStrings.find("http://schemas.android.com/apk/res/android"));
                    writeInt(newData, off + 4, idIndex);
                    writeInt(newData, off + 8, axml.mTableStrings.getSize());
                    writeInt(newData, off + 12, 0x03000008);
                    writeInt(newData, off + 16, axml.mTableStrings.getSize());
                    axml.setData(newData);
                }
                success = true;
                break;
            }
        }
        if (!success)
            throw new IOException();
        ArrayList<String> list = new ArrayList<>(axml.mTableStrings.getSize());
        axml.mTableStrings.getStrings(list);
        list.add(targetApplicationName);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        axml.write(list, baos);
        // return baos.toByteArray();
        ParserChunkUtils.xmlStruct.byteSrc = baos.toByteArray();

        if (parser.findResourceID(0x0101000f) != -1) {
            XmlEditor.modifyAttr("application", "", "debuggable", "true");
        } else {
            XmlEditor.addAttr("application", "", "debuggable", "true");
        }
        return ParserChunkUtils.xmlStruct.byteSrc;
    }

    private static void writeInt(byte[] data, int off, int value) {
        data[off++] = (byte) (value & 0xFF);
        data[off++] = (byte) ((value >>> 8) & 0xFF);
        data[off++] = (byte) ((value >>> 16) & 0xFF);
        data[off] = (byte) ((value >>> 24) & 0xFF);
    }

    private static int readInt(byte[] data, int off) {
        return data[off + 3] << 24 | (data[off + 2] & 0xFF) << 16 | (data[off + 1] & 0xFF) << 8
                | data[off] & 0xFF;
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
