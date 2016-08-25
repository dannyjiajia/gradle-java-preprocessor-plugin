package wang.dannyhe.tools

import groovy.io.FileType
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.apache.tools.ant.util.FileUtils
import de.pleumann.antenna.misc.Strings
import antenna.preprocessor.PreprocessorException
import antenna.preprocessor.IPreprocessor

public class PreprocessorTask extends DefaultTask {

    File sourceDir
    File targetDir
    String symbols
    String encoding = "UTF-8"
    boolean verbose = true

    @TaskAction
    def process() {
        if (null == sourceDir || null == targetDir || null == symbols) {
            return
        }

        if (!targetDir?.exists()) {
            if (!targetDir.mkdirs()) {
                throw PreprocessorException("targetDir create failed!", targetDir)
            }
        }

        IPreprocessor pp
        if(true) { //should we add the version 3?
            pp = new antenna.preprocessor.v2.PreprocessorBridge(null)
        }

        if (symbols != null && !symbols.isEmpty()) {
            pp.addSymbols(symbols)
        }
        log("symbols:" + symbols)
        processTask(pp)
    }

    void processTask(IPreprocessor pp){
        final def fileUtil = FileUtils.getFileUtils()
        //we want to filter the java source files
        sourceDir.traverse(type:FileType.FILES,nameFilter:~/.*\.java/) { File currentFile -> 
            String subFilePath = fileUtil.removeLeadingPath(sourceDir, currentFile)
            String targetFilePath = targetDir.getCanonicalPath() + File.separator + subFilePath
            File targetFile = new File(targetFilePath)
            Strings lines = loadFile(encoding, currentFile)
            pp.setFile(currentFile)
            //mkdir for target parent
            targetFile.getParentFile().mkdirs()
            boolean modified = pp.preprocess(lines, encoding)
            lines.saveToFile(targetFilePath, encoding)
            if (modified) {
                log(currentFile.getName() + " ... modified")
            } else {
                log(currentFile.getName() + " ... not modified")
            }
        }
    }

    void log(String msg) {
        if (verbose) {
            println(msg)
        }
    }

    //copy from WtkPreprocess.java
    Strings loadFile(String encoding, File sourceFile) throws PreprocessorException {
        Strings lines = new Strings()
        try {
            if (encoding != null && encoding.length() > 0)
                lines.loadFromFile(sourceFile, encoding)
            else
                lines.loadFromFile(sourceFile)
        }
        catch (java.io.UnsupportedEncodingException e) {
            throw new PreprocessorException("Unknown encoding \"" + encoding + "\"", sourceFile, e)
        }
        catch (java.io.IOException e) {
            throw new PreprocessorException("File read error", sourceFile, e)
        }
        return lines
    }
}