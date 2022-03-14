package com.edmazur.eqrs.game.listener;

import com.edmazur.eqrs.game.GameLogEvent;

public interface GameLogListener {

  String getConfig();

  void onGameLogEvent(GameLogEvent gameLogEvent);

}