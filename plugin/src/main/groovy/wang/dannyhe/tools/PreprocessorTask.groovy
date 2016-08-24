package wang.dannyhe.tools

import antenna.preprocessor.v2.PreprocessorBridge
import groovy.io.FileType
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.apache.tools.ant.util.FileUtils
import de.pleumann.antenna.misc.Strings
import antenna.preprocessor.PreprocessorException

public class PreprocessorTask extends DefaultTask {

    File sourceDir
    File targetDir
    String symbols
    String encoding = "UTF-8"
    boolean verbose = true

    @TaskAction
    def process() {
        if (null == sourceDir || null == targetDir || null == symbols) {
            return;
        }

        if (!targetDir?.exists()) {
            if (!targetDir.mkdirs()) {
                throw PreprocessorException("destination create failed!", destination);
            }
        }

        PreprocessorBridge pp = new PreprocessorBridge(null);
        if (symbols != null && !symbols.isEmpty()) {
            pp.addSymbols(symbols)
        }
        if (verbose) {
            println("symbols:" + symbols)
        }

        final def fileUtil = FileUtils.getFileUtils();
        sourceDir.eachFileRecurse(FileType.FILES) { it ->
            String fileName = fileUtil.removeLeadingPath(sourceDir, it);
            String targetFilePath = targetDir.getCanonicalPath() + File.separator + fileName;
            def targetFile = new File(targetFilePath);
            Strings lines = loadFile(encoding, it);
            pp.setFile(it);

            targetFile.getParentFile().mkdirs();
            boolean modified = pp.preprocess(lines, encoding);
            lines.saveToFile(targetFilePath, encoding);
            if (verbose) {
                if (modified) {
                    println(it.getName() + " ... modified")
                } else {
                    println(it.getName() + " ... not modified")
                }
            }
        }
    }

    //copy from WtkPreprocess.java
    Strings loadFile(String encoding, File sourceFile) throws PreprocessorException {
        Strings lines = new Strings();
        try {
            if (encoding != null && encoding.length() > 0)
                lines.loadFromFile(sourceFile, encoding);
            else
                lines.loadFromFile(sourceFile);
        }
        catch (java.io.UnsupportedEncodingException e) {
            throw new PreprocessorException("Unknown encoding \"" + encoding + "\"", sourceFile, e);
        }
        catch (java.io.IOException e) {
            throw new PreprocessorException("File read error", sourceFile, e);
        }
        return lines;
    }
}