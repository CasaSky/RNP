package LogFile;

import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by mint on 03.11.15.
 */
public final class LogFileUtils {
    private File logger;
    private BufferedWriter logwrite;

    private LogFileUtils() throws IOException{
        logger = new File("log.txt");
        logger.createNewFile();
        FileWriter fileWriter = new FileWriter(logger);
        logwrite = new BufferedWriter(fileWriter);
    }

    public static LogFileUtils createLogFile() throws IOException{
        return new LogFileUtils();
    }

    public void protokolliere(String text) throws IOException{
        logwrite.write(text);
    }
}
