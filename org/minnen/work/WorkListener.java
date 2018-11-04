package org.minnen.work;

/** marks a class as a listener to work events */
public interface WorkListener
{
  public void workStarted(WorkEvent e);
  public void workEnded(WorkEvent e);
  public void workProgress(WorkEvent e);
}
