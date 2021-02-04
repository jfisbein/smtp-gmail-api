package net.saltando;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.Base64;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.Message;
import com.google.common.collect.ImmutableList;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Properties;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GmailAPI {

  private static final String APPLICATION_NAME = "Gmail API Client";
  private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

  /**
   * Global instance of the scopes. If modifying these scopes, delete your previously saved tokens/ folder.
   */
  private static final List<String> SCOPES = ImmutableList.of(GmailScopes.GMAIL_SEND);
  private final Gmail service;
  private final File tokensDirectory;

  public GmailAPI(File credentialsFile, File tokensDirectory) throws GeneralSecurityException, IOException {
    this.tokensDirectory = tokensDirectory;
    NetHttpTransport httpTrans = GoogleNetHttpTransport.newTrustedTransport();
    service = new Gmail.Builder(httpTrans, JSON_FACTORY, getCredentials(httpTrans, credentialsFile))
      .setApplicationName(APPLICATION_NAME)
      .build();
  }

  /**
   * Creates an authorized Credential object.
   *
   * @param httpTransport The network HTTP Transport.
   * @return An authorized Credential object.
   * @throws IOException If the credentials.json file cannot be found.
   */
  private Credential getCredentials(NetHttpTransport httpTransport, File credentialsFile) throws IOException {
    // Load client secrets.
    GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new FileReader(credentialsFile));

    // Build flow and trigger user authorization request.
    GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
      httpTransport, JSON_FACTORY, clientSecrets, SCOPES)
      .setDataStoreFactory(new FileDataStoreFactory(tokensDirectory))
      .setAccessType("offline")
      .build();
    LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
    return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
  }

  /**
   * Create a MimeMessage using the parameters provided.
   *
   * @param to      email address of the receiver
   * @param from    email address of the sender, the mailbox account
   * @param subject subject of the email
   * @param body    body text of the email
   * @return the MimeMessage to be used to send email
   */
  private MimeMessage createEmail(String to, String from, String subject, String body, String mimeType) throws MessagingException {
    Properties props = new Properties();
    Session session = Session.getDefaultInstance(props, null);

    MimeMessage email = new MimeMessage(session);

    email.setFrom(new InternetAddress(from));
    email.addRecipient(javax.mail.Message.RecipientType.TO, new InternetAddress(to));
    email.setSubject(subject);
    email.setText(body);
    email.setContent(body, mimeType);

    return email;
  }

  /**
   * Create a message from an email.
   *
   * @param emailContent Email to be set to raw of message
   * @return a message containing a base64url encoded email
   * @throws IOException        if an error occurs writing the MimeMessage to the stream or if an error is generated by the javax.activation
   *                            layer
   * @throws MessagingException for other failures
   */
  private Message createMessageWithEmail(MimeMessage emailContent) throws MessagingException, IOException {
    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    emailContent.writeTo(buffer);
    byte[] bytes = buffer.toByteArray();
    String encodedEmail = Base64.encodeBase64URLSafeString(bytes);
    Message message = new Message();
    message.setRaw(encodedEmail);

    return message;
  }

  /**
   * Send an email from the user's mailbox to its recipient.
   *
   * @param emailContent Email to be sent.
   * @return Message sent.
   * @throws IOException        if an error occurs writing the MimeMessage to the stream or if an error is generated by the javax.activation
   *                            layer
   * @throws MessagingException for other failures
   */
  public Message sendMessage(MimeMessage emailContent) throws MessagingException, IOException {
    Message message = createMessageWithEmail(emailContent);
    message = service.users().messages().send("me", message).execute();

    log.info("Sent message. Message id: {}", message.getId());
    log.debug(message.toPrettyString());

    return message;
  }

  /**
   * Creates and Send an email from the user's mailbox to its recipient.
   *
   * @param to       email recipient
   * @param from     email from
   * @param subject  email subject
   * @param bodyHtml email body in html format
   * @return Message sent.
   * @throws IOException        if an error occurs writing the MimeMessage to the stream or if an error is generated by the javax.activation
   *                            layer
   * @throws MessagingException for other failures
   */
  public Message sendMessage(String to, String from, String subject, String bodyHtml) throws MessagingException, IOException {
    return sendMessage(createEmail(to, from, subject, bodyHtml, "text/html"));
  }
}