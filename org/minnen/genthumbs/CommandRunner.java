package org.minnen.genthumbs;

import java.io.*;

/**
 * Runs a program
 *
 * @author CEHJ
 * @created 23 February 2004
 */
public class CommandRunner
{

  private int exitCode = Integer.MIN_VALUE;

  /**
   * Gets the returnValue attribute of the CommandRunner object
   *
   * @return The returnValue value
   */
  public int getExitCode()
  {
    return exitCode;
  }

  /**
   * Main processing method for the CommandRunner object
   *
   * @param args Description of the Parameter
   */
  public void run(String[] args)
  {

    try {

      if (args.length < 1) return;
      Process pro = null;
      if (args.length > 1) pro = Runtime.getRuntime().exec(args);
      else pro = Runtime.getRuntime().exec(args[0]);
      InputStream error = pro.getErrorStream();
      InputStream output = pro.getInputStream();
      Thread err = new Thread(new IOBridge(System.err, error));
      Thread out = new Thread(new IOBridge(System.out, output));
      long msStart = System.currentTimeMillis();
      out.start();
      err.start();
      exitCode = pro.waitFor();
      System.out.printf("run time: %dms\n", System.currentTimeMillis() - msStart);
    } catch (java.io.IOException e) {
      e.printStackTrace();
    } catch (java.lang.InterruptedException e) {
      e.printStackTrace();
    }

  }

  public void run(String arg)
  {
    String[] a = new String[1];
    a[0] = arg;
    run(a);
  }

  /**
   * Threaded class that reads from specified input stream and writes to specified print stream.
   */
  class IOBridge implements Runnable
  {
    InputStream in;
    PrintStream out;

    public IOBridge(PrintStream _out, InputStream _in)
    {
      out = _out;
      in = _in;
    }

    public void run()
    {
      try {
        BufferedReader inb = new BufferedReader(new InputStreamReader(in));
        String temp = null;
        while ((temp = inb.readLine()) != null)
          out.println(temp);
        out.flush();
        inb.close();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }
}
