package org.minnen.genthumbs.gui;

import org.minnen.genthumbs.*;

import java.awt.*;
import java.io.*;
import javax.swing.*;

/** feedback component for a single file */
public class FileFeedbackComp extends JPanel
{
	protected StatusComp status;

	public FileFeedbackComp(File src, File dst)
	{
		super(new BorderLayout());
		setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, Color.lightGray), BorderFactory
				.createEmptyBorder(2, 4, 2, 4)));

		status = new StatusComp();
		status.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(4, 4, 4, 8), BorderFactory.createLineBorder(Color.lightGray)));
		add(status, BorderLayout.WEST);
		
		JPanel p = new JPanel(new GridLayout(2, 1));
		add(p, BorderLayout.CENTER);

		String sSrc = src.getAbsolutePath();
		String sDst = dst.getAbsolutePath();
		String sPrefix = GenThumbs.getCommonPrefix(sSrc, sDst, true);
		int n = sPrefix.length();
		p.add(new JLabel(sSrc.substring(n) + " -> " + sDst.substring(n)));
		p.add(new JLabel(sPrefix));
	}

  public StatusComp getStatusComp()
  {
    return status;
  }

}
