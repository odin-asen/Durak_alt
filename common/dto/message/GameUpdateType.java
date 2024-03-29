package common.dto.message;

/**
 * User: Timm Herrmann
 * Date: 21.10.12
 * Time: 02:39
 */
public enum GameUpdateType {
  CLIENT_CARDS,           /* Sending object: List<DTOCard>
                             cards for the client in the current round */
  GAME_CANCELED,          /* Sending object: String
                             reason for the abort */
  GAME_FINISHED,          /* Sending object: nothing */
                          /* Should be send in case that a match is over */
  IN_GAME_CARDS,          /* Sending object: List<List<DTOCard>>;
                             attacker cards and defender cards in pairs
                             each List<DTOCard> object has as first card the attacker
                             card and as second card the defender card */
  INITIALISE_PLAYERS,     /* Sending object: List<ClientInfo>
                             Initial list of clients that are in the game */
  NEXT_ROUND_INFO,        /* Sending object: List<Boolean> - length 3
                             first object: server switched to the next round (true) or not (false)
                             second object: defender took cards (true) or not (false)
                             third object: all attackers finished the round (true) or not (false) */
  PLAYERS_UPDATE,         /* Sending object: List<ClientInfo>
                             Updated list of clients that are in the game */
  STACK_UPDATE,           /* Sending object: DTOCardStack
                             Information about the current card stack */
}
