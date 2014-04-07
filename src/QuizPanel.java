import java.awt.AWTException;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;


public class QuizPanel extends JPanel {
	private static final long serialVersionUID = 7539756518913774434L;
	//private JPanel[] fieldPanels;
	private JLabel[] fieldLabels;
	private JTextField[] answerFields;
	JPanel fieldScrollPanel;
	JTextArea questionArea;
	JButton submitBtn, skipBtn;

	private boolean solved, attempted, skipped;
	private int fieldCount, solution;
	private String question;
	private String[] fieldNames;
	private String[][] answers;
	
	private static final String[] setting_keys = {
		"correct_max_time",
		"correct_min_time",
		"wrong_max_time",
		"wrong_min_time",
		"skip_max_time",
		"skip_min_time"
	};
	
	private static final String[] setting_vals  = {
		"600",
		"200",
		"400",
		"100",
		"300",
		"60"
	};
	
	public QuizPanel(int numFields, String[] fieldNames, String[][] answerTable) {
		setUpTrayIcon();
		/*System.out.println("Making a Quiz Panel with args");
		System.out.printf("Number of Fields: %d\nField Names: ", numFields);
		for (int i=1; i<fieldNames.length; i++)
			System.out.printf("%s, ", fieldNames[i-1]);
		System.out.printf("%s\nAnswer Table: ", fieldNames[fieldNames.length-1]);
		for (int i=0; i<answerTable.length; i++) {
			for (int j=1; j<numFields; j++) {
				System.out.printf("%s, ", answerTable[i][j-1]);
			}
			System.out.printf("%s\n              ", answerTable[i][numFields-1]);
		}
		System.out.println();*/
		this.fieldCount = numFields;
		this.fieldNames = fieldNames;
		this.answers = answerTable;
		setLayout(null);

		JPanel panel = new JPanel();
		panel.setBounds(15, 5, 345, 54);
		add(panel);

		questionArea = new JTextArea();
		questionArea.setEditable(false);
		questionArea.setFont(new Font("Monospaced", Font.BOLD, 14));
		questionArea.setBackground(UIManager.getColor("Panel.background"));
		questionArea.setColumns(40);
		questionArea.setWrapStyleWord(true);
		questionArea.setLineWrap(true);
		questionArea.setRows(2);
		questionArea.setText(question);
		panel.add(questionArea);

		JPanel responsePanel = new JPanel();
		responsePanel.setBounds(15, 70, 375, 134);
		add(responsePanel);

		fieldScrollPanel = new JPanel();
		responsePanel.add(fieldScrollPanel);
		fieldScrollPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		fieldScrollPanel.setPreferredSize(new Dimension(responsePanel.getWidth(), fieldCount*40));

		skipBtn = new JButton("Skip");
		skipBtn.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent evt) {
				solved = false;
				attempted = false;
				skipped = true;
			}
		});
		skipBtn.setFont(new Font("Monospaced", Font.PLAIN, 14));
		skipBtn.setBounds(15, 215, 121, 23);
		add(skipBtn);

		submitBtn = new JButton("Submit");
		submitBtn.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent evt) {
				checkAnswerFields();
			}
		});
		submitBtn.setFont(new Font("Monospaced", Font.PLAIN, 14));
		submitBtn.setBounds(277, 215, 113, 23);
		add(submitBtn);

		JLabel killLabel = new JLabel("KILL");
		killLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
		killLabel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent evt) {
				System.exit(0);
			}
		});
		killLabel.setFont(new Font("Monospaced", Font.BOLD, 12));
		killLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		killLabel.setForeground(Color.RED);
		killLabel.setOpaque(true);
		killLabel.setBackground(Color.BLACK);
		killLabel.setBounds(370, 0, 30, 14);
		add(killLabel);

	}
	
	public void checkAnswerFields() {
		solved = true;
		attempted = true;
		for (int i=0; i<fieldCount; i++) {
			answerFields[i].setEditable(false);
			String ans = answerFields[i].getText();
			boolean none = ans.equals("") && answers[solution][i].equalsIgnoreCase("none");
			if (none || ans.equalsIgnoreCase(answers[solution][i])) {
				answerFields[i].setForeground(Color.GREEN);
			} else {
				answerFields[i].setText(answers[solution][i]);
				answerFields[i].setForeground(Color.RED);
				solved = false;
			}
		}
		if (solved) {
			questionArea.setForeground(Color.GREEN);
		} else {
			questionArea.setForeground(Color.RED);
		}
	}
	
	public static Properties checkSettings(Properties props) {
		for (String s:setting_keys) {
			if (!props.containsKey(s)) {
				return createDefaultSettingFile();
			}
		}
		return props;
	}

	public boolean isSolved() {
		return solved;
	}

	public boolean isAttempted() {
		return attempted;
	}

	public boolean isSkipped() {
		return skipped;
	}

	public void prepareNewPrompt() {
		fieldScrollPanel.removeAll();
		JPanel fieldPanel;

		skipped = false;
		attempted = false;
		solved = false;
		
		solution = (int)((1-Math.random())*answers.length);
		questionArea.setText("What is the rest of the information associated with "+fieldNames[1]+" : "+ answers[solution][1]);
		questionArea.setForeground(Color.BLACK);
		
		fieldLabels = new JLabel[fieldCount];
		answerFields = new JTextField[fieldCount];
		for (int i=0; i<fieldCount; i++) {
			fieldPanel = new JPanel();
			fieldScrollPanel.add(fieldPanel);
			fieldLabels[i] = new JLabel(fieldNames[i]);
			fieldLabels[i].setFont(new Font("Monospaced", Font.PLAIN, 12));
			fieldPanel.add(fieldLabels[i]);

			answerFields[i] = new JTextField();
			answerFields[i].setFont(new Font("Monospaced", Font.PLAIN, 12));
			answerFields[i].setColumns(20);
			answerFields[i].addFocusListener(new FocusAdapter() {
				@Override
				public void focusGained(FocusEvent evt) {
					focusField(evt.getComponent());
				}
			});
			answerFields[i].addKeyListener(new KeyAdapter() {
				@Override
				public void keyPressed(KeyEvent evt) {
					if (evt.getKeyCode() == KeyEvent.VK_UP)
						focusPreviousField(evt.getComponent());
					else if (evt.getKeyCode() == KeyEvent.VK_DOWN)
						focusNextField(evt.getComponent());
					else if (evt.getKeyCode() == KeyEvent.VK_ENTER)
						checkAnswerFields();
				}
			});
			fieldPanel.add(answerFields[i]);
			if (i == 1) {
				answerFields[1].setText(answers[solution][i]);
				answerFields[i].setEditable(false);
				answerFields[i].setFocusable(false);
			}
		}

		repaint();
	}

	public void focusPreviousField(Component current) {
		for (int i=0; i<answerFields.length; i++) {
			Component c = answerFields[i];
			if (c.equals(current)) {
				if (i == 0) return;
				c = answerFields[i-1];
				if (!c.isFocusable()) focusPreviousField(c);
				Component o = c.getParent();
				fieldScrollPanel.setLocation(fieldScrollPanel.getX(), -o.getY() + 70);
				c.requestFocus();
				repaint();
			}
		}
	}

	public void focusNextField(Component current) {
		for (int i=0; i<answerFields.length; i++) {
			Component c = answerFields[i];
			if (c.equals(current)) {
				if (i == answerFields.length-1) return;
				c = answerFields[i+1];
				if (!c.isFocusable()) focusNextField(c);
				Component o = c.getParent();
				fieldScrollPanel.setLocation(fieldScrollPanel.getX(), -o.getY() + 70);
				c.requestFocus();
				repaint();
			}
		}
	}

	public void focusField(Component current) {
		for (int i=0; i<answerFields.length; i++) {
			Component c = answerFields[i];
			if (c.equals(current)) {
				Component o = c.getParent();
				fieldScrollPanel.setLocation(fieldScrollPanel.getX(), -o.getY() + 70);
				repaint();
			}
		}
	}

	public static Properties createDefaultSettingFile() {
		Properties p = new Properties();
		for (int i=0; i<setting_keys.length; i++) {
			p.put(setting_keys[i], setting_vals[i]);
		}
		return p;
	}

	public void setUpTrayIcon() {
		if (!SystemTray.isSupported()) {
            System.out.println("SystemTray is not supported");
            return;
        }
        final PopupMenu popup = new PopupMenu();
        final TrayIcon trayIcon =
                new TrayIcon((new ImageIcon("bulb.gif", "tray icon")).getImage());
        final SystemTray tray = SystemTray.getSystemTray();
         
        // Create a popup menu components
        MenuItem exitItem = new MenuItem("Kill Program");
         
        //Add components to popup menu
        popup.add(exitItem);
         
        trayIcon.setPopupMenu(popup);
         
        try {
            tray.add(trayIcon);
        } catch (AWTException e) {
            System.out.println("TrayIcon could not be added.");
            return;
        }
        
        exitItem.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent evt) {
        		System.exit(0);
        	}
        });
	}
	
	public static void main(String[] args) {
		JFrame frame = new JFrame();
		Dimension framesize = new Dimension(400, 250);
		frame.setMinimumSize(framesize);
		frame.setAlwaysOnTop(true);
		frame.setResizable(false);
		frame.setUndecorated(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		int fieldCount;
		String[] fieldNames;
		ArrayList<String[]> fieldValues = new ArrayList<String[]>();

		try {
			File settingFile = new File("quiz_settings.txt");
			if (!settingFile.exists()) {
				settingFile.createNewFile();
				FileWriter out = new FileWriter(settingFile);
				createDefaultSettingFile().store(out, null);
				out.close();
			}
			
			Properties settings = new Properties();
			settings.load(new InputStreamReader(new FileInputStream(settingFile)));
			settings = checkSettings(settings);
			
			File f = new File("fieldtable.txt");
			if (!f.exists()) {
				f.createNewFile();
				System.exit(1);
			}
			BufferedReader in = new BufferedReader(new FileReader(f));
			fieldCount = Integer.parseInt(in.readLine());
			fieldNames = in.readLine().split(";");
			String line;
			while ((line = in.readLine()) != null) {
				fieldValues.add(line.split(";"));
			}
			in.close();

			String[][] ansTbl = fieldValues.toArray(new String[fieldValues.size()][fieldCount]);

			QuizPanel qp = new QuizPanel(fieldCount, fieldNames, ansTbl);
			frame.getContentPane().add(qp);
			qp.prepareNewPrompt();
			frame.pack();
			frame.setVisible(true);

			long correct_max = Long.parseLong(settings.getProperty("correct_max_time"))*1000;
			long correct_min = Long.parseLong(settings.getProperty("correct_min_time"))*1000;
			long wrong_max = Long.parseLong(settings.getProperty("wrong_max_time"))*1000;
			long wrong_min = Long.parseLong(settings.getProperty("wrong_min_time"))*1000;
			long skip_max = Long.parseLong(settings.getProperty("skip_max_time"))*1000;
			long skip_min = Long.parseLong(settings.getProperty("skip_min_time"))*1000;
			while(true) {
				if (qp.attempted || qp.skipped) {
					long time;
					if (qp.solved) {
						Thread.sleep(750);
						time = (long)(Math.random()*(correct_max-correct_min) + correct_min);
					} else if (qp.attempted) {
						Thread.sleep(3000);
						time = (long)(Math.random()*(wrong_max-wrong_min) + wrong_min);
					} else if (qp.skipped) {
						time = (long)(Math.random()*(skip_max-skip_min) + skip_min);
					} else {
						//Wat?
						time = 0;
						System.exit(1);
					}
					frame.setVisible(false);
					Thread.sleep(time);
					qp.prepareNewPrompt();
					frame.setVisible(true);
					frame.pack();
				}
				Thread.sleep(500);
			}

		} catch(FileNotFoundException e) {
			e.printStackTrace();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}