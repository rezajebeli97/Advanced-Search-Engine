import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;

public class UserInterface extends JFrame {

	public UserInterface() {
		setSize(680, 600);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setTitle("موتور جستجوی گل‌جفلی");
		setLayout(null);
		setResizable(false);

		JLabel text = new JLabel("query:");
		text.setFont(new Font("font", text.getFont().getStyle(), 13));
		text.setLocation(20, 20);
		text.setSize(400, 25);
		getContentPane().add(text);

		JTextField queryField = new JTextField();
		queryField.setFont(new Font("tahoma", queryField.getFont().getStyle(), 12));
		queryField.setLocation(20, 45);
		queryField.setSize(400, 27);
		getContentPane().add(queryField);

		JTextArea textArea = new JTextArea();
		textArea.setFont(new Font("font", queryField.getFont().getStyle(), 11));
		textArea.setLocation(10, 100);
		textArea.setSize(660, 350);
		textArea.setEditable(false);
		textArea.setBorder(BorderFactory.createEtchedBorder());
		textArea.setLineWrap(true);
		getContentPane().add(textArea);

		JScrollPane scroll = new JScrollPane(textArea);
		scroll.setBounds(10, 100, 660, 350);
		scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		getContentPane().add(scroll);

		JButton browseButton = new JButton("Search");
		browseButton.setLocation(445, 45);
		browseButton.setSize(100, 27);
		getContentPane().add(browseButton);
		browseButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println(queryField.getText());
				PostingList postingList = array.search(queryField.getText());
				if (postingList == null) {
					textArea.setText("not found!\nArticles:\n");
				} else {
					textArea.setText(postingList.articles.size() + " articles found!\nArticles:\n");
					for (Article article : postingList.articles) {
						textArea.setText(textArea.getText() + "[" + article.articleNumber + "]: ");
						for (int index : article.positions) {
							textArea.setText(textArea.getText() + index + " ");
						}
						textArea.setText(textArea.getText() + "\n");
					}
				}
			}
		});

		setVisible(true);
	}
	
	static Array array = new Array();

	public static void main(String[] args) {
		array.build(new File("News/IR-F19-Project01-Input.xls"), new File("News/stopWords.txt"), new File("News/hamsanSaz.txt"), new File("News/tarkibi_porkarbord.txt"));
		new UserInterface();
	}
}
