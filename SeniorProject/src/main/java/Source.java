import java.util.*;
import java.util.regex.*;

public class Source {
    private String colors;
    private String type;
    private boolean basic;
    private boolean untapped;
    private boolean multiple;
    private boolean filter;

    public Source(String oracleText, String typeLine) {
        //First check type line and if the card enters tapped
        //For simplicity, all cards that say "tapped" will enter tapped
        if(typeLine.contains("Land")){
            type = "Land";
            if(oracleText.contains("tapped"))
                untapped = false;
            else
                untapped = true;
            if(typeLine.contains("Basic"))
                basic = true;
            else
                basic = false;
        }
        else if(typeLine.contains("Creature")) {
            if(oracleText.toLowerCase().contains("haste"))
                untapped = true;
            else
                untapped = false;
            type = "Creature";
            basic = false;
        }
        else if(typeLine.contains("Artifact")) {
            type = "Artifact";
            basic = false;
            if(oracleText.contains("tapped"))
                untapped = false;
            else
                untapped = true;
        }



        colors = "";
        String [] tempArray;
        Pattern pattern = Pattern.compile("\\{T}: Add (.*?)\\.");
        Matcher matcher = pattern.matcher(oracleText);

        while(matcher.find()) {
            String substring = matcher.group(0);

            //Determine mana the source produces
            if(oracleText.contains("mana of any")) {
                colors += "{A}";
            }

            if(substring.contains("{W}")) {
                tempArray = substring.split("\\{W}");
                for(int i = 0; i < tempArray.length - 1; i++) {
                    colors += "{W}";
                }
            }
            if(substring.contains("{U}")) {
                tempArray = substring.split("\\{U}");
                for(int i = 0; i < tempArray.length - 1; i++) {
                    colors += "{U}";
                }
            }
            if(substring.contains("{B}")) {
                tempArray = substring.split("\\{B}");
                for(int i = 0; i < tempArray.length - 1; i++) {
                    colors += "{B}";
                }
            }
            if(substring.contains("{R}")) {
                tempArray = substring.split("\\{R}");
                for(int i = 0; i < tempArray.length - 1; i++) {
                    colors += "{R}";
                }
            }
            if(substring.contains("{G}")) {
                tempArray = substring.split("\\{G}");
                for(int i = 0; i < tempArray.length - 1; i++) {
                    colors += "{G}";
                }
            }
            if(substring.contains("{C}")) {
                tempArray = substring.split("\\{C}");
                for(int i = 0; i < tempArray.length - 1; i++) {
                    colors += "{C}";
                }
            }
        }

        //Determine if source produces one or more mana
        if(oracleText.contains("}{"))
            multiple = true;
        else
            multiple = false;

        //Determine if source is a filter
        if(oracleText.contains("}, {T}:"))
            filter = true;
        else
            filter = false;
    }

    public String getColors() {
        return colors;
    }

    public void setColors(String colors) {
        this.colors = colors;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isBasic() {
        return basic;
    }

    public void setBasic(boolean basic) {
        this.basic = basic;
    }

    public boolean isUntapped() {
        return untapped;
    }

    public void setUntapped(boolean untapped) {
        this.untapped = untapped;
    }

    public boolean isMultiple() {
        return multiple;
    }

    public void setMultiple(boolean multiple) {
        this.multiple = multiple;
    }

    public boolean isFilter() {
        return filter;
    }

    public void setFilter(boolean filter) {
        this.filter = filter;
    }
}
