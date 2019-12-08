package com.airta.platform.engine.nanoscript;

import java.util.ArrayList;

public class Block {
    public int lineNumber = 0;
    public String name = null;
    public Tokenizer nizer = null;
    public ArrayList<Block> content = null;
    public boolean isScope = false;

    public static final int NORMAL = 0, VAR = 1, IF = 2, IFSUB = 3, IFDEF = 4, FUNC = 5;
    public int type = 0; // statement, if, ifchild, function
    public OP expr = null; // compiled expr if have

    public Block(int lineNumber, String name, Tokenizer tokenizer, boolean isScope, int type) {
        this.lineNumber = lineNumber;
        this.name = name;
        this.nizer = tokenizer;
        this.isScope = isScope;
        this.type = type;
    }

    public void addStatment(Block block) {
        if (content == null) content = new ArrayList<>();
        content.add(block);
    }
}