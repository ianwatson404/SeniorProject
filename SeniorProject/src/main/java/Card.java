public class Card {
    private String name;
    private String manaCost;
    private int cmc;
    private String typeLine;
    private String oracleText;
    private String [] producedMana;

    public Card(String name, String manaCost, int cmc, String typeLine, String oracleText, String[] producedMana) {
        this.name = name;
        this.manaCost = manaCost;
        this.cmc = cmc;
        this.typeLine = typeLine;
        this.oracleText = oracleText;
        this.producedMana = producedMana;
    }

    public Card(String name, String manaCost, int cmc, String typeLine, String oracleText) {
        this.name = name;
        this.manaCost = manaCost;
        this.cmc = cmc;
        this.typeLine = typeLine;
        this.oracleText = oracleText;
        producedMana = new String[]{"NA"};
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getManaCost() {
        return manaCost;
    }

    public void setManaCost(String manaCost) {
        this.manaCost = manaCost;
    }

    public int getCmc() {
        return cmc;
    }

    public void setCmc(int cmc) {
        this.cmc = cmc;
    }

    public String getTypeLine() {
        return typeLine;
    }

    public void setTypeLine(String typeLine) {
        this.typeLine = typeLine;
    }

    public String getOracleText() {
        return oracleText;
    }

    public void setOracleText(String oracleText) {
        this.oracleText = oracleText;
    }

    public String[] getProducedMana() {
        return producedMana;
    }

    public void setProducedMana(String[] producedMana) {
        this.producedMana = producedMana;
    }

    @Override public String toString(){
        return(this.getName() + " " + this.getManaCost() + " " + this.getCmc() + " " +
                this.getTypeLine() + " " + this.getOracleText());
    }
}
