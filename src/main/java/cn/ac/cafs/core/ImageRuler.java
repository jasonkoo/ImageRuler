package cn.ac.cafs.core;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Stack;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;

import org.apache.log4j.Logger;

import cn.ac.cafs.bean.Line;
import cn.ac.cafs.util.ExcelManager;
import cn.ac.cafs.util.ImageUtil;
import cn.ac.cafs.util.PageController;

public class ImageRuler {
	public static Logger logger = Logger.getLogger(ImageRuler.class);
	
	private String confFileName = "./ir.conf";
	private String imageInputDir;
	private String excelOutputDir;
	
	private ExcelManager em;
	private PageController pc;

	private int mainFrameWidth = 1000;
	private int mainFrameHeight = 750;
	private String mainFrameTitle = "Image Ruler";

	private int imageOrigWidth;
	private int imageOrigHeight;
	
	private int imageNewWidth;
	private int imageNewHeight;

	private JFrame mainFrame;
	private JPanel imagePanel;
	private JLabel imageLabel;
	private JPanel controlPanel;

	private Stack<BufferedImage> imageStack;
	private Stack<Line> lineStack;

	private char knob;
	
	private List<Integer> resultCols;
	
	public ImageRuler() {
		configPath();
		initExcel();
		initPageController(imageInputDir);
		prepareGUI();
	}

	public static void main(String[] args) {
		ImageRuler imageRuler = new ImageRuler();
		imageRuler.displayFrame();
	}
	
	// Config Image Input Directory and Excel Output Directory
	// If Image Input Directory does not exist, exit
	// If Excel Output Directory does not exist, make a new one.
	private void configPath() {
		Properties prop = new Properties();
		File confFile = new File(confFileName);
		try {
			FileInputStream fis = new FileInputStream(confFile);
			prop.load(fis);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		this.imageInputDir = prop.getProperty("imageInputDir");
		logger.info("imageInputDir: " + imageInputDir);
		
		File imageInputFolder = new File(imageInputDir);
		if (!imageInputFolder.exists()) {
			logger.error("Error: imageInputDir does not exit!");
			System.exit(1);
		}
		
		this.excelOutputDir = prop.getProperty("excelOutputDir");
		logger.info("excelOutputDir: " + excelOutputDir);
		
		File excelOutputFolder = new File(excelOutputDir);
		if (! excelOutputFolder.exists()) {
			excelOutputFolder.mkdir();
		}
		
	}
	
	private void initExcel()
	{
		String excelFileName = this.excelOutputDir + File.separator + getCurrentTimeStamp() + ".xlsx";
	    this.em = new ExcelManager(excelFileName);	    
	    this.em.create();
	}
	
	private String getCurrentTimeStamp() {
	    SimpleDateFormat sdfDate = new SimpleDateFormat("yyyyMMdd-HHmmss");
	    Date now = new Date();
	    String curTime = sdfDate.format(now);
	    return curTime;
	}
	
	private void initPageController(String inputDir) {
		pc = new PageController();
		File folder = new File(inputDir);
		File[] files = folder.listFiles();
		for (File f : files) {
			if (f.isFile() && f.getName().toLowerCase().endsWith("jpg")) {
				pc.addImageName(f.getName());
			}
		}
	}

	private void prepareGUI() {
		mainFrame = new JFrame();
		mainFrame.setSize(mainFrameWidth, mainFrameHeight);
		mainFrame.setLayout(new GridBagLayout());
		
		mainFrame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent windowEvent) {
				System.exit(0);
			}
		});

		GridBagConstraints c = new GridBagConstraints();

		imageLabel = new JLabel();
		Border border = BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2);
		imageLabel.setBorder(border);
		imageLabel.addMouseListener(new ImageMouseListener());
		
		imagePanel = new JPanel();
		imagePanel.add(imageLabel);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 0;
		mainFrame.add(imagePanel, c);

		controlPanel = new JPanel();
		controlPanel.setLayout(new FlowLayout());
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 1;
		mainFrame.add(controlPanel, c);
		mainFrame.setVisible(true);
	}

	private void showImage(String imageName) {
		
		mainFrame.setTitle(pc.cur() + " - " + mainFrameTitle);
		
		try {
			BufferedImage original = ImageIO.read(new File(imageInputDir + File.separator + imageName));
			imageOrigWidth = original.getWidth();
			imageOrigHeight = original.getHeight();
			int widthLimit = new Double(mainFrame.getWidth() * .85).intValue();
			int heightLimit = new Double(mainFrame.getHeight() * .85).intValue();
			
			BufferedImage newImage = ImageUtil.resize(original, widthLimit, heightLimit);
			
			imageNewWidth = newImage.getWidth();
			imageNewHeight = newImage.getHeight();	
			
			// Initialize a stack to store image changes
			imageStack = new Stack<BufferedImage>();			
			imageStack.push(newImage);
			
			imageLabel.setIcon(new ImageIcon(newImage));
			
			// Initialize a stack to store lines drawn on the image
			lineStack = new Stack<Line>();
		
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void displayFrame() {
		// show image
		showImage(pc.cur());

		// show buttons
		JButton prevButton = new JButton("Prev");
		JButton horizontalButton = new JButton("—");
		JButton verticalButton = new JButton("|");
		JButton undoButton = new JButton("U");
		JButton computeButton = new JButton("C");
		JButton saveButton = new JButton("S");
		JButton nextButton = new JButton("Next");

		prevButton.setActionCommand("prev");
		nextButton.setActionCommand("next");

		horizontalButton.setActionCommand("horizontal");
		verticalButton.setActionCommand("vertical");
		
		undoButton.setActionCommand("undo");
		computeButton.setActionCommand("compute");
		saveButton.setActionCommand("save");

		prevButton.addActionListener(new PageButtonClickListener());
		nextButton.addActionListener(new PageButtonClickListener());

		horizontalButton.addActionListener(new KnobButtonClickListener());
		verticalButton.addActionListener(new KnobButtonClickListener());

		undoButton.addActionListener(new UndoButtonClickListener());
		computeButton.addActionListener(new ComputeButtonClickListener());
		saveButton.addActionListener(new SaveButtonClickListener());
		
		controlPanel.add(prevButton);
		controlPanel.add(horizontalButton);
		controlPanel.add(verticalButton);
		controlPanel.add(undoButton);
		controlPanel.add(computeButton);
		controlPanel.add(saveButton);
		controlPanel.add(nextButton);

		mainFrame.setVisible(true);
	}

	private class PageButtonClickListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			
			// clear image stack
			if (imageStack != null) {
				imageStack.clear();
			}
			
			// clear line stack
			if (lineStack != null) {
				lineStack.clear();
			}
			
			// initialize knob
			knob = 'x';
			
			String command = e.getActionCommand();
			String imageName = pc.cur();
			if (command.equals("prev")) {
				imageName = pc.prev();
			} else if (command.equals("next")) {
				imageName = pc.next();
			}
			showImage(imageName);
		}
	}

	private class KnobButtonClickListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			String command = e.getActionCommand();
			if (command.equals("horizontal")) {
				knob = 'h';
			} else if (command.equals("vertical")) {
				knob = 'v';
			}
		}
	}
	
	private class UndoButtonClickListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			String command = e.getActionCommand();
			if (command.equals("undo")) {
				if (imageStack.size() > 1) {
					imageStack.pop();
					imageLabel.setIcon(new ImageIcon(imageStack.peek()));
				}
				
				if (!lineStack.empty()) {
					lineStack.pop();
				}
				
			}
		}
	}
	
	private class ComputeButtonClickListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			String command = e.getActionCommand();
			if (command.equals("compute")) {
				BufferedImage tmp = ImageUtil.deepCopy(imageStack.peek());
				Graphics g = tmp.getGraphics();
				g.setColor(Color.green);
				g.setFont(new Font("Arial", Font.PLAIN, 30));
				
				Stack<Line> lines = (Stack<Line>) lineStack.clone();
				List<Integer> xList = new ArrayList<Integer>();
				List<Integer> yList = new ArrayList<Integer>();
				while (!lines.empty()) {
					Line aLine = lines.pop();
					if (aLine.getKnob() == 'h') {
						yList.add(aLine.getPosition());
					} else if (aLine.getKnob() == 'v') {
						xList.add(aLine.getPosition());
					}
				}
				Collections.sort(xList);
				Collections.sort(yList);
				
				resultCols = new ArrayList<Integer>();
				
				for (int i = 0; i < xList.size() -1; i++) {
					int xdiff = xList.get(i + 1) - xList.get(i);
					
					resultCols.add(xdiff);
					// scale
					//double real = xdiff * scale
					if (xdiff > 99) {
						g.drawString(String.valueOf(xdiff), xList.get(i) + xdiff / 2 - 30, 150);
					} else {
						g.drawString(String.valueOf(xdiff), xList.get(i) + xdiff / 2 - 17, 150);
					}
					
				}
				
			/*	for (int i = 0; i < yList.size() -1; i++) {
					int ydiff = yList.get(i + 1) - yList.get(i);
					System.out.println(ydiff);
				}*/
				
				g.dispose();
				imageLabel.setIcon(new ImageIcon(tmp));
				// store image changes
				imageStack.push(tmp);
				
				// store placeholder line
				lineStack.push(new Line('x', -1));
			}
		}
	}
	
	private class SaveButtonClickListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			String command = e.getActionCommand();
			if (command.equals("save")) {
				em.addRow(pc.cur(), resultCols);
			}
		}
	}
	
	private class ImageMouseListener extends MouseAdapter {
		@Override
		public void mouseClicked(MouseEvent e) {
			
			// left click: draw line
			if (SwingUtilities.isLeftMouseButton(e)) {
				if (knob == 'h') {
					BufferedImage tmp = ImageUtil.deepCopy(imageStack.peek());
					Graphics g = tmp.getGraphics();
					g.setColor(Color.red);
					g.drawLine(0, e.getY(), imageNewWidth, e.getY());			
					g.dispose();
					imageLabel.setIcon(new ImageIcon(tmp));
					
					// store image changes
					imageStack.push(tmp);
					
					// store horizontal line
					lineStack.push(new Line(knob, e.getY()));
					
				} else if (knob == 'v') {
					BufferedImage tmp = ImageUtil.deepCopy(imageStack.peek());
					Graphics g = tmp.getGraphics();
					g.setColor(Color.orange);
					g.drawLine(e.getX(), 0, e.getX(), imageNewHeight);
					g.dispose();
					
					imageLabel.setIcon(new ImageIcon(tmp));
					
					// store image changes
					imageStack.push(tmp);
					
					// store vertical line
					lineStack.push(new Line(knob, e.getX()));
				}
			// right click: remove last line
			} else if (SwingUtilities.isRightMouseButton(e)) {
				if (imageStack.size() > 1) {
					imageStack.pop();
					imageLabel.setIcon(new ImageIcon(imageStack.peek()));
				} 
				
				if (!lineStack.empty()) {
					lineStack.pop();
				}
			}
		}
	}
}
