package org.minnen.genthumbs.gui;

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

import org.minnen.genthumbs.*;
import org.minnen.work.*;

import java.util.*;
import java.io.*;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** GUI front end for thumbnail generation */
public class GenThumbsGUI implements DropTargetListener, WorkListener, ActionListener
{
	protected JFrame frame;
	protected JPanel workp;
	protected GenThumbs gt;
	protected Map<WorkItem, FileFeedbackComp> mapWork2Comp;
	protected JComboBox<String> cbResolution, cbResample;
	protected JCheckBox checkStrip;

	public GenThumbsGUI(GenThumbs gt)
	{
		this.gt = gt;
		gt.setGUI(this);

		frame = new JFrame("Thumbnail Generator");
		frame.setSize(600, 500); // TODO check screen size
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLayout(new BorderLayout());

		workp = new JPanel(new VerticalLayout(-1, 2));
		workp.setBackground(Color.white);
		
		JPanel controlp = new JPanel();
		cbResolution = new JComboBox<String>(new String[]{
		    "400 x 300", "720 x 600", "800 x 600", "1024 x 768",
		    "1200 x 900", "1600 x 1200", "1920 x 1080",
		    "1920 x 1200", "2560 x 1600", "3840 x 2160", "4096 x 2160" });//, "Add Resolution..." });
		cbResolution.setSelectedIndex(9);
		cbResolution.addActionListener(this);
		controlp.add(cbResolution);
		
		cbResample = new JComboBox<String>(new String[]{ "Sharp", "Sharper", "Smooth" });
    cbResample.setSelectedIndex(0);
    cbResample.addActionListener(this);
    controlp.add(cbResample);
		
		checkStrip = new JCheckBox("Strip Meta", false);
		controlp.add(checkStrip);
		

		frame.add(new JScrollPane(workp), BorderLayout.CENTER);
		frame.add(controlp, BorderLayout.NORTH);

		mapWork2Comp = new HashMap<WorkItem, FileFeedbackComp>();

		new DropTarget(frame, this);
	}

	public Dimension getResizeDim()
	{
		String s = (String)cbResolution.getSelectedItem();
		Pattern p = Pattern.compile("^\\s*(\\d+)\\s*x\\s*(\\d+)\\s*$");
		Matcher m = p.matcher(s);
		if (!m.matches()) return null;
		int w = Integer.parseInt(m.group(1));
		int h = Integer.parseInt(m.group(2));
		return new Dimension(w, h);
	}
	
	public String getResampleGoal()
	{
	  return (String)cbResample.getSelectedItem();
	}
	
	public boolean getStripMeta(){ return checkStrip.isSelected(); }
	
	public void setVisible(boolean b)
	{
		frame.setVisible(b);
	}

	@Override
	public void dragEnter(DropTargetDragEvent dtde)
	{}

	@Override
	public void dragExit(DropTargetEvent dte)
	{}

	@Override
	public void dragOver(DropTargetDragEvent dtde)
	{}

	@Override
	public void drop(DropTargetDropEvent evt)
	{
		try{
			Transferable t = evt.getTransferable();
			if (t.isDataFlavorSupported(DataFlavor.javaFileListFlavor)){
				evt.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);

				List<File> files = (List<File>)t.getTransferData(DataFlavor.javaFileListFlavor);
				evt.getDropTargetContext().dropComplete(true);

				List<WorkItem> list = new ArrayList<WorkItem>(); 
				
				for(File f : files){
					System.out.printf("drop: %s\n", f.getAbsolutePath());
					final WorkFile work = new WorkFile(f, gt);
					work.addWorkListener(this);
					if (gt.prepWork(work)){
						final FileFeedbackComp comp = new FileFeedbackComp(work.getSrc(), work.getDst());
						synchronized (mapWork2Comp){
							mapWork2Comp.put(work, comp);
						}
						synchronized (workp){
							workp.add(comp);
						}						
						list.add(work);						
					}
				}
				
				workp.validate();
				workp.repaint();
				for(WorkItem work : list) gt.addWork(work);
			}
			else{
				DataFlavor[] flavors = evt.getCurrentDataFlavors();
				System.out.println("Flavors:");
				for(DataFlavor flavor : flavors)
					System.out.printf(" - %s\n", flavor);

				evt.rejectDrop();
			}
		} catch (Exception e){
			evt.rejectDrop();
			e.printStackTrace();
		}
	}

	@Override
	public void dropActionChanged(DropTargetDragEvent dtde)
	{}

	@Override
	public void workEnded(WorkEvent e)
	{
		WorkItem work = e.getItem();
		System.out.printf("finished (%d): %s\n", Thread.currentThread().getId(), work.getSrc().getAbsolutePath());
		final FileFeedbackComp comp = mapWork2Comp.get(work);
		StatusComp status = comp.getStatusComp();
		status.setStatus(StatusComp.Status.Finished);
		synchronized (mapWork2Comp){
			mapWork2Comp.remove(work);
		}
		EventQueue.invokeLater(new Runnable() {
			public void run()
			{
				synchronized (workp){
					workp.remove(comp);
				}
				workp.revalidate();
				workp.repaint();
			}
		});
	}

	@Override
	public void workProgress(WorkEvent e)
	{
		WorkItem work = e.getItem();
		System.out.printf("progress (%.2f): %s\n", e.getProgress(), work.getSrc().getAbsolutePath());
		FileFeedbackComp comp = mapWork2Comp.get(work);
		StatusComp status = comp.getStatusComp();
		status.setProgress(e.getProgress());
	}

	@Override
	public void workStarted(WorkEvent e)
	{
		WorkItem work = e.getItem();
		if (work instanceof WorkFile){
			System.out.printf("started (%d): %s\n", Thread.currentThread().getId(), work.getSrc().getAbsolutePath());
		}
		else if (work instanceof WorkDir){
			System.out.printf("work started on dir: %s\n", work.getSrc().getAbsolutePath());
			// TODO
		}

		FileFeedbackComp comp = mapWork2Comp.get(work);
		StatusComp status = comp.getStatusComp();
		status.setStatus(StatusComp.Status.Working);
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		Object src = e.getSource();
		
		if (src == cbResolution){
			String cmd = (String)cbResolution.getSelectedItem();
			if (cmd.startsWith("Add")){
				// TODO				
			}
		}
		
	}

}
