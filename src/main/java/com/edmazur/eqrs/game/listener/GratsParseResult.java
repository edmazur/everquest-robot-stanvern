package com.edmazur.eqrs.game.listener;

import com.google.common.annotations.VisibleForTesting;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.javacord.api.entity.message.MessageBuilder;

public class GratsParseResult {

  private List<String> lines = new ArrayList<String>();
  private List<File> files = new ArrayList<File>();

  public GratsParseResult addLine(String line) {
    lines.add(line);
    return this;
  }

  public GratsParseResult addFile(File file) {
    files.add(file);
    return this;
  }

  @VisibleForTesting
  List<String> getLines() {
    return lines;
  }

  @VisibleForTesting
  List<File> getFiles() {
    return files;
  }

  public MessageBuilder getMessageBuilder() {
    MessageBuilder messageBuilder = new MessageBuilder();

    // Add lines.
    for (int i = 0; i < lines.size(); i++) {
      String line = lines.get(i);
      messageBuilder.append((i > 0 ? "\n" : "") + line);
    }

    // Add files.
    for (File file : files) {
      messageBuilder.addAttachment(file);
    }

    return messageBuilder;
  }

}
