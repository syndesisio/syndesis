/*
 * Copyright (C) 2016 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.syndesis.connector.email.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.io.IOException;
import java.nio.charset.Charset;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import javax.mail.Flags.Flag;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import org.apache.commons.codec.binary.Base64;
import com.icegreen.greenmail.server.AbstractServer;
import com.icegreen.greenmail.smtp.SmtpServer;
import com.icegreen.greenmail.user.GreenMailUser;
import com.icegreen.greenmail.util.DummySSLServerSocketFactory;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetup;
import com.icegreen.greenmail.util.ServerSetupTest;
import io.syndesis.common.util.StringConstants;
import io.syndesis.connector.email.EMailConstants.Protocol;
import io.syndesis.connector.email.model.EMailMessageModel;

public class EMailTestServer implements StringConstants {

    public enum Options {
        IMAP, IMAPS,
        POP3, POP3S,
        SMTP, SMTPS
    }

    private List<Options> optionsList;

    private GreenMail greenMail;

    private AbstractServer server;

    public EMailTestServer(Options... options) {
        this(null, options);
    }

    public EMailTestServer(String hostName, Options... options) {
        this.optionsList = Arrays.asList(options);

        if (optionsList.contains(Options.IMAP)) {
            initServer(hostName, ServerSetupTest.IMAP, () -> greenMail.getImap());
        } else if (optionsList.contains(Options.IMAPS)) {
            initServer(hostName, ServerSetupTest.IMAPS, () -> greenMail.getImaps());
        } else if (optionsList.contains(Options.POP3)) {
            initServer(hostName, ServerSetupTest.POP3, () -> greenMail.getPop3());
        } else if (optionsList.contains(Options.POP3S)) {
            initServer(hostName, ServerSetupTest.POP3S, () -> greenMail.getPop3s());
        } else if (optionsList.contains(Options.SMTP)) {
            initServer(hostName, ServerSetupTest.SMTP, () -> greenMail.getSmtp());
        } else if (optionsList.contains(Options.SMTPS)) {
            initServer(hostName, ServerSetupTest.SMTPS, () -> greenMail.getSmtps());
        } else {
            throw new UnsupportedOperationException("Server must be either IMAP(S), POP3(S) or SMTP(S)");
        }
    }

    private void initServer(String hostName, ServerSetup type, Supplier<AbstractServer> supplier) {
        if (hostName != null) {
            greenMail = new GreenMail(type.createCopy(hostName));
        } else {
            greenMail = new GreenMail(type);
        }

        server = supplier.get();
    }

    public void start() {
        if (greenMail == null) {
            throw new IllegalStateException("Green mail server not initialized");
        }

        greenMail.start();
    }

    public void clear() throws Exception {
        greenMail.purgeEmailFromAllMailboxes();
    }

    public void stop() {
        if (greenMail == null) {
            return;
        }

        greenMail.stop();
    }

    public String getHost() {
        return server.getServerSetup().getBindAddress();
    }

    public int getPort() {
        return server.getPort();
    }

    public String getProtocol() {
        return server.getProtocol();
    }

    public void createUser(String username, String password) {
        greenMail.setUser(username, password);
    }

    protected static String convertToPem(Certificate cert) throws CertificateEncodingException {
        Base64 encoder = new Base64(64);
        String cert_begin = "-----BEGIN CERTIFICATE-----\n";
        String end_cert = "-----END CERTIFICATE-----";

        byte[] derCert = cert.getEncoded();
        String pemCertPre = new String(encoder.encode(derCert), Charset.defaultCharset());
        String pemCert = cert_begin + pemCertPre + end_cert;
        return pemCert;
    }

    public String getCertificate() throws KeyStoreException, CertificateEncodingException {
        KeyStore keyStore = new DummySSLServerSocketFactory().getKeyStore();
        Certificate certificate = keyStore.getCertificate("greenmail");
        return convertToPem(certificate);
    }

    private MimeMessage createTextMessage(String to, String from, String subject, String body) {
        return GreenMailUtil.createTextEmail(to, from, subject, body, server.getServerSetup());
    }

    public void deliverMultipartMessage(String user, String password, String from, String subject,
                                                                       String contentType, Object body) throws Exception {
        GreenMailUser greenUser = greenMail.setUser(user, password);
        MimeMultipart multiPart = new MimeMultipart();
        MimeBodyPart textPart = new MimeBodyPart();
        multiPart.addBodyPart(textPart);
        textPart.setContent(body, contentType);

        Session session = GreenMailUtil.getSession(server.getServerSetup());
        MimeMessage mimeMessage = new MimeMessage(session);
        mimeMessage.setRecipients(Message.RecipientType.TO, greenUser.getEmail());
        mimeMessage.setFrom(from);
        mimeMessage.setSubject(subject);
        mimeMessage.setContent(multiPart, "multipart/mixed");
        greenUser.deliver(mimeMessage);
    }

    public void generateMail(String user, String password) {
        for (int i = 1; i <= 5; ++i) {
            // Use random content to avoid potential residual lingering problems
            String subject = GreenMailUtil.random();
            String body = GreenMailUtil.random();
            GreenMailUser greenUser = greenMail.setUser(user, password);
            MimeMessage message = createTextMessage(greenUser.getEmail(), "Ben" + i + "@test.com", subject, body); // Construct message
            greenUser.deliver(message);
        }

        assertEquals(5, greenMail.getReceivedMessages().length);
    }

    public void generateFolder(String user, String password, String folderName) throws Exception {
        if (server instanceof SmtpServer) {
            throw new Exception("SMTP not applicable for generating folders");
        }

        Store store = server.createStore();
        store.connect(user, password);

        Folder newFolder = store.getFolder(folderName);
        if (! newFolder.exists()) {
            newFolder.create(Folder.HOLDS_MESSAGES);
            assertTrue(newFolder.exists());
        }

        newFolder.open(Folder.READ_WRITE);
        assertTrue(newFolder.isOpen());

        List<MimeMessage> msgs = new ArrayList<>();
        for (int i = 1; i <= 5; ++i) {
            // Use random content to avoid potential residual lingering problems
            String subject = folderName + SPACE + HYPHEN + SPACE + GreenMailUtil.random();
            String body = folderName + NEW_LINE + GreenMailUtil.random();
            GreenMailUser greenUser = greenMail.setUser(user, password);
            msgs.add(createTextMessage(greenUser.getEmail(), "Ben" + i + "@test.com", subject, body)); // Construct message
        }

        newFolder.appendMessages(msgs.toArray(new MimeMessage[0]));
        assertEquals(msgs.size(), newFolder.getMessageCount());
    }

    public int getEmailCountInFolder(String user, String password, String folderName) throws Exception {
        if (server instanceof SmtpServer) {
            throw new Exception("SMTP not applicable for reading folders");
        }

        Store store = server.createStore();
        store.connect(user, password);

        Folder newFolder = store.getFolder(folderName);
        if (! newFolder.exists()) {
            throw new Exception("No folder with name " + folderName);
        }

        newFolder.open(Folder.READ_ONLY);
        return newFolder.getMessageCount();
    }

    public int getEmailCount() {
        return greenMail.getReceivedMessages().length;
    }

    private EMailMessageModel createMessageModel(Message msg) throws MessagingException, IOException {
        EMailMessageModel model = new EMailMessageModel();
        model.setFrom(msg.getFrom()[0].toString());
        model.setTo(msg.getRecipients(RecipientType.TO)[0].toString());
        model.setSubject(msg.getSubject());
        model.setContent(msg.getContent());
        return model;
    }

    public List<EMailMessageModel> readEmails(int number) throws Exception {
        List<EMailMessageModel> models = new ArrayList<>();
        MimeMessage[] msgs = greenMail.getReceivedMessages();
        for (int i = 0; i < number; ++i) {
            if (i > msgs.length) {
                break;
            }

            MimeMessage msg = msgs[i];
            msg.setFlag(Flag.SEEN, true);
            models.add(createMessageModel(msg));
        }

        return models;
    }

    public List<EMailMessageModel> getEmails() throws Exception {
        List<EMailMessageModel> models = new ArrayList<>();
        for (MimeMessage msg : greenMail.getReceivedMessages()) {
            models.add(createMessageModel(msg));
        }

        return models;
    }

    public List<EMailMessageModel> getEmailsInFolder(String user, String password, String folderName) throws Exception {
        if (server instanceof SmtpServer) {
            throw new Exception("SMTP not applicable for reading folders");
        }

        Store store = server.createStore();
        store.connect(user, password);

        Folder newFolder = store.getFolder(folderName);
        if (! newFolder.exists()) {
            throw new Exception("No folder with name " + folderName);
        }

        newFolder.open(Folder.READ_ONLY);
        List<EMailMessageModel> models = new ArrayList<>();
        for (Message msg : newFolder.getMessages()) {
            models.add(createMessageModel(msg));
        }

        return models;
    }

    public boolean isSmtp() {
        return server.getProtocol().equalsIgnoreCase(Protocol.SMTP.id()) ||
            server.getProtocol().equalsIgnoreCase(Protocol.SMTPS.id());
    }

    public boolean isImap() {
        return server.getProtocol().equalsIgnoreCase(Protocol.IMAP.id()) ||
            server.getProtocol().equalsIgnoreCase(Protocol.IMAPS.id());
    }
}
