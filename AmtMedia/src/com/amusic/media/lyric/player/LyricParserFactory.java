
package com.amusic.media.lyric.player;

import com.amusic.media.lyric.parser.KscParser;
import com.amusic.media.lyric.parser.LyricParser;

public class LyricParserFactory {

    // String path
    public LyricParser createLyricsParser(String path) {
        LyricParser lyricsParser = null;

        if (path.contains(".ksc") || path.contains(".html")) {
            lyricsParser = new KscParser(path, LyricConfig.paserWithPath);
        } else if (path.equals("lrc")) {
            // lyricsParser = new LrcParser();
        } else if (path.equals("txt")) {
            // lyricsParser = new TxtKscParser();
        } else if (path.equals("srt")) {
            // lyricsParser = new SrtKscParser();
        }
        return lyricsParser;
    }

    // String lyrics, String type
    public LyricParser createLyricsParser(String lyrics, String type) {
        LyricParser lyricsParser = null;

        if (type.contains(".ksc") || type.contains(".html")) {
            lyricsParser = new KscParser(lyrics, LyricConfig.paserWithString);
        } else if (type.equals("lrc")) {
            // lyricsParser = new LrcParser();
        } else if (type.equals("txt")) {
            // lyricsParser = new TxtKscParser();
        } else if (type.equals("srt")) {
            // lyricsParser = new SrtKscParser();
        }
        return lyricsParser;
    }

}
