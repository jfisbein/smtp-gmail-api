package net.saltando;

import java.io.IOException;
import java.security.GeneralSecurityException;
import lombok.extern.slf4j.Slf4j;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.ParserProperties;
import org.subethamail.smtp.MessageHandlerFactory;
import org.subethamail.smtp.server.SMTPServer;

@Slf4j
public class Main {

  public static void main(String[] args) {
    CliParams params = new CliParams();
    CmdLineParser parser = new CmdLineParser(params, ParserProperties.defaults().withUsageWidth(120));

    try {
      parser.parseArgument(args);
      if (params.isHelp()) {
        printUsage(parser);
      } else {
        new Main().start(params);
      }
    } catch (Exception e) {
      log.info("{}", e.getMessage(), e);
      printUsage(parser);
    }
  }

  private void start(CliParams params) throws GeneralSecurityException, IOException {
    int port = params.getPort();
    GmailAPI gmail = new GmailAPI(params.getCredentialsFile(), params.getTokensFolder());

    MessageHandlerFactory myFactory = new GmailMessageHandlerFactory(gmail);
    SMTPServer smtpServer = SMTPServer.port(port).messageHandlerFactory(myFactory).build();
    log.info("Starting Basic SMTP-GMail-Api gateway on port {} ...", port);
    smtpServer.start();
  }

  private static void printUsage(CmdLineParser parser) {
    System.out.println("Usage: java -jar smtp-gmail-api.jar [OPTIONS]\n");
    System.out.println("Available options:");
    parser.printUsage(System.out);
  }
}
