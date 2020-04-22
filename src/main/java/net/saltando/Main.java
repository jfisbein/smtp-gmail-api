package net.saltando;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import lombok.extern.slf4j.Slf4j;
import org.subethamail.smtp.MessageHandlerFactory;
import org.subethamail.smtp.server.SMTPServer;

@Slf4j
public class Main {

  public static final int PORT = 25;

  public static void main(String[] args) throws GeneralSecurityException, IOException {
    GmailAPI gmail = new GmailAPI(new File("src/main/resources/credentials.json"), new File("tokens"));

    MessageHandlerFactory myFactory = new GmailMessageHandlerFactory(gmail);
    SMTPServer smtpServer = SMTPServer.port(PORT).messageHandlerFactory(myFactory).build();
    log.info("Starting Basic SMTP Server on port " + PORT + "...");
    smtpServer.start();
  }
}
