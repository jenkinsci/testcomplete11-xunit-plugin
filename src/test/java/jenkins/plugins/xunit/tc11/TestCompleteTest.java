/**
 * The MIT License
 *
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

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.junit.Ignore;

public class TestCompleteTest extends AbstractXUnitXSLTest {

  @Test
  public void testKeywordTestProject() throws Exception {
    convertAndValidate(TestCompleteInputMetric.class,
        "TC11-testKeywordTestProject.mht",
        "JUnit-TC11-testKeywordTestProject.xml");
  }

  @Test
  public void testScriptTestProject() throws Exception {
    convertAndValidate(TestCompleteInputMetric.class,
        "TC11-testScriptTestProject.mht",
        "JUnit-TC11-testScriptTestProject.xml");
  }

  @Test
  public void testProjectSuiteTC11() throws Exception {
    convertAndValidate(TestCompleteInputMetric.class,
        "TC11-testProjectSuite.mht", "JUnit-TC11-testProjectSuite.xml");
  }

  @Test
  public void testProjectSuiteTC12() throws Exception {
    convertAndValidate(TestCompleteInputMetric.class,
        "TC12-testProjectSuite.mht", "JUnit-TC12-testProjectSuite.xml");
  }

  @Test
  public void testSingleKeywordTest() throws Exception {
    convertAndValidate(TestCompleteInputMetric.class,
        "TC11-testSingleKeywordTest.mht",
        "JUnit-TC11-testSingleKeywordTest.xml");
  }

  @Test
  public void testSingleScriptTest() throws Exception {
    convertAndValidate(TestCompleteInputMetric.class,
        "TC11-testSingleScriptTest.mht", "JUnit-TC11-testSingleScriptTest.xml");
  }

  @Ignore
  @Test
  public void testParameters() throws Exception {

    Map<String, Object> params = new HashMap<String, Object>();
    // Filter out KT3 and ST3 tests from result using external parameter
    // "testPattern"
    params.put(TestCompleteInputMetric.PARAM_TEST_PATTERN, ".*T[12]");

    convertAndValidate(TestCompleteInputMetric.class,
        "TC11-testParameters.mht", "JUnit-TC11-testParameters.xml", params);
  }
}
