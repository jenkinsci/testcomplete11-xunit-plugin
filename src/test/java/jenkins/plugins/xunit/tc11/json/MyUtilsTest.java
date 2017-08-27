/*
 * The MIT License
 *
 * Copyright 2017 Michael Gärtner and all contributors.
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
import org.json.JSONObject;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;

/**
 *
 * @author Michael Gärtner
 */
public class MyUtilsTest {

  public MyUtilsTest() {
  }

  @BeforeClass
  public static void setUpClass() {
  }

  @AfterClass
  public static void tearDownClass() {
  }

  @Before
  public void setUp() {
  }

  @After
  public void tearDown() {
  }

  /**
   * Test of parseJSONFile method, of class MyUtils.
   */
  @Ignore
  @Test
  public void testParseJSONFile() throws Exception {
    File jsonFile = new File(this.getClass().getResource("testJSON.json")
        .toURI());
    String encoding = "UTF-8";
    JSONObject expResult = new JSONObject();
    JSONObject result = MyUtils.parseJSONFile(jsonFile, encoding);
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of readJSONFile method, of class MyUtils.
   */
  @Ignore
  @Test
  public void testReadJSONFile() throws Exception {
    File jsonFile = null;
    String encoding = "";
    String expResult = "";
    String result = MyUtils.readJSONFile(jsonFile, encoding);
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of convertTc2DateTime method, of class MyUtils.
   */
  @Ignore
  @Test
  public void testConvertTc2DateTime_String() {
    String inputDateTime = "";
    String expResult = "";
    String result = MyUtils.convertTc2DateTime(inputDateTime);
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of convertTc2DateTime method, of class MyUtils.
   */
  @Ignore
  @Test
  public void testConvertTc2DateTime_long() {
    long msec = 0L;
    String expResult = "";
    String result = MyUtils.convertTc2DateTime(msec);
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

}
