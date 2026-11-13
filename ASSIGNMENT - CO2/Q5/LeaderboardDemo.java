
import java.util.*;

class Player {
    String name;
    int score;
    int age;

    Player(String name, int score, int age) {
        this.name = name;
        this.score = score;
        this.age = age;
    }

    @Override
    public String toString() {
        return name + " - Score: " + score + ", Age: " + age;
    }
}

class Leaderboard<T> {
    private List<T> players = new ArrayList<>();

    public void addPlayer(T player) {
        players.add(player);
    }

    public void display(Comparator<T> comparator) {
        players.sort(comparator);
        for (T player : players) {
            System.out.println(player);
        }
    }
}

public class LeaderboardDemo {
    public static void main(String[] args) {

        Leaderboard<Player> board = new Leaderboard<>();

        board.addPlayer(new Player("Arthi", 95, 20));
        board.addPlayer(new Player("Priya", 88, 19));
        board.addPlayer(new Player("Ravi", 95, 18));
        board.addPlayer(new Player("Kumar", 75, 21));

        Comparator<Player> ranking =
            Comparator.comparingInt((Player p) -> p.score)
                      .reversed()
                      .thenComparing(p -> p.name);

        System.out.println("LEADERBOARD");
        board.display(ranking);
    }
}
