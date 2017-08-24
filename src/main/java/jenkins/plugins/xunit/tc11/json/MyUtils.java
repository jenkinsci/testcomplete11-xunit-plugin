package jenkins.plugins.xunit.tc11.json;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import jenkins.plugins.xunit.tc11.TestCompleteInputMetric;

public class MyUtils {

  /**
   *
   * @param jsonFile
   * @param encoding
   * @return
   * @throws FileNotFoundException
   * @throws IOException
   */
  public static JSONObject parseJSONFile(File jsonFile, String encoding) throws FileNotFoundException, IOException {
    InputStreamReader fi = new InputStreamReader(new FileInputStream(jsonFile), encoding);
    String content = IOUtils.toString(fi);
    return new JSONObject(content);
  }

  /**
   *
   * @param jsonFile
   * @param encoding
   * @return
   * @throws FileNotFoundException
   * @throws IOException
   */
  public static String readJSONFile(File jsonFile, String encoding) throws FileNotFoundException, IOException {
    InputStreamReader fi = new InputStreamReader(new FileInputStream(jsonFile), encoding);
    String content = IOUtils.toString(fi);
    return content;
  }

  /**
   *
   * @param inputDateTime
   * @return
   */
  public static String convertTc2DateTime(String inputDateTime) {
    SimpleDateFormat formatter = null;
    String dateTime = "";
    if (inputDateTime != null && !inputDateTime.isEmpty()) {
      if (inputDateTime.matches("[0-9]+/[0-9]+/[0-9]+ [0-9]+:[0-9]+:[0-9]+ PM|AM$")) {
        formatter = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss aa");
      } else if (inputDateTime.matches("[0-9]+/[0-9]+/[0-9]+ [0-9]+:[0-9]+:[0-9]+$")) {
        formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
      } else if (inputDateTime.matches("[0-9]+\\.[0-9]+\\.[0-9]+ [0-9]+:[0-9]+:[0-9]+$")) {
        formatter = new SimpleDateFormat("dd.MM.yyyy hh:mm:ss");
      }
      if (formatter != null) {
        try {
          SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
          Date date = formatter.parse(inputDateTime);
          if (date != null) {
            dateTime = df.format(date);
          }
        } catch (ParseException ex) {
          Logger.getLogger(TestCompleteInputMetric.class.getName()).log(Level.SEVERE, "[TC11 - xUnit] - {0}", ex);
        }
      }
    }
    return dateTime;

  }

  /**
   *
   * @param inputDateTime
   * @return
   */
  public static long convertTcDateTime2MillSec(String inputDateTime) {
    SimpleDateFormat formatter = null;
    long dateTime = 0;
    if (inputDateTime != null && !inputDateTime.isEmpty()) {
      if (inputDateTime.matches("[0-9]+/[0-9]+/[0-9]+ [0-9]+:[0-9]+:[0-9]+ PM|AM$")) {
        formatter = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss aa");
      } else if (inputDateTime.matches("[0-9]+/[0-9]+/[0-9]+ [0-9]+:[0-9]+:[0-9]+$")) {
        formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
      } else if (inputDateTime.matches("[0-9]+\\.[0-9]+\\.[0-9]+ [0-9]+:[0-9]+:[0-9]+$")) {
        formatter = new SimpleDateFormat("dd.MM.yyyy hh:mm:ss");
      }
      if (formatter != null) {
        try {
          Date date = formatter.parse(inputDateTime);
          if (date != null) {
            dateTime = date.getTime();
          }
        } catch (ParseException ex) {
          Logger.getLogger(TestCompleteInputMetric.class.getName()).log(Level.SEVERE, "[TC11 - xUnit] - {0}", ex);
        }
      }
    }
    return dateTime;

  }

  /**
   *
   * @param msec
   * @return
   */
  public static String convertTc2DateTime(long msec) {
    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
    Date date = new Date(msec);
    return df.format(date);
  }
}
