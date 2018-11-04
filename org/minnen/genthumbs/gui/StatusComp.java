package org.minnen.genthumbs.gui;

import java.awt.*;
import javax.swing.*;

/** component that shows the status of a work item inside of a feedback comp */
public class StatusComp extends JMyComponent
{
  public enum Status {
    Waiting, Working, Finished, Error
  }

  protected Status status;
  protected float  progress;

  public StatusComp()
  {
    status = Status.Waiting;
    progress = 0;
  }

  public void setProgress(float progress)
  {
    this.progress = progress;
    repaint();
  }

  public void setStatus(Status status)
  {
    this.status = status;
    repaint();
  }

  @Override
  public Dimension getPreferredSize()
  {
    return new Dimension(40, 40);
  }

  @Override
  public void paintComponent(Graphics2D g, int w, int h)
  {

    if (status == Status.Waiting) g.setColor(Color.lightGray);
    else if (status == Status.Finished) g.setColor(Color.blue);
    else if (status == Status.Error) g.setColor(Color.red);

    if (status == Status.Working) {
      int hh = (int) Math.round(progress * h);
      g.setColor(Color.yellow);
      g.fillRect(0, 0, w, h - hh);
      g.setColor(Color.green);
      g.fillRect(0, h - hh, w, hh);
    } else g.fillRect(0, 0, w, h);
  }
}
