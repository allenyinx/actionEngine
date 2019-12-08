package com.airta.platform.engine.nanoscript;

import java.util.ArrayList;
import java.util.Stack;

/**
 * a static light weight compiler for flow script
 * flow script is the main action interpreted layer
 */
public class Parser {

    public static ArrayList<Block> compile(String src) throws Exception {
        if (src == null || src.length() < 1) {
            throw new Exception("no script to parse!");
        }
        return compileInternal(src);
    }

    private static String _escapeString = "()[]{}:#-\"\'"; // {} is not allowed anyway

    /**
     * parse source code into line segment, remove macro, meta stuff, only script allowed
     *
     * @param source
     * @return
     */
    private static ArrayList<Block> compileInternal(String source) throws Exception {
        // preprocess the source code, replace the ^
        // stage 1, line based procesing
        ArrayList<Block> lines = parseLines(source);
        // stage 2, statement processing
        lines = parseBlocks(lines);
        // stage 3, syntax procesing
        compileBlocks(lines);
        return lines;
    }


    private static ArrayList<Block> parseLines(String source) throws Exception {
        String[] lines = source.split("\n");
        char[] buf = source.toCharArray();
        int pos = 0;
        int len = 0;
        char last = ' ';
        for (int i = 0; i < buf.length; i++) {
            char c = buf[pos++];
            if (last == '^') {
                int idx = _escapeString.indexOf(c);
                if (idx >= 0) {
                    buf[len++] = (char) idx;
                } else {
                    buf[len++] = c;
                }
            } else if (c == '^') {
                last = c;
            } else {
                buf[len++] = c;
            }
            last = c;
        }
        source = new String(buf, 0, len);
        ArrayList<Block> lns = new ArrayList<>();
        for (int i = 0; i < lines.length; i++) {
            String l = lines[i].trim();
            if (l.length() < 1) {
                continue;
            }
            if (l.startsWith("#")) {
                continue;
            }
            Tokenizer nizer = new Tokenizer(l, true);
            if (nizer.getErr() != null || nizer.getOpSize() < 1) {
                throw new Exception("error@line " + (i + 1) + ":" + (nizer.getErr() == null ? "expect tokens" : nizer.getErr()));
            }
            lns.add(new Block(i + 1, null, nizer, false, 0));
        }
        return lns;
    }

    private static ArrayList<Block> parseBlocks(ArrayList<Block> lines) throws Exception {
        Block global = new Block(0, "#main#", null, true, Block.FUNC);
        Block func = null;
        Stack<Block> ifs = new Stack<>(); // as current design, only if/else can be stacked (if elif else)
        Block pa = global;
        Block first = null;
        for (Block l : lines) {
            // function(): expr scenario
            OP p = l.nizer.nextNext();
            if (pa == null) {
                if (!ifs.isEmpty()) {
                    pa = ifs.peek();
                    pa = pa.content.get(pa.content.size() - 1);
                } else if (func != null) {
                    pa = func;
                } else {
                    pa = global;
                }
            }

            if (p.Flag == OP.TOKEN_MIN || first == null) { // -, or it could be a empty statement, or  a invalid expresion
                if (p.Flag == OP.TOKEN_MIN && l.nizer.getOpSize() == 1 && !l.nizer.hasInvalidToken()) {
                    // end scope
                    if (!ifs.isEmpty()) {
                        ifs.pop();
                    } else if (func != null) {
                        func = null;
                    } else {
                        // global can not be eneded
                        throw new Exception("unexpected -, not scope to close");
                    }
                    pa = null;
                } else {
                    if (p.Flag == OP.TOKEN_MIN) {
                        p = l.nizer.nextNext();
                    }
                    if (p.Flag == OP.TOKEN_RESERVED && (p.TkValue.equals("if") || p.TkValue.equals("elif") || p.TkValue.equals("else"))) {
                        // if/elif/else/var, currently we only support those
                        if (p.TkValue.equals("if") || pa.name.equals("if") || pa.name.equals("elif")) {
                            l.isScope = true;
                            l.name = p.TkValue;
                            if (p.TkValue.equals("if")) {
                                ifs.push(l);
                                l.addStatment(new Block(l.lineNumber, "if", l.nizer, true, Block.IFSUB));
                                pa.addStatment(l);
                                l.type = Block.IF;
                                pa = l.content.get(0);
                            } else {
                                ifs.peek().addStatment(l);
                                l.type = l.name.equals("elif") ? Block.IFSUB : Block.IFDEF;
                                pa = l;
                            }
                            first = null;
                        } else {
                            throw new Exception(p.TkValue + " is not allowed in current scope!");
                        }
                    } else if (p.Flag == OP.TOKEN_RESERVED && p.TkValue.equals("var")) {
                        // this is a var statement
                        if (!l.nizer.expect(OP.TOKEN_VAR)) {
                            throw new Exception("var expect a var name=value_expr format");
                        }
                        l.name = l.nizer.op().TkValue;
                        l.type = Block.VAR;
                        pa.addStatment(l);
                    } else if (p.Flag == OP.TOKEN_RESERVED && p.TkValue.equals("func")) {
                        // this should be a function call
                        if (!pa.name.equals("#main#")) {
                            throw new Exception("function is only allowed in global scope!");
                        }
                        p = l.nizer.nextNext();
                        if (p == null || p.Flag != OP.TOKEN_VAR) {
                            throw new Exception("function name expected!");
                        }
                        l.isScope = true;
                        l.name = p.TkValue;
                        l.type = Block.FUNC;
                        global.addStatment(func = l);
                        first = null;
                    } else {
                        l.type = Block.NORMAL;
                        pa.addStatment(l); // a standard
                        l.name = "-";
                    }
                }
            } else {
                // add back to previous statement
                if (pa.content == null || pa.content.size() < 1) {
                    pa.addStatment(l);
                    l.name = "-";
                } else {
                    Block block = pa.content.get(pa.content.size() - 1);
                    if (block.type == Block.VAR) {
                        throw new Exception("var block can not have multiple lines!");
                    }
                    block.nizer = new Tokenizer(block.nizer.getOrigin() + "\n" + l.nizer.getOrigin(), true);
                    //block.addStatment(l); // this is the partial block
                }
            }
            if (first == null) {
                first = l;
            }
        }
        if (!ifs.isEmpty() || func != null) {
            throw new Exception("unclosed scope: " + (ifs.isEmpty() ? (func.name + "@" + func.lineNumber) : (ifs.peek().lineNumber + "@" + ifs.peek().name)));
        }
        if (global.content == null || global.content.size() < 1) {
            throw new Exception("empty/invalid script!");
        }
        return global.content;
    }


    // check all the blocks, compile them as needed
    private static void compileBlocks(ArrayList<Block> blocks) throws Exception {
        if (blocks == null || blocks.size() < 1) {
            return;
        }
        ArrayList<Block> ret = new ArrayList<>();
        for (Block block : blocks) {
            //NORMAL  = 0, VAR  = 1, IF = 2, IFSUB = 3, IFDEF = 4, FUNC = 5
            block.nizer.reset();
            OP p = block.nizer.nextNext(); // skip -
            if (p.Flag == OP.TOKEN_MIN) {
                p = block.nizer.nextNext();
            }
            if (block.type == Block.NORMAL) {
                block.nizer.prev();
                block.expr = parseExpr(block.nizer, null, 0);
            } else if (block.type == Block.VAR) {
                p = block.nizer.nextNext();
                p = block.nizer.nextNext();
                if (p.Flag != OP.TOKEN_ASSIGN) {
                    throw new Exception("var expect name = expr format!@" + block.lineNumber);
                }
                block.expr = parseExpr(block.nizer, null, 0);
            } else if (block.type == Block.IF) {
                compileBlocks(block.content);
            } else if (block.type == Block.IFSUB) {
                // if or elif
                block.expr = parseExpr(block.nizer, null, 0); //
                if (block.expr == null) {
                    throw new Exception(block.name + " expect a expression!");
                }
                compileBlocks(block.content);
            } else if (block.type == Block.IFDEF) {
                if (block.nizer.hasNext()) {
                    throw new Exception("invalid else block: " + block.nizer.getOrigin() + "@" + block.lineNumber);
                }
                compileBlocks(block.content);
            } else {
                //if(block.type == Block.FUNC)continue; // function is skipped for now
                continue;
            }
        }
    }

    private static OP getInvalidOper(int start, Tokenizer st, int endOps, boolean force) {
        OP op = null;
        OP oper = null; // keep find the
        OP temp = null;
        OP ret = null;
        String tmp = null;
        boolean scope = true;
        while (st.hasNext()) {
            op = st.nextNext();
            if (op.Flag == OP.TOKEN_LBK && scope) {
                temp = parseExpr(st, _noPriority, OP.TOKEN_RBK);
                if (st.expect(OP.TOKEN_RBK)) {
                    if (temp == null) {
                        temp = new OP("", OP.TOKEN_STR, start);
                    }

                    if (op.pos > start) {
                        tmp = trimPos(st.getOrigin(), start, op.pos, oper != null ? 0 : 1);
                        if (tmp.length() > 0) {
                            if (oper == null) {
                                oper = new OP(tmp, OP.TOKEN_STR, start);
                            } else {
                                oper = oper.operate(new OP("+", OP.TOKEN_PLU, oper.pos), new OP(tmp, OP.TOKEN_STR, start));
                            }
                        }
                    }
                    oper = oper != null ? oper.operate(new OP("+", OP.TOKEN_PLU, oper.pos), temp) : temp;
                    start = st.op().pos + 1;

                } else {
                    scope = false;
                }
            }
            if (endOps != 0 && op.isEndOp() && (op.Flag & endOps) != 0) {
                st.prev();
                tmp = trimPos(st.getOrigin(), start, op.pos, oper == null ? -1 : 2);
                if (oper == null) {
                    ret = new OP(tmp, OP.TOKEN_STR, start);
                } else if (start < op.pos) {
                    ret = oper.operate(new OP("+", OP.TOKEN_PLU, oper.pos), new OP(tmp, OP.TOKEN_STR, start));
                } else {
                    ret = oper;
                }
                break;
            }
        }
        if ((force && endOps != 0) || ret != null) {
            return ret;
        }
        st.setEof();
        ret = new OP(st.getOrigin().substring(start).trim(), OP.TOKEN_STR, start);
        if (oper != null) {
            ret = oper.operate(new OP("+", OP.TOKEN_PLU, oper.pos), ret);
        }
        return ret;
    }

    private static String trimPos(String str, int start, int pos, int mode) {
        if ((mode & 1) != 0) {
            while (start < pos && Character.isWhitespace(str.charAt(start))) {
                start++;
            }
        }
        if ((mode & 2) != 0) {
            while (start < pos && Character.isWhitespace(str.charAt(pos - 1))) {
                pos--;
            }
        }
        return str.substring(start, pos);
    }

    private static final OP _noPriority = new OP("dum", 0, 0); // 0 as lowest priority

    // this is running in error tolerance mode as always
    private static OP parseExpr(Tokenizer st, OP priority, int endOps) {
        if (!st.hasNext()) {
            return null;
        }
        OP oper1 = st.nextNext();
        if (oper1.isEndOp()) {
            if ((endOps & oper1.Flag) != 0) {
                st.prev();
                return null;
            }
            return getInvalidOper(oper1.pos, st, endOps, false);
        }
        int p = oper1.pos;
        if (oper1.Flag == OP.TOKEN_LBK) {
            oper1 = parseExpr(st, _noPriority, OP.TOKEN_RBK);
            if (oper1 == null || !st.expect(OP.TOKEN_RBK)) {
                //st.prev();
                return getInvalidOper(p, st, endOps, false); // invalid
            }
            oper1.pos = p;
            oper1.operate(new OP("dum2", OP.TOKEN_DUM2, oper1.pos), new OP("nop", OP.TOKEN_NOP, p));
        } else if (oper1.Flag <= OP.TOKEN_VAR) {
            if (!oper1.isExprValid()) {
                return getInvalidOper(p, st, endOps, false);
            }
        } else if (oper1.Flag == OP.TOKEN_LPR) {
            oper1 = parseExpr(st, _noPriority, OP.TOKEN_RPR);
            if (oper1 != null && !st.expect(OP.TOKEN_RPR)) {
                return getInvalidOper(p, st, OP.TOKEN_RPR, false);
                //return new OP(st.getOrigin().substring(p),OP.TOKEN_STR,p);
            }
            oper1.pos = p;
            oper1.operate(new OP("dum2", OP.TOKEN_DUM2, oper1.pos), new OP("nop", OP.TOKEN_NOP, p));
            //throw new Exception("expect a closed )");
        } else if (oper1.Flag == OP.TOKEN_LBR) {
            // support object (json like)
            //TODO: not implemented yet, not necessary to support it yet
        } else if (oper1.Flag == OP.TOKEN_SELF_NOT
                || oper1.Flag == OP.TOKEN_SELF_BNOT) {
            oper1 = new OP("nop", OP.TOKEN_NOP, p);
            st.prev();
        } else if (oper1.Flag == OP.TOKEN_MIN || oper1.Flag == OP.TOKEN_PLU) {
            oper1.Flag = oper1.Flag == OP.TOKEN_MIN ? OP.TOKEN_SELF_NEGA
                    : OP.TOKEN_SELF_POSI;
            oper1 = new OP("nop", OP.TOKEN_NOP, p);
            st.prev();
        } else {
            // { support? map? array? all in one
            st.prev();
            return getInvalidOper(p, st, endOps, false);
        }
        if (oper1 == null || !st.hasNext()) {
            return oper1; // done!
        }
        OP oper2 = null, para = null, op = null;
        while (st.hasNext()) {// expect a operator now
            op = st.nextOp();
            if (!op.isExprValid()) {
                oper1 = getInvalidOper(oper1.pos, st, endOps, false);
                break;
            } else if (op.Flag == OP.TOKEN_LPR) {
                op = new OP("$", OP.TOKEN_INVOKE, op.pos);// && priority <= (OP.TOKEN_CALL
                // & OP.TOKEN_PRIORITY)) // a
                // func call , disable the
                // member (.) function for now
            } else if (op.Flag == OP.TOKEN_LBK) {
                // make it as member2
                op = new OP(".", OP.TOKEN_MEMBER2, op.pos); //TODO: might need to be disabled
            } else if (op.isEndOp()) {
                if ((op.Flag & endOps) == 0) {
                    oper1 = getInvalidOper(oper1.pos, st, endOps, false);
                }// caller process it
                break;
            }
            if (!op.isOp()) {
                if (priority == null) { // only for a b,c,d scenario
                    op = new OP("$", OP.TOKEN_INVOKE2, op.pos);
                    st.prev();
                } else {
                    oper1 = getInvalidOper(oper1.pos, st, endOps, false); // throw new Exception("expect a expr operator");
                    break;
                }
            }
            if (priority != null && (op.priority() < priority.priority() || (op.priority() == priority.priority() && op.Flag != OP.TOKEN_ASSIGN && op.Flag != OP.TOKEN_QM))) {
                break;
            }
            st.next();
            if (op.Flag == OP.TOKEN_MEMBER2) {
                p = st.getPos();
                if ((oper2 = parseExpr(st, _noPriority, OP.TOKEN_RBK)) == null || !st.expect(OP.TOKEN_RBK) || (oper2.TkValue.length() < 1 && oper2._next == null)) {
                    // a chance to be invoker2
                    if (priority == null) {
                        // restore
                        st.setPos(p - 1);
                        oper2 = getInvalidOper(op.pos, st, endOps, false);//
                        op.set(OP.TOKEN_INVOKE, "$");
                        if (oper1._last == null && oper1.Flag == OP.TOKEN_VAR) {
                            oper1.Flag = OP.TOKEN_STR;
                        }
                    } else {
                        oper1 = getInvalidOper(oper1.pos, st, endOps, false);
                        break;
                    }
                } else {
                    op.Flag = OP.TOKEN_MEMBER; // no member2 needed
                }
            } else if (op.Flag == OP.TOKEN_QM) { // ?, match the :
                if ((oper2 = parseExpr(st, op, OP.TOKEN_DEC)) == null
                        || !st.expect(OP.TOKEN_DEC)) {
                    oper2 = getInvalidOper(op.pos + 1, st, OP.TOKEN_DEC, true);
                    if (oper2 == null) {
                        oper1 = getInvalidOper(oper1.pos, st, endOps, false);
                        break;
                    }
                    //throw new Exception(oper2 == null ? "expect expression"
                    //        : "expect : operator");
                }
                oper1.operate(op.set(OP.TOKEN_JMPF, "jmpf"),
                        new OP(oper2.getCount() + 1 + "", OP.TOKEN_NUM, oper2.pos))
                        .operate(new OP("", OP.TOKEN_JMP, oper2.pos), oper2);
                p = st.op().pos;
                if ((oper2 = parseExpr(st, op, endOps)) == null) {
                    oper2 = getInvalidOper(p + 1, st, endOps, false);
                }
                oper1._last.TkValue = oper2.getCount() + 1 + "";
                op = new OP("dum", OP.TOKEN_DUM, oper1.pos);
            } else if (op.Flag == OP.TOKEN_INVOKE || op.Flag == OP.TOKEN_INVOKE2) {// ok, process the parameters
                // if it's a call
                if (oper1._last != null && oper1._last.Flag == OP.TOKEN_MEMBER) {
                    oper1._last.set(OP.TOKEN_MEMBER3, "member3");
                } else if (oper1._last == null && oper1.Flag == OP.TOKEN_VAR) {
                    oper1.Flag = OP.TOKEN_STR;
                } else {
                    oper1 = getInvalidOper(oper1.pos, st, endOps, false); // disable it
                    break;
                }
                // invoke2, treat everything as single parameters
                if (op.Flag == OP.TOKEN_INVOKE2) {
                    if ((oper2 = parseExpr(st, _noPriority, endOps)) == null || st.hasNext()) {
                        // not cool, we need to treat everything as string now
                        oper2 = getInvalidOper(op.pos, st, endOps, false);//
                    }
                    op.Flag = OP.TOKEN_INVOKE;
                } else {
                    oper2 = null;
                    while (true) {
                        if (!st.hasNext()) {
                            op = null;
                            break; //
                        }

                        if (st.nextOp().Flag == OP.TOKEN_RPR && oper2 == null) {
                            break;
                        }
                        if ((para = parseExpr(st, _noPriority, OP.TOKEN_RPR | OP.TOKEN_CM)) == null) {
                            op = null;
                            break; //
                        }
                        if (oper2 == null) {
                            oper2 = para;
                        } else {
                            oper2.operate(new OP("para", OP.TOKEN_PARA, para.pos), para);
                        }
                        if (!st.expect(OP.TOKEN_CM)) {
                            break;
                        }
                    }
                    if (op == null || !st.expect(OP.TOKEN_RPR)) {
                        //throw new Exception("exepct ) at end of func call");
                        oper1 = getInvalidOper(oper1.pos, st, endOps, false);
                        break;
                    } else if (oper2 == null) {
                        oper2 = new OP("nop", OP.TOKEN_NOP, op.pos);
                    }
                }
            } else {
                if ((oper2 = parseExpr(st, op, endOps)) == null) {
                    oper1 = getInvalidOper(oper1.pos, st, endOps, false);
                    break;
                }
                if (op.Flag == OP.TOKEN_MEMBER) {
                    if (oper2._last != null || oper2.Flag != OP.TOKEN_VAR) {
                        oper1 = getInvalidOper(oper1.pos, st, endOps, false);
                        break;
                    }
                    oper2.Flag = OP.TOKEN_STR;
                } else if (op.Flag == OP.TOKEN_AND || op.Flag == OP.TOKEN_OR) {
                    oper1.operate(op.Flag == OP.TOKEN_AND ? new OP("jmpf",
                                    OP.TOKEN_JMPF, op.pos) : new OP("jmpt", OP.TOKEN_JMPT, op.pos),
                            new OP(oper2.getCount() + 1 + "", OP.TOKEN_NUM, op.pos));
                }
            }
            oper1.operate(op, oper2);
        }
        return oper1;
    }
}
