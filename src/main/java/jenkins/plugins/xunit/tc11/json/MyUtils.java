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
   * Opens the given JSON file and returns the content as {@link org.json.JSONObject}
   * <br>
   * If the named file does not exist, is a directory rather than a regular file, or for some other reason cannot be opened for reading then a
   * <code>FileNotFoundException</code> is thrown.
   *
   * @param jsonFile JSON File
   * @param encoding The name of a supported {@link java.nio.charset.Charset charset}
   * @return The file Content as {@link org.json.JSONObject}
   * @throws FileNotFoundException if the file does not exist, is a directory rather than a regular file, or for some other reason cannot be opened for reading.
   * @throws IOException if an I/O error occurs
   */
  public static JSONObject parseJSONFile(File jsonFile, String encoding) throws FileNotFoundException, IOException {
    InputStreamReader fi = new InputStreamReader(new FileInputStream(jsonFile), encoding);
    String content = IOUtils.toString(fi);
    return new JSONObject(content);
  }

  /**
   * Opens the given JSON file and returns the content as string
   * <br>
   * If the named file does not exist, is a directory rather than a regular file, or for some other reason cannot be opened for reading then a
   * <code>FileNotFoundException</code> is thrown.
   *
   * @param jsonFile JSON File
   * @param encoding The name of a supported {@link java.nio.charset.Charset charset}
   * @return The file content as string
   * @throws FileNotFoundException if the file does not exist, is a directory rather than a regular file, or for some other reason cannot be opened for reading.
   * @throws IOException if an I/O error occurs
   */
  public static String readJSONFile(File jsonFile, String encoding) throws FileNotFoundException, IOException {
    InputStreamReader fi = new InputStreamReader(new FileInputStream(jsonFile), encoding);
    String content = IOUtils.toString(fi);
    return content;
  }

  /**
   * Converts the given date time string from the format 'MM/dd/yyyy hh:mm:ss aa', 'dd/MM/yyyy HH:mm:ss' or 'dd.MM.yyyy hh:mm:ss' to the format 'yyyy-MM-dd
   * hh:mm:ss.SSS'
   *
   * @param inputDateTime Time in the format 'MM/dd/yyyy hh:mm:ss aa', 'dd/MM/yyyy HH:mm:ss' or 'dd.MM.yyyy HH:mm:ss'
   * @return Date time string formated as 'yyyy-MM-dd hh:mm:ss.SSS'
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
        formatter = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
      }
      if (formatter != null) {
        try {
          SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
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
   * Converts the given date time String from the format 'MM/dd/yyyy hh:mm:ss aa', 'dd/MM/yyyy HH:mm:ss' or 'dd.MM.yyyy HH:mm:ss' to a time in milli seconds
   *
   * @param inputDateTime Time in the format 'MM/dd/yyyy hh:mm:ss aa', 'dd/MM/yyyy HH:mm:ss' or 'dd.MM.yyyy HH:mm:ss'
   * @return Time in milli seconds
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
        formatter = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
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
   * Converts time given in milliseconds to a Date string formated as 'yyyy-MM-dd hh:mm:ss.SSS'
   *
   * @param msec Time in milli seconds
   * @return Date string formated as 'yyyy-MM-dd hh:mm:ss.SSS'
   */
  public static String convertTc2DateTime(long msec) {
    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
    Date date = new Date(msec);
    return df.format(date);
  }
}
