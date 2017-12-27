import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.FileNotFoundException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

public class messageCounter {

    /**
     * Convert string month to month number
     */
    private HashMap<String, Integer> monthToInt = new HashMap<String, Integer>() {{
        put("January", 1);
        put("February", 2);
        put("March", 3);
        put("April", 4);
        put("May", 5);
        put("June", 6);
        put("July", 7);
        put("August", 8);
        put("September", 9);
        put("October", 10);
        put("November", 11);
        put("December", 12);
    }};

    /**
     * @return HashMap of the number of messages sent per person in the file given by &lt;
     * filePath&gt;
     */
    public HashMap<String, HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>>>
    getMessages(String filePath) {
        // Load filePath into a BufferedReader
        BufferedReader reader = readFile(filePath);

        if (reader == null) { // If the file doesn't exist, return an empty HashMap
            return new HashMap<>();
        } else { // If the file exists get the amount of messages in it
            return countMessages(reader);
        }
    }

    /**
     * Return HashMap of the number of messages sent per person in the file given by &lt;reader&gt;
     *
     * @param reader BufferedReader of file containing messages
     * @return HashMap of the number of messages sent per person in the file given by &lt;reader&gt;
     */
    private HashMap<String, HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>>>
    countMessages(BufferedReader reader) {
        // Name: Year: Month: Day: # of messages
        HashMap<String, HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>>>
                convoMessages = new HashMap<>();

        try {
            String line = reader.readLine();
            while (!line.contains("<title>")) {
                // Read a new line till the first line needed is found
                line = reader.readLine();
            }

            // Remove all html from before the first message
            line = line.substring(line.indexOf("<div class=\"message\">"));

            while (line != null) { // While there is another line
                // Retrieve the amount of messages into convoMessages that were sent on line
                retrieveDayCount(line, convoMessages);
                line = reader.readLine();
            }
        } catch (IOException e) {
            System.out.println(e);
        }

        return convoMessages;
    }

    /**
     * Add # of messages for each person on line &lt;line&gt;
     *
     * @param line line of html
     * @param convoMessages HashMap of message count for each person
     */
    private void retrieveDayCount(String line, HashMap<String, HashMap<Integer, HashMap<Integer,
            HashMap<Integer, Integer>>>> convoMessages) {
        Document doc = Jsoup.parse(line); // Parse the line of html

        String date = "."; // String version of the date
        Integer year = 0; // Current year number
        Integer month = 0; // Current month number
        Integer day = 0; // Current day number
        String contactName = ""; // Current person's name
        boolean isName = true; // If reading a person's name or the date
        for (Element info : doc.select("span")) { // Reads in name->date->name->...
            // For every span element in the html line
            String text = info.text();

            if (text.equals("")) { // If either the name or date is blank, act like it didn't exist
                isName = !isName;
            } else if (text.contains("Duration: ")) {
                // If call occured, add it in seconds to "VoiceChat"
                int secondsCount = 0;

                // Add HashMap if not already added
                addPerson("VoiceChat", year, month, day, convoMessages);
                // Get HashMap for the current day
                HashMap<Integer, Integer> nameHash = convoMessages.get("VoiceChat").get(year).get(month);

                // Get the time in x units
                int rawTime = Integer.parseInt(text.substring(10, text.indexOf(' ', 10)));
                // Convert the time to seconds
                if (text.contains("hour")) { // If the call was in hours
                    secondsCount += rawTime * 3600;
                } else if (text.contains("minute")) { // If the call was in minutes
                    secondsCount = rawTime * 60;
                } else { // If the call was in seconds
                    secondsCount = rawTime;
                }

                // Add current call seconds to total call seconds
                nameHash.put(day, secondsCount + nameHash.get(day));
                // Pretend this didn't happen because it doesn't have a date associated
                isName = !isName;
            } else if (isName) { // If the current element contains a name
                contactName = text; // Cache name till date is read
            } else { // If current element is a date
                // Get year/month/day
                if(!date.equals(text)) { // If date isn't the same as previously
                    date = text;
                    int space1 = text.indexOf(", ") + 2;
                    int space2 = text.indexOf(' ', space1) + 1;
                    int space3 = text.indexOf(", ", space2) + 2;
                    month = monthToInt.get(text.substring(space1, space2 - 1));
                    day = Integer.parseInt(text.substring(space2, space3 - 2));
                    year = Integer.parseInt(text.substring(space3, space3 + 4));
                }

                // Add HashMap if not already added
                addPerson(contactName, year, month, day, convoMessages);
                // Get HashMap for the current day
                HashMap<Integer, Integer> nameHash = convoMessages.get(contactName).get(year).get
                        (month);

                // Add a count to the number of messages the current person has sent you on <day>
                nameHash.put(day, nameHash.get(day) + 1);
            }
            isName = !isName; // toggle between name and date
        }
    }

    /**
     * Recursively add HashMap for the current message date by &lt;name&gt;
     *
     * @param name Name of author of message
     * @param year Year of message
     * @param month Month of message
     * @param day Day of message
     * @param convoMessages HashMap of the message count for each person
     */
    private void addPerson(String name, int year, int month, int day, HashMap<String,
            HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>>> convoMessages){
        if(convoMessages.containsKey(name)){ // If name exists
            if(convoMessages.get(name).containsKey(year)){ // If year exists
                if(convoMessages.get(name).get(year).containsKey(month)){ // If month exists
                    if(!convoMessages.get(name).get(year).get(month).containsKey(day)) {
                        // If day doesn't exist, set the count to 0
                        convoMessages.get(name).get(year).get(month).put(day, 0);
                    }
                } else { // Create a new month for this <month> and then recursively add it
                    convoMessages.get(name).get(year).put(month, new HashMap<>());
                    addPerson(name, year, month, day, convoMessages);
                }
            } else { // Create a new year for this <year> and then recursively add it
                convoMessages.get(name).put(year, new HashMap<>());
                addPerson(name, year, month, day, convoMessages);
            }
        } else { // Create a new name for this <name> and then recursively add it
            convoMessages.put(name, new HashMap<>());
            addPerson(name, year, month, day, convoMessages);
        }
    }

    /**
     * Return BufferedReader of the file url
     *
     * @param filePath file path to file
     * @return BufferedReader of file at &lt;filepath&gt;
     */
    private BufferedReader readFile(String filePath) {
        try {
            return new BufferedReader(new FileReader(filePath));
        } catch (FileNotFoundException e) {
            System.out.println("File doesn't exist + " + e);
            return null;
        }
    }
}