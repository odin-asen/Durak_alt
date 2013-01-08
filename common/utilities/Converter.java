package common.utilities;


import common.dto.DTOCard;
import common.dto.DTOCardStack;
import common.dto.DTOClient;
import common.game.GameCard;
import common.game.GameCardStack;
import common.game.Player;

import javax.swing.*;
import java.util.*;
import java.util.logging.Logger;

/**
 * User: Timm Herrmann
 * Date: 07.10.12
 * Time: 15:04
 */
@SuppressWarnings("unchecked")
public class Converter {
  private static final Logger LOGGER = LoggingUtility.getLogger(Converter.class.getName());

  public static DTOClient toDTO(DTOClient client, Player player) {
    if(client != null) {
      client.cardCount = player.getCards().size();
      client.playerType = player.getType();
    }
    return client;
  }

  public static DTOCard toDTO(GameCard card) {
    if(card == null)
      return null;

    final DTOCard dto = new DTOCard();
    dto.cardColour = card.getCardColour();
    dto.cardValue = card.getCardValue();
    return dto;
  }

  public static GameCard fromDTO(DTOCard dto) {
    if(dto == null)
      return null;

    final GameCard card = new GameCard();
    card.setCardColour(dto.cardColour);
    card.setCardValue(dto.cardValue);
    return card;
  }

  public static List<DTOCard> toDTO(List<GameCard> cards) {
    final List<DTOCard> dto = new ArrayList<DTOCard>();
    for (GameCard card : cards) {
      dto.add(toDTO(card));
    }
    return dto;
  }

  public static List<GameCard> fromDTO(List<DTOCard> dtoList) {
    final List<GameCard> cards = new ArrayList<GameCard>();
    for (DTOCard dtoCard : dtoList) {
      cards.add(fromDTO(dtoCard));
    }
    return cards;
  }

  public static DTOCardStack toDTO(GameCardStack stack) {
    if(stack == null)
      return null;

    final DTOCardStack dto = new DTOCardStack();

    final Deque<GameCard> cardDeque = stack.getCardStack();
    dto.cardStack = new ArrayDeque<DTOCard>();
    for (GameCard card  : cardDeque) {
      dto.cardStack.add(Converter.toDTO(card));
    }
    dto.trumpCard = toDTO(stack.getTrumpCard());

    return dto;
  }

  public static GameCardStack fromDTO(DTOCardStack dto) {
    if(dto == null)
      return null;

    final GameCardStack stack = new GameCardStack();
    final Deque<GameCard> cardDeque = new ArrayDeque<GameCard>();
    for (DTOCard card  : dto.cardStack) {
      cardDeque.add(Converter.fromDTO(card));
    }
    stack.setCardStack(cardDeque);
    stack.setTrumpCard(fromDTO(dto.trumpCard));

    return stack;
  }

  public static List<List<DTOCard>> playersCardsToDTO(List<Player> playerList) {
    final List<List<DTOCard>> playersHands = new ArrayList<List<DTOCard>>(playerList.size());
    for (Player player : playerList) {
      playersHands.add(playerCardsToDTO(player));
    }
    return playersHands;
  }

  public static List<DTOCard> playerCardsToDTO(Player player) {
    final List<DTOCard> cards = new ArrayList<DTOCard>();
    for (GameCard gameCard : player.getCards()) {
      cards.add(Converter.toDTO(gameCard));
    }
    return cards;
  }

  public static List<List<DTOCard>> toDTO(List<GameCard>... cardLists) {
    final List<List<DTOCard>> dtoLists = new ArrayList<List<DTOCard>>();
    for (List<GameCard> cardList : cardLists) {
      dtoLists.add(Converter.toDTO(cardList));
    }

    return dtoLists;
  }

  public static List<List<GameCard>> fromDTO(List<DTOCard>... dtoLists) {
    final List<List<GameCard>> cardLists = new ArrayList<List<GameCard>>();
    for (List<DTOCard> dtoList : dtoLists) {
      cardLists.add(fromDTO(dtoList));
    }
    return cardLists;
  }

  public static <T> List<T> getList(DefaultListModel<T> listModel) {
    final List<T> list = new ArrayList<T>(listModel.size());
    for (int index = 0; index < listModel.size(); index++) {
      list.add(listModel.get(index));
    }

    return list;
  }

  public static <T>String getCollectionString(Collection<T> collection) {
    String string = "{";
    for (T t : collection) {
      string = string + "( " + t.toString() + " )";
    }
    string = string + "}";
    return string;
  }
}
