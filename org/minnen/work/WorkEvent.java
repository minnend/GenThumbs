package org.minnen.work;

/** holds information about a work item event */
public class WorkEvent
{
  protected long     time;
  protected WorkItem item;
  protected float    progress;

  public WorkEvent(WorkItem item, float progress, long time)
  {
    this.item = item;
    this.progress = progress;
    this.time = time;
  }

  public long getTime()
  {
    return time;
  }

  public float getProgress()
  {
    return progress;
  }

  public WorkItem getItem()
  {
    return item;
  }
}
