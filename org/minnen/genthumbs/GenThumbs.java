package org.minnen.genthumbs;

import java.awt.Dimension;
import java.io.*;
import java.util.*;

import org.minnen.genthumbs.gui.GenThumbsGUI;
import org.minnen.work.*;

import com.drew.imaging.jpeg.JpegMetadataReader;
import com.drew.metadata.*;
import com.drew.metadata.jpeg.JpegDirectory;

/** helper app for generating thumbnail images */
public class GenThumbs
{
  public static final String psep     = File.separator;
  public static boolean      bWindows = System.getProperty("os.name").contains("Windows");

  protected CommandRunner    runner   = new CommandRunner();
  protected WorkQueue        wq       = new WorkQueue(4);
  protected GenThumbsGUI     gui;

  public void setGUI(GenThumbsGUI gui)
  {
    this.gui = gui;
  }

  /** application entry point */
  public static void main(String[] args)
  {
    GenThumbs gt = new GenThumbs();
    GenThumbsGUI gui = new GenThumbsGUI(gt);
    gui.setVisible(true);
  }

  public boolean prepWork(WorkItem work)
  {
    if (work instanceof WorkFile) {
      WorkFile wf = (WorkFile) work;

      File f = wf.getSrc();
      if (f == null || !f.exists() || f.isDirectory()) {
        System.err.printf("Error: prep work file (%s)\n", f.getAbsolutePath());
        return false;
      }

      String ext = extractExt(f.getName());
      if (!isImage(ext)) {
        System.err.printf("Error: prep work ext (%s)\n", ext);
        return false;
      }

      File basedir = f.getParentFile();
      if (!basedir.isDirectory()) {
        System.err.printf("Error: prep work base dir (%s)\n", basedir.getAbsolutePath());
        return false;
      }

      Dimension dimThumb = gui.getResizeDim();
      String sResampleGoal = gui.getResampleGoal();
      boolean bStrip = gui.getStripMeta();
      String subdirName = String.format("thumbs%d", dimThumb.width);

      File webdir = new File(basedir, subdirName);
      if (!webdir.exists() && !webdir.mkdirs()) {
        System.err.printf("Error: prep work web dir (%s)\n", webdir.getAbsolutePath());
        return false;
      }

      ext = ext.toLowerCase();
      String fname = f.getName();
      String title = extractTitle(fname);
      String dst = webdir.getAbsolutePath() + psep + title + "." + ext;

      Dimension dimOrig = null;
      if (isJpeg(ext)) {
        try {
          Metadata metadata = JpegMetadataReader.readMetadata(f);
          JpegDirectory jd = (JpegDirectory) getMetaDir(metadata, JpegDirectory.class);
          dimOrig = new Dimension(jd.getImageWidth(), jd.getImageHeight());
        } catch (Exception ex) {
          ex.printStackTrace();
        }
      }

      work.setup(f, new File(dst), dimOrig, dimThumb, 90, sResampleGoal, bStrip);
    } else if (work instanceof WorkDir) {
      // TODO
      System.err.println("directories not yet supported!");
      return false;
    }

    return true;
  }

  /** @return first directory in metadata with the given class; null if none found */
  public static Directory getMetaDir(Metadata meta, Class type)
  {
    if (meta == null) return null;
    Iterator it = meta.getDirectoryIterator();
    while (it.hasNext()) {
      Directory dir = (Directory) it.next();
      if (dir.getClass().equals(type)) return dir;
    }
    return null;
  }

  public boolean addWork(WorkItem work)
  {
    wq.addWork(work);
    return true;
  }

  protected double[] computeSteps(Dimension dimOrig, Dimension dimThumb)
  {
    double fw = (double) dimOrig.width / dimThumb.width;
    double fh = (double) dimOrig.height / dimThumb.height;
    double f = Math.max(fw, fh);
    // System.out.printf("fw=%f fh=%f", fw, fh);
    if (f < 2.5) return new double[] { Math.max(1.0, f) };

    // build the step list
    List<Double> stepList = new ArrayList<Double>();
    stepList.add(2.0);
    f /= 2.0;
    while (f > 1.000001) {
      if (f >= 2.0) {
        double v = (f <= 2.3) ? Math.sqrt(f) : 2.0;
        stepList.add(v);
        f /= v;
      } else {
        stepList.add(f);
        break;
      }
    }
    // convert list to array and return
    double[] ret = new double[stepList.size()];
    for (int i = 0; i < ret.length; i++)
      ret[ret.length - 1 - i] = stepList.get(i);
    return ret;
  }

  /** Downscale image followed by unsharp mask. */
  protected boolean convert(WorkItem work)
  {
    Dimension dimThumb = work.getThumbDim();
    String sResampleGoal = work.getResampleGoal();

    String cmd = String.format("magick convert \"%s\" -filter lanczos2sharp -resize \"%dx%d>\"",
        work.getSrc().getAbsolutePath(), (int) Math.round(dimThumb.getWidth()), (int) Math.round(dimThumb.getHeight()));

    if (sResampleGoal != "Smooth") {
      double sigma = 0.5, strength = 0.2;
      if (sResampleGoal == "Sharp") {
        sigma = 0.8;
        strength = 0.7;
      } else if (sResampleGoal == "Sharper") {
        sigma = 1.1;
        strength = 0.9;
      }
      cmd += String.format(" -unsharp 0x%.2f+%.2f+0.02", sigma, strength);
    }
    if (work.getStripMeta()) {
      cmd += " -strip";
    }
    if (work.getQuality() > 0) {
      cmd += String.format(" -quality %d", work.getQuality());
    }
    cmd += String.format(" \"%s\"", work.getDst().getAbsolutePath());

    String[] cmda = new String[3];
    if (bWindows) {
      cmda[0] = "cmd";
      cmda[1] = "/c";
      cmda[2] = cmd;
    } else {
      cmda = new String[3];
      cmda[0] = "/bin/bash";
      cmda[1] = "-c";
      cmda[2] = cmd;
    }

    System.out.printf("cmd: [%s]\n", cmd);
    runner.run(cmda);
    return (runner.getExitCode() == 0);
  }

  /** @return true if the extension is a known image type */
  public static boolean isImage(String ext)
  {
    ext = ext.toLowerCase();
    return (isJpeg(ext) || ext.equals("png"));
  }

  /** @return true if the extension codes for a jpeg image */
  public static boolean isJpeg(String ext)
  {
    ext = ext.toLowerCase();
    return (ext.equals("jpg") || ext.equals("jpeg"));
  }

  public boolean handleDirectory(File file, boolean bRecurse)
  {
    // if (file == null || !file.exists()) return false;
    //
    // File[] files = file.listFiles(new FileFilter() {
    //
    // @Override
    // public boolean accept(File f)
    // {
    // if (!f.isFile() || !f.canRead()) return false;
    // String ext = extractExt(f.getName()).toLowerCase();
    // if (!ext.equals("jpg") && !ext.equals("jpeg") && !ext.equals("png")) return false;
    // return true;
    // }
    //
    // });
    //
    // for(File f : files)
    // handleFile(f);
    //
    // return true;
    return false;
  }

  public boolean handleFile(WorkFile work)
  {
    return convert(work);
  }

  /** @return directory for file (/home/bob/foo.txt -> /home/bob) */
  public static String extractPath(String s)
  {
    int i = s.lastIndexOf(psep);
    if (i < 1) return "";
    return s.substring(0, i);
  }

  /** @return file name (/home/bob/foo.txt -> foo.txt) */
  public static String extractFile(String s)
  {
    int i = s.lastIndexOf(psep);
    if (i >= 0) s = s.substring(i + 1);
    return s;
  }

  /** @return name of file without extension (/home/bob/foo.txt -> foo) */
  public static String extractTitle(String s)
  {
    s = extractFile(s);
    int i = s.lastIndexOf('.');
    if (i < 0) return s;
    return s.substring(0, i);
  }

  /** @return file extension (/home/bob/foo.txt -> txt) */
  public static String extractExt(String s)
  {
    s = extractFile(s);
    int i = s.lastIndexOf('.');
    if (i < 0) return "";
    return s.substring(i + 1);
  }

  /** @return common prefix of the given strings */
  public static String getCommonPrefix(String a, String b, boolean bIgnoreCase)
  {
    int na = a.length();
    int nb = b.length();
    int nn = Math.min(na, nb);
    int n = 0;

    if (bIgnoreCase) {
      a = a.toLowerCase();
      b = b.toLowerCase();
    }

    while (n < nn && a.charAt(n) == b.charAt(n))
      n++;

    if (n == 0) return "";
    else return a.substring(0, n);
  }
}
