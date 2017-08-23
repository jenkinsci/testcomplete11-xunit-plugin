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

import org.json.JSONObject;

/**
 * @deprecated @author mgaert
 */
public class TCLogProviderItem {

  private String name_;
  private String startTime_;
  private String endTime_;
  private long runTime_;
  private String testItemId_;
  private long endTimeInMilliSec_;
  private long startTimeInMilliSec_;

  /**
   * @deprecated
   */
  TCLogProviderItem() {

  }

  /**
   * @deprecated
   */
  TCLogProviderItem(JSONObject obj) {
    this.startTimeInMilliSec_ = 0;
    this.endTimeInMilliSec_ = 0;
    this.runTime_ = 0;
    this.testItemId_ = "";
    this.endTime_ = "";
    this.startTime_ = "";
    this.name_ = "";
    if (obj != null) {
      if (obj.has("Name")) {
        this.name_ = obj.getString("Name");
      }
      if (obj.has("Details")) {
        String str = obj.getString("Details");
        this.testItemId_ = str.substring(str.indexOf("('") + 2, str.indexOf("')"));
      }
      if (obj.has("StartTime")) {
        JSONObject startTime = obj.getJSONObject("StartTime");
        this.startTime_ = MyUtils.convertTc2DateTime(startTime.getString("text"));
        this.startTimeInMilliSec_ = startTime.getLong("msec");
      }
      if (obj.has("EndTime")) {
        JSONObject endTime = obj.getJSONObject("EndTime");
        this.endTime_ = MyUtils.convertTc2DateTime(endTime.getString("text"));
        this.endTimeInMilliSec_ = endTime.getLong("msec");
      }
      if (obj.has("RunTime")) {
        JSONObject runTime = obj.getJSONObject("RunTime");
        this.runTime_ = runTime.getLong("msec");
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

  /**
   *
   * @return
   */
  public String getTestItemId() {
    return this.testItemId_;
  }

  /**
   *
   * @return
   */
  public String getStartTime() {
    return this.startTime_;
  }

  /**
   *
   * @return
   */
  public String getEndTime() {
    return this.endTime_;
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
  public long getRunTime() {
    return this.runTime_;
  }

  void setName(String name) {
    this.name_ = name;
  }

  void setStartTime(String time) {
    this.startTime_ = time;
    this.startTimeInMilliSec_ = MyUtils.convertTcDateTime2MillSec(time);
  }

  void setEndTime(String time) {
    this.endTime_ = time;
    this.endTimeInMilliSec_ = MyUtils.convertTcDateTime2MillSec(time);
  }

  void setRunTime(long time) {
    this.runTime_ = time;
  }

  void setEndTimeInMilliSec(long time) {
    this.endTime_ = MyUtils.convertTc2DateTime(time);
    this.endTimeInMilliSec_ = time;
  }

  void setStartTimeInMilliSec(long time) {
    this.startTime_ = MyUtils.convertTc2DateTime(time);
    this.startTimeInMilliSec_ = time;
  }
}
