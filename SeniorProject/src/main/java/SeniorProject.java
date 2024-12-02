import com.squareup.okhttp.*;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.*;
import java.io.*;
import java.util.stream.Collectors;
import org.jfree.chart.*;
import javax.swing.*;
import java.awt.GraphicsEnvironment;

public class SeniorProject {

    public static double [] leftover = new double[10];
    public static double totalMulligans;
    public static int mostMulligans = 0;
    public static HashMap<String, Integer> cardsPlayed = new HashMap<>();

    public static void main(String [] args) throws IOException {
        Scanner scan = new Scanner(System.in);
        int choice = 0;
        double simNum;
        while(choice != 1 && choice != 2){
            System.out.println("Get data for one card: Enter 1\nGet data for multiple cards: Enter 2");
            choice = scan.nextInt();
        }

        if(choice == 1){
            singleCard();
        }
        else{
            System.out.println("How many times would you like to run the simulator? ");
            simNum = scan.nextDouble();
            ArrayList<Card> cardData = cardList();
            ArrayList<Card> tempData = new ArrayList<>();
            for(int i = 0; i < simNum; i++) {
                tempData.clear();
                tempData.addAll(cardData);
                System.out.println("Sim Number " + (i + 1) + ":");
                sim(tempData);
            }
            double mulAverage = totalMulligans / simNum;
            System.out.println("Average number of mulligans per game (assuming infinite mulligans): " + mulAverage);
            System.out.println("Most mulligans required in a single game: " + mostMulligans);
            System.out.println("Average amount of mana leftover after each turn:");
            for(int i = 0; i < leftover.length; i++) {
                System.out.println("Turn " + (i + 1) + ": " + leftover[i] / simNum);
            }
            System.out.println("Number of times each card is played:");
            String [] keys = cardsPlayed.keySet().toArray(new String[0]);
            for(int i = 0; i < keys.length; i++) {
                System.out.println(keys[i] + ": " + cardsPlayed.get(keys[i]));
            }

            if(!GraphicsEnvironment.isHeadless()) {
                DefaultCategoryDataset leftoverData = new DefaultCategoryDataset();
                for (int i = 0; i < leftover.length; i++) {
                    String rowName = "Turn " + (i + 1);
                    leftoverData.setValue(leftover[i] / simNum, "", rowName);
                }

                JFreeChart manaChart = ChartFactory.createBarChart(
                        "Leftover Mana",
                        "Turn",
                        "Mana",
                        leftoverData);

                ChartPanel manaPanel = new ChartPanel(manaChart);
                JFrame manaFrame = new JFrame();
                manaFrame.setSize(800, 600);
                manaFrame.setContentPane(manaPanel);
                manaFrame.setLocationRelativeTo(null);
                manaFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                manaFrame.setVisible(true);

                DefaultPieDataset<String> playedData = new DefaultPieDataset();
                for (int i = 0; i < cardsPlayed.size(); i++) {
                    playedData.setValue(keys[i], cardsPlayed.get(keys[i]));
                }

                JFreeChart playedChart = ChartFactory.createPieChart(
                        "Cards Played",
                        playedData,
                        false,
                        true,
                        false
                );

                ChartPanel playedPanel = new ChartPanel(playedChart);
                JFrame playedFrame = new JFrame();
                playedFrame.setSize(800, 600);
                playedFrame.setContentPane(playedPanel);
                playedFrame.setLocationRelativeTo(null);
                playedFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                playedFrame.setVisible(true);
            }
            else {
                System.out.println("The current system environment does not support JFrame, " +
                        "which is used to display charts of this data.\n" +
                        "Run this program in a headful environment to show them.");
            }
        }
    }

    public static void singleCard() throws IOException {
        String url = "https://api.scryfall.com/cards/named?exact=";
        Scanner scan = new Scanner(System.in);

        System.out.println("Enter a card name:\n");
        String card = scan.nextLine();
        url += card;

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();

        Response response = client.newCall(request).execute();
        //System.out.println(response.body().string());

        JSONObject jsonObject = new JSONObject(response.body().string());
        System.out.println(jsonObject);

        String text = jsonObject.getString("oracle_text");
        String MC = jsonObject.getString("mana_cost");
        System.out.println(text + "\n" + MC);
        if(jsonObject.has("produced_mana")) {
            JSONArray mana = jsonObject.getJSONArray("produced_mana");
            System.out.println(mana);
        }
    }

    public static ArrayList<Card> cardList() throws IOException {
        Scanner scan = new Scanner(System.in);

        System.out.println("Enter the file name:");
        String fileName = scan.nextLine();

        File file = new File(fileName);
        Scanner fileScan = new Scanner(file);
        ArrayList<Card> cardData = new ArrayList<>();
        int count = 0;

        OkHttpClient client = new OkHttpClient();

        while(fileScan.hasNextLine()){
            String url = "https://api.scryfall.com/cards/named?exact=";
            String cardName = fileScan.nextLine();
            Request request = new Request.Builder()
                    .url(url + cardName)
                    .build();
            Response response = client.newCall(request).execute();

            JSONObject jsonObject = new JSONObject(response.body().string());
            String manaCost = jsonObject.getString("mana_cost");
            int cmc = jsonObject.getInt("cmc");
            String type = jsonObject.getString("type_line");
            String text = jsonObject.getString("oracle_text");
            if(jsonObject.has("produced_mana")){
                JSONArray producedMana = jsonObject.getJSONArray("produced_mana");
                String [] temp = new String [producedMana.length()];
                for(int i = 0; i < producedMana.length(); i++){
                    temp[i] = producedMana.getString(i);
                }
                Card newCard = new Card(cardName, manaCost, cmc, type, text, temp);
                cardData.add(newCard);
            }
            else{
                Card newCard = new Card(cardName, manaCost, cmc, type, text);
                cardData.add(newCard);
            }

            count++;
        }

        fileScan.close();

        for(int i = 0; i < cardData.size(); i++){
            System.out.println(cardData.get(i).toString());
            for(int j = 0; j < cardData.get(i).getProducedMana().length; j++){
                System.out.print(cardData.get(i).getProducedMana()[j] + " ");
            }
        }

        return cardData;
    }

    public static void sim(ArrayList<Card> cardData){
        Random rand = new Random();
        ArrayList<Card> hand = new ArrayList<>();
        int deckSize = cardData.size();
        int deckIndex;
        int tempDeckSize;
        int handIndex;
        int mulligans = -1;
        int first = rand.nextInt(2) + 7;
        String colors;
        ArrayList<String> required = new ArrayList<>();
        ArrayList<String> tempRequired;
        //Number of cards requiring the mana
        HashMap<String, Integer> requiredMap = new HashMap<>();
        //Total mana available
        int manaPool = 0;
        ArrayList<Source> battlefield = new ArrayList<>();
        HashMap<String, Integer> manaMap = new HashMap<>();
        List<Card> availableLands;

        do {
            hand.clear();
            tempDeckSize = deckSize;
            for (int i = 0; i < first; i++) {
                handIndex = 0;
                deckIndex = rand.nextInt(tempDeckSize);
                Card tempCard = cardData.get(deckIndex);

                while (handIndex < hand.size()) {
                    if (tempCard.getCmc() <= hand.get(handIndex).getCmc()) {
                        if (tempCard.getTypeLine().contains("Land"))
                            break;
                        else
                            handIndex++;
                    } else
                        handIndex++;
                }

                hand.add(handIndex, tempCard);

                tempDeckSize--;
            }

            availableLands = hand.stream()
                    .filter(p -> p.getTypeLine().contains("Land"))
                    .collect(Collectors.toList());

            mulligans++;
        }
        while(availableLands.size() < 3);

        //Track the total number of mulligans
        totalMulligans += mulligans;
        if(mulligans > mostMulligans)
            mostMulligans = mulligans;

        //For loop here to remove all cards drawn
        //Also do the required thing
        for(int i = 0; i < hand.size(); i++) {
            colors = hand.get(i).getManaCost().replaceAll("[^A-Z]", "");

            cardData.remove(hand.get(i));
            deckSize--;

            //Determine all required colors
            //and keep running count of the number of cards
            //in hand that require each color
            if (colors.length() != 0) {
                requiredMap.putAll(mapRequired(requiredMap, colors, true));
                tempRequired = new ArrayList<>(setRequired(required, requiredMap, colors, true));
                required.clear();
                required.addAll(tempRequired);
            }
        }

        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
        //10 turns of sim
        for(int i = 0; i < 10; i++) {
            System.out.println(deckSize);
            //Testing the hand
            System.out.println("Hand on turn " + (i + 1) + ":\n");
            for (int j = 0; j < hand.size(); j++) {
                System.out.println(hand.get(j).getName() + "\n");
            }

            //Untap everything here
            manaMap.clear();
            manaPool = 0;
            for (int j = 0; j < battlefield.size(); j++) {
                battlefield.get(j).setUntapped(true);
                manaMap.putAll(availableMana(manaMap, battlefield.get(j), true));
                if (battlefield.get(j).isMultiple())
                    manaPool += battlefield.get(j).getColors().replaceAll("[^A-Z]", "").length();
                else
                    manaPool++;
            }

            System.out.println(manaPool);
            System.out.println(Arrays.asList(manaMap));

            System.out.println("All sources untapped");

            //Sort "required" array
            for (int j = 0; j < required.size() - 1; j++) {
                int count = 0;
                while (j - count >= 0 && requiredMap.get(required.get(j - count)) < requiredMap.get(required.get(j + 1 - count))) {
                    Collections.swap(required, j - count, j + 1 - count);
                    count++;
                }
            }

            System.out.println("Required array sorted");

            //Determine best land to play
            //Need to add mulligan if availableLands is below 2 or 3
            //Possibly make optional
            availableLands = hand.stream()
                    .filter(p -> p.getTypeLine().contains("Land"))
                    .collect(Collectors.toList());

            //This fails if entire deck requires generic mana


            Source tempSource;
            if(availableLands.size() > 0) {
                Card bestLand = availableLands.get(0);
                Source bestSource = new Source(bestLand.getOracleText(), bestLand.getTypeLine());
                if (required.size() == 1) {
                    for (int j = 0; j < availableLands.size(); j++) {
                        tempSource = new Source(availableLands.get(j).getOracleText(), availableLands.get(j).getTypeLine());
                        if (tempSource.getColors().contains(required.get(0))) {
                            bestLand = availableLands.get(j);
                            break;
                        }
                    }
                } else if(required.size() != 0){
                    for (int j = 0; j < availableLands.size(); j++) {
                        tempSource = new Source(availableLands.get(j).getOracleText(), availableLands.get(j).getTypeLine());
                        if (tempSource.getColors().contains(required.get(0))
                                && tempSource.getColors().contains(required.get(1))) {
                            bestLand = availableLands.get(j);
                            break;
                        } else if (tempSource.getColors().length() > bestSource.getColors().length()) {
                            bestLand = availableLands.get(j);
                            bestSource = new Source(bestLand.getOracleText(), bestLand.getTypeLine());
                        } else if (tempSource.getColors().contains(required.get(0))
                                && !(bestSource.getColors().contains(required.get(0)))) {
                            bestLand = availableLands.get(j);
                            bestSource = new Source(bestLand.getOracleText(), bestLand.getTypeLine());
                        }
                    }
                }

                System.out.println("Best land found: " + bestLand.getName());

                //Play land and remove card from hand
                battlefield.add(bestSource);
                hand.remove(bestLand);

                if(bestSource.isUntapped()) {
                    //Make possible mana produced known
                    manaMap.putAll(availableMana(manaMap, bestSource, true));

                    //Determine if source produces more than one mana
                    //or just produces one of multiple options
                    if (!bestSource.isMultiple())
                        manaPool++;
                    else
                        manaPool += bestSource.getColors().replaceAll("[^A-Z]", "").length();

                }

                System.out.println(manaPool);
                System.out.println(Arrays.asList(manaMap));
                System.out.println("Mana mapped");

            }

            //Determine all cards that can be played
            boolean flag = true;
            while(flag && !hand.isEmpty()) {
                //Dummy Card failsafe
                Card bestCard = new Card("Dummy Card", "", 0, "", "");
                //Find a playable card
                for (int j = 0; j < hand.size(); j++) {
                    if (!hand.get(j).getTypeLine().contains("Land") && (hand.get(j).getCmc() <= manaPool)
                    && isPlayable(hand.get(j).getManaCost(), manaMap)) {
                        System.out.println("CMC: " + hand.get(j).getCmc());
                        bestCard = hand.get(j);
                        break;
                    }
                }

                if (!bestCard.getName().equals("Dummy Card")) {
                    for (int j = 0; j < hand.size(); j++) {
                        if (!hand.get(j).getTypeLine().contains("Land") && hand.get(j).getCmc() == manaPool
                        && isPlayable(hand.get(j).getManaCost(), manaMap)) {
                            bestCard = hand.get(j);
                            break;
                        }
                        else if (!hand.get(j).getTypeLine().contains("Land") && hand.get(j).getCmc() <= manaPool
                                && manaPool - bestCard.getCmc() > manaPool - hand.get(j).getCmc()
                                && isPlayable(hand.get(j).getManaCost(), manaMap)) {
                            bestCard = hand.get(j);
                        }
                    }

                    System.out.println("Non-Dummy Card found: " + bestCard.getName());

                    //Lambda for all basics
                    List<Source> basicLands = battlefield.stream()
                            .filter(p -> p.isBasic() && p.isUntapped())
                            .collect(Collectors.toList());
                    //Lambda for all nonbasics
                    List<Source> nonbasic = battlefield.stream()
                            .filter(p -> !p.isBasic() && p.isUntapped())
                            .collect(Collectors.toList());
                    //Lambda for all colorless producers
                    List<Source> colorless = battlefield.stream()
                            .filter(p -> p.getColors().contains("C") && p.isUntapped())
                            .collect(Collectors.toList());

                    System.out.println("All available sources parsed");

                    //Tap mana here
                    //First, color pips
                    //Then, generic mana
                    //Maybe do X cost spells here
                    colors = bestCard.getManaCost().replaceAll("[^A-Z]", "");
                    for(int j = 0; j < colors.length(); j++) {
                        boolean basicFlag = false;

                        for(int k = 0; k < basicLands.size(); k++) {
                            if(basicLands.get(k).getColors().contains(colors.substring(j, j + 1))) {
                                battlefield.get(battlefield.indexOf(basicLands.get(k))).setUntapped(false);
                                manaMap.putAll((availableMana(manaMap, basicLands.get(k), false)));
                                basicLands.remove(k);
                                manaPool--;
                                basicFlag = true;
                                break;
                            }
                        }
                        if(!basicFlag) {
                            for(int k = 0; k < nonbasic.size(); k++) {
                                if(nonbasic.get(k).getColors().contains(colors.substring(j, j + 1))) {
                                    battlefield.get(battlefield.indexOf(nonbasic.get(k))).setUntapped(false);
                                    manaMap.putAll(availableMana(manaMap, nonbasic.get(k), false));
                                    nonbasic.remove(k);
                                    manaPool--;
                                    break;
                                }
                            }
                        }
                    }

                    System.out.println("All colored pips accounted for");

                    int generic;
                    String tempGeneric = bestCard.getManaCost().replaceAll("[^0-9]", "");
                    if(tempGeneric.equals(""))
                        generic = 0;
                    else
                        generic = Integer.parseInt(tempGeneric);
                    while(generic > 0) {
                        while(colorless.size() > 0 && generic > 0) {
                            battlefield.get(battlefield.indexOf(colorless.get(0))).setUntapped(false);
                            manaMap.putAll(availableMana(manaMap, colorless.get(0), false));
                            if(colorless.get(0).isMultiple()) {
                                generic -= colorless.get(0).getColors().replace("C", "").length();
                                manaPool -= colorless.get(0).getColors().replace("C", "").length();
                            }
                            else {
                                generic--;
                                manaPool--;
                            }
                            basicLands.remove(colorless.get(0));
                            nonbasic.remove(colorless.get(0));
                            colorless.remove(0);
                        }
                        while(basicLands.size() > 0 && generic > 0){
                            battlefield.get(battlefield.indexOf(basicLands.get(0))).setUntapped(false);
                            manaMap.putAll(availableMana(manaMap, basicLands.get(0), false));
                            generic--;
                            manaPool--;
                            basicLands.remove(0);
                        }
                        while(nonbasic.size() > 0 && generic > 0) {
                            battlefield.get(battlefield.indexOf(nonbasic.get(0))).setUntapped(false);
                            manaMap.putAll(availableMana(manaMap, nonbasic.get(0), false));
                            if(nonbasic.get(0).isMultiple()) {
                                generic -= nonbasic.get(0).getColors().replaceAll("[^A-Z]", "").length();
                                manaPool -= nonbasic.get(0).getColors().replaceAll("[^A-Z]", "").length();
                            }
                            else {
                                generic--;
                                manaPool--;
                            }
                            nonbasic.remove(0);
                        }
                    }

                    System.out.println("All generic mana accounted for");

                    //Add source to battlefield if card produces mana
                    if (!bestCard.getProducedMana()[0].equals("NA")) {
                        tempSource = new Source(bestCard.getOracleText(), bestCard.getTypeLine());
                        battlefield.add(tempSource);
                        //Add to available mana if card is untapped/has haste
                        if(tempSource.isUntapped()) {
                            manaMap.putAll(availableMana(manaMap, tempSource, true));
                            if (!tempSource.isMultiple())
                                manaPool++;
                            else
                                manaPool += tempSource.getColors().replaceAll("[^A-Z]", "").length();
                        }
                    }

                    System.out.println("Checked if card is a source");

                    //Remove card from hand
                    colors = bestCard.getManaCost().replaceAll("[^A-Z]", "");
                    requiredMap.putAll(mapRequired(requiredMap, colors, false));
                    tempRequired = new ArrayList<>(setRequired(required, requiredMap, colors, false));
                    required.clear();
                    required.addAll(tempRequired);

                    //Update cardsPlayed HashMap
                    if(cardsPlayed.containsKey(bestCard.getName())) {
                        cardsPlayed.put(bestCard.getName(), cardsPlayed.get(bestCard.getName()) + 1);
                    }
                    else {
                        cardsPlayed.put(bestCard.getName(), 1);
                    }

                    hand.remove(bestCard);

                    System.out.println("Best card played");
                }
                else {
                    flag = false;
                    System.out.println("Dummy card found");
                }
            }
            //Draw a card here
            handIndex = 0;
            deckIndex = rand.nextInt(deckSize);
            Card tempCard = cardData.get(deckIndex);
            colors = tempCard.getManaCost().replaceAll("[^A-Z]", "");

            System.out.println("New card drawn");

            //Determine all required colors
            //and keep running count of the number of cards
            //in hand that require each color
            if(colors.length() != 0) {
                requiredMap.putAll(mapRequired(requiredMap, colors, true));
                tempRequired = new ArrayList<>(setRequired(required, requiredMap, colors, true));
                required.clear();
                required.addAll(tempRequired);
            }

            System.out.println("All required colors added");

            while(handIndex < hand.size()){
                if(tempCard.getCmc() <= hand.get(handIndex).getCmc()) {
                    if(tempCard.getTypeLine().contains("Land"))
                        break;
                    else
                        handIndex++;
                }
                else
                    handIndex++;
            }

            hand.add(handIndex, tempCard);

            cardData.remove(deckIndex);
            deckSize--;

            leftover[i] += manaPool;

            System.out.println("One Turn Has Passed");
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    public static HashMap<String, Integer> mapRequired(HashMap<String, Integer> requiredMap, String colors, boolean action) {
        if(action){
            for(int i = 0; i < colors.length(); i++) {
                switch (colors.charAt(i)) {
                    case 'W':
                        if(requiredMap.containsKey("W"))
                            requiredMap.put("W", requiredMap.get("W") + 1);
                        else {
                            requiredMap.put("W", 1);
                        }
                        break;
                    case 'U':
                        if(requiredMap.containsKey("U"))
                            requiredMap.put("U", requiredMap.get("U") + 1);
                        else {
                            requiredMap.put("U", 1);
                        }
                        break;
                    case 'B':
                        if(requiredMap.containsKey("B"))
                            requiredMap.put("B", requiredMap.get("B") + 1);
                        else {
                            requiredMap.put("B", 1);
                        }
                        break;
                    case 'R':
                        if(requiredMap.containsKey("R"))
                            requiredMap.put("R", requiredMap.get("R") + 1);
                        else {
                            requiredMap.put("R", 1);
                        }
                        break;
                    case 'G':
                        if(requiredMap.containsKey("G"))
                            requiredMap.put("G", requiredMap.get("G") + 1);
                        else {
                            requiredMap.put("G", 1);
                        }
                        break;
                    case 'C':
                        if(requiredMap.containsKey("C"))
                            requiredMap.put("C", requiredMap.get("C") + 1);
                        else {
                            requiredMap.put("C", 1);
                        }
                        break;
                }
            }
        }
        else {
            for(int i = 0; i < colors.length(); i++) {
                switch (colors.charAt(i)) {
                    case 'W':
                        requiredMap.put("W", requiredMap.get("W") - 1);
                        if(requiredMap.get("W") == 0)
                            requiredMap.remove("W");
                        break;
                    case 'U':
                        requiredMap.put("U", requiredMap.get("U") - 1);
                        if(requiredMap.get("U") == 0)
                            requiredMap.remove("U");
                        break;
                    case 'B':
                        requiredMap.put("B", requiredMap.get("B") - 1);
                        if(requiredMap.get("B") == 0)
                            requiredMap.remove("B");
                        break;
                    case 'R':
                        requiredMap.put("R", requiredMap.get("R") - 1);
                        if(requiredMap.get("R") == 0)
                            requiredMap.remove("R");
                        break;
                    case 'G':
                        requiredMap.put("G", requiredMap.get("G") - 1);
                        if(requiredMap.get("G") == 0)
                            requiredMap.remove("G");
                        break;
                    case 'C':
                        requiredMap.put("C", requiredMap.get("C") - 1);
                        if(requiredMap.get("C") == 0)
                            requiredMap.remove("C");
                        break;
                }
            }
        }

        return requiredMap;
    }

    public static ArrayList<String> setRequired(ArrayList<String> required, HashMap<String, Integer> requiredMap, String colors, boolean action) {
        if(action) {
            for(int i = 0; i < colors.length(); i++) {
                switch (colors.charAt(i)) {
                    case 'W':
                        if (!required.contains("W"))
                            required.add("W");
                        break;
                    case 'U':
                        if (!required.contains("U"))
                            required.add("U");
                        break;
                    case 'B':
                        if (!required.contains("B"))
                            required.add("B");
                        break;
                    case 'R':
                        if (!required.contains("R"))
                            required.add("R");
                        break;
                    case 'G':
                        if (!required.contains("G"))
                            required.add("G");
                        break;
                    case 'C':
                        if (!required.contains("C"))
                            required.add("C");
                        break;
                }
            }
        }
        else{
            for(int i = 0; i < colors.length(); i++) {
                switch (colors.charAt(i)){
                    case 'W':
                        if(!requiredMap.containsKey("W"))
                            required.remove("W");
                        break;
                    case 'U':
                        if(!requiredMap.containsKey("U"))
                            required.remove("U");
                        break;
                    case 'B':
                        if(!requiredMap.containsKey("B"))
                            required.remove("B");
                        break;
                    case 'R':
                        if(!requiredMap.containsKey("R"))
                            required.remove("R");
                        break;
                    case 'G':
                        if(!requiredMap.containsKey("G"))
                            required.remove("G");
                        break;
                    case 'C':
                        if(!requiredMap.containsKey("C"))
                            required.remove("C");
                        break;
                }
            }
        }

        return required;
    }

    public static HashMap<String, Integer> availableMana(HashMap<String, Integer> manaMap, Source source, boolean action) {
        String colors = source.getColors().replaceAll("[^A-Z]", "");
        //Case: add mana
        if(action) {
            for(int i = 0; i < colors.length(); i++) {
                switch (colors.charAt(i)) {
                    case 'W':
                        if(manaMap.containsKey("W"))
                            manaMap.put("W", manaMap.get("W") + 1);
                        else
                            manaMap.put("W", 1);
                        break;
                    case 'U':
                        if(manaMap.containsKey("U"))
                            manaMap.put("U", manaMap.get("U") + 1);
                        else
                            manaMap.put("U", 1);
                        break;
                    case 'B':
                        if(manaMap.containsKey("B"))
                            manaMap.put("B", manaMap.get("B") + 1);
                        else
                            manaMap.put("B", 1);
                        break;
                    case 'R':
                        if(manaMap.containsKey("R"))
                            manaMap.put("R", manaMap.get("R") + 1);
                        else
                            manaMap.put("R", 1);
                        break;
                    case 'G':
                        if(manaMap.containsKey("G"))
                            manaMap.put("G", manaMap.get("G") + 1);
                        else
                            manaMap.put("G", 1);
                        break;
                    case 'C':
                        if(manaMap.containsKey("C"))
                            manaMap.put("C", manaMap.get("C") + 1);
                        else
                            manaMap.put("C", 1);
                }
            }
        }
        //Case: remove mana
        else {
            for(int i = 0; i < colors.length(); i++) {
                switch (colors.charAt(i)) {
                    case 'W':
                        manaMap.put("W", manaMap.get("W") - 1);
                        break;
                    case 'U':
                        manaMap.put("U", manaMap.get("U") - 1);
                        break;
                    case 'B':
                        manaMap.put("B", manaMap.get("B") - 1);
                        break;
                    case 'R':
                        manaMap.put("R", manaMap.get("R") - 1);
                        break;
                    case 'G':
                        manaMap.put("G", manaMap.get("G") - 1);
                        break;
                    case 'C':
                        manaMap.put("C", manaMap.get("C") - 1);
                        break;
                }
            }
        }

        return manaMap;
    }

    public static boolean isPlayable(String manaCost, HashMap<String, Integer> manaMap) {
        boolean flag = true;
        if(manaCost.contains("W") && manaMap.containsKey("W")) {
            if(manaCost.length() - manaCost.replace("W", "").length() > manaMap.get("W"))
                flag = false;
        }
        if(manaCost.contains("U") && manaMap.containsKey("U")) {
            if(manaCost.length() - manaCost.replace("U", "").length() > manaMap.get("U"))
                flag = false;
        }
        if(manaCost.contains("B") && manaMap.containsKey("B")) {
            if(manaCost.length() - manaCost.replace("B", "").length() > manaMap.get("B"))
                flag = false;
        }
        if(manaCost.contains("R") && manaMap.containsKey("R")) {
            if(manaCost.length() - manaCost.replace("R", "").length() > manaMap.get("R"))
                flag = false;
        }
        if(manaCost.contains("G") && manaMap.containsKey("G")) {
            if(manaCost.length() - manaCost.replace("G", "").length() > manaMap.get("G"))
                flag = false;
        }
        if(manaCost.contains("C") && manaMap.containsKey("C")) {
            if(manaCost.length() - manaCost.replace("C", "").length() > manaMap.get("C"))
                flag = false;
        }

        return flag;
    }

}
