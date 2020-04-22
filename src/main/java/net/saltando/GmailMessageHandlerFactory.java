package net.saltando;

import com.google.api.client.util.IOUtils;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.subethamail.smtp.MessageContext;
import org.subethamail.smtp.MessageHandler;
import org.subethamail.smtp.MessageHandlerFactory;
import org.subethamail.smtp.RejectException;

public class GmailMessageHandlerFactory implements MessageHandlerFactory {

  private final GmailAPI gmailAPI;

  public GmailMessageHandlerFactory(GmailAPI gmailAPI) {
    this.gmailAPI = gmailAPI;
  }

  @Override
  public MessageHandler create(MessageContext messageContext) {
    return new Handler(gmailAPI);
  }

  @Slf4j
  static final class Handler implements MessageHandler {

    private final GmailAPI gmailAPI;
    private MimeMessage message = null;

    Handler(GmailAPI gmailAPI) {
      this.gmailAPI = gmailAPI;
    }

    @Override
    public void from(String s) throws RejectException {
      try {
        new InternetAddress(s);
      } catch (MessagingException e) {
        throw new RejectException(e.getClass().getSimpleName() + ": " + e.getMessage());
      }
    }

    @Override
    public void recipient(String s) throws RejectException {
      try {
        new InternetAddress(s);
      } catch (MessagingException e) {
        throw new RejectException(e.getClass().getSimpleName() + ": " + e.getMessage());
      }
    }

    @Override
    public String data(InputStream inputStream) throws RejectException, IOException {
      try {
        message = new MimeMessage(null, new ByteArrayInputStream(inputStreamToByteArray(inputStream)));
      } catch (MessagingException e) {
        throw new RejectException(e.getClass().getSimpleName() + ": " + e.getMessage());
      }

      return null;
    }

    private byte[] inputStreamToByteArray(InputStream data) throws IOException {
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      IOUtils.copy(data, out, false);

      return out.toByteArray();
    }

    @Override
    public void done() {
      if (message != null) {
        try {
          gmailAPI.sendMessage(message);
        } catch (MessagingException | IOException e) {
          log.error(e.getMessage(), e);
        }
      } else {
        log.warn("Null message");
      }
    }
  }
}
