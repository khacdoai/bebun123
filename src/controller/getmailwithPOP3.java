package controller;

import model.Thonngtindulieumodel;
import model.ThongtinDuLieu;
import java.io.IOException;

import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import javax.mail.Authenticator;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.InternetAddress;
import javax.swing.table.DefaultTableModel;

public class getmailwithPOP3 {

    public final DefaultTableModel tableModel = new DefaultTableModel();
    Thonngtindulieumodel thongtindulieu;
    List<ThongtinDuLieu> Data = new LinkedList<>();

    public Thonngtindulieumodel getmailwithPOP3(String user, String password) throws NoSuchProviderException, MessagingException, IOException {
        Properties pro = System.getProperties();
        pro.put("mail.pop3.host", "pop.gmail.com");
        pro.put("mail.pop3.port", "995");
        pro.put("mail.store.protocol", "pop3");
        pro.put("mail.pop3.socketFactory.class", javax.net.ssl.SSLSocketFactory.class.getName());
        Session session = Session.getDefaultInstance(pro, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(user, password);
            }
        });

        Store store = session.getStore();
        store.connect();
        Folder[] folders = store.getDefaultFolder().list("*");
        for (Folder folder : folders) {
            if (!folder.isOpen()) {
                folder.open(Folder.READ_ONLY);
            }

            Message[] messages = folder.getMessages(); 
            for (Message message : messages) {
                String from = "";
                InternetAddress[] addresses = (InternetAddress[]) message.getFrom();
                String contentType = message.getContentType();
                String messageContent = "";
                if (contentType.contains("text/plain")
                        || contentType.contains("text/html")) {
                    try {
                        Object content = message.getContent();
                        if (content != null) {
                            messageContent = content.toString();
                        }
                    } catch (Exception ex) {
                        messageContent = "[Error downloading content]";
                        ex.printStackTrace();
                    }
                }
                for (InternetAddress address : addresses) {
                    from += address.getAddress();
                }
                String[] headers = {"Tiêu Đề", "Người Gửi", "Thời Gian"};
                Data.add(new ThongtinDuLieu(message.getSubject(), message.getSentDate().toString(), from));
                System.out.println("" + messageContent.toString() + "");
                thongtindulieu = new Thonngtindulieumodel(headers, Data);
            }
        }
        return thongtindulieu;
    }
}

