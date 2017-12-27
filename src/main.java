import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

public class main {

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
     * @return hashmap of all messages
     */
    public HashMap<String, HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>>>
    getMessages(String url) {
        BufferedReader reader = readFile(url);

        if (reader == null) {
            return new HashMap<>();
        } else {
            return countMessages(reader);
        }
    }

    private HashMap<String, HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>>>
    countMessages
            (BufferedReader
                     reader) {
        // Name: Year: Month: Day: # of messages
        HashMap<String, HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>>>
                convoMessages = new HashMap<>();

        try {
            String line = reader.readLine();

            while (line.indexOf("<title>") == -1) {
                line = reader.readLine();
            }

            // Add title
            String title = line.substring(line.indexOf("<title>") + 7, line.indexOf("</title>"));

            line = line.substring(line.indexOf("<div class=\"message\">"));

            while (line != null) {
                retrieveDayCount(line, convoMessages);
                line = reader.readLine();
            }

        } catch (IOException e) {
            System.out.println(e);
        }

        return convoMessages;
    }

    private void retrieveDayCount(String line, HashMap<String, HashMap<Integer, HashMap<Integer,
            HashMap<Integer, Integer>>>>
            convoMessages) {
        Document doc = Jsoup.parse(line);

        String date = "";
        Integer year = 0;
        Integer month = 0;
        Integer day = 0;
        String contactName = "";
        boolean isName = true;
        for (Element info : doc.select("span")) {
            String text = info.text();

            if (text.equals("")) {
                isName = !isName;
            } else if (text.contains("Duration: ")) {
                int timeCount = 0;

                addPerson("VoiceChat", year, month, day, convoMessages);
                HashMap<Integer, Integer> nameHash = convoMessages.get("VoiceChat").get(year).get(month);

                int rawTime = Integer.parseInt(text.substring(10, text.indexOf(' ', 10)));
                if (text.contains("hour")) {
                    timeCount += rawTime * 3600;
                } else if (text.contains("minute")) {
                    timeCount = rawTime * 60;
                } else {
                    timeCount = rawTime;
                }

                try {
                    timeCount = timeCount + nameHash.get(day);
                } catch (Exception e) {
                }

                nameHash.put(day, timeCount);
                isName = !isName;

            } else if (isName) {
                contactName = text;
            } else {
                // Get year/month/day
                if(!date.equals(text)) {
                    date = text;
                    int space1 = text.indexOf(", ") + 2;
                    int space2 = text.indexOf(' ', space1) + 1;
                    int space3 = text.indexOf(", ", space2) + 2;
                    month = monthToInt.get(text.substring(space1, space2 - 1));
                    day = Integer.parseInt(text.substring(space2, space3 - 2));
                    year = Integer.parseInt(text.substring(space3, space3 + 4));
                }

                // Add hashmap if not already added
                addPerson(contactName, year, month, day, convoMessages);
                // Get hashmap for the current day
                HashMap<Integer, Integer> nameHash = convoMessages.get(contactName).get(year).get
                        (month);

                nameHash.put(day, nameHash.get(day) + 1);
            }
            isName = !isName;
        }
    }

    private void addPerson(String name, int year, int month, int day,
                           HashMap<String, HashMap<Integer, HashMap<Integer,
            HashMap<Integer, Integer>>>> convoMessages){
        if(convoMessages.containsKey(name)){
            if(convoMessages.get(name).containsKey(year)){
                if(convoMessages.get(name).get(year).containsKey(month)){
                    if(!convoMessages.get(name).get(year).get(month).containsKey(day)) {
                        convoMessages.get(name).get(year).get(month).put(day, 0);
                    }
                } else {
                    convoMessages.get(name).get(year).put(month, new HashMap<>());
                    addPerson(name, year, month, day, convoMessages);
                }
            } else {
                convoMessages.get(name).put(year, new HashMap<>());
                addPerson(name, year, month, day, convoMessages);
            }
        } else {
            convoMessages.put(name, new HashMap<>());
            addPerson(name, year, month, day, convoMessages);
        }
    }

    private BufferedReader readFile(String url) {
        try {
            return new BufferedReader(new FileReader(url));
        } catch (FileNotFoundException e) {
            System.out.println("File doesn't exist + " + e);
            return null;
        }
    }

    public static void main(String[] args) {
        System.out.println(new main().getMessages("1.html"));
    }
}