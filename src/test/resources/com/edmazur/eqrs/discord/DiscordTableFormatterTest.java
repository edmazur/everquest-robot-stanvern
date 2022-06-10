package com.edmazur.eqrs.discord;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.edmazur.eqrs.table.DataRow;
import com.edmazur.eqrs.table.HeaderRow;
import com.edmazur.eqrs.table.Justification;
import com.edmazur.eqrs.table.SubTable;
import com.edmazur.eqrs.table.Table;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DiscordTableFormatterTest {

  DiscordTableFormatter discordTableFormatter;

  @BeforeEach
  void init() {
    discordTableFormatter = new DiscordTableFormatter();
  }

  @Test
  void getMessages() {
    Table table = new Table().addSubTable(new SubTable()
        .setHeading("My table")
        .setHeaderRow(new HeaderRow()
            .addColumn("Column 1", Justification.LEFT)
            .addColumn("Column 2", Justification.RIGHT)
            .addColumn("Column 3", Justification.LEFT))
        .addDataRow(new DataRow()
            .addColumn("Some data")
            .addColumn("X")
            .addColumn("Some more data")
            .setCodeFontEndIndex(1))
        .addDataRow(new DataRow()
            .addColumn("Another row of data")
            .addColumn("Y")
            .addColumn("Yet more data")));
    assertEquals(
        "My table\n"
        + "`Column 1               Column 2    Column 3      `\n"
        + "`-------------------------------------------------`\n"
        + "`Some data                     X    `Some more data\n"
        + "`Another row of data           Y    Yet more data `\n",
        discordTableFormatter.getMessages(table).get(0));
  }

}
