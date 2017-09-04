/**
 * The MIT License
 * Copyright (c) 2017 Michael Gärtner and all contributors
 * Original Copyright (c) 2015 Fernando Miguélez Palomo and all contributors
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
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import jenkins.plugins.xunit.tc11.json.MyUtils;
import jenkins.plugins.xunit.tc11.json.TCLog;
import jenkins.plugins.xunit.tc11.json.TCLogItem;

import jenkins.plugins.xunit.tc11.mht.*;
import org.apache.commons.io.FileExistsException;
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
  private final static String CONTENT_TYPE_OCTETSTREAM = "application/octet-stream";
  private final static String CONTENT_TYPE_PLAIN = "text/plain";
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
  private String fileName_;

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
    return "11.x-12.x";
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
  private File extractFilesFromMHTFile(File inputFile, Map<String, Object> params) throws IOException, SecurityException {
    File tempDir = Files.createTempDir();
    try (MHTInputStream mis = new MHTInputStream(new FileInputStream(inputFile))) {

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
        if ((CONTENT_TYPE_PLAIN.equals(entry.getContentType())
            || CONTENT_TYPE_JAVASCRIPT.equals(entry.getContentType())
            || CONTENT_TYPE_OCTETSTREAM.equals(entry.getContentType()))
            && (entry.getName().startsWith("_") || entry.getName().contains("test"))) {
          File out = new File(tempDir, entry.getName());
          boolean createNewFile = out.createNewFile();
          if (createNewFile) {
            try (FileOutputStream fos = new FileOutputStream(out)) {
              while ((readBytes = mis.read(buffer)) > 0) {
                fos.write(buffer, 0, readBytes);
              }
            }
          } else {
            FileUtils.deleteDirectory(tempDir);
            mis.close();
            throw new FileExistsException("File " + entry.getName() + " already exists.");
          }
        }
      }

      return tempDir;

    } catch (IOException | SecurityException e) {
      // Cleanup temporary directory here upon failure
      FileUtils.deleteDirectory(tempDir);
      throw e;
    }
  }

  @Override
  public void convert(File inputFile, File outFile, Map<String, Object> params) throws ConversionException {
    File inputTempDir = null;
    Map<String, Object> conversionParams = new HashMap<>();
    if (params != null) {
      conversionParams.putAll(params);
    }

    try {
      inputTempDir = this.extractFilesFromMHTFile(inputFile, conversionParams);
      this.fileName_ = FilenameUtils.removeExtension(inputFile.getName());

      Collection<File> jsFiles = FileUtils.listFiles(inputTempDir, FileFilterUtils.nameFileFilter("_root.js"), null);
      if (jsFiles.isEmpty()) {
        throw new ConversionException(
            "Invalid TestComplete 11 or 12 MHT file '" + inputFile.getName() + "'. No '_root.js' found.");
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

      this.convertJson(inputTempDir, rootJS, outFile, conversionParams);

    } catch (IOException e) {
      throw new ConversionException("Errors parsing input MHT file '" + inputFile.getName() + "'", e);
    } catch (SecurityException e) {
      throw new ConversionException("Error writing temp files.", e);
    } finally {

      if (inputTempDir != null) {
        try {
          FileUtils.deleteDirectory(inputTempDir);
        } catch (IOException e) {

        }
      }
    }
  }

  /**
   * Parses the given JavaScript file for JSON parts and generates a JUnit xml file
   *
   * @param inputTempDir Tempory directory used for temp files. will be deleted at the end.
   * @param inputFile JavaScript file containing JSON inforamtion
   * @param outFile JUnit xml file
   */
  private void convertJson(File inputTempDir, File inputFile, File outFile, Map<String, Object> conversionParams) {

    String jsonRaw = null;
    try {
      String fileContent = MyUtils.readJSONFile(inputFile, "UTF-8");
      int start = fileContent.indexOf('(');
      int end = fileContent.indexOf(')');
      fileContent = fileContent.substring(start + 1, end);

      jsonRaw = fileContent.substring(fileContent.indexOf(',') + 1);

    } catch (FileNotFoundException e) {
      throw new ConversionException("File '" + inputFile.getName() + "' not found.");
    } catch (IOException e) {
      throw new ConversionException("File '" + inputFile.getName() + "' can not be read.");
    }

    if ((jsonRaw != null) && (!jsonRaw.trim().isEmpty())) {
      JSONObject jsonData = new JSONObject(jsonRaw);
      TCLog tcLog;
      tcLog = new TCLog(jsonData, inputTempDir);
      OutputStreamWriter fw;
      try {
        fw = new OutputStreamWriter(new FileOutputStream(outFile), StandardCharsets.UTF_8);
        try {
          if (!tcLog.isEmpty()) {
            fw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            fw.write("<testsuite xmlns:xs=\"http://www.w3.org/2001/XMLSchema\""
                + " xmlns:fo=\"http://www.w3.org/1999/XSL/Format\""
                + " xmlns:fn=\"http://www.w3.org/2005/xpath-functions\""
                + " xmlns:xdt=\"http://www.w3.org/2005/xpath-datatypes\"");
            fw.write(" name=\"" + htmlEscape(this.fileName_) + "\"");
            fw.write(" tests=\"" + tcLog.getTestCount() + "\"");
            fw.write(" failures=\"" + tcLog.getFailures() + "\"");
            fw.write(" skipped=\"0\"");
            fw.write(" timestamp=\"" + MyUtils.convertTc2DateTime(tcLog.getTimeStamp()) + "\"");
            String time = String.format("%d", tcLog.duration() / 1000);
            fw.write(" time=\"" + time + "\"");
            fw.write(">\n");
            for (Iterator<TCLogItem> it = tcLog.getTCLogItems().iterator(); it.hasNext();) {
              TCLogItem item = it.next();
              String filterPattern = "[^\\]]+";
              if (conversionParams != null && conversionParams.get(PARAM_TEST_PATTERN) != null) {
                filterPattern = (String) conversionParams.get(PARAM_TEST_PATTERN);
              }
              String regex = "[^ ]+ Test Log \\[(" + filterPattern + ")\\]";
              Pattern p = Pattern.compile(regex);
              Matcher m = p.matcher(item.getName());
              if (m.matches()) {

                fw.write("<testcase");
                fw.write(" classname=\"" + htmlEscape(this.fileName_) + "." + htmlEscape(tcLog.getName()) + "." + htmlEscape(item.getName()) + "\"");
                fw.write(" name=\"" + htmlEscape(item.getCaption()) + "\"");
                time = String.format("%d", (item.getRunTime() / 1000));
                fw.write(" time=\"" + time + "\"");
                fw.write(">\n");
                if (item.getStatus() == 2 && item.getType().equals("Error")) {
                  fw.write("<failure message=\"" + htmlEscape(item.getMessage()) + "\"></failure>\n");
                  writeSystemOut(fw, item);
                  fw.write("<system-err/>\n");
                } else if (item.getStatus() > 0 || item.getStatus() < 2) {
                  writeSystemOut(fw, item);
                  fw.write("<system-err/>\n");
                }
                fw.write("</testcase>\n");
              }
            }
            fw.write("</testsuite>\n");
          }
        } catch (IOException e) {
          throw new ConversionException("Error in creating JUnit XML file.", e);
        } finally {
          fw.close();
        }
      } catch (FileNotFoundException ex) {
        Logger.getLogger(TestCompleteInputMetric.class.getName()).log(Level.SEVERE, null, ex);
      } catch (IOException ex) {
        Logger.getLogger(TestCompleteInputMetric.class.getName()).log(Level.SEVERE, null, ex);
      }

    }
  }

  private void writeSystemOut(OutputStreamWriter fw, TCLogItem item) throws IOException {
    fw.write("<system-out><![CDATA[\n");
    fw.write("[" + MyUtils.convertTc2DateTime(item.getTestTimeInMilliSec()) + "] ** " + item.getType() + " ** " + item.getMessage() + "\n");
    if (!item.getCallStack().isEmpty()) {
      writeCallStack(fw, item);
    }
    if (!item.getInfo().isEmpty()) {
      fw.write("Additional Info:\n");
      fw.write(htmlEscape(item.getInfo()));
      fw.write("\n");
    }
    fw.write("]]></system-out>\n");
  }

  private void writeCallStack(OutputStreamWriter fw, TCLogItem item) throws IOException {
    fw.write("Call Stack:\n");
    List list = item.getCallStack();
    for (int i = 0; i < list.size(); i++) {
      HashMap<String, String> m = (HashMap<String, String>) list.get(i);
      fw.write("    " + m.get("Line") + ": ");
      if (!m.get("Unit").isEmpty()) {
        fw.write(m.get("Unit") + ".");
      }
      fw.write(m.get("Test"));
      fw.write("\n");
    }
    fw.write("\n");
  }

  /**
   * makes String HTML conform
   *
   * @param str String to be converted
   * @return HTML conform String
   */
  private String htmlEscape(String str) {
    return str.replaceAll("&", "&amp;").replaceAll("\"", "&quot;").replaceAll("'", "&#39;").replaceAll("<", "&lt;").replaceAll(">", "&gt;");
  }

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
