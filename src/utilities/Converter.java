package utilities;


import dto.ClientInfo;
import dto.DTOCard;
import dto.DTOCardStack;
import game.GameCard;
import game.GameCardStack;
import game.Player;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * User: Timm Herrmann
 * Date: 07.10.12
 * Time: 15:04
 */
@SuppressWarnings("unchecked")
public class Converter {
  private static final Logger LOGGER = Logger.getLogger(Converter.class.getName());

  public static ClientInfo toDTO(ClientInfo client, Player player) {
    if(client != null) {
      client.setCardCount(player.getCards().size());
      client.setPlayerType(player.getType());
    }
    return client;
  }

  public static DTOCard toDTO(GameCard card) {
    DTOCard dto = new DTOCard();
    dto.cardColour = card.getCardColour();
    dto.cardValue = card.getCardValue();
    return dto;
  }

  public static GameCard fromDTO(DTOCard dto) {
    GameCard card = new GameCard();
    card.setCardColour(dto.cardColour);
    card.setCardValue(dto.cardValue);
    return card;
  }

  public static List<DTOCard> toDTO(List<GameCard> cards) {
    List<DTOCard> dto = new ArrayList<DTOCard>();
    for (GameCard card : cards) {
      dto.add(toDTO(card));
    }
    return dto;
  }

  public static List<GameCard> fromDTO(List<DTOCard> dtoList) {
    List<GameCard> cards = new ArrayList<GameCard>();
    for (DTOCard dtoCard : dtoList) {
      cards.add(fromDTO(dtoCard));
    }
    return cards;
  }

  public static DTOCardStack toDTO(GameCardStack stack) {
    DTOCardStack dto = new DTOCardStack();
    try {
      final Deque<GameCard> cardDeque = stack.getCardStack();
      dto.setCardStack((Deque<DTOCard>) Class.forName(cardDeque.getClass().getName()).newInstance());
      for (GameCard card  : cardDeque) {
        dto.getCardStack().add(Converter.toDTO(card));
      }
    } catch (InstantiationException e) {
      LOGGER.log(Level.SEVERE, "Error converting object to dto!");
    } catch (IllegalAccessException e) {
      LOGGER.log(Level.SEVERE, "Error converting object to dto!");
    } catch (ClassNotFoundException e) {
      LOGGER.log(Level.SEVERE, "Error converting object to dto!");
    }

    return dto;
  }

  public static GameCardStack fromDTO(DTOCardStack dto) {
    GameCardStack stack = GameCardStack.getInstance();
    try {
      final Deque<GameCard> cardDeque = (Deque<GameCard>) Class.forName(dto.getCardStack().getClass().getName()).newInstance();
      for (DTOCard card  : dto.getCardStack()) {
        cardDeque.add(Converter.fromDTO(card));
      }
      stack.setCardStack(cardDeque);
    } catch (InstantiationException e) {
      LOGGER.log(Level.SEVERE, "Error converting object to dto!");
    } catch (IllegalAccessException e) {
      LOGGER.log(Level.SEVERE, "Error converting object to dto!");
    } catch (ClassNotFoundException e) {
      LOGGER.log(Level.SEVERE, "Error converting object to dto!");
    }

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
}
