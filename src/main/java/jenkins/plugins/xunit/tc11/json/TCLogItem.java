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
import java.util.List;
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
  private long testRunTimeInMilliSec_;
  private String testRunTime_;
  private String type_;
  private String caption_;
  private String info_;
  private long runTime_;
  private long endTimeInMilliSec_;
  private String endTime_;
  private long startTimeInMilliSec_;
  private String startTime_;

  public TCLogItem(JSONObject parent, JSONObject obj, File inputTempDir) {
    this.info_ = "";
    this.caption_ = "";
    this.type_ = "";
    this.testRunTime_ = "";
    this.testRunTimeInMilliSec_ = 0;
    this.message_ = "";
    this.status_ = 0;
    this.name_ = "";
    this.id_ = "";

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
          filename = filename.substring(filename.lastIndexOf("/") + 1).toLowerCase();

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
                if (obj2.has("Time")) {
                  JSONObject time = obj2.getJSONObject("Time");
                  this.testRunTimeInMilliSec_ = time.getLong("msec");
                  this.testRunTime_ = time.getString("text");
                } else {
                  this.testRunTimeInMilliSec_ = 0;
                  this.testRunTime_ = "";
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
          filename = filename.substring(filename.lastIndexOf("/") + 1).toLowerCase();

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
      this.startTime_ = this.testRunTime_;
      this.startTimeInMilliSec_ = MyUtils.convertTcDateTime2MillSec(this.testRunTime_);
      this.runTime_ = this.testRunTimeInMilliSec_;

    }

  }

  /**
   *
   * @return
   */
  public String getName() {
    return this.name_;
  }

  /**
   *
   * @return
   */
  public String getCaption() {
    return this.caption_;
  }

  /**
   *
   * @return
   */
  public long getTestRunTimeInMilliSec() {
    return this.testRunTimeInMilliSec_;
  }

  /**
   *
   * @return
   */
  public long getStartTimeInMilliSec() {
    return this.startTimeInMilliSec_;
  }

  /**
   *
   * @return
   */
  public long getEndTimeInMilliSec() {
    return this.endTimeInMilliSec_;
  }

  /**
   *
   * @return
   */
  public String getTestRunTime() {
    return this.testRunTime_;
  }

  /**
   *
   * @return
   */
  public String getType() {
    return this.type_;
  }

  /**
   *
   * @return
   */
  public String getMessage() {
    return this.message_;
  }

  /**
   *
   * @return
   */
  public int getState() {
    return this.status_;
  }

  /**
   *
   * @return
   */
  public String getTimeStamp() {
    return this.startTime_;
  }

  /**
   * The run time in msec
   *
   * @return
   */
  long getRunTime() {
    return this.runTime_;
  }

  /**
   *
   * @return
   */
  public String getInfo() {
    return info_;
  }
}
