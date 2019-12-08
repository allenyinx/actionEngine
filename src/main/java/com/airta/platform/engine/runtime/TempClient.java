package com.airta.platform.engine.runtime;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TempClient {

    public static class Payload {
        public String[] headers = null;
        public String[] strs = null;
        public String err = null;
        public String str = null;

        public Payload(String strVal) {
            str = strVal;
        }

        /**
         * constructor
         *
         * @param hdrs   http headers
         * @param vals   http values
         * @param errInf error information
         * @param strVal string values
         */
        public Payload(String[] hdrs, String[] vals, String errInf, String strVal) {
            headers = hdrs;
            strs = vals;
            err = errInf;
            str = strVal;
        }

        /**
         * unpack the action String to string Array
         *
         * @param Value action String
         * @return action String array
         */
        public static String[] _unpack(String Value) {
            if (Value == null || Value.length() < 1) {
                return null;
            }
            int len = Value.length();
            if (len < 2) {
                return null;
            }
            int pos = 0;
            List<String> ret = new ArrayList<String>();
            int segLen = 0;
            int startPos = 0;
            while (pos < len - 1) {
                // try to get the next index
                segLen = Value.indexOf(":", pos);
                if (segLen <= pos) {
                    break;
                }
                startPos = segLen + 1;
                segLen = Integer.parseInt(Value.substring(pos, segLen));
                if (segLen >= 0 && startPos + segLen <= len) {
                    ret.add(Value.substring(startPos, startPos + segLen));
                    pos = startPos + segLen;
                } else if (segLen == -1) {
                    ret.add(null);
                    pos = startPos;
                } else {
                    break;
                }
            }

            if (pos != len || ret.size() < 1) {
                return null;
            }
            String[] rets = new String[ret.size()];
            ret.toArray(rets);
            ret.clear();
            return rets;
        }

        /**
         * pack String array to String
         *
         * @param arr action String array
         * @return action String
         */
        public static String _pack(String[] arr) {
            if (arr == null || arr.length < 1) {
                return "";
            }
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < arr.length; i++) {
                if (arr[i] == null) {
                    sb.append("-1:");
                } else {
                    sb.append(arr[i].length());
                    sb.append(":");
                    sb.append(arr[i]);
                }
            }
            String s = sb.toString();
            sb.setLength(0);
            return s;
        }

        /**
         * pack actions to String
         *
         * @return action String
         */
        public String pack() {
            String[] sets = new String[4];
            sets[0] = str;
            sets[1] = _pack(strs);
            sets[2] = _pack(headers);
            sets[3] = err;
            return "#" + _pack(sets);
        }

        /**
         * unpack String to ActionData
         *
         * @param val input String value
         * @return ActionData object
         */
        public static Payload unpack(String val) {
            if (val == null || !val.startsWith("#")) {
                return null;
            }
            String[] sets = _unpack(val.substring(1));
            if (sets == null || sets.length != 4) {
                return null;
            }
            return new Payload(_unpack(sets[2]), _unpack(sets[1]), sets[3],
                    sets[0]);
        }
    }

    public static Payload invokeAction(String url, String action,
                                       Payload callData, String exParas, String exUrlParams) throws IOException {
        if (callData == null || action == null || url == null) {
            return null;
        }
        if (exParas == null) {
            exParas = "";
        }
        if (exUrlParams == null) {
            exUrlParams = "";
        } else {
            exUrlParams = "&" + exUrlParams;
        }
        long tt = new Date().getTime();
        String postData = "r=false&a=" + URLEncoder.encode(action, "utf-8") + "&i="
                + URLEncoder.encode(callData.pack(), "utf-8") + "&p="
                + URLEncoder.encode(exParas, "utf-8") + exUrlParams;

        String response = postHttpData(url, postData);
        Payload ret = Payload.unpack(response);
        return ret;
    }


    public static String postHttpData(String url, String postData) throws IOException {
        if (url == null) {
            throw new IOException("invalid parameters");
        }
        URL uri = new URL(url);
        Proxy prx = Proxy.NO_PROXY;
        HttpURLConnection conn = null;
        if (prx != null) {
            conn = (HttpURLConnection) uri.openConnection(prx);
        } else {
            conn = (HttpURLConnection) uri.openConnection();
        }
        if (conn instanceof HttpsURLConnection) {
        }
        java.io.ByteArrayOutputStream bos = null;
        java.io.OutputStream os = null;
        java.io.InputStream is = null;
        try {
            if (postData != null) {
                conn.setRequestMethod("POST");
                conn.addRequestProperty("Content-Type",
                        "application/x-www-form-urlencoded");
                conn.setDoOutput(true);
                os = conn.getOutputStream();
                os.write(postData.getBytes("utf-8"));
                os.close();
                os = null;
            }
            if (conn.getResponseCode() != 200) {
                //Config.getRemoteHandler().log("http call failed due to http " + conn.getResponseCode());
                // make sure print out the request
                //Config.getRemoteHandler().log("http url: " + url);
                throw new IOException("http post error code: " + conn.getResponseCode());
            }
            is = conn.getInputStream();
            byte[] bts = new byte[1024];
            int t = 0;
            bos = new java.io.ByteArrayOutputStream();
            while ((t = is.read(bts, 0, 1024)) > 0) {
                bos.write(bts, 0, t);
            }
            String response = null;
            if (bos.size() > 0) {
                response = new String(bos.toByteArray(), "utf-8");
            }
            bos.close();
            bos = null;
            is.close();
            is = null;
            conn.disconnect();
            conn = null;
            return response;
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (Exception e) {

                }

            }
            if (bos != null) {
                try {
                    bos.close();
                } catch (Exception e) {

                }
            }
            if (is != null) {
                try {
                    is.close();
                } catch (Exception e) {

                }

            }
            if (conn != null) {
                try {
                    conn.disconnect();
                } catch (Exception e) {

                }

            }
        }
    }
}
