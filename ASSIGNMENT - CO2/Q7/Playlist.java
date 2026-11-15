
import java.util.*;

public class Playlist {
    public static void main(String[] args) {

        int n = 100000;

        // Original design: repeated String concatenation
        long start = System.nanoTime();

        String playlist = "";
        for (int i = 0; i < n; i++) {
            playlist += "Song" + i + "\n";
        }

        long oldTime = System.nanoTime() - start;

        // Redesigned: StringBuilder
        start = System.nanoTime();

        StringBuilder improvedPlaylist = new StringBuilder();

        for (int i = 0; i < n; i++) {
            improvedPlaylist.append("Song")
                            .append(i)
                            .append("\n");
        }

        long newTime = System.nanoTime() - start;

        System.out.println("Original playlist length: " + playlist.length());
        System.out.println("Improved playlist length: " + improvedPlaylist.length());

        System.out.println("Original String time: " + oldTime + " ns");
        System.out.println("Improved StringBuilder time: " + newTime + " ns");

        System.out.println("Original and improved results match: "
                + playlist.equals(improvedPlaylist.toString()));
    }
}
