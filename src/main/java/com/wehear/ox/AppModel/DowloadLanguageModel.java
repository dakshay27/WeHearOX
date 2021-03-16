package com.wehear.ox.AppModel;

public class DowloadLanguageModel {
    String languageName;
    boolean isFav;
    boolean isProcess;
    String langaugeCode;

    public DowloadLanguageModel() {
    }

    public String getLanguageName() {
        return languageName;
    }

    public void setLanguageName(String languageName) {
        this.languageName = languageName;
    }

    public boolean isFav() {
        return isFav;
    }

    public void setFav(boolean fav) {
        isFav = fav;
    }

    public boolean isProcess() {
        return isProcess;
    }

    public void setProcess(boolean process) {
        isProcess = process;
    }

    public String getLangaugeCode() {
        return langaugeCode;
    }

    public void setLangaugeCode(String langaugeCode) {
        this.langaugeCode = langaugeCode;
    }
}
