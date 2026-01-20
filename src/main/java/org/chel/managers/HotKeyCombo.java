package org.chel.managers;

import java.util.ArrayList;
import java.util.List;

public class HotKeyCombo {
    private String comboName;
    //list that will contain all int represantions of keys
    private List<Integer> keys;

    public HotKeyCombo(String comboName, List<Integer> keys) {
        this.comboName = comboName;
        this.keys = keys;
    }

    public HotKeyCombo(String comboName) {
        this.comboName = comboName;
        this.keys = new ArrayList<>();
    }

    public void setComboName(String comboName) {
        this.comboName = comboName;
    }

    public void setKeys(List<Integer> keys) {
        this.keys = keys;
    }

    public String getComboName() {
        return comboName;
    }

    public List<Integer> getKeys() {
        return keys;
    }
}
