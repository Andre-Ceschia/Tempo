package tempo;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Scanner;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.search.FlagTerm;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

//check function returns string of email text then delete the email
//if string does not contain youtbe link switch statemnt if it is one of the commands(trim and tolower), if command perform command ie. set the play value to to false or somethng else
//diffetrent threads 
//first thread while loop checks emails and returns them to an arraylist of links
//second thread finds that array and donwload videos and saves the names of files to diffetnty arraylist
//third thread plays the asounds throuhg virtual audio cable


public class Tempo {
	volatile static ArrayList<String> downloadQueue = new ArrayList<String>();
	volatile static ArrayList<String> playQueue = new ArrayList<String>();
	volatile static boolean skip = false;
	volatile static boolean pause = false;
	volatile static boolean play = false;
	
	public static ArrayList<String> check() throws MessagingException {
		String email = "EMAIL HERE";
		String pass = "PASSWORD HERE";
		ArrayList <String> messagesText = new ArrayList<String>();
		FlagTerm unseen = new FlagTerm(new Flags(Flags.Flag.SEEN), false);
		
		//get session, then the palce that sotres it with imap, then connects and logs in
		Session session = Session.getDefaultInstance(new Properties());
		Store store = session.getStore("imaps");
		store.connect("imap.gmail.com", email, pass);
		
		//gets the inbox and opens, adds unsees messages to array
		Folder inbox = store.getFolder("INBOX");
		inbox.open(Folder.READ_WRITE);
		Message[] messages = inbox.search(unseen);
		
		for (Message message: messages) {
			if (message.getSubject().trim().toLowerCase().contains("help")) {
				String finResp;
				//if contains  " " then finemail = without parsed
				//email back with commands
				Address[] address = message.getFrom();
				if(address[0].toString().contains(" ")) {
					String[] emailBreak = address[0].toString().split("<");
					String finEmail[] = emailBreak[1].split(">");
					finResp = finEmail[0];
				}else {
					finResp = address[0].toString();
				}
				
				Properties smtpProperties = new Properties();
				smtpProperties.put("mail.smtp.auth", "true");
				smtpProperties.put("mail.smtp.starttls.enable", "true");
				smtpProperties.put("mail.smtp.host", "smtp.gmail.com");
				smtpProperties.put("mail.smtp.port", "587");
				
				Session smtpSession = Session.getInstance(smtpProperties, new Authenticator() {
					protected PasswordAuthentication getPasswordAuthentication() {
						return new PasswordAuthentication(email, pass);
					}
				});
				
				Message emailToSend = new MimeMessage(smtpSession);
				emailToSend.setFrom(new InternetAddress(email));
				emailToSend.setRecipient(Message.RecipientType.TO, new InternetAddress(finResp));
				emailToSend.setSubject("Tempo Bot Help");
				emailToSend.setText("Help Options\n\nTo execute a command you must send it as the subject of the email.\n\nYoutube Link- Send Youtube link to play song through Tempo.\n\nSkip - Skip current song and starts the next song in the queue.\n\nPause - Pauses current song.\n\nPlay - Resumes current song if song is paused.\n\n Help - Displays this menu.");
				
				Transport.send(emailToSend);
				System.out.println("Help sent to " + finResp);
				continue;
			}else {
				messagesText.add(message.getSubject());
			}
		}
		//marks emails as seen
		inbox.setFlags(messages, new Flags(Flags.Flag.SEEN), true);
		inbox.close();
		store.close();
		return messagesText;
		
	}
	
	public static void main(String[] args) throws MessagingException, InterruptedException, IOException {
		
		//checks if youtube-dl.exe is out of date, if its out of date automatically updates
		String latestVersion = null;
		String currentVersion = null;
		
		Document doc = Jsoup.connect("http://ytdl-org.github.io/youtube-dl/download.html").get();
		Elements h2 = doc.getElementsByTag("h2");
		
		for (Element a:h2) {
			latestVersion = a.getElementsByTag("a").first().text();
		}
		
		File file = new File("C:\\Users\\Owner\\Documents\\Code\\Java\\tempo\\dl-version.txt");
		@SuppressWarnings("resource")
		Scanner scan = new Scanner(file);
		currentVersion = scan.nextLine();
		
		if (!currentVersion.equals(latestVersion)) {
			File oldVersion = new File("C:\\Users\\Owner\\Documents\\Code\\Java\\tempo\\youtube-dl.exe");
			oldVersion.delete();
			
			Runtime.getRuntime().exec("cmd /c cd C:\\Users\\Owner\\Documents\\Code\\Java\\tempo && curl -L https://yt-dl.org/downloads/" + latestVersion + "/youtube-dl.exe --output youtube-dl.exe");
			while (!oldVersion.exists()) {
				continue;
			}
			FileWriter fileW = new FileWriter("C:\\Users\\Owner\\Documents\\Code\\Java\\tempo\\dl-version.txt");
			fileW.write(latestVersion);
			fileW.close();
			
		}
		
		//anothere thread here to while loop through file locations and plays sound files in virtual audio cable
		
		//starts thread that downlaods videos 
		Download downloadThread = new Download();
		downloadThread.start();
		
		Play playThread = new Play();
		playThread.start();
		
		System.out.println("Scanning for emails....");
		//parses thorugh what is returnd from checlk, fixed youtube link if it has to
		while(true) {
			Thread.sleep(2000);
			ArrayList<String> messages = check();
			if (messages != null) {
				for (String message: messages) {
					if(message.contains("https://www.youtube.com/")) {
						downloadQueue.add(message);
					}else if(message.contains("https://youtu.be/")) {
						String[] link = message.split("https://youtu.be/");
						downloadQueue.add("https://www.youtube.com/watch?v=" + link[1]);
						
					} else {
						String lowerMessage = message.trim().toLowerCase();
						switch(lowerMessage) {
						case("skip"):
							System.out.println("Skipping Song");
							skip = true;
							break;
						case("pause"):
							System.out.println("Pausing Song");
							pause = true;
							break;
						case("play"):
							System.out.println("Playing Song");
							play = true;
							break;
						default:
							continue;
						}
					}
				}
			}
		}
	}
}

class Download extends Thread{
	public void run() {		
		while(true) {
			//checks if array with downloa dlinks is empty if not then gets  first instnce and saves to vbariable
			if(Tempo.downloadQueue.isEmpty()) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				continue;
			}else {
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				int filename = 1;
				boolean check = true;
				String link = Tempo.downloadQueue.get(0);
				System.out.println("Downloading " + link);
				//checks the first filename that is free so i dont overwrite
				while(check) {
					File file = new File("C:\\Users\\Owner\\Documents\\Code\\Java\\tempo\\mp3\\" + filename + ".mp3");
					if(file.exists()) {
						filename++;
						continue;
					}else {
						check = false;
					}
				}
				String filePath = "C:\\Users\\Owner\\Documents\\Code\\Java\\tempo\\mp3\\" + filename + ".mp3";
				//sends commadn to donwlao dvideo link and save it with right filename when i want to play it back
				try {
					Thread.sleep(1000);
					Process proc = Runtime.getRuntime().exec("cmd /c cd C:\\Users\\Owner\\Documents\\Code\\Java\\tempo && youtube-dl.exe " + link + " -o " + filePath);
					//prints cmd results
					
					BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));
					String s = null;
					while ((s = stdInput.readLine()) != null) {
					    System.out.println(s);
					}
				
					
					File file = new File("C:\\Users\\Owner\\Documents\\Code\\Java\\tempo\\mp3\\" + filename + ".mp3");
					while(!file.exists()) {
						continue;
					}
					//converts mp3 to wav format
					Runtime.getRuntime().exec("cmd /c cd C:\\Users\\Owner\\Documents\\Code\\Java\\tempo\\ffmpeg && ffmpeg -i " + filePath + " C:\\Users\\Owner\\Documents\\Code\\Java\\tempo\\mp3\\" + filename + ".wav");
				
					
					File fileConvert = new File("C:\\Users\\Owner\\Documents\\Code\\Java\\tempo\\mp3\\" + filename + ".wav");
					while(!fileConvert.exists()) {
						continue;
					}
					Thread.sleep(1000);
					Tempo.downloadQueue.remove(0);
					Tempo.playQueue.add("C:\\Users\\Owner\\Documents\\Code\\Java\\tempo\\mp3\\" + filename + ".wav");
					continue;
				} catch (IOException | InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
}

class Play extends Thread{
	public void run() {
		while (true) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			if (Tempo.playQueue.isEmpty()) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}else {
				String playPath = Tempo.playQueue.get(0);
				
				try {
					
					boolean isPaused = false;
					AudioInputStream audioStream = AudioSystem.getAudioInputStream(new File(playPath));
					Clip clip = AudioSystem.getClip();
					clip.open(audioStream);
					clip.start();
					
					//checks global volatile variables if it needs to perform a command
					Thread.sleep(750);
					while(clip.isOpen()) {
						if(Tempo.skip == true) {
							clip.stop();
							Tempo.skip = false;
							break;
						}else if(Tempo.pause == true) {
							clip.stop();
							Tempo.pause = false;
							isPaused = true;
							continue;
						}else if(Tempo.play == true) {
							if(isPaused) {
								clip.start();
								Tempo.play = false;
							}else 
								continue;
						}else if(clip.getMicrosecondPosition() == clip.getMicrosecondLength()) {
							clip.stop();
							break;
						}
					}
					Tempo.playQueue.remove(0);
					audioStream.close();
					clip.close();
					continue;
					
				} catch (UnsupportedAudioFileException | IOException | LineUnavailableException | InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
}