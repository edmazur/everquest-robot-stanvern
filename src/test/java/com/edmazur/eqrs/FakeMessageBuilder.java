package com.edmazur.eqrs;

import java.io.File;
import org.javacord.api.entity.message.MessageBuilder;

public class FakeMessageBuilder extends MessageBuilder {

  private File file;

  @Override
  public MessageBuilder addAttachment(File file) {
    this.file = file;
    return this;
  }

  public File getAttachment() {
    return file;
  }

}
