package com.airta.platform.engine.nanoscript;

import java.util.ArrayList;
import java.util.List;

class Tokenizer {
    private static final String reservedKeywords = ".if.elif.else.while.do.for.return.var.me.func.";
    private List<OP> sops;
    private int pos;
    private String err = null;
    private String origin = null;
    private boolean invalid = false;

    public boolean isUserCall(String Name) {
        return false;
    }

    public boolean isUserObject(String Name) {
        return false;
    }

    public OP op() {
        return sops.get(pos);
    }

    public OP nextOp() {
        if (pos < sops.size() - 1) {
            return sops.get(pos + 1);
        }
        return null;
    }

    public boolean hasNext() {
        return pos < sops.size() - 1;
    }

    public void reset() {
        pos = -1;

    }

    public OP nextPrev() {
        if (pos < sops.size() - 1) {
            return sops.get(pos++);
        }
        return null;
    }

    public OP nextNext() {
        if (pos < sops.size() - 1) {
            return sops.get(++pos);
        }
        return null;
    }

    public boolean next() {
        if (pos < sops.size() - 1) {
            pos++;
            return true;
        }
        return false;
    }

    public void prev() {
        if (pos >= 0) {
            pos--;
        }
    }

    public boolean expectOp() {
        if (pos < sops.size() - 1
                && (sops.get(pos + 1).isOp())) {
            pos++;
            return true;
        }
        return false;
    }

    public int getPos() {
        return pos;
    }

    public void setPos(int pos) {
        this.pos = pos;
    }

    public boolean expect2(int flag1, int flag2) {
        if (pos < sops.size() - 2 && sops.get(pos + 1).Flag == flag1
                && sops.get(pos + 2).Flag == flag2) {
            pos++;
            return true;
        } else {
            return false;
        }
    }

    public void setEof() {
        if (sops != null) {
            pos = sops.size() - 1;
        } else {
            pos = -1;
        }
    }

    public boolean expect(int flag) {
        if (pos < sops.size() - 1 && sops.get(pos + 1).Flag == flag) {
            pos++;
            return true;
        }
        return false;
    }

    public OP getFirstToken() {
        if (sops != null && sops.size() > 0) {
            return sops.get(0);
        } else {
            return null;
        }
    }

    public OP getLastToken() {
        if (sops != null && sops.size() > 0) {
            return sops.get(sops.size() - 1);
        } else {
            return null;
        }
    }

    public Tokenizer(String src, boolean ignoreError) {
        if (src != null) {
            sops = new ArrayList<OP>();
            err = _tokenize(src, ignoreError);
            if (err != null) {
                sops = null;
            } else {
                pos = -1;
            }
        } else {
            sops = null;
        }
    }

    public int getOpSize() {
        return sops != null ? sops.size() : -1;
    }

    public String getErr() {
        return err;
    }

    /**
     * a(a,b=):
     * b: expr = 123;
     * if expr:
     * elif expr:
     * else:
     * <p>
     * functionname(): expr
     * -aa
     * <p>
     * if expr:
     * elif expr:
     * else:
     * this is all we supported so far
     *
     * @param source
     * @return
     */
    private String _tokenize(String source, boolean igoreError) {
        char[] chars = source.toCharArray();
        int pos = 0;
        int len = chars.length;
        int start = -1;
        int end = 0;
        char c = ' ';
        int state = 0;
        String str = "";
        origin = source;
        char h = ' ';
        for (pos = 0; pos < len; pos++) {
            c = chars[pos];
            if (Character.isWhitespace(c)) {
                continue;
            }
            switch (c) {
                case '\"':
                case '\'':
                    state = 0;
                    h = c;
                    start = end = ++pos;
                    for (; pos < len; pos++) {
                        c = chars[pos];
                        if (state == 1) {
                            switch (c) {
                                case 'r':
                                    c = '\r';
                                    break;
                                case 'n':
                                    c = '\n';
                                    break;
                                case 'b':
                                    c = '\b';
                                    break;
                                case 't':
                                    c = '\t';
                                    break;
                                case '\"':
                                case '\'':
                                default:
                                    break;
                            }
                            state = 0;
                            chars[end++] = c;
                        } else if (c == h) {
                            sops.add(new OP(new String(chars, start, end - start),
                                    OP.TOKEN_STR, start - 1));
                            start = -1;
                            break;
                        } else if (c == '\\') {
                            state = 1;
                        } else {
                            chars[end++] = c;
                        }
                    }
                    if (start != -1) {
                        // this error can not be ignored
                        return "unexpected string dec";
                    }
                    break;
                case '(':
                case ')':
                case '[':
                case ']':
                case '{':
                case '}':
                    sops.add(new OP(c + "", "()][}{".indexOf(c), pos));
                    if (c == '{' || c == '}') {
                        if (!igoreError) {
                            return "{} scope is disabled";
                        } else {
                            sops.add(new OP(null, OP.TOKEN_INVALID, pos));
                            len = pos;
                        }
                    }
                    break;
                case '+':
                    sops.add(new OP(c + "", OP.TOKEN_PLU, pos));
                    break;
                case '-':
                    sops.add(new OP(c + "", OP.TOKEN_MIN, pos));
                    break;
                case '*':
                    sops.add(new OP(c + "", OP.TOKEN_MUL, pos));
                    break;
                case '/':
                    sops.add(new OP(c + "", OP.TOKEN_DIV, pos));
                    break;
                case '%':
                    sops.add(new OP(c + "", OP.TOKEN_MOD, pos));
                    break;
                case '|':
                    if (pos < len - 1 && chars[pos + 1] == '|') {
                        sops.add(new OP("||", OP.TOKEN_OR, pos));
                        pos++;
                    } else {
                        sops.add(new OP("|", OP.TOKEN_BOR, pos));
                    }
                    break;
                case '&':
                    if (pos < len - 1 && chars[pos + 1] == '&') {
                        sops.add(new OP("&&", OP.TOKEN_AND, pos));
                        pos++;
                    } else {
                        sops.add(new OP("&", OP.TOKEN_BAND, pos));
                    }
                    break;
                case '^':
                    sops.add(new OP("^", OP.TOKEN_BNOR, pos));
                    break;
                case '>':
                case '<':
                    if (pos < len - 1 && chars[pos + 1] == '=') {
                        sops.add(new OP(c == '>' ? ">=" : "<=",
                                c == '>' ? OP.TOKEN_GET : OP.TOKEN_LET, pos));
                        pos++;
                    } else if (pos < len - 1
                            && (chars[pos + 1] == '>' || chars[pos + 1] == '<')) {
                        if (chars[pos + 1] == c) {
                            //ops.add(new OP(c == '>' ? ">>" : "<<",
                            //        c == '>' ? OP.TOKEN_RIGHT_JOIN
                            //                : OP.TOKEN_LEFT_JOIN));
                        } else {
                            //ops.add(new OP(c == '>' ? "><" : "<>",
                            //       c == '>' ? OP.TOKEN_INNER_JOIN
                            //               : OP.TOKEN_OUTER_JOIN));
                        }
                        pos++;
                        if (!igoreError) {
                            return "bit op is disabled!";
                        }
                        sops.add(new OP(null, OP.TOKEN_INVALID, pos));
                    } else {
                        sops.add(new OP(c + "", c == '>' ? OP.TOKEN_GT : OP.TOKEN_LT, pos));
                    }
                    break;
                case '!':
                    if (pos < len - 1 && chars[pos + 1] == '=') {
                        sops.add(new OP("!=", OP.TOKEN_NEQ, pos));
                        pos++;
                    } else {
                        sops.add(new OP("!", OP.TOKEN_SELF_NOT, pos));
                    }
                    break;
                case '~':
                    sops.add(new OP("!", OP.TOKEN_SELF_BNOT, pos));
                    break;
                case '=':
                    if (pos < len - 1 && chars[pos + 1] == '=') {
                        sops.add(new OP("=", OP.TOKEN_EQ, pos));
                        pos++;
                    } else {
                        sops.add(new OP("=", OP.TOKEN_ASSIGN, pos));
                    }
                    break;
                case ':':
                    sops.add(new OP(":", OP.TOKEN_DEC, pos));
                    break;
                case '?':
                    sops.add(new OP("?", OP.TOKEN_QM, pos));
                    break;
                case '@':
                    sops.add(new OP("@", OP.TOKEN_LOCA, pos));
                    break;
                case ',':
                    sops.add(new OP(",", OP.TOKEN_CM, pos));
                    break;
                case '.':
                    // could be a double
                    if (pos < len - 1 && Character.isDigit(chars[pos + 1])) {
                        // read all digits
                        start = pos++;
                        for (; pos < len; pos++) {
                            c = chars[pos];
                            if (!Character.isDigit(c)) {
                                if (!Character.isWhitespace(c)) {
                                    start = -1;
                                }
                                break;
                            }
                        }
                        if (start == -1) {
                            if (!igoreError) {
                                return "invalid number format";
                            }
                            invalid = true;
                            sops.add(new OP(null, OP.TOKEN_INVALID, pos));
                            pos--;
                            break;
                        }
                        sops.add(new OP("0" + new String(chars, start, pos - start),
                                OP.TOKEN_NUM, start));
                        if (pos != len) {
                            pos--;
                        }
                    } else {
                        sops.add(new OP(".", OP.TOKEN_MEMBER, pos));
                    }
                    break;
                case '#':
                    len = pos;
                    break; // it's a comment already
                default:
                    if (Character.isLetter(c) || c == '_') {
                        start = pos++;
                        for (; pos < len; pos++) {
                            c = chars[pos];
                            if (!(Character.isLetter(c) || Character.isDigit(c) || c == '_')) {
                                break;
                            }
                        }
                        str = new String(chars, start, pos - start);

                        if (str.equals("true") || str.equals("false")) {
                            sops.add(new OP(str, OP.TOKEN_BOOL, start));
                        } else if (str.equals("null")) {
                            sops.add(new OP(str, OP.TOKEN_VOID, start));
                        } else {
                            sops.add(new OP(str, reservedKeywords.indexOf("." + str + ".") >= 0 ? OP.TOKEN_RESERVED : OP.TOKEN_VAR, start));
                        }
                        if (pos != len) {
                            pos--;
                        }
                    } else if (Character.isDigit(c)) {
                        start = pos++;
                        state = 0;
                        for (; pos < len; pos++) {
                            c = chars[pos];
                            if (Character.isDigit(c)) {
                                if (state == 1) {
                                    state = 2;
                                }
                                continue;
                            }
                            if (c == '.') {
                                if (state != 0) {
                                    break;
                                }
                                state = 1;
                                continue;
                            }
                            if (Character.isLetter(c)) {
                                start = -1;
                            }
                            break;
                        }
                        if (start == -1) {
                            if (!igoreError) {
                                return "invalid number format";
                            }
                            pos--;
                            invalid = true;
                            sops.add(new OP(null, OP.TOKEN_INVALID, start));
                            break;
                        }
                        if (state == 1) {
                            sops.add(new OP(new String(chars, start, pos - start)
                                    + "0", OP.TOKEN_NUM, start));
                        } else {
                            sops.add(new OP(new String(chars, start, pos - start),
                                    OP.TOKEN_NUM, start));
                        }
                        if (pos != len) {
                            pos--;
                        }
                    } else {
                        if (!igoreError) {
                            return "unexpected token";
                        }
                        invalid = true;
                        sops.add(new OP(null, OP.TOKEN_INVALID, pos));
                    }
                    break;
            }
        }
        if (len != chars.length) {
            origin = source.substring(0, len);
        }
        return null;
    }

    public String getOrigin() {
        return origin;
    }

    public boolean hasInvalidToken() {
        return invalid;
    }
}