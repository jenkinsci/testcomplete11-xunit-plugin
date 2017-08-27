/**
 * The MIT License
 * Copyright (c) 2017 Michael Gärtner and all contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package jenkins.plugins.xunit.tc11.json;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.jenkinsci.lib.dtkit.util.converter.ConversionException;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * This Class represents in MHT _root.js a JSON Object matching "TestItem"
 *
 * @author mgaert
 */
public class TCLogItem {

  private String name_;
  private int status_;
  private String id_;
  private String message_;
  private long testTimeInMilliSec_;
  private String testTime_;
  private String type_;
  private String caption_;
  private String info_;
  private long runTime_;
  private long endTimeInMilliSec_;
  private String endTime_;
  private long startTimeInMilliSec_;
  private String startTime_;
  private List<Map<String, String>> callStack_;

  public TCLogItem(JSONObject parent, JSONObject obj, File inputTempDir) {
    this.info_ = "";
    this.caption_ = "";
    this.type_ = "";
    this.testTime_ = "";
    this.testTimeInMilliSec_ = 0;
    this.message_ = "";
    this.status_ = 0;
    this.name_ = "";
    this.id_ = "";
    this.callStack_ = new ArrayList<>();

    if (obj.has("name")) {
      this.name_ = obj.getString("name");
    }
    if (obj.has("status")) {
      this.status_ = obj.getInt("status");
    }
    if (obj.has("id")) {
      this.id_ = obj.getString("id");
    }
    if (obj.has("providers")) {
      JSONArray jsArray = obj.getJSONArray("providers");
      for (int i = 0; i < jsArray.length(); i++) {
        JSONObject jsObject = jsArray.optJSONObject(i);
        if (jsObject.has("href")) {
          String filename = jsObject.getString("href");
          filename = filename.substring(filename.lastIndexOf("/") + 1).toLowerCase(Locale.ENGLISH);

          Collection<File> jsFiles = FileUtils.listFiles(inputTempDir, FileFilterUtils.nameFileFilter(filename), null);
          if (jsFiles.isEmpty()) {
            String message = "Invalid TestComplete MHT file. No entry with '" + filename + "' found.";
            throw new ConversionException(message);
          }
          File jsFile = jsFiles.iterator().next();
          try {
            String fileContent = MyUtils.readJSONFile(jsFile, "UTF-8");
            int start = fileContent.indexOf('(');
            int end = fileContent.length() - 1;
            fileContent = fileContent.substring(start + 1, end);

            String jsonRaw = fileContent.substring(fileContent.indexOf(',') + 1);
            JSONObject tmpObject = new JSONObject(jsonRaw);
            if (tmpObject.has("caption")) {
              this.caption_ = tmpObject.getString("caption");
            }
            if (tmpObject.has("items")) {
              JSONArray items = tmpObject.getJSONArray("items");

              JSONObject obj2 = items.optJSONObject(0);
              if (obj2 != null) {
                if (obj2.has("Message")) {
                  String str = obj2.getString("Message");
                  this.message_ = str;
                }
                if (obj2.has("AdditionalInfo")) {
                  JSONObject info = obj2.getJSONObject("AdditionalInfo");
                  if (!info.getBoolean("isfilename")) {
                    this.info_ = info.getString("text");
                  }
                }
                if (obj2.has("CallStack")) {
                  JSONObject callStack = obj2.getJSONObject("CallStack");
                  JSONArray callStackItems = callStack.optJSONArray("items");
                  for (int j = 0; j < callStackItems.length(); j++) {
                    JSONObject js = callStackItems.optJSONObject(j);
                    if (js != null) {
                      Map<String, String> cs = new HashMap<>();
                      if (js.has("UnitName")) {
                        cs.put("Unit", js.getString("UnitName"));
                      } else {
                        cs.put("Unit", "");
                      }
                      cs.put("Line", Integer.toString(js.getInt("LineNo")));
                      cs.put("Test", js.getString("Test"));
                      this.callStack_.add(cs);
                    }
                  }
                }
                if (obj2.has("Time")) {
                  JSONObject time = obj2.getJSONObject("Time");
                  this.testTimeInMilliSec_ = time.getLong("msec");
                  this.testTime_ = time.getString("text");
                } else {
                  this.testTimeInMilliSec_ = 0;
                  this.testTime_ = "";
                }
                if (obj2.has("TypeDescription")) {
                  String str = obj2.getString("TypeDescription");
                  this.type_ = str;
                } else {
                  this.type_ = "";
                }
              }
            }
          } catch (FileNotFoundException e) {
            throw new ConversionException("File '" + jsFile.getName() + "' not found.");
          } catch (IOException e) {
            throw new ConversionException("File '" + jsFile.getName() + "' can not be read.");
          }
        }
      }
    }
    /*
     * Fetch content of attached referenced document to parse out execution time of test
     */
    if (parent != null && parent.has("providers")) {
      JSONArray jsArray = parent.getJSONArray("providers");
      for (int i = 0; i < jsArray.length(); i++) {
        JSONObject jsObject = jsArray.optJSONObject(i);
        if (jsObject.has("href")) {
          String filename = jsObject.getString("href");
          filename = filename.substring(filename.lastIndexOf("/") + 1).toLowerCase(Locale.ENGLISH);

          Collection<File> jsFiles = FileUtils.listFiles(inputTempDir, FileFilterUtils.nameFileFilter(filename), null);
          if (jsFiles.isEmpty()) {
            String message = "Invalid TestComplete MHT file. No entry with '" + filename + "' found.";
            throw new ConversionException(message);
          }
          File jsFile = jsFiles.iterator().next();
          try {
            String fileContent = MyUtils.readJSONFile(jsFile, "UTF-8");
            int start = fileContent.indexOf('(');
            int end = fileContent.length() - 1;
            fileContent = fileContent.substring(start + 1, end);

            String jsonRaw = fileContent.substring(fileContent.indexOf(',') + 1);
            JSONObject tmpObject = new JSONObject(jsonRaw);
            if (tmpObject.has("items")) {
              JSONArray items = tmpObject.getJSONArray("items");
              for (int j = 0; j < items.length(); j++) {
                JSONObject js = items.optJSONObject(j);

                if (js.has("StartTime")) {
                  JSONObject startTime = js.getJSONObject("StartTime");
                  this.startTime_ = MyUtils.convertTc2DateTime(startTime.getString("text"));
                  this.startTimeInMilliSec_ = startTime.getLong("msec");
                }
                if (js.has("EndTime")) {
                  JSONObject endTime = js.getJSONObject("EndTime");
                  this.endTime_ = MyUtils.convertTc2DateTime(endTime.getString("text"));
                  this.endTimeInMilliSec_ = endTime.getLong("msec");
                }
                if (js.has("RunTime")) {
                  JSONObject runTime = js.getJSONObject("RunTime");
                  this.runTime_ = runTime.getLong("msec");
                }
              }
            }

          } catch (FileNotFoundException e) {
            throw new ConversionException("File '" + jsFile.getName() + "' not found.");
          } catch (IOException e) {
            throw new ConversionException("File '" + jsFile.getName() + "' can not be read.");
          }
        }
      }
    } else {
      this.startTime_ = this.testTime_;
      this.startTimeInMilliSec_ = MyUtils.convertTcDateTime2MillSec(this.testTime_);
      this.runTime_ = 0;

    }

  }

  /**
   * @return The name of the test log item
   */
  public String getName() {
    return this.name_;
  }

  /**
   * @return The id of the test log item
   */
  public String getId() {
    return this.id_;
  }

  /**
   *
   * @return The test cae name of the test log item
   */
  public String getCaption() {
    return this.caption_;
  }

  /**
   * The test run time of the test case
   *
   * @return Time in milli seconds
   */
  public long getTestRunTimeInMilliSec() {
    return this.testTimeInMilliSec_;
  }

  /**
   * The start time of the test suit
   *
   * @return Time in milli seconds
   */
  public long getStartTimeInMilliSec() {
    return this.startTimeInMilliSec_;
  }

  /**
   * The end time of the test suit
   *
   * @return Time in milli seconds
   */
  public long getEndTimeInMilliSec() {
    return this.endTimeInMilliSec_;
  }

  /**
   * The type of the test case message. It is one of 'Error', 'Warning' or 'Info'
   *
   * @return The Type of the test case message
   */
  public String getType() {
    return this.type_;
  }

  /**
   * The message of the test case. If the type is one of 'Error', 'Warning' or 'Info'.
   *
   * @return The test case message
   */
  public String getMessage() {
    return this.message_;
  }

  /**
   *
   * @return The State of the test case 2: Error, 1: Warning and 0:Info
   */
  public int getStatus() {
    return this.status_;
  }

  /**
   *
   * @return The date and time in msec of the test item
   */
  public long getTestTimeInMilliSec() {
    return testTimeInMilliSec_;
  }

  /**
   *
   * @return The date and time of the test item
   */
  public String getTestTime() {
    return testTime_;
  }

  /**
   *
   * @return The starting time of the test suit
   */
  public String getTimeStamp() {
    return this.startTime_;
  }

  /**
   *
   * @return The ending time of the test suit
   */
  public String getEndTime() {
    return this.endTime_;
  }

  /**
   *
   * @return The run time in msec
   */
  public long getRunTime() {
    return this.runTime_;
  }

  /**
   *
   * @return Additional info from the testcase in case of state error or an empty string
   */
  public String getInfo() {
    return info_;
  }

  /**
   *
   * @return CallStack from the testcase in case of the state error or an empty string
   */
  public List<Map<String, String>> getCallStack() {
    return this.callStack_;
  }
}
