package org.minnen.work;

import java.io.File;
import org.minnen.genthumbs.GenThumbs;

/** work to be done on a directory */
public class WorkDir extends WorkItem
{
  public WorkDir(File file, GenThumbs gt)
  {
    super(file, gt);
  }

  @Override
  public void run()
  {
    // TODO Auto-generated method stub
  }
}
