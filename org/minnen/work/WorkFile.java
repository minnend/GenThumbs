package org.minnen.work;

import org.minnen.genthumbs.GenThumbs;
import java.io.*;

/** work to be done on a single file */
public class WorkFile extends WorkItem
{
  public WorkFile(File file, GenThumbs gt)
  {
    super(file, gt);
  }

  @Override
  public void run()
  {
    fireStartEvent(new WorkEvent(this, 0, System.currentTimeMillis()));
    gt.handleFile(this);
    fireEndEvent(new WorkEvent(this, 100, System.currentTimeMillis()));
  }
}
