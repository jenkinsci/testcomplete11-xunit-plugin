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
package jenkins.plugins.xunit.tc11.mht;

import java.io.IOException;

/**
 * Signals that a MHT exception of some sort has occurred.
 *
 * @author Michael Gärtner
 *
 */
public class MHTException extends IOException {

  /**
   * Serial version of this exception
   */
  private static final long serialVersionUID = 6097799246130577951L;

  /**
   * Constructs a {@link MHTException} with <code>null</code> as its error detail message.
   */
  public MHTException() {
    super();
  }

  /**
   * Constructs a {@link MHTException} with the specified detail message.
   *
   * @param s the detail message.
   */
  public MHTException(String s) {
    super(s);
  }

  /**
   * Constructs a {@link MHTException} with the specified detail message and the given cause.
   *
   * @param s the detail message.
   * @param t the cause.
   */
  public MHTException(String s, Throwable t) {
    super(s, t);
  }
}
