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
package jenkins.plugins.xunit.tc11;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.jenkinsci.lib.dtkit.model.InputMetric;
import org.jenkinsci.lib.dtkit.model.InputType;
import org.jenkinsci.lib.dtkit.model.OutputMetric;
import org.jenkinsci.lib.dtkit.util.converter.ConversionException;
import org.jenkinsci.plugins.xunit.types.model.JUnitModel;

import org.json.JSONObject;

import com.google.common.io.Files;
import java.io.FileWriter;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import jenkins.plugins.xunit.tc11.json.JSONUtil;
import jenkins.plugins.xunit.tc11.json.TCLog;

import jenkins.plugins.xunit.tc11.mht.*;
import org.jenkinsci.lib.dtkit.util.validator.ValidationException;
import org.jenkinsci.lib.dtkit.util.validator.ValidationService;

/**
 *
 * @author Michael Gärtner
 *
 *
 */
public class TestCompleteInputMetric extends InputMetric {

  /**
   *
   */
  private static final long serialVersionUID = 1260330207046310240L;

  private static final Logger LOGGER = Logger.getLogger("XUnitService");

  private final static String CONTENT_TYPE_JAVASCRIPT = "application/javascript";
  /**
   * Base URL links inside MHT and XML files refer to
   */
  private final static String INTERNAL_PARAM_BASE_URL = "baseUrl";
  /**
   * Path holding XML files
   */
  private final static String INTERNAL_PARAM_BASE_PATH = "basePath";

  /**
   * Parameter that defines the regular expression to apply to transformation so only those tests matching it will be included
   */
  public final static String PARAM_TEST_PATTERN = "testPattern";

  /**
   * Pattern provided by the user to apply a filtering to the tests results (we may only want to filter out some tests from the results such as start-up or
   * tear-down type tests).
   */
  private String testFilterPattern = "";

  @Override
  public InputType getToolType() {
    return InputType.TEST;
  }

  @Override
  public String getToolName() {
    return Messages.testcomplete_toolName();
  }

  @Override
  public String getToolVersion() {
    return "11.x";
  }

  protected void setTestFilterPattern(String testFilterPattern) {
    this.testFilterPattern = testFilterPattern != null ? testFilterPattern.trim() : "";
  }

  @Override
  public OutputMetric getOutputFormatType() {
    return JUnitModel.LATEST;
  }

  /**
   * Log an info output to the system logger
   *
   * @param message The message to be outputted
   */
  protected void infoSystemLogger(String message) {
    LOGGER.log(Level.INFO, "[TC11 - xUnit] - {0}", message);
  }

  /**
   * This method extracts all XML files inside a MHT file produced by TestComplete/TestExecute into a temporary directory and returns a reference to such
   * directory.
   *
   * @param inputFile MHT file to process
   * @param params map where parameter with key "baseUrl" and value returned by {@link MHTInputStream#getBaseUrl()} is added
   * @return temporary file that contains all the extracted XML files from input MHT file
   * @throws MHTException if and MHT error occurs
   * @throws IOException if an I/O error ocurrs
   */
  private File extractFilesFromMHTFile(File inputFile, Map<String, Object> params) throws IOException {
    File tempDir = Files.createTempDir();
    MHTInputStream mis = null;

    try {
      mis = new MHTInputStream(new FileInputStream(inputFile));

      if (params != null) {
        params.put(INTERNAL_PARAM_BASE_URL, mis.getBaseUrl());
        // It seems that backslashes should be escaped in XSL references
        // so we just convert to UNIX format
        // that works also on Windows for Java.
        params.put(INTERNAL_PARAM_BASE_PATH, FilenameUtils.normalize(tempDir.getAbsolutePath(), true));
      }

      MHTEntry entry;
      byte buffer[] = new byte[1024];
      int readBytes;

      while ((entry = mis.getNextEntry()) != null) {
        if (CONTENT_TYPE_JAVASCRIPT.equals(entry.getContentType()) && entry.getName().startsWith("_")) {
          File out = new File(tempDir, entry.getName());
          out.createNewFile();
          FileOutputStream fos = new FileOutputStream(out);
          try {
            while ((readBytes = mis.read(buffer)) > 0) {
              fos.write(buffer, 0, readBytes);
            }
          } finally {
            fos.close();
          }
        }
      }

      return tempDir;

    } catch (IOException e) {
      // Cleanup temporary directory here upon failure
      FileUtils.deleteDirectory(tempDir);
      throw e;
    } finally {
      if (mis != null) {
        mis.close();
      }
    }
  }

  @Override
  public void convert(File inputFile, File outFile, Map<String, Object> params) throws ConversionException {
    File inputTempDir = null;
    Map<String, Object> conversionParams = new HashMap<String, Object>();
    if (params != null) {
      conversionParams.putAll(params);
    }

    try {
      inputTempDir = this.extractFilesFromMHTFile(inputFile, conversionParams);

      Collection<File> jsFiles = FileUtils.listFiles(inputTempDir, FileFilterUtils.nameFileFilter("_root.js"), null);
      if (jsFiles.isEmpty()) {
        throw new ConversionException(
          "Invalid TestComplete MHT file '" + inputFile.getName() + "'. No '_root.js' found.");
      }

      File rootJS = jsFiles.iterator().next();

      /*
       * TODO We are unable to pass testFilterPattern as specified by user because
       * xUnit does not pass the TestType instance (TestCompleteTestType). Base plugin
       * should be extended to pass customized parameters.
       */
      if (this.testFilterPattern.length() > 0) {
        if (this.testFilterPattern.startsWith("^") || this.testFilterPattern.endsWith("$")) {
          /*
           * We do not allow these special symbols because they will make our pattern to
           * fail, since provided pattern is only part of another pattern that matches
           * test names.
           */
          throw new ConversionException("Invalid test filter pattern provided '" + this.testFilterPattern
            + "'. Start (^) and end ($) line pattern symbols are not allowed.");
        }

        infoSystemLogger("Applying test filter pattern '" + this.testFilterPattern + "' to TestComplete test: " + inputFile.getName());

        conversionParams.put(PARAM_TEST_PATTERN, this.testFilterPattern);
      }

      this.convertJson(rootJS, outFile);
    } catch (IOException e) {
      throw new ConversionException("Errors parsing input MHT file '" + inputFile.getName() + "'", e);
    } finally {

      if (inputTempDir != null) {
        try {
          FileUtils.deleteDirectory(inputTempDir);
        } catch (IOException e) {

        }
      }
    }
  }

  private void convertJson(File inputFile, File outFile) {

    String jsonRaw = null;
    try {
      String fileContent = JSONUtil.readJSONFile(inputFile, "UTF-8");
      int start = fileContent.indexOf('(');
      int end = fileContent.indexOf(')');
      fileContent = fileContent.substring(start + 1, end);

      jsonRaw = fileContent.substring(fileContent.indexOf(',') + 1);

    } catch (FileNotFoundException e) {
      throw new ConversionException("File '" + inputFile.getName() + "' not found.");
    } catch (IOException e) {
      throw new ConversionException("File '" + inputFile.getName() + "' can not be read.");
    }

    // setting optional arguments
    // jsonType = options.jsonType || 'mochawesome';
    // junitXml = fs.openSync(junitPath, 'w');
    if ((jsonRaw != null) && (!jsonRaw.trim().isEmpty())) {
      JSONObject jsonData = new JSONObject(jsonRaw);
      infoSystemLogger(jsonData.get("name").toString());
      TCLog tcLog;
      tcLog = new TCLog(jsonData);
      FileWriter fw;
      try {
        fw = new FileWriter(outFile);
        try {
          fw.write("<testsuites name=\"" + jsonData.getString("name") + "\">\n");

          fw.write("<testsuite");
          fw.write(" name=\"" + htmlEscape(jsonData.getString("name")) + "\"");
          // writeString(' tests="' + testCount + '"');
          // writeString(' failures="' + failures + '"');
          // writeString(' skipped="' + skips + '"');
          // writeString(' timestamp="' + dateTimestamp.toUTCString() + '"');
          // writeString(' time="' + (duration / 1000) + '"');
          fw.write(">\n");

          fw.write("</testsuite>\n");
          fw.write("</testsuites>\n");
        } finally {
          fw.close();
        }
      } catch (FileNotFoundException ex) {
        Logger.getLogger(TestCompleteInputMetric.class.getName()).log(Level.SEVERE, null, ex);
      } catch (IOException ex) {
        Logger.getLogger(TestCompleteInputMetric.class.getName()).log(Level.SEVERE, null, ex);
      }

    }

    // Formatting start time to javascript date then extracting milliseconds
    // for later timestamp incrementation
    // String dateFormatted = new Date(jsonData.stats.start);
    // dateMilliseconds = dateFormatted.getTime();
    // suites = jsonData.suites.suites;
    //
    // writeString('<testsuites name="' + jsonData.reportTitle + '">\n');
    //
    // suites.forEach(function (suite) {
    //
    // var testCount = 0,
    // failures = 0,
    // skips = 0,
    // duration = 0;
    //
    // var tests = suite.tests;
    //
    // tests.forEach(function (test) {
    // testCount++;
    // duration = duration + test.duration;
    // if (test.fail == true) failures++;
    // if (test.skipped == true) skips++;
    // });
    //
    // //incrementing millisecond timestamp by adding duration of all tests in order
    // //to correctly input testsuite 'timestamp' value
    // dateMilliseconds = dateMilliseconds + duration;
    //
    // var dateTimestamp = new Date(dateMilliseconds);
    //
    // writeString('<testsuite');
    // writeString(' name="' + htmlEscape(suite.title) + '"');
    // writeString(' tests="' + testCount + '"');
    // writeString(' failures="' + failures + '"');
    // writeString(' skipped="' + skips + '"');
    // writeString(' timestamp="' + dateTimestamp.toUTCString() + '"');
    // writeString(' time="' + (duration / 1000) + '"');
    // writeString('>\n');
    //
    // tests.forEach(function (test) {
    // writeString('<testcase');
    // writeString(' classname="' + htmlEscape(suite.title) + '"');
    // writeString(' name="' + htmlEscape(test.title) + '"');
    // writeString(' time="' + (test.duration / 1000) + '">\n');
    // if (test.state == "failed") {
    // writeString('<failure message="');
    // if (test.err.message) writeString(htmlEscape(test.err.message));
    // writeString('">\n');
    // writeString(htmlEscape(unifiedDiff(test.err)));
    // writeString('\n</failure>\n');
    //
    // } else if (test.state === undefined) {
    // writeString('<skipped/>\n');
    // }
    //
    // //TODO : extract console output to leverage this logic
    // //if (test.logEntries && test.logEntries.length) {
    // // writeString('<system-out><![CDATA[');
    // // test.logEntries.forEach(function (entry) {
    // // var outstr = util.format.apply(util, entry) + '\n';
    // // outstr = removeInvalidXmlChars(outstr);
    // // // We need to escape CDATA ending tags inside CDATA
    // // outstr = outstr.replace(/]]>/g, ']]]]><![CDATA[>')
    // // writeString(outstr);
    // // });
    // // writeString(']]></system-out>\n');
    // //}
    //
    // writeString('</testcase>\n');
    // });
    //
    // writeString('</testsuite>\n');
    // });
    //
    // writeString('</testsuites>\n');
    // if (junitXml) fs.closeSync(junitXml);
    // }
    //
    // } else {
    // console.log("Unable to parse json file.");
    // }
  }

  /*
    private void writeString(String str) {
      if (junitXml) {
         var buf = new Buffer(str);
         fs.writeSync(junitXml, buf, 0, buf.length, null);
      }
    }
   */
  private String htmlEscape(String str) {
    return str.replaceAll("&", "&amp;").replaceAll("\"", "&quot;").replaceAll("'", "&#39;").replaceAll("<", "&lt;").replaceAll(">", "&gt;");
  }

  /* function escapeInvisibles(line) { return line.replace(/\t/g, '<tab>')
   * .replace(/\r/g, '<CR>') .replace(/\n/g, '<LF>\n'); }
   *
   * function cleanUp(line) { if (line.match(/\@\@/)) return null; if
   * (line.match(/\\ No newline/)) return null; return escapeInvisibles(line); }
   *
   * function notBlank(line) { return line != null; }
   *
   * private String unifiedDiff(Object err) {
   *
   *
   * actual = err.actual; expected = err.expected;
   *
   * lines = null; String msg = "";
   *
   * if (err.actual && err.expected) { // make sure actual and expected are
   * strings if (!(typeof actual === 'string' || actual instanceof String)) {
   * actual = JSON.stringify(err.actual); }
   *
   * if (!(typeof expected === 'string' || expected instanceof String)) { expected
   * = JSON.stringify(err.actual); }
   *
   * msg = diff.createPatch('string', actual, expected); lines =
   * msg.split('\n').splice(4); msg +=
   * lines.map(cleanUp).filter(notBlank).join('\n'); }
   *
   * //TODO: Leverage if needed //if (options.junit_report_stack && err.stack) {
   * // if (msg) msg += '\n'; // lines = err.stack.split('\n').slice(1); // msg +=
   * lines.map(cleanUp).filter(notBlank).join('\n'); //}
   *
   * return msg; }
   */
  @Override
  public boolean validateInputFile(File inputXMLFile) throws ValidationException {
    return getInputValidationErrors().isEmpty();
  }

  @Override
  public boolean validateOutputFile(File inputXMLFile) throws ValidationException {
    //If no format is specified, exit validation and returns true
    if (this.getOutputFormatType() == null) {
      return true;
    }

    //If there no given xsd, exit validation and returns true
    if (this.getOutputXsdNameList() == null) {
      return true;
    }

    //Validate given XSD
    Source[] sources = new Source[getOutputXsdNameList().length];
    for (int i = 0; i < sources.length; i++) {
      sources[i] = new StreamSource(this.getOutputFormatType().getClass().getResourceAsStream(getOutputXsdNameList()[i]));
    }

    ValidationService validationService = new ValidationService();
    setOutputValidationErrors(validationService.processValidation(sources, inputXMLFile));
    return getOutputValidationErrors().isEmpty();
  }

  /**
   * the XSD file associated to this output format
   *
   * @return the relative xsd path. Can be null if there no XSD for the output format
   */
  public String[] getOutputXsdNameList() {
    if (getOutputFormatType() == null) {
      return null;
    }
    return getOutputFormatType().getXsdNameList();
  }
}
