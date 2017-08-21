package jenkins.plugins.xunit.tc11.json;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

public class JSONUtil {

  public static JSONObject parseJSONFile(File jsonFile, String encoding) throws FileNotFoundException, IOException {
    InputStreamReader fi = new InputStreamReader(new FileInputStream(jsonFile), encoding);
    String content = IOUtils.toString(fi);
    return new JSONObject(content);
  }

  public static String readJSONFile(File jsonFile, String encoding) throws FileNotFoundException, IOException {
    InputStreamReader fi = new InputStreamReader(new FileInputStream(jsonFile), encoding);
    String content = IOUtils.toString(fi);
    return content;
  }
}