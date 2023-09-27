/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package view;

import com.sun.mail.util.BASE64DecoderStream;
import java.awt.HeadlessException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Properties;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.NoSuchProviderException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author nO
 */
public class HomeJFrame extends javax.swing.JFrame {

    /**
     * Creates new form HomeJFrame
     */
    public HomeJFrame() {
        initComponents();
        pb_cc_bcc.setVisible(false);
    }
    public DefaultTableModel tableModel1 = new DefaultTableModel();

    private String userName;
    private String password;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String user) {
        userName = user;
    }

    public String getpassword() {
        return password;
    }

    public void setpassword(String pass) {
        this.password = pass;
    }

    //Hộp thư đến 
    public void ReadMail(int n) throws IOException {
        Properties props = new Properties();
        props.setProperty("mail.store.protocol", "imaps");
        try {
            Session session = Session.getInstance(props, null);
            Store store = session.getStore();
            store.connect("imap.gmail.com", userName, password);
            Folder folderInbox = store.getFolder("INBOX");
            folderInbox.open(Folder.READ_ONLY);
            Message[] messages = folderInbox.getMessages();
            Message msg = messages[n];
            Address[] fromAddress = msg.getFrom();
            String contentType = msg.getContentType();
            String messageContent = "";
            if (contentType.contains("multipart")) {
                Multipart multiPart = (Multipart) msg.getContent();
                int numberOfParts = multiPart.getCount();
                for (int partCount = 0; partCount < numberOfParts; partCount++) {
                    MimeBodyPart part = (MimeBodyPart) multiPart.getBodyPart(partCount);
                    messageContent = part.getContent().toString();
                }
            } else if (contentType.contains("multipart") || contentType.contains("text/html")) {
                try {
                    BASE64DecoderStream content = (BASE64DecoderStream) msg.getContent();
                    if (content != null) {
                        messageContent = content.toString();
                    }
                } catch (Exception ex) {
                    messageContent = "[Không thể tải nội dung]";
                    ex.printStackTrace();
                }
            }
            System.out.println("Nội dung: " + messageContent);
            ePnlNoiDung.setContentType("text/html");
            ePnlNoiDung.setText(messageContent);
            folderInbox.close(false);
            store.close();
        } catch (NoSuchProviderException ex) {
            System.out.println("Lỗi 1: " + ex.getMessage());
            ex.printStackTrace();
        } catch (MessagingException ex) {
            System.out.println("Lỗi 2: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public static void sendEmail(Properties smtpProperties, String To_1, String To_2, String To_3,
            String Subject, String Content, String attachment_path)
            throws AddressException, MessagingException, IOException {

        final String user_mail = smtpProperties.getProperty("mail.user");
        final String password = smtpProperties.getProperty("mail.password"); 
        Authenticator auth = new Authenticator() {
            public PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(user_mail, password);
            }
        }; 

        Session session = Session.getInstance(smtpProperties, auth);

        Message message = new MimeMessage(session); 
        message.setFrom(new InternetAddress(user_mail)); 
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(To_1, false));
        if(To_2!= null && To_2.length() > 0) {
            message.setRecipients(Message.RecipientType.CC, InternetAddress.parse(To_2, false));
        }
        if(To_3!= null && To_3.length() > 0) {
           message.setRecipients(Message.RecipientType.BCC, InternetAddress.parse(To_3, false));
        } 
        message.setSubject(Subject);
        message.setSentDate(new Date());

        // Phan 1 gom doan tin nhan
        BodyPart messageBodyPart1 = new MimeBodyPart();
        messageBodyPart1.setText(Content);

        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(messageBodyPart1);

        // phan 2 chua tap tin
        if (attachment_path != null && attachment_path.length() > 0) {
            MimeBodyPart messageBodyPart2 = new MimeBodyPart(); // chèn vào body
            messageBodyPart2.setContent(message, "text/html; charset=uft-8");
            DataSource source = new FileDataSource(attachment_path);
            messageBodyPart2.setDataHandler(new DataHandler(source));
            messageBodyPart2.setFileName(attachment_path);
            multipart.addBodyPart(messageBodyPart2);
        }

        message.setContent(multipart);

        Transport.send(message);
        JOptionPane.showMessageDialog(null, "Gui mail thanh cong");
    }

    private boolean validateFields() {
        if (txt_to1.getText().equals("")) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng nhập địa chỉ nhận!",
                    "Thông báo", JOptionPane.ERROR_MESSAGE);
            txt_to1.requestFocus();
            return false;
        }

        if (txt_Subject.getText().equals("")) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng nhập chủ đề email!",
                    "Thông báo", JOptionPane.ERROR_MESSAGE);
            txt_Subject.requestFocus();
            return false;
        }

        if (txt_content.getText().equals("")) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng nhập nội dung email!",
                    "Error", JOptionPane.ERROR_MESSAGE);
            txt_content.requestFocus();
            return false;
        }

        return true;
    }
    private File configFile = new File("smtp.properties");
    private Properties configProps;

    public Properties loadProperties() throws IOException {
        Properties defaultProps = new Properties();
        // sets default properties
        defaultProps.setProperty("mail.smtp.host", "smtp.gmail.com");
        defaultProps.setProperty("mail.smtp.port", "587");
        defaultProps.setProperty("mail.user", getUserName());
        defaultProps.setProperty("mail.password", getpassword());
        defaultProps.setProperty("mail.smtp.starttls.enable", "true");
        defaultProps.setProperty("mail.smtp.auth", "true");

        configProps = new Properties(defaultProps);

        // loads properties from file
        if (configFile.exists()) {
            InputStream inputStream = new FileInputStream(configFile);
            configProps.load(inputStream);
            inputStream.close();
        }

        return configProps;
    }

    public HomeJFrame(String user, String pass) throws MessagingException, NoSuchProviderException, IOException {
        initComponents();
        System.out.println("user: " + user);
        System.out.println("pass: " + pass);
        lblUser.setText(user);
        controller.getmailwithPOP3 gm = new controller.getmailwithPOP3();
        jTable1.setModel(gm.getmailwithPOP3(user, pass));
        pnView.setVisible(true);
        pnlSoanThu.setVisible(false);
        pnlNoiDung.setVisible(false);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        btnSoanThu = new com.k33ptoo.components.KButton();
        btnHopThuDen = new com.k33ptoo.components.KButton();
        btnGopY = new com.k33ptoo.components.KButton();
        btnThoat = new com.k33ptoo.components.KButton();
        btnDangXuat = new com.k33ptoo.components.KButton();
        pnView = new com.k33ptoo.components.KGradientPanel();
        pnlNoiDung = new javax.swing.JPanel();
        jLabel16 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        txtTieuDe = new javax.swing.JTextField();
        jLabel18 = new javax.swing.JLabel();
        txtNguoiGui = new javax.swing.JTextField();
        jScrollPane6 = new javax.swing.JScrollPane();
        ePnlNoiDung = new javax.swing.JEditorPane();
        btnChuyenTiep = new com.k33ptoo.components.KButton();
        btnTraLoi = new com.k33ptoo.components.KButton();
        pnLeft = new javax.swing.JPanel();
        pnlHopThuDen = new javax.swing.JPanel();
        jLabel10 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        pnlSoanThu = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        txt_to1 = new javax.swing.JTextField();
        jLabel20 = new javax.swing.JLabel();
        txt_Subject = new javax.swing.JTextField();
        jLabel21 = new javax.swing.JLabel();
        jScrollPane7 = new javax.swing.JScrollPane();
        txt_content = new javax.swing.JEditorPane();
        btn_sent = new com.k33ptoo.components.KButton();
        bt_add_file = new javax.swing.JButton();
        txt_attach = new javax.swing.JTextField();
        pb_cc_bcc = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        txt_to2 = new javax.swing.JTextPane();
        jLabel23 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        txt_to3 = new javax.swing.JTextPane();
        jLabel24 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        bt_hide_cc_bcc = new javax.swing.JButton();
        bt_cc_bcc = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        lblUser = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Trang Chủ");

        jPanel1.setPreferredSize(new java.awt.Dimension(1920, 1080));
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        btnSoanThu.setIcon(new javax.swing.ImageIcon(getClass().getResource("/view/view/image/create_gm_grey_24dp.png"))); // NOI18N
        btnSoanThu.setText("Soạn thư");
        btnSoanThu.setkBackGroundColor(new java.awt.Color(255, 255, 255));
        btnSoanThu.setkEndColor(new java.awt.Color(102, 0, 102));
        btnSoanThu.setkHoverEndColor(new java.awt.Color(0, 51, 153));
        btnSoanThu.setkHoverForeGround(new java.awt.Color(255, 51, 255));
        btnSoanThu.setkHoverStartColor(new java.awt.Color(204, 51, 255));
        btnSoanThu.setkPressedColor(new java.awt.Color(0, 51, 153));
        btnSoanThu.setkStartColor(new java.awt.Color(204, 0, 204));
        btnSoanThu.setName(""); // NOI18N
        btnSoanThu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSoanThuActionPerformed(evt);
            }
        });
        jPanel1.add(btnSoanThu, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 200, 130, 60));

        btnHopThuDen.setIcon(new javax.swing.ImageIcon(getClass().getResource("/view/view/image/inbox_fill_white_20dp.png"))); // NOI18N
        btnHopThuDen.setText("Hộp thư đến");
        btnHopThuDen.setkBackGroundColor(new java.awt.Color(255, 255, 255));
        btnHopThuDen.setkEndColor(new java.awt.Color(0, 0, 153));
        btnHopThuDen.setkHoverEndColor(new java.awt.Color(0, 51, 255));
        btnHopThuDen.setkHoverForeGround(new java.awt.Color(0, 204, 204));
        btnHopThuDen.setkHoverStartColor(new java.awt.Color(0, 51, 255));
        btnHopThuDen.setkPressedColor(new java.awt.Color(102, 102, 255));
        btnHopThuDen.setkStartColor(new java.awt.Color(0, 51, 153));
        btnHopThuDen.setName(""); // NOI18N
        btnHopThuDen.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnHopThuDenActionPerformed(evt);
            }
        });
        jPanel1.add(btnHopThuDen, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 390, 170, 50));

        btnGopY.setIcon(new javax.swing.ImageIcon(getClass().getResource("/view/view/image/chat_white_20dp.png"))); // NOI18N
        btnGopY.setText("Góp ý");
        btnGopY.setkBackGroundColor(new java.awt.Color(255, 255, 255));
        btnGopY.setkEndColor(new java.awt.Color(0, 0, 153));
        btnGopY.setkHoverEndColor(new java.awt.Color(0, 51, 255));
        btnGopY.setkHoverForeGround(new java.awt.Color(0, 204, 204));
        btnGopY.setkHoverStartColor(new java.awt.Color(0, 51, 255));
        btnGopY.setkPressedColor(new java.awt.Color(102, 102, 255));
        btnGopY.setkStartColor(new java.awt.Color(0, 51, 153));
        btnGopY.setName(""); // NOI18N
        btnGopY.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGopYActionPerformed(evt);
            }
        });
        jPanel1.add(btnGopY, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 460, 170, 40));

        btnThoat.setIcon(new javax.swing.ImageIcon(getClass().getResource("/view/view/image/label_important_white_20dp.png"))); // NOI18N
        btnThoat.setText("Thoát");
        btnThoat.setkBackGroundColor(new java.awt.Color(255, 255, 255));
        btnThoat.setkEndColor(new java.awt.Color(0, 0, 153));
        btnThoat.setkHoverEndColor(new java.awt.Color(255, 0, 0));
        btnThoat.setkHoverForeGround(new java.awt.Color(255, 255, 0));
        btnThoat.setkHoverStartColor(new java.awt.Color(255, 0, 0));
        btnThoat.setkPressedColor(new java.awt.Color(102, 102, 255));
        btnThoat.setkStartColor(new java.awt.Color(0, 51, 153));
        btnThoat.setName(""); // NOI18N
        btnThoat.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnThoatActionPerformed(evt);
            }
        });
        jPanel1.add(btnThoat, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 530, 170, 40));

        btnDangXuat.setText("Đằng Xuất");
        btnDangXuat.setkBackGroundColor(new java.awt.Color(255, 0, 0));
        btnDangXuat.setkEndColor(new java.awt.Color(255, 0, 0));
        btnDangXuat.setkHoverEndColor(new java.awt.Color(255, 0, 0));
        btnDangXuat.setkHoverForeGround(new java.awt.Color(153, 0, 0));
        btnDangXuat.setkHoverStartColor(new java.awt.Color(153, 0, 0));
        btnDangXuat.setkStartColor(new java.awt.Color(255, 0, 0));
        btnDangXuat.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDangXuatActionPerformed(evt);
            }
        });
        jPanel1.add(btnDangXuat, new org.netbeans.lib.awtextra.AbsoluteConstraints(1120, 50, 80, 30));

        pnView.setkFillBackground(false);

        jLabel16.setFont(new java.awt.Font("Times New Roman", 1, 24)); // NOI18N
        jLabel16.setForeground(new java.awt.Color(204, 0, 153));
        jLabel16.setText("NỘI DUNG THƯ");

        jLabel17.setFont(new java.awt.Font("Tahoma", 0, 13)); // NOI18N
        jLabel17.setText("Tiêu đề");

        txtTieuDe.setFont(new java.awt.Font("Tahoma", 0, 13)); // NOI18N

        jLabel18.setFont(new java.awt.Font("Tahoma", 0, 13)); // NOI18N
        jLabel18.setText("Người gửi");

        txtNguoiGui.setFont(new java.awt.Font("Tahoma", 0, 13)); // NOI18N

        ePnlNoiDung.setFont(new java.awt.Font("Tahoma", 0, 13)); // NOI18N
        jScrollPane6.setViewportView(ePnlNoiDung);

        btnChuyenTiep.setIcon(new javax.swing.ImageIcon(getClass().getResource("/view/view/image/forward_baseline_nv700_20dp.png"))); // NOI18N
        btnChuyenTiep.setText("Chuyển tiếp");
        btnChuyenTiep.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnChuyenTiepActionPerformed(evt);
            }
        });

        btnTraLoi.setIcon(new javax.swing.ImageIcon(getClass().getResource("/view/view/image/reply_baseline_nv700_20dp.png"))); // NOI18N
        btnTraLoi.setText("Trả lời");
        btnTraLoi.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnTraLoiActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pnlNoiDungLayout = new javax.swing.GroupLayout(pnlNoiDung);
        pnlNoiDung.setLayout(pnlNoiDungLayout);
        pnlNoiDungLayout.setHorizontalGroup(
            pnlNoiDungLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlNoiDungLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel16)
                .addGap(74, 74, 74))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlNoiDungLayout.createSequentialGroup()
                .addGroup(pnlNoiDungLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(pnlNoiDungLayout.createSequentialGroup()
                        .addGroup(pnlNoiDungLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(pnlNoiDungLayout.createSequentialGroup()
                                .addGap(21, 21, 21)
                                .addComponent(jLabel17, javax.swing.GroupLayout.PREFERRED_SIZE, 69, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(pnlNoiDungLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(jLabel18, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnlNoiDungLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtNguoiGui)
                            .addComponent(txtTieuDe)))
                    .addGroup(pnlNoiDungLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jScrollPane6))
                    .addGroup(pnlNoiDungLayout.createSequentialGroup()
                        .addGap(47, 47, 47)
                        .addComponent(btnTraLoi, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 179, Short.MAX_VALUE)
                        .addComponent(btnChuyenTiep, javax.swing.GroupLayout.PREFERRED_SIZE, 122, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(23, 23, 23))
        );
        pnlNoiDungLayout.setVerticalGroup(
            pnlNoiDungLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlNoiDungLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel16)
                .addGap(13, 13, 13)
                .addGroup(pnlNoiDungLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel17)
                    .addComponent(txtTieuDe, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlNoiDungLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel18)
                    .addComponent(txtNguoiGui, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane6, javax.swing.GroupLayout.DEFAULT_SIZE, 412, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlNoiDungLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnChuyenTiep, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnTraLoi, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(23, 23, 23))
        );

        jLabel10.setFont(new java.awt.Font("Times New Roman", 1, 24)); // NOI18N
        jLabel10.setForeground(new java.awt.Color(255, 0, 51));
        jLabel10.setText("HỘP THƯ ĐẾN");

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jTable1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTable1MouseClicked(evt);
            }
        });
        jScrollPane2.setViewportView(jTable1);

        javax.swing.GroupLayout pnlHopThuDenLayout = new javax.swing.GroupLayout(pnlHopThuDen);
        pnlHopThuDen.setLayout(pnlHopThuDenLayout);
        pnlHopThuDenLayout.setHorizontalGroup(
            pnlHopThuDenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlHopThuDenLayout.createSequentialGroup()
                .addContainerGap(255, Short.MAX_VALUE)
                .addComponent(jLabel10)
                .addGap(244, 244, 244))
            .addGroup(pnlHopThuDenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(pnlHopThuDenLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 647, Short.MAX_VALUE)
                    .addContainerGap()))
        );
        pnlHopThuDenLayout.setVerticalGroup(
            pnlHopThuDenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlHopThuDenLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel10)
                .addContainerGap(571, Short.MAX_VALUE))
            .addGroup(pnlHopThuDenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlHopThuDenLayout.createSequentialGroup()
                    .addContainerGap(58, Short.MAX_VALUE)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 468, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(84, 84, 84)))
        );

        pnlSoanThu.setEnabled(false);

        jLabel5.setFont(new java.awt.Font("Times New Roman", 1, 24)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(0, 0, 204));
        jLabel5.setText("SOẠN THƯ");

        jLabel19.setFont(new java.awt.Font("Tahoma", 0, 13)); // NOI18N
        jLabel19.setText("Đến");

        txt_to1.setFont(new java.awt.Font("Tahoma", 0, 13)); // NOI18N
        txt_to1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txt_to1ActionPerformed(evt);
            }
        });

        jLabel20.setFont(new java.awt.Font("Tahoma", 0, 13)); // NOI18N
        jLabel20.setText("Tiêu đề");

        txt_Subject.setFont(new java.awt.Font("Tahoma", 0, 13)); // NOI18N
        txt_Subject.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txt_SubjectActionPerformed(evt);
            }
        });

        jLabel21.setFont(new java.awt.Font("Tahoma", 0, 13)); // NOI18N
        jLabel21.setText("Nội dung");

        txt_content.setFont(new java.awt.Font("Tahoma", 0, 13)); // NOI18N
        jScrollPane7.setViewportView(txt_content);

        btn_sent.setText("Gửi");
        btn_sent.setkHoverForeGround(new java.awt.Color(0, 204, 204));
        btn_sent.setkHoverStartColor(new java.awt.Color(0, 153, 102));
        btn_sent.setkSelectedColor(new java.awt.Color(0, 51, 153));
        btn_sent.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_sentActionPerformed(evt);
            }
        });

        bt_add_file.setIcon(new javax.swing.ImageIcon(getClass().getResource("/view/view/image/kemfile.png"))); // NOI18N
        bt_add_file.setText("Đính kềm tệp");
        bt_add_file.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bt_add_fileActionPerformed(evt);
            }
        });

        txt_attach.setText("Đường dẫn tệp");
        txt_attach.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                txt_attachMouseClicked(evt);
            }
        });
        txt_attach.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txt_attachActionPerformed(evt);
            }
        });

        pb_cc_bcc.setEnabled(false);

        jScrollPane1.setViewportView(txt_to2);

        jLabel23.setFont(new java.awt.Font("Tahoma", 0, 13)); // NOI18N
        jLabel23.setText("CC");

        jScrollPane3.setViewportView(txt_to3);

        jLabel24.setFont(new java.awt.Font("Tahoma", 0, 13)); // NOI18N
        jLabel24.setText("BCC");

        javax.swing.GroupLayout pb_cc_bccLayout = new javax.swing.GroupLayout(pb_cc_bcc);
        pb_cc_bcc.setLayout(pb_cc_bccLayout);
        pb_cc_bccLayout.setHorizontalGroup(
            pb_cc_bccLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pb_cc_bccLayout.createSequentialGroup()
                .addComponent(jLabel23)
                .addGap(32, 32, 32)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 273, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(20, 20, 20)
                .addComponent(jLabel24)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 262, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        pb_cc_bccLayout.setVerticalGroup(
            pb_cc_bccLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pb_cc_bccLayout.createSequentialGroup()
                .addGroup(pb_cc_bccLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel23)
                    .addComponent(jLabel24)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 3, Short.MAX_VALUE))
        );

        jPanel3.setEnabled(false);

        bt_hide_cc_bcc.setText("ẩn CC, BCC");
        bt_hide_cc_bcc.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bt_hide_cc_bccActionPerformed(evt);
            }
        });

        bt_cc_bcc.setText("thêm CC, BCC");
        bt_cc_bcc.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bt_cc_bccActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addComponent(bt_cc_bcc, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(bt_hide_cc_bcc, javax.swing.GroupLayout.PREFERRED_SIZE, 108, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(bt_hide_cc_bcc)
                    .addComponent(bt_cc_bcc))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout pnlSoanThuLayout = new javax.swing.GroupLayout(pnlSoanThu);
        pnlSoanThu.setLayout(pnlSoanThuLayout);
        pnlSoanThuLayout.setHorizontalGroup(
            pnlSoanThuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlSoanThuLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlSoanThuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlSoanThuLayout.createSequentialGroup()
                        .addComponent(jLabel21)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnlSoanThuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(pnlSoanThuLayout.createSequentialGroup()
                                .addComponent(bt_add_file, javax.swing.GroupLayout.PREFERRED_SIZE, 203, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(pnlSoanThuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(btn_sent, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(txt_attach)))
                            .addComponent(jScrollPane7, javax.swing.GroupLayout.PREFERRED_SIZE, 574, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(pnlSoanThuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addGroup(pnlSoanThuLayout.createSequentialGroup()
                            .addComponent(jLabel19)
                            .addGap(26, 26, 26)
                            .addComponent(txt_to1, javax.swing.GroupLayout.PREFERRED_SIZE, 268, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(18, 18, 18)
                            .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(pnlSoanThuLayout.createSequentialGroup()
                            .addGap(264, 264, 264)
                            .addComponent(jLabel5))
                        .addGroup(pnlSoanThuLayout.createSequentialGroup()
                            .addComponent(jLabel20)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(txt_Subject, javax.swing.GroupLayout.PREFERRED_SIZE, 581, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addComponent(pb_cc_bcc, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)))
                .addContainerGap(56, Short.MAX_VALUE))
        );
        pnlSoanThuLayout.setVerticalGroup(
            pnlSoanThuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlSoanThuLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel5)
                .addGroup(pnlSoanThuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlSoanThuLayout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addGroup(pnlSoanThuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel19)
                            .addComponent(txt_to1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(pnlSoanThuLayout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pb_cc_bcc, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(pnlSoanThuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel20)
                    .addComponent(txt_Subject, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(pnlSoanThuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane7, javax.swing.GroupLayout.PREFERRED_SIZE, 221, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel21))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlSoanThuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(bt_add_file)
                    .addComponent(txt_attach, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(35, 35, 35)
                .addComponent(btn_sent, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(145, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout pnLeftLayout = new javax.swing.GroupLayout(pnLeft);
        pnLeft.setLayout(pnLeftLayout);
        pnLeftLayout.setHorizontalGroup(
            pnLeftLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnLeftLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(pnlSoanThu, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(307, 307, 307))
            .addGroup(pnLeftLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(pnLeftLayout.createSequentialGroup()
                    .addComponent(pnlHopThuDen, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 344, Short.MAX_VALUE)))
        );
        pnLeftLayout.setVerticalGroup(
            pnLeftLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlSoanThu, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(pnLeftLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(pnLeftLayout.createSequentialGroup()
                    .addComponent(pnlHopThuDen, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 59, Short.MAX_VALUE)))
        );

        javax.swing.GroupLayout pnViewLayout = new javax.swing.GroupLayout(pnView);
        pnView.setLayout(pnViewLayout);
        pnViewLayout.setHorizontalGroup(
            pnViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnViewLayout.createSequentialGroup()
                .addGap(0, 666, Short.MAX_VALUE)
                .addComponent(pnlNoiDung, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(pnViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(pnViewLayout.createSequentialGroup()
                    .addComponent(pnLeft, javax.swing.GroupLayout.PREFERRED_SIZE, 662, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 0, Short.MAX_VALUE)))
        );
        pnViewLayout.setVerticalGroup(
            pnViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnViewLayout.createSequentialGroup()
                .addComponent(pnlNoiDung, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 68, Short.MAX_VALUE))
            .addGroup(pnViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(pnViewLayout.createSequentialGroup()
                    .addComponent(pnLeft, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 0, Short.MAX_VALUE)))
        );

        jPanel1.add(pnView, new org.netbeans.lib.awtextra.AbsoluteConstraints(210, 100, 1150, 610));

        jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/view/view/image/—Pngtree—vector users icon_4144740.png"))); // NOI18N
        jPanel1.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(1040, 10, 70, 70));

        jLabel3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/view/view/image/logo.png"))); // NOI18N
        jPanel1.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 30, 70, 70));

        jLabel4.setFont(new java.awt.Font("Perpetua Titling MT", 1, 22)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(255, 153, 153));
        jLabel4.setText("Mail Client");
        jPanel1.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 47, -1, 40));

        lblUser.setFont(new java.awt.Font("Times New Roman", 3, 18)); // NOI18N
        lblUser.setForeground(new java.awt.Color(204, 255, 255));
        lblUser.setText("Mail User");
        jPanel1.add(lblUser, new org.netbeans.lib.awtextra.AbsoluteConstraints(1120, 20, 230, 20));

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/view/view/image/windowns.jpg"))); // NOI18N
        jLabel1.setAlignmentX(0.5F);
        jLabel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel1.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(-10, -70, 1380, 830));
        jLabel1.getAccessibleContext().setAccessibleName("23");
        jLabel1.getAccessibleContext().setAccessibleDescription("2333");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 1371, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 761, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnHopThuDenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnHopThuDenActionPerformed
        pnlHopThuDen.setVisible(true);
        pnlNoiDung.setVisible(false);
        pnlSoanThu.setVisible(false);
    }//GEN-LAST:event_btnHopThuDenActionPerformed

    private void btnDangXuatActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDangXuatActionPerformed
        setUserName("");
        setpassword("");
        this.dispose();
        Login_JFrame frmLogin = new Login_JFrame();
        frmLogin.setVisible(true);
    }//GEN-LAST:event_btnDangXuatActionPerformed

    private void btnSoanThuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSoanThuActionPerformed
        pnlHopThuDen.setVisible(false);
        pnlNoiDung.setVisible(false);
        pnlSoanThu.setVisible(true);
        txt_to1.setText("");
        txt_Subject.setText("");
        txt_content.setText("");
    }//GEN-LAST:event_btnSoanThuActionPerformed

    private void btnGopYActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGopYActionPerformed
        txt_to1.setText("trungdao9a1@gmail.com");
        txt_Subject.setText("Góp ý về chương trình Mail Client!");
        pnlHopThuDen.setVisible(false);
        pnlNoiDung.setVisible(false);
        pnlSoanThu.setVisible(true);
    }//GEN-LAST:event_btnGopYActionPerformed

    private void btnThoatActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnThoatActionPerformed
        this.dispose();
    }//GEN-LAST:event_btnThoatActionPerformed

    private void btnTraLoiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTraLoiActionPerformed
        txt_to1.setText(txtNguoiGui.getText());
        pnlHopThuDen.setVisible(false);
        pnlNoiDung.setVisible(false);
        pnlSoanThu.setVisible(true);
    }//GEN-LAST:event_btnTraLoiActionPerformed

    private void btnChuyenTiepActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnChuyenTiepActionPerformed
        txt_content.setText(ePnlNoiDung.getText());
        txt_Subject.setText(txtTieuDe.getText());
        pnlHopThuDen.setVisible(false);
        pnlNoiDung.setVisible(false);
        pnlSoanThu.setVisible(true);
        txt_content.setContentType("text/html");
        txt_content.setText(ePnlNoiDung.getText());
    }//GEN-LAST:event_btnChuyenTiepActionPerformed

    private void btn_sentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_sentActionPerformed
        if (!validateFields()) {
            return;
        }
        String to_1 = txt_to1.getText();
        String to_2 = txt_to2.getText();
        String to_3 = txt_to2.getText();
        String subject = txt_Subject.getText();
        String content = txt_content.getText();

        try {
            System.out.println("user: " + getUserName());
            System.out.println("pass: " + getpassword());
            Properties p = loadProperties();
            sendEmail(p, to_1, to_2, to_3, subject, content, attachment_path);
//                LoaderJFrame jrame = new LoaderJFrame(); 
//                jrame.setVisible(true);
//                this.dispose();
            JOptionPane.showMessageDialog(this, "Gửi email thành công!");
        } catch (HeadlessException | IOException | MessagingException ex) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage(), "Thông báo", JOptionPane.ERROR_MESSAGE);
            System.out.println(ex.getMessage());
        }
    }//GEN-LAST:event_btn_sentActionPerformed

    private void jTable1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTable1MouseClicked
        // TODO add your handling code here:
        pnlNoiDung.setVisible(true);
        AbstractTableModel model = (AbstractTableModel) jTable1.getModel();
        txtTieuDe.setText(model.getValueAt(jTable1.getSelectedRow(), 0).toString());
        txtNguoiGui.setText(model.getValueAt(jTable1.getSelectedRow(), 1).toString());
        int n = jTable1.getSelectedRow();
        System.out.println("Hang: " + n);
        try {
            ReadMail(n);
        } catch (IOException ex) {
            System.out.println("Lỗi: " + ex.getMessage());
        }
    }//GEN-LAST:event_jTable1MouseClicked
    String attachment_path = "";
    private void bt_add_fileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bt_add_fileActionPerformed
        JFileChooser chooser = new JFileChooser("C:\\Users\\nO\\Documents\\subjects");
        chooser.setDialogTitle("Mở tệp");
        chooser.showOpenDialog(null);
        File imgName = chooser.getSelectedFile();
        attachment_path = imgName.getAbsolutePath();
        txt_attach.setText(attachment_path);
    }//GEN-LAST:event_bt_add_fileActionPerformed

    private void txt_attachActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txt_attachActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txt_attachActionPerformed

    private void txt_attachMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_txt_attachMouseClicked
        txt_attach.setText("");
    }//GEN-LAST:event_txt_attachMouseClicked

    private void txt_to1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txt_to1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txt_to1ActionPerformed

    private void bt_cc_bccActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bt_cc_bccActionPerformed
        // TODO add your handling code here:
        bt_hide_cc_bcc.setVisible(true);
        pb_cc_bcc.setVisible(true);
    }//GEN-LAST:event_bt_cc_bccActionPerformed

    private void txt_SubjectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txt_SubjectActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txt_SubjectActionPerformed

    private void bt_hide_cc_bccActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bt_hide_cc_bccActionPerformed
        bt_cc_bcc.setVisible(true);
        bt_hide_cc_bcc.setVisible(false);
        pb_cc_bcc.setVisible(false);
    }//GEN-LAST:event_bt_hide_cc_bccActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(HomeJFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(HomeJFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(HomeJFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(HomeJFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new HomeJFrame().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton bt_add_file;
    private javax.swing.JButton bt_cc_bcc;
    private javax.swing.JButton bt_hide_cc_bcc;
    private com.k33ptoo.components.KButton btnChuyenTiep;
    private com.k33ptoo.components.KButton btnDangXuat;
    private com.k33ptoo.components.KButton btnGopY;
    private com.k33ptoo.components.KButton btnHopThuDen;
    private com.k33ptoo.components.KButton btnSoanThu;
    private com.k33ptoo.components.KButton btnThoat;
    private com.k33ptoo.components.KButton btnTraLoi;
    private com.k33ptoo.components.KButton btn_sent;
    private javax.swing.JEditorPane ePnlNoiDung;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JTable jTable1;
    private javax.swing.JLabel lblUser;
    private javax.swing.JPanel pb_cc_bcc;
    private javax.swing.JPanel pnLeft;
    private com.k33ptoo.components.KGradientPanel pnView;
    private javax.swing.JPanel pnlHopThuDen;
    private javax.swing.JPanel pnlNoiDung;
    private javax.swing.JPanel pnlSoanThu;
    private javax.swing.JTextField txtNguoiGui;
    private javax.swing.JTextField txtTieuDe;
    private javax.swing.JTextField txt_Subject;
    private javax.swing.JTextField txt_attach;
    private javax.swing.JEditorPane txt_content;
    private javax.swing.JTextField txt_to1;
    private javax.swing.JTextPane txt_to2;
    private javax.swing.JTextPane txt_to3;
    // End of variables declaration//GEN-END:variables
}
