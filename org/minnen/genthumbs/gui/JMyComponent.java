package org.minnen.genthumbs.gui;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import javax.swing.*;

/**
 * A JComponent that handles borders automatically -- subclasses should do all rendering in the new
 * paintComponent(Graphics2D, int, int) method. Note that this component includes a mouse listener (for popups) and
 * focus listener so you don't need to add one in subclases.
 */
public abstract class JMyComponent extends JPanel implements MouseListener, FocusListener
{
  protected boolean bAllowPopup = true;

  /** construct a component; not that the constructor adds a mouse listener */
  public JMyComponent()
  {
    setOpaque(true);
    addMouseListener(this);
    setFocusable(false);
  }

  @Override
  public void setFocusable(boolean bFocusable)
  {
    boolean bAlready = isFocusable();
    if (bAlready == bFocusable) return;
    super.setFocusable(bFocusable);
    if (bFocusable) addFocusListener(this);
    else removeFocusListener(this);
  }

  public void setAllowPopup(boolean allowPopup)
  {
    bAllowPopup = allowPopup;
  }

  /** @return width of client area after accounting for insets */
  public int getClientWidth()
  {
    Insets ins = getInsets();
    return getWidth() - (ins.left + ins.right);
  }

  /** @return height of client area after accounting for insets */
  public int getClientHeight()
  {
    Insets ins = getInsets();
    return getHeight() - (ins.top + ins.bottom);
  }

  /** @return size of client area after accounting for insets */
  public Dimension getClientSize()
  {
    Insets ins = getInsets();
    Dimension dim = getSize();
    dim.width -= (ins.left + ins.right);
    dim.height -= (ins.top + ins.bottom);
    return dim;
  }

  public void paintComponent(Graphics g)
  {
    Insets ins = getInsets();
    int w = getWidth();
    int h = getHeight();

    int cw = w - (ins.left + ins.right);
    int ch = h - (ins.top + ins.bottom);

    if (cw > 0 && ch > 0) {
      Graphics2D g2 = (Graphics2D) g;
      AffineTransform at = g2.getTransform();
      if (ins.left != 0 || ins.top != 0) g2.translate(ins.left, ins.top);
      paintBackground((Graphics2D) g, cw, ch);
      paintComponent((Graphics2D) g, cw, ch);
      if (ins.left != 0 || ins.top != 0) g2.setTransform(at);
    }
  }

  /** paint the background area of this component's client window */
  public void paintBackground(Graphics2D g, int cw, int ch)
  {
    Color cBG = getBackground();
    if (cBG != null && isOpaque()) {
      g.setColor(cBG);
      g.fillRect(0, 0, cw, ch);
    }
  }

  /** paint the foreground area of this component's client window */
  public abstract void paintComponent(Graphics2D g, int cw, int ch);

  /** build a new popup menu or add items to your parent's popup */
  public JPopupMenu buildPopup(boolean bAppend)
  {
    return null;
  }

  /** append items to the end of the popup menu */
  public JPopupMenu appendPopup(JPopupMenu menu)
  {
    return menu;
  }

  public void showPopup(JPopupMenu menu, MouseEvent e)
  {
    if (menu == null || e == null) return;
    menu.show(e.getComponent(), e.getX(), e.getY());
  }

  public void mouseClicked(MouseEvent e)
  {}

  public void mouseEntered(MouseEvent e)
  {}

  public void mouseExited(MouseEvent e)
  {}

  public void mousePressed(MouseEvent e)
  {
    if (bAllowPopup && e.isPopupTrigger()) {
      showPopup(buildPopup(true), e);
      e.consume();
    }
  }

  public void mouseReleased(MouseEvent e)
  {
    if (bAllowPopup && e.isPopupTrigger()) showPopup(buildPopup(true), e);

    // generate mouseExit event if we drag outside of component and then release
    Point p = e.getPoint();
    Dimension dim = getSize();
    if (p.x < 0 || p.x >= dim.width || p.y < 0 || p.y >= dim.height) mouseExited(e);
  }

  public void focusGained(FocusEvent e)
  {
    repaint();
  }

  public void focusLost(FocusEvent e)
  {
    repaint();
  }
}
