package com.airta.platform.engine.nanoscript;

class OP {
    public static final int TOKEN_VAR = -1;
    public static final int TOKEN_STR = -2;
    public static final int TOKEN_NUM = -3;
    public static final int TOKEN_BOOL = -4;
    public static final int TOKEN_NOP = -5;
    public static final int TOKEN_VOID = -6;
    public static final int TOKEN_INVALID = -7; // invalid just means it's not belong to expre
    public static final int TOKEN_RESERVED = -8;

    public static final int TOKEN_LPR = 0; // (
    public static final int TOKEN_RPR = 1; // )
    public static final int TOKEN_LBK = 3; // [
    public static final int TOKEN_RBK = 2; // ]
    public static final int TOKEN_LBR = 5; // {
    public static final int TOKEN_RBR = 4; // }
    public static final int TOKEN_DEC = 8; // a special stuff
    public static final int TOKEN_CM = 16;


    public static final int TOKEN_MINOP = 0x100; //
    public static final int TOKEN_ASSIGN = 0x100;
    public static final int TOKEN_QM = 0x200; // ? : match

    public static final int TOKEN_OR = 0x301; //
    public static final int TOKEN_AND = 0x401; //

    public static final int TOKEN_BOR = 0x501; //
    public static final int TOKEN_BNOR = 0x601; //
    public static final int TOKEN_BAND = 0x701; //

    public static final int TOKEN_NEQ = 0x801; //
    public static final int TOKEN_EQ = 0x802; //

    public static final int TOKEN_GT = 0x0901; //
    public static final int TOKEN_GET = 0x902; //
    public static final int TOKEN_LT = 0x903; //
    public static final int TOKEN_LET = 0x904; //

    public static final int TOKEN_PLU = 0xA01; //
    public static final int TOKEN_MIN = 0xA02; //
    public static final int TOKEN_MUL = 0xB01; //
    public static final int TOKEN_DIV = 0xB02; //
    public static final int TOKEN_MOD = 0xB03; //

    public static final int TOKEN_SELF_NOT = 0xC01; //
    public static final int TOKEN_SELF_BNOT = 0xC02; //
    public static final int TOKEN_SELF_POSI = 0xC03;
    public static final int TOKEN_SELF_NEGA = 0xC04;

    public static final int TOKEN_INVOKE = 0xD01; // EX_CALL is nothing different
    public static final int TOKEN_INVOKE2 = 0xD02; // for inline call scenario
    // than CALL, but it can not
    // return any value
    public static final int TOKEN_MEMBER = 0xD03; //
    public static final int TOKEN_MEMBER2 = 0xD04;
    public static final int TOKEN_MEMBER3 = 0xD05; // not used yet

    public static final int TOKEN_MAXOP = 0xD05; //


    //public static final int TOKEN_DEC = 0xE01; // a special stuff
    //public static final int TOKEN_CM = 0xE02;
    public static final int TOKEN_JMPF = 0xE03;
    public static final int TOKEN_JMP = 0xE04;
    public static final int TOKEN_DUM = 0xE05;
    public static final int TOKEN_JMPT = 0xE06;
    public static final int TOKEN_LOCA = 0xE07;
    public static final int TOKEN_PARA = 0xE08; // internal op
    public static final int TOKEN_DUM2 = 0xE09;

    public static final int TOKEN_PRIORITY = 0xFF00;
    public static final int TOKEN_IDX = 0xFF;

    public int Flag = 0;
    public String TkValue = null;
    public int pos = 0;

    // for expression!!!!
    public OP _next = null;
    public OP _last = null;

    public OP set(int Flg, String Val) {
        Flag = Flg;
        TkValue = Val;
        return this;
    }

    public OP operate(OP Op, OP Oper2) {
        if (Op._next != null) {
            return this;
        }
        Op._next = null;
        Op._last = this;

        (_next != null ? _last : this)._next = Oper2;
        _last = Oper2._next != null ? Oper2._last : Oper2;
        _last._next = Op;
        _last = Op;
        return this;
    }

    public OP(String val, int flag, int pos) {
        TkValue = val;
        Flag = flag;
        this.pos = pos;
    }

    public OP() {
        TkValue = null;
        Flag = TOKEN_NOP;
    }

    public int getCount() {
        int ret = 1;
        OP r = _next;
        while (r != null) {
            ret++;
            r = r._next;
        }
        return ret;
    }

    @Override
    public String toString() {
        return TkValue;
    }

    public boolean isExprValid() {
        return !(Flag == TOKEN_INVALID || Flag == TOKEN_RESERVED);
    }

    public static int getPriority(int pr) {
        return pr & TOKEN_PRIORITY;
    }

    public int priority() {
        return Flag & TOKEN_PRIORITY;
    }

    public boolean isOper() {
        return Flag < 0 && Flag > TOKEN_INVALID;
    }

    public boolean isBooleanOp() {
        return Flag >= TOKEN_OR && Flag <= TOKEN_LET;
    }

    public boolean isOp() {
        return TOKEN_MINOP <= Flag && Flag <= TOKEN_MAXOP;
    }

    public boolean isInvalid() {
        return Flag <= TOKEN_INVALID;
    }


    public boolean isEndOp() {
        return Flag == TOKEN_RPR || Flag == TOKEN_CM || Flag == TOKEN_DEC
                || Flag == TOKEN_RBK || Flag == TOKEN_RBR; // add ; in the
        // future
    }
}