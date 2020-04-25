package net.saltando;

import java.io.File;
import lombok.Getter;
import org.kohsuke.args4j.Option;

@Getter
public class CliParams {

  @Option(name = "-p", aliases = "--port", usage = "SMTP server port")
  private int port = 25;

  @Option(name = "-c", aliases = "--credentials", usage = "GMail API credentials file")
  private File credentialsFile = new File("credentials.json");

  @Option(name = "-t", aliases = "--tokens", usage = "GMail api tokens folder")
  private File tokensFolder = new File("tokens");

  @Option(name = "-h", aliases = "--help", help = true)
  private boolean help;
}
