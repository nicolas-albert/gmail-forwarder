package com.github.nicolasalbert.gmailforwarder;

import java.io.IOException;
import java.util.Date;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sun.mail.imap.IMAPFolder;

import jakarta.mail.Address;
import jakarta.mail.Folder;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Store;
import jakarta.mail.Transport;
import jakarta.mail.event.MessageCountEvent;
import jakarta.mail.event.MessageCountListener;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMessage.RecipientType;

public class App {
	Session session;
	InternetAddress addr[];

	String username;
	String password;
	String folder;

	Matcher sender;
	Matcher subject;

	public App() throws Exception {
		log("Initializing...");

		username = System.getenv("USERNAME");
		if (username == null || username.isBlank()) {
			throw new Exception("USERNAME not defined");
		}
		log("User used: ", username);

		password = System.getenv("PASSWORD");
		if (password == null || password.isBlank()) {
			throw new Exception("PASSWORD not defined");
		}

		String to = System.getenv("TO");
		if (to == null || to.isBlank()) {
			throw new Exception("TO not defined");
		}

		addr = InternetAddress.parse(to);
		log("Forward to: ", to);

		folder = System.getenv("FOLDER");
		if (folder == null || folder.isBlank()) {
			folder = "Inbox";
		}

		log("Use folder: ", folder);

		String t_sender = System.getenv("SENDER");
		if (t_sender != null && !t_sender.isBlank()) {
			sender = Pattern.compile(t_sender).matcher("");
			log("Forward if sender matches: ", t_sender);
		}

		String t_subject = System.getenv("SUBJECT");
		if (t_subject != null && !t_subject.isBlank()) {
			subject = Pattern.compile(t_subject).matcher("");
			log("Forward if subject matches: ", t_subject);
		}

		Properties props = System.getProperties();
		props.setProperty("mail.store.protocol", "gimap");
		props.setProperty("mail.smtp.starttls.enable", "true");
		props.setProperty("mail.smtp.host", "smtp.gmail.com");
		props.setProperty("mail.smtp.port", "587");
		props.setProperty("mail.smtp.auth", "true");

		session = Session.getDefaultInstance(props, null);
	}

	private void log(String... message) {
		StringBuilder sb = new StringBuilder();
		sb.append("[").append(new Date().toString()).append("] ");
		for (String m: message) {
			sb.append(m);
		}
		System.out.println(sb.toString());
	}

	private void forward(Message message) throws Exception {
		try (Transport t = session.getTransport("smtp")) {
			t.connect(username, password);
			Message copy = new MimeMessage((MimeMessage) message);
			copy.setRecipients(RecipientType.TO, addr);
			t.sendMessage(copy, addr);
		}
	}

	public void run() throws MessagingException, IOException {
		try (Store store = session.getStore("gimap")) {
			store.connect(username, password);
			try (IMAPFolder folder = (IMAPFolder) store.getFolder(this.folder)) {
				folder.open(Folder.READ_WRITE);
				folder.addMessageCountListener(new MessageCountListener() {

					@Override
					public void messagesRemoved(MessageCountEvent e) {
					}

					@Override
					public void messagesAdded(MessageCountEvent e) {
						for (Message m: e.getMessages()) {
							try {
								Address[] froms = m.getFrom();
								String from = froms != null && froms.length > 0 ? froms[0].toString() : "";
								if (sender != null && !sender.reset(from).find()) {
									log("Message skip, sender mismatch: ", from);
									continue;
								}
								if (subject != null && !subject.reset(m.getSubject()).find()) {
									log("Message skip, subject mismatch: ", m.getSubject());
									continue;
								}
								log("Message is forwarded: <", from, "> ", m.getSubject());
								forward(m);
							} catch (Exception e1) {
								e1.printStackTrace();
							}
						}
					}
				});
				for (;;) {
					log("Waiting for event...");
					folder.idle();
				}
			}
		}
	}

	public static void main(String[] args) throws Exception {
		App app = new App();
		for (;;) {
			try {
				app.run();
			} catch (Exception e) {
				e.printStackTrace();
				app.log("Exception catched, listening again");
			}
		}
	}
}
