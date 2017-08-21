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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import org.json.JSONObject;

/**
 *
 * @author mgaert
 */
public class TCLogProviderItem {

  private String name_;
  private String id_;
  private String startTime_;
  private String endTime_;
  private String runTime_;
  private String testItemId_;

  /*
      "StartTime": {
        "msec": 1484066920290,
        "text": "10.01.2017 16:48:40"
      },
      "EndTime": {
        "msec": 1484066920381,
        "text": "10.01.2017 16:48:40"
      },
      "RunTime": {
        "msec": 91,
        "text": "0:00:00"
      }
   */
  //"Details": "<a href=\"#\" onclick=\"return (parent.logtree_openNode || logtree_openNode)('{2170EE31-75ED-49BD-BA73-9FB10ECABFE9}')\">Details</a>",
  TCLogProviderItem(JSONObject obj) {
    if (obj != null) {
      if (obj.has("Name")) {
        this.name_ = obj.getString("Name");
      } else {
        this.name_ = "";
      }
      if (obj.has("Details")) {
        String str = obj.getString("Details");
        this.testItemId_ = str.substring(str.indexOf("'{") + 1, str.indexOf("}'"));
      } else {
        this.testItemId_ = "";
      }
      if (obj.has("StartTime")) {
        JSONObject startTime = obj.getJSONObject("StartTime");
        this.startTime_ = convertTc2DateTime(startTime.getString("text"), (startTime.getInt("msec") % 1000));
      }
      if (obj.has("EndTime")) {
        JSONObject endTime = obj.getJSONObject("EndTime");
        this.endTime_ = convertTc2DateTime(endTime.getString("text"), (endTime.getInt("msec") % 1000));
      }
      if (obj.has("RunTime")) {
        JSONObject runTime = obj.getJSONObject("RunTime");
        Float time = new Float(runTime.getInt("msec") / 1000);
        this.runTime_ = time.toString();
      }
    }
  }

  /**
   *
   * @return
   */
  public String getName() {
    return name_;
  }

  private String convertTc2DateTime(String inputDateTime, int inputMillis) {
    DateTimeFormatter formatter = null;
    String dateTime = "";
    if (inputDateTime.matches("[0-9]+/[0-9]+/[0-9]+ [0-9]+:[0-9]+:[0-9]+ PM|AM$")) {
      formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm:ss aa");
    } else if (inputDateTime.matches("[0-9]+/[0-9]+/[0-9]+ [0-9]+:[0-9]+:[0-9]+$")) {
      formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    } else if (inputDateTime.matches("[0-9]+\\.[0-9]+\\.[0-9]+ [0-9]+:[0-9]+:[0-9]+$")) {
      formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy hh:mm:ss");
    }
    try {
      LocalDateTime date = LocalDateTime.parse(inputDateTime, formatter);
      dateTime = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss"));
      dateTime += "." + inputMillis;
    } catch (DateTimeParseException exc) {

    }
    return dateTime;

  }

  String getTestItemId() {
    return this.testItemId_;
  }
}
