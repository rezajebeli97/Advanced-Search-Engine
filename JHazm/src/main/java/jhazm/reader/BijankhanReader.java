package jhazm.reader;

import com.infomancers.collections.yield.Yielder;
import edu.stanford.nlp.ling.TaggedWord;
import jhazm.Normalizer;
import jhazm.tokenizer.WordTokenizer;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * interfaces Bijankhan Corpus (http://ece.ut.ac.ir/dbrg/bijankhan/Corpus/BijanKhan_Corpus_Processed.zip) that
 * you must download and extract it.
 *
 * @author Mojtaba Khallash
 */
public class BijankhanReader {
    //
    // Fields
    //

    private final String[] punctuation = new String[] { "#", "*", ".", "؟", "!" };

    private String bijankhanFile;
    private boolean joinedVerbParts;
    private String posMap;
    private Normalizer normalizer;
    private WordTokenizer tokenizer;



    //
    // Constructors
    //

    public BijankhanReader() throws IOException {
        this("resources/corpora/bijankhan.txt", true, "resources/data/posMaps.dat");
    }

    public BijankhanReader(boolean joinedVerbParts) throws IOException {
        this("resources/corpora/bijankhan.txt", joinedVerbParts, "resources/data/posMaps.dat");
    }

    public BijankhanReader(String posMap) throws IOException {
        this("resources/corpora/bijankhan.txt", true, posMap);
    }

    public BijankhanReader(boolean joinedVerbParts, String posMap)
            throws IOException {
        this("resources/corpora/bijankhan.txt", joinedVerbParts, posMap);
    }

    public BijankhanReader(String bijankhanFile, boolean joinedVerbParts, String posMap)
            throws IOException {
        this.bijankhanFile = bijankhanFile;
        this.joinedVerbParts = joinedVerbParts;
        this.posMap = posMap;
        this.normalizer = new Normalizer(true, false, true);
        this.tokenizer = new WordTokenizer();
    }




    //
    // API
    //

    public Iterable<List<TaggedWord>> getSentences() {
        return new YieldSentence();
    }





    //
    // Helper
    //

    private String getBijankhanFile() {
        return bijankhanFile;
    }

    private boolean isJoinedVerbParts() {
        return joinedVerbParts;
    }

    private HashMap getPosMap() throws IOException {
        if (this.posMap != null) {
            HashMap mapper = new HashMap();
            for (String line : Files.readAllLines(Paths.get(this.posMap), Charset.forName("UTF8"))) {
                String[] parts = line.split(",");
                mapper.put(parts[0], parts[1]);
            }

            return mapper;
        }
        else
            return null;
    }

    private Normalizer getNormalizer() {
        return normalizer;
    }

    class YieldSentence extends Yielder<List<TaggedWord>> {
        private BufferedReader br;

        public YieldSentence() {
            try {
                FileInputStream fstream = new FileInputStream(getBijankhanFile());
                DataInputStream in = new DataInputStream(fstream);
                br = new BufferedReader(new InputStreamReader(in, Charset.forName("UTF8")));
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        @Override
        protected void yieldNextCore() {
            try {
                HashMap mapper = getPosMap();
                List<TaggedWord> sentence = new ArrayList<>();

                String line;

                while ((line = br.readLine()) != null) {
                    String[] parts = line.trim().split("  +");
                    if (parts.length == 2) {
                        String word = parts[0];
                        String tag = parts[1];
                        if (!(word.equals("#") || word.equals("*"))) {
                            word = getNormalizer().run(word);
                            if (word.isEmpty())
                                word = "_";
                            sentence.add(new TaggedWord(word, tag));
                        }
                        if (tag.equals("DELM") && Arrays.asList(punctuation).contains(word)) {
                            if (!sentence.isEmpty()) {
                                if (isJoinedVerbParts())
                                    sentence = PeykareReader.joinVerbParts(sentence);

                                if (mapper != null) {
                                    for (TaggedWord tword : sentence) {
                                        tword.setTag(mapper.get(tword.tag()).toString());
                                    }
                                }

                                yieldReturn(sentence);
                                return;
                            }
                        }
                    }
                }
                br.close();
            } catch(Exception ex){
                ex.printStackTrace();
            }
        }
    }
}