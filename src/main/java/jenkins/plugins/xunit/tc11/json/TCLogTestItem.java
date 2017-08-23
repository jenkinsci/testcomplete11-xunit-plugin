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
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.jenkinsci.lib.dtkit.util.converter.ConversionException;
import org.json.JSONObject;
import org.json.JSONArray;

/**
 *
 * @author mgaertner
 * @deprecated
 */
public class TCLogTestItem {

  private String name_;
  private int status_;
  private String message_;
  private int runTime_;
  private String type_;
  private String caption_;
  private String id_;
  private ArrayList<TCLogProviderItem> providers_;

  /**
   * The JSON Object should look like    <code>{
   *               "name": "Test Log",
   *               "schemaType": "aqds:tree",
   *               "href": "TestKeywordProject/ProjectTestItem1/KeywordTestLogKT1/_TestLog.js"
   *             }
   * </code>
   *
   * @param inputTempDir
   * @param obj
   * @param providerItem
   */
  public TCLogTestItem(File inputTempDir, JSONObject obj, TCLogProviderItem providerItem) {
    if (obj != null) {
      if (obj.has("name")) {
        this.name_ = obj.getString("name");
      } else {
        this.name_ = "";
      }
      if (obj.has("id")) {
        this.id_ = obj.getString("id");
      } else {
        this.id_ = "";
      }

      if (obj.has("status")) {
        this.status_ = obj.getInt("status");
      } else {
        this.status_ = 0;
      }
      this.providers_ = new ArrayList<TCLogProviderItem>();
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
              } else {
                this.caption_ = "";
              }
              if (tmpObject.has("items")) {
                JSONArray items = tmpObject.getJSONArray("items");

                JSONObject obj2 = items.optJSONObject(0);
                if (obj2 != null) {
                  if (obj2.has("Message")) {
                    String str = obj2.getString("Message");
                    this.message_ = str;
                  } else {
                    this.message_ = "";
                  }
                  if (obj2.has("Time")) {
                    JSONObject time = obj2.getJSONObject("Time");
                    this.runTime_ = time.getInt("msec");
                  } else {
                    this.runTime_ = 0;
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
    }
  }

  public String getName() {
    return this.name_;
  }

  public String getCaption() {
    return this.caption_;
  }

  public int getRunTime() {
    return this.runTime_;
  }

  public String getType() {
    return this.type_;
  }

  public String getMessage() {
    return this.message_;
  }
}
