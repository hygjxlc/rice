package com.bjdx.rice.business.utils;

public class NumberToChineseUtil {

    private static final String[] CN_NUMBERS = {"零", "壹", "贰", "叁", "肆", "伍", "陆", "柒", "捌", "玖"};
    private static final String[] CN_UNITS = {"", "拾", "佰", "仟"};
    private static final String[] CN_BIG_UNITS = {"", "万", "亿"};

    public static String number2Chinese(double amount) {
        if (amount == 0) return "零元整";

        long yuan = (long) amount;
        int fen = (int) Math.round((amount - yuan) * 100);

        StringBuilder sb = new StringBuilder();

        if (yuan > 0) {
            sb.append(convertInteger(yuan)).append("元");
        }

        if (fen > 0) {
            long jiao = fen / 10;
            long f = fen % 10;
            if (yuan == 0) {
                sb.append("零");
            }
            if (jiao > 0) {
                sb.append(CN_NUMBERS[(int) jiao]).append("角");
            }
            if (f > 0) {
                sb.append(CN_NUMBERS[(int) f]).append("分");
            }
        } else if (yuan > 0) {
            sb.append("整");
        }

        return sb.toString();
    }

    private static String convertInteger(long num) {
        if (num == 0) return "零";

        StringBuilder sb = new StringBuilder();
        boolean needZero = false;

        for (int level = 0; num > 0; level++) {
            long chunk = num % 10000;
            if (chunk != 0) {
                String part = convertChunk((int) chunk);
                if (level > 0) {
                    part += CN_BIG_UNITS[level];
                }
                if (needZero) {
                    sb.insert(0, "零");
                }
                sb.insert(0, part);
                needZero = false;
            } else if (sb.length() > 0) {
                needZero = true;
            }
            num /= 10000;
        }

        return sb.toString()
                .replaceAll("零+$", "")
                .replaceAll("零+", "零");
    }

    private static String convertChunk(int num) {
        StringBuilder sb = new StringBuilder();
        boolean needZero = false;

        for (int i = 0; i < 4 && num > 0; i++) {
            int digit = num % 10;
            if (digit == 0) {
                if (!needZero && sb.length() > 0) {
                    needZero = true;
                }
            } else {
                if (needZero) {
                    sb.insert(0, "零");
                    needZero = false;
                }
                sb.insert(0, CN_UNITS[i]);
                sb.insert(0, CN_NUMBERS[digit]);
            }
            num /= 10;
        }

        return sb.toString();
    }
}