/*
 * Created by JFormDesigner on Tue Apr 08 14:19:12 CDT 2014
 */

package grade;

import java.awt.*;
import java.awt.Point;
import java.awt.event.*;
import java.awt.Graphics;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.Set;
import java.util.List;

import javax.swing.*;

import protobuf.srl.commands.Commands.SrlUpdateList;
import response.Response;
import srl.core.sketch.*;


/**
 * @author Zhengliang Yin
 */
public class GradeHere extends JFrame {
	/**
	 * 
	 */
	// constants
	private final static String SELECTITEM = "Select...";

	private boolean userSelect = false;
	private Submissions submissions = new Submissions();

	public static void main(String[] args) {
		GradeHere g = new GradeHere();
		g.show();
	}

	private static final long serialVersionUID = 1L;

	public GradeHere() {
		initComponents();
	}

	private void loadMouseClicked(MouseEvent e) {
		// TODO add your code here
	}

	private void nextMouseClicked(MouseEvent e) {
		// TODO add your code here
	}
	
	private void prevMouseClicked(MouseEvent e) {
		// TODO add your code here
	}
	
	private void nextActionPerformed(ActionEvent e) {
		int count = problemList.getItemCount();
		int pos = problemList.getSelectedIndex();
		if (pos < count - 1) pos++;
			problemList.setSelectedIndex(pos);
		if (pos == (count - 1)) next.setEnabled(false);
		if (pos > 0) prev.setEnabled(true);
	}

	private void prevActionPerformed(ActionEvent e) {
		int count = problemList.getItemCount();
		int pos = problemList.getSelectedIndex();
		if (pos > 0) pos--;
			problemList.setSelectedIndex(pos);
		if (pos == 0) prev.setEnabled(false);
		if (pos < (count - 1)) next.setEnabled(true);
	}
	
	private void loadActionPerformed(ActionEvent e) {
		submissions.clear();
		usernameList.removeAllItems();
		assignmentList.removeAllItems();
		problemList.removeAllItems();
		JFileChooser chooser = new JFileChooser();
		chooser.showOpenDialog(null);
		File folder = chooser.getCurrentDirectory();
		String dir = folder.getAbsolutePath() + "/";
		// for (File file : folder.listFiles())
		for (String filename : folder.list()) {
			if (!filename.contains(".dat")) {
				continue;
			}
			String filenameShort = filename.split(".dat")[0];
			String[] splits = filenameShort.split("_");
			submissions.addSubmission(new SubmissionInfo(splits, dir + filename));
		}

		usernameList.addItem(SELECTITEM);
		assignmentList.addItem(SELECTITEM);
		problemList.addItem(SELECTITEM);

		Set<String> usernames = submissions.getUsernames("", "");
		Set<String> assignments = submissions.getAssignemtns("", "");

		for (String username : usernames)
			usernameList.addItem(username);
		for (String assignment : assignments)
			assignmentList.addItem(assignment);

		usernameList.setSelectedIndex(0);
		assignmentList.setSelectedIndex(0);
		problemList.setSelectedIndex(0);

		userSelect = true;
	}

	private void UpdateSketch(String filename) {
		try {
			StatusLabel.setText("Current Open:" + filename);
			
			sketchPanel.removeAll();
			Graphics g = sketchPanel.getGraphics();
			sketchPanel.paint(g);
			
			BufferedInputStream instream = new BufferedInputStream(new FileInputStream(filename));
			SrlUpdateList updates = SrlUpdateList.parseFrom(instream);
            instream.close();
            
            Sketch tester = Response.viewTest(updates);
            
            Integer xmin = Integer.MAX_VALUE;
            Integer xmax = Integer.MIN_VALUE;
            Integer ymin = Integer.MAX_VALUE;
            Integer ymax = Integer.MIN_VALUE;       
            
            
            for (srl.core.sketch.Stroke m_stroke : tester.getStrokes()) {
            	for (srl.core.sketch.Point p : m_stroke.getPoints()) {
            		if (p.getX() < xmin) xmin = (int)p.getX();
            		if (p.getX() > xmax) xmax = (int)p.getX();
            		if (p.getY() < ymin) ymin = (int)p.getY();
            		if (p.getY() > ymax) ymax = (int)p.getY();
            	}
            }
            
            int x_range = xmax - xmin + 200;
            int y_range = ymax - ymin + 150;
            
            for (srl.core.sketch.Stroke m_stroke : tester.getStrokes()) {
            	List<srl.core.sketch.Point> pl = m_stroke.getPoints();
            	srl.core.sketch.Point s = pl.remove(0);
				for (srl.core.sketch.Point p : pl) {
					g.drawLine((int) ((p.getX() - xmin + 100) * 900 / x_range + 40), (int) ((p.getY()
							- ymin + 75) * 400 / y_range + 40),
							(int) ((s.getX() - xmin + 100) * 900 / x_range + 40),
							(int) ((s.getY() - ymin + 75) * 400 / y_range + 40));
					s = p;
//            		Or just use large canvas and original points, no normalization and relocation
//            		g.drawLine((int)p.getX(), (int)p.getY(), (int)s.getX(), (int)s.getY());
//                    		s = p;
            	}
            }
    		sketchPanel.paintComponents(g);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void chooseProblem(ItemEvent e) {
		if (e.getStateChange() == ItemEvent.SELECTED && userSelect) {
			if (usernameList.getItemCount() == 0 || assignmentList.getItemCount() == 0
					|| problemList.getItemCount() == 0)
				return;

			String cur_assignment = assignmentList.getSelectedItem().toString();
			String cur_username = usernameList.getSelectedItem().toString();
			String cur_problem = problemList.getSelectedItem().toString();

			if (cur_assignment.equals(SELECTITEM) || cur_username.equals(SELECTITEM)
					|| cur_problem.equals(SELECTITEM))
				return;

			String filePath = submissions.getFilename(cur_username, cur_assignment, cur_problem);

			UpdateSketch(filePath);
		}
	}

	private void u_a_ListItemStateChanged(ItemEvent e) {
		if (e.getStateChange() == ItemEvent.SELECTED && userSelect) {
			if (usernameList.getItemCount() == 0 || assignmentList.getItemCount() == 0
					|| problemList.getItemCount() == 0)
				return;

			userSelect = false;

			String cur_assignment = assignmentList.getSelectedItem().toString();
			String cur_username = usernameList.getSelectedItem().toString();

			// usernameList.removeAll();
			// assignmentList.removeAll();
			// problemList.removeAll();

			usernameList.removeAllItems();
			assignmentList.removeAllItems();
			problemList.removeAllItems();

			usernameList.addItem(SELECTITEM);
			assignmentList.addItem(SELECTITEM);
			problemList.addItem(SELECTITEM);

			if (!cur_username.equals(SELECTITEM) && !cur_assignment.equals(SELECTITEM)) {
//				int indexUser = 0;
//				int indexAssign = 0;

				Set<String> usernames = submissions.getUsernames(cur_assignment, "");
				Set<String> assignments = submissions.getAssignemtns(cur_username, "");

				for (String username : usernames) {
					usernameList.addItem(username);
					if (username.equals(cur_username))
						usernameList.setSelectedItem(username);
					// usernameList.setSelectedIndex(indexUser);
					// indexUser++;
				}
				for (String assignment : assignments) {
					assignmentList.addItem(assignment);
					if (assignment.equals(cur_assignment))
						assignmentList.setSelectedItem(assignment);
					// assignmentList.setSelectedIndex(indexAssign);
					// indexAssign++;
				}

				Set<String> problems = submissions.getProblems(cur_username, cur_assignment);
				for (String problem : problems)
					problemList.addItem(problem);

				problemList.setSelectedIndex(0);

			} else if (cur_username.equals(SELECTITEM) && !cur_assignment.equals(SELECTITEM)) {
//				int indexAssign = 0;

				Set<String> usernames = submissions.getUsernames(cur_assignment, "");
				Set<String> assignments = submissions.getAssignemtns("", "");

				for (String username : usernames) {
					usernameList.addItem(username);
				}

				for (String assignment : assignments) {
					assignmentList.addItem(assignment);
					if (assignment.equals(cur_assignment))
						assignmentList.setSelectedItem(assignment);
					// assignmentList.setSelectedIndex(indexAssign);
					// indexAssign++;
				}

				Set<String> problems = submissions.getProblems("", cur_assignment);
				for (String problem : problems)
					problemList.addItem(problem);

				usernameList.setSelectedIndex(0);
				problemList.setSelectedIndex(0);

			} else if (!cur_username.equals(SELECTITEM) && cur_assignment.equals(SELECTITEM)) {
//				int indexUser = 0;

				Set<String> usernames = submissions.getUsernames("", "");
				Set<String> assignments = submissions.getAssignemtns(cur_username, "");

				for (String username : usernames) {
					usernameList.addItem(username);
					if (username.equals(cur_username))
						usernameList.setSelectedItem(username);
					// usernameList.setSelectedIndex(indexUser);
					// indexUser++;
				}

				for (String assignment : assignments) {
					assignmentList.addItem(assignment);
				}

				assignmentList.setSelectedIndex(0);
				problemList.setSelectedIndex(0);
			} else {
				Set<String> usernames = submissions.getUsernames("", "");
				Set<String> assignments = submissions.getAssignemtns("", "");

				for (String username : usernames)
					usernameList.addItem(username);
				for (String assignment : assignments)
					assignmentList.addItem(assignment);

				usernameList.setSelectedIndex(0);
				assignmentList.setSelectedIndex(0);
				problemList.setSelectedIndex(0);
			}

			// usernameList.setSelectedItem(cur_username);
			// assignmentList.setSelectedItem(cur_assignment);
			// problemList.setSelectedIndex(0);

			userSelect = true;
		}
	}

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY
		// //GEN-BEGIN:initComponents
		// Generated using JFormDesigner Evaluation license - Zhengliang Yin
		userNameLabel = new JLabel();
		sketchPanel = new JPanel();
		assignmentLabel = new JLabel();
		problemLabel = new JLabel();
		prev = new JButton();
		next = new JButton();
		grades = new JTextField();
		record = new JButton();
		gradesLabe = new JLabel();
		usernameList = new JComboBox<String>();
		assignmentList = new JComboBox<String>();
		problemList = new JComboBox<String>();
		load = new JButton();
		StatusLabel = new JLabel();

		usernameList.addItem(SELECTITEM);
		assignmentList.addItem(SELECTITEM);
		problemList.addItem(SELECTITEM);

		// ======== this ========
		setResizable(false);
		setTitle("Dr. Hammond TAMU 2014 Spring CSCE 222 Grader");
		setMinimumSize(new Dimension(1024, 576));
		Container contentPane = getContentPane();
		contentPane.setLayout(null);

		// ---- userNameLabel ----
		userNameLabel.setText("Student:");
		contentPane.add(userNameLabel);
		userNameLabel.setBounds(90, 20, 60, userNameLabel.getPreferredSize().height);

		// ======== sketchPanel ========
		{
			sketchPanel.setBackground(Color.white);

			sketchPanel.setLayout(null);

			{ // compute preferred size
				Dimension preferredSize = new Dimension();
				for (int i = 0; i < sketchPanel.getComponentCount(); i++) {
					Rectangle bounds = sketchPanel.getComponent(i).getBounds();
					preferredSize.width = Math.max(bounds.x + bounds.width, preferredSize.width);
					preferredSize.height = Math.max(bounds.y + bounds.height, preferredSize.height);
				}
				Insets insets = sketchPanel.getInsets();
				preferredSize.width += insets.right;
				preferredSize.height += insets.bottom;
				sketchPanel.setMinimumSize(preferredSize);
				sketchPanel.setPreferredSize(preferredSize);
			}
		}
		contentPane.add(sketchPanel);
		sketchPanel.setBounds(15, 50, 980, 480);

		// ---- assignmentLabel ----
		assignmentLabel.setText("Assignment:");
		contentPane.add(assignmentLabel);
		assignmentLabel.setBounds(new Rectangle(new Point(265, 20), assignmentLabel
				.getPreferredSize()));

		// ---- problemLabel ----
		problemLabel.setText("Problem:");
		contentPane.add(problemLabel);
		problemLabel.setBounds(new Rectangle(new Point(470, 20), problemLabel.getPreferredSize()));

		// ---- prev ----
		prev.setText("Prev");
		prev.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				prevMouseClicked(e);
			}
		});
		prev.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				prevActionPerformed(e);
			}
		});
		contentPane.add(prev);
		prev.setBounds(650, 16, prev.getPreferredSize().width, 22);

		// ---- next ----
		next.setText("Next");
		next.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				nextMouseClicked(e);
			}
		});
		next.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				nextActionPerformed(e);
			}
		});
		contentPane.add(next);
		next.setBounds(721, 16, next.getPreferredSize().width, 22);
		contentPane.add(grades);
		grades.setBounds(844, 16, 66, 22);

		// ---- record ----setBounds
		record.setText("Record");
		record.setEnabled(false);
		contentPane.add(record);
		record.setBounds(924, 16, record.getPreferredSize().width, 22);

		// ---- gradesLabe ----
		gradesLabe.setText("Grades:");
		contentPane.add(gradesLabe);
		gradesLabe.setBounds(new Rectangle(new Point(792, 20), gradesLabe.getPreferredSize()));

		// ---- usernameList ----
		usernameList.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				u_a_ListItemStateChanged(e);
			}
		});

		// ---- assignmentList ----
		assignmentList.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				u_a_ListItemStateChanged(e);
			}
		});

		// ---- assignmentList ----
		problemList.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				chooseProblem(e);
			}
		});

		contentPane.add(usernameList);
		usernameList.setBounds(140, 16, 110, 22);
		contentPane.add(assignmentList);
		assignmentList.setBounds(345, 16, 120, 22);
		contentPane.add(problemList);
		problemList.setBounds(520, 16, 116, 22);

		// ---- load ----
		load.setText("Load");
		load.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				loadMouseClicked(e);
			}
		});
		load.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				loadActionPerformed(e);
			}
		});
		contentPane.add(load);
		load.setBounds(20, 16, load.getPreferredSize().width, 22);
		// load.setBounds(new Rectangle(new Point(15, 15),
		// load.getPreferredSize()));
		
		//---- StatusLabel ----
		StatusLabel.setText("Current Open:");
		contentPane.add(StatusLabel);
		StatusLabel.setBounds(0, 530, 1020, 25);


		{ // compute preferred size
			Dimension preferredSize = new Dimension();
			for (int i = 0; i < contentPane.getComponentCount(); i++) {
				Rectangle bounds = contentPane.getComponent(i).getBounds();
				preferredSize.width = Math.max(bounds.x + bounds.width, preferredSize.width);
				preferredSize.height = Math.max(bounds.y + bounds.height, preferredSize.height);
			}
			Insets insets = contentPane.getInsets();
			preferredSize.width += insets.right;
			preferredSize.height += insets.bottom;
			contentPane.setMinimumSize(preferredSize);
			contentPane.setPreferredSize(preferredSize);
		}
		setLocationRelativeTo(getOwner());
		// JFormDesigner - End of component initialization
		// //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY
	// //GEN-BEGIN:variables
	// Generated using JFormDesigner Evaluation license - Zhengliang Yin
	private JLabel userNameLabel;
	private JPanel sketchPanel;
	private JLabel assignmentLabel;
	private JLabel problemLabel;
	private JButton prev;
	private JButton next;
	private JTextField grades;
	private JButton record;
	private JLabel gradesLabe;
	private JComboBox<String> usernameList;
	private JComboBox<String> assignmentList;
	private JComboBox<String> problemList;
	private JButton load;
	private JLabel StatusLabel;
	// JFormDesigner - End of variables declaration //GEN-END:variables
}
