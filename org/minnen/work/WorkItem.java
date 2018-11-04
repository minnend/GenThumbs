package org.minnen.work;

import java.util.*;
import java.awt.Dimension;
import java.io.*;

import org.minnen.genthumbs.GenThumbs;

/** abstract base class for work items */
public abstract class WorkItem
{
  protected List<WorkListener> listeners = new ArrayList<WorkListener>();
  protected GenThumbs          gt;
  protected File               fsrc, fdst;
  protected int                quality;
  protected Dimension          dimOrig, dimThumb;
  protected String             sResampleGoal;
  protected boolean            bStripMeta;

  public WorkItem(File src, GenThumbs gt)
  {
    this.fsrc = src;
    this.gt = gt;
  }

  public File getSrc()
  {
    return fsrc;
  }

  public File getDst()
  {
    return fdst;
  }

  public Dimension getThumbDim()
  {
    return dimThumb;
  }

  public Dimension getOrigDim()
  {
    return dimOrig;
  }

  public int getQuality()
  {
    return quality;
  }

  public String getResampleGoal()
  {
    return sResampleGoal;
  }

  public boolean getStripMeta()
  {
    return bStripMeta;
  }

  public void setup(File fsrc, File fdst, Dimension dimOrig, Dimension dimThumb, int quality, String sResampleGoal,
      boolean bStripMeta)
  {
    this.fsrc = fsrc;
    this.fdst = fdst;
    this.dimOrig = dimOrig;
    this.dimThumb = dimThumb;
    this.quality = quality;
    this.sResampleGoal = sResampleGoal;
    this.bStripMeta = bStripMeta;
  }

  public abstract void run();

  public void addWorkListener(WorkListener listener)
  {
    if (listener == null) return;

    synchronized (listeners) {
      listeners.add(listener);
    }
  }

  public void removeWorkListener(WorkListener listener)
  {
    if (listener == null) return;

    synchronized (listeners) {
      listeners.remove(listener);
    }
  }

  public void fireStartEvent(WorkEvent e)
  {
    if (e == null) return;

    synchronized (listeners) {
      for (WorkListener listener : listeners)
        listener.workStarted(e);
    }
  }

  public void fireEndEvent(WorkEvent e)
  {
    if (e == null) return;

    synchronized (listeners) {
      for (WorkListener listener : listeners)
        listener.workEnded(e);
    }
  }

  public void fireProgressEvent(WorkEvent e)
  {
    if (e == null) return;

    synchronized (listeners) {
      for (WorkListener listener : listeners)
        listener.workProgress(e);
    }
  }
}
