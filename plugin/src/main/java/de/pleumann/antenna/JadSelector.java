package de.pleumann.antenna;

import java.io.File;

import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;


/**
 * omry 
 * Aug 8, 2006
 */
class JadSelector extends JDialog
{
	private final File m_dir;

	public JadSelector(File dir)
	{
		m_dir = dir;
	}
	
	public File selectJad()
	{
		JFileChooser chooser = new JFileChooser(m_dir);
		chooser.setFileFilter(new FileFilter()
		{
			public String getDescription()
			{
				return "Jad files";
			}
		
			public boolean accept(File f)
			{
				return f.getName().toLowerCase().endsWith(".jad");
			}
		});
	    int returnVal = chooser.showOpenDialog(null);
	    if(returnVal == JFileChooser.APPROVE_OPTION) 
	    {
	    	return chooser.getSelectedFile();
	    }
	    else
	    {
	    	return null;
	    }
	}
}
