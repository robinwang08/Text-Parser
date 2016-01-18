package com.robin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.*;
import org.springframework.stereotype.*;
import org.springframework.web.bind.annotation.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.Map;
import java.util.Collections;
import java.util.Comparator;
import java.text.BreakIterator;
import java.util.Locale;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.util.StringUtils;

@RestController
public class TextController {

@RequestMapping("/")
    @ResponseBody
    String home() {
        return "Hello, my name is Robin!";
    }

    @RequestMapping(value = "/words/avg_len", method = RequestMethod.POST)
    public Text avgLen(@RequestBody Text text) {
        String[] words = parse(text.getText());
        if (words.length == 0)
            return new Text("No words found");
        int totalLen = 0;
        for (String s : words) {
            totalLen += s.length();
        }
        double avg = (double) totalLen / words.length;
        return new Text(Double.toString(avg));
    }

    @RequestMapping(value = "/words/most_com", method = RequestMethod.POST)
    public Text mostCom(@RequestBody Text text) {
        HashMap<String, Integer> map = new HashMap<String, Integer>();
        String[] words = parse(text.getText());
        if (words.length == 0)
            return new Text("No words found");
        else if (words.length == 1)
            return new Text(words[0]);

        for (String s : words) {
            if (map.containsKey(s))
                map.put(s, map.get(s) + 1);
            else
                map.put(s, 1);
        }

        int max = 0;
        String mostcommon = "";
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            if (entry.getValue() > max) {
                max = entry.getValue();
                mostcommon = entry.getKey();
            } else if (entry.getValue() == max) {
                if (entry.getKey().compareToIgnoreCase(mostcommon) < 0)
                    mostcommon = entry.getKey();
            }
        }
        return new Text(mostcommon);
    }

    @RequestMapping(value = "/words/median", method = RequestMethod.POST)
    public Text median(@RequestBody Text text) {
        String[] words = parse(text.getText());
        if (words.length == 0)
            return new Text("No words found");
        else if (words.length == 1)
            return new Text(words[0]);
        else if (words.length == 2)
            return new Text(words[0] + " " + words[1]);

        // Using treemap instead of hashmap since it sorts keys
        TreeMap<String, Integer> map = new TreeMap<String, Integer>();

        for (String s : words) {
            if (map.containsKey(s))
                map.put(s, map.get(s) + 1);
            else
                map.put(s, 1);
        }

        ArrayList<Map.Entry<String, Integer>> list = new ArrayList<Map.Entry<String, Integer>>(
                map.entrySet());

        Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
            public int compare(Map.Entry<String, Integer> a,
                    Map.Entry<String, Integer> b) {
                return a.getValue() - b.getValue();
            }
        });

        double median = Math.ceil(words.length / 2.0);
        int count = 0;

        if (words.length % 2 == 0) {
            for (int i = 0; i < list.size(); i++) {
                count += list.get(i).getValue();
                if (count == median) {
                    return new Text(list.get(i).getKey() + " "
                            + list.get(i + 1).getKey());
                } else if (count > median) {
                    return new Text(list.get(i).getKey());
                }
            }

        } else {
            for (int i = 0; i < list.size(); i++) {
                count += list.get(i).getValue();
                if (count >= median) {
                    return new Text(list.get(i).getKey());
                }
            }
        }

        return new Text("No median word found");
    }

    @RequestMapping(value = "/sentences/avg_len", method = RequestMethod.POST)
    public Text sentenceAvgLen(@RequestBody Text text) {

        if (text.getText().length() == 0 || !text.getText().matches(".*\\w.*"))
            return new Text("No words found");

        BreakIterator breaker = BreakIterator.getSentenceInstance(Locale.US);
        breaker.setText(text.getText());

        int totalSen = 0;
        int totalWords = 0;

        int start = breaker.first();
        for (int end = breaker.next(); end != BreakIterator.DONE; start = end, end = breaker
                .next()) {
            String sentence = text.getText().substring(start, end);
            String[] words = parse(sentence);
            totalWords += words.length;
            totalSen++;
        }

        return new Text(String.valueOf((double) totalWords / totalSen));
    }

    @RequestMapping(value = "/phones", method = RequestMethod.POST)
    public Text phones(@RequestBody Text text) {
        if (text.getText().length() == 0 || !text.getText().matches(".*\\d+.*"))
            return new Text("No numbers found");

        List<String> phones = new ArrayList<String>();
        phones = parseTelly(text.getText());

        String phoneList = StringUtils.collectionToCommaDelimitedString(phones);
        return new Text(phoneList);

    }


    //helper methods
    public static String[] parse(String text) {
        String trimmed = text
                .replaceAll("[^A-Za-z' ]+|(?<=^|\\W)'|'(?=\\W|$)", "")
                .replaceAll(" +", " ").trim();
        String[] words = trimmed.split("\\s+");
        return words;
    }

    public static List<String> parseTelly(String text) {
        ArrayList<String> phones = new ArrayList<String>();
        Pattern pattern = Pattern
                .compile("\\d{3}[-\\.\\s]\\d{3}[-\\.\\s]\\d{4}|\\d{10}|\\(\\d{3}\\)-\\d{3}-\\d{4}");
        Matcher matcher = pattern.matcher(text);

        while (matcher.find())
            phones.add(matcher.group());

        return phones;
    }

}