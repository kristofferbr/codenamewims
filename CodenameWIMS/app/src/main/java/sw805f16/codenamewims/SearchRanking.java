package sw805f16.codenamewims;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import info.debatty.java.stringsimilarity.NormalizedLevenshtein;

/**
 * Created by replacedleaf60 on 3/17/16.
 */
public abstract class SearchRanking {

    /**
     * This method ranks the stores after similarity between the query and the provided search set
     * @param query The search query
     */
    public static ArrayList<String> rankSearchResults(String query, ArrayList<String> searchSet) {
        String key = "";
        ArrayList<String> returnList = new ArrayList<>();
        //We have a local HashMap of storenames and associated rank
        HashMap<String, Double> ranks = new HashMap<>();
        //We use the normalized levenshtein similarity between the strings
        NormalizedLevenshtein levenshtein = new NormalizedLevenshtein();
        Iterator iterator;

        //Here we iterate through the search set
        for (int i = 0; i < searchSet.size(); i++) {
            key = searchSet.get(i);
            ranks.put(key, levenshtein.similarity(query.toLowerCase(), key.toLowerCase()));
        }

        double highest;
        Map.Entry pair;
        //Here we use the size of the search set instead of the local ranks map, because they initially are the same size
        for (int i = 0; i < searchSet.size(); i++) {
            //We re-instantiate the iterator to iterate over ranks HashMap and set the highest value to 0
            iterator = ranks.entrySet().iterator();
            highest = 0;

            //Here we iterate over the ranks HashMap
            while (iterator.hasNext()) {
                pair = (Map.Entry) iterator.next();

                //If the value in the extracted pair is greater than or equal to the highest value and above a threshold
                //The string key is set the pair key and the highest value is updated
                if (((Double) pair.getValue()) >= highest && ((Double) pair.getValue()) > 0.3) {
                    key = (String) pair.getKey();
                    highest = (Double) pair.getValue();
                }
            }
            //If the highest value has not been set
            //Then that means there were no meaningful matches for the query
            if (highest == 0) {
                break;
            }
            //When the storename with the highest similarity with query is found it is added to the list
            returnList.add(key);
            //When we are done with the storename it is removed from the ranks HashMap
            ranks.remove(key);
        }
        return returnList;
    }


    /**
     * This method capitalises the first letter in the provided string
     * @param str The string that is capitalised
     * @return The capitalised string
     */
    private static String capitaliseFirstLetter(String str) {
        str = str.substring(0, 1).toUpperCase() + str.substring(1);
        return str;
    }

    /**
     * This method capitalises the first letter of all the words in a string
     * @param str Input string
     * @return A string with capitalised first letters
     */
    public static String capitaliseFirstLetters(String str) {
        String tmpString = "";
        //We split the string at whitespaces
        String[] strParts = str.split("\\s");
        for (String st : strParts) {
            //Then we reassemble the string and call the capitaliseFirstLetter method on each words
            tmpString = tmpString + capitaliseFirstLetter(st) + " ";
        }
        //We return the string while trimming leading and trailing whitespaces
        return tmpString.trim();
    }

    /**
     * This method removes special characters from the input string and returns the modified string
     * @param str Input string
     * @return Modified string with no special characters
     */
    public static String removeSpecialCharacters(String str) {
        String tmpString = "";
        //We replace all instances of special characters with a whitespace
        tmpString = str.replaceAll("-|\\.|,|\"|'|:|;|\\\\|/|_|\\[|\\]|\\{|\\}|\\(|\\)|\\?", " ");
        //The above could introduce redundant whitespaces, so we remove all instances of more than two
        //whitespaces
        tmpString = tmpString.replaceAll("\\s{2,}", " ");
        return tmpString;
    }
}
