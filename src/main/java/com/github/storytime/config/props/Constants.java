package com.github.storytime.config.props;

import java.math.BigDecimal;

public class Constants {

    public static final int PB_WAIT = 0;
    public static final String START_DATE = "sd";
    public static final String END_DATE = "ed";
    public static final String CARD = "card";
    public static final String EMPTY = "";
    public static final String CMT = "cmt";
    public static final int TEST = 0;
    public static final int XML_VERSION = 1;
    public static final String MD5 = "MD5";
    public static final String SHA_1 = "SHA-1";
    public static final String N_A = "N/A";
    public static final String API_PREFIX = "/app/api";
    public static final int CARD_LAST_DIGITS = 4;
    public static final int CARD_TWO_DIGITS = 2;
    public static final String ACCOUNT_TITLE_PREFIX = "Счет ";
    public static final String TITLE_CARD_SEPARATOR = "-";
    public static final Integer DEFAULT_CURRENCY_ZEN = 4;
    public static final String VERSION_PROPERTIES = "version.properties";
    public static final String AUTHORIZATION_HEADER_NAME = "Authorization";
    public static final int PB_ZEN_ID = 12574;
    public static final String ZEN_ACCOUNT_TYPE = "checking";
    public static final String RATE = " Rate: ";
    public static final String CASH = "cash";
    public static final int CURRENCY_SCALE = 2;
    public static final String UAH_STR = "UAH";
    public static final int COMMENT_SIZE = 50;
    public static final String USD_COMMENT = "$ ";
    public static final String BANK_RATE = "";
    public static final int EMPTY_AMOUNT = 0;
    public static final int NOT_CHANGED = 0;
    public static final int ONE_HUNDRED = 100;
    public static final int ZERO_SCALE = 0;
    public static final int SAVINGS_STRING_SIZE = 10;
    public static final int SAVINGS_PERCENT_SIZE = 7;
    public static final String UAH = "₴";
    public static final String TOTAL = "Всего ~ ";
    public static final String DOTS = ":";
    public static final String PERCENT = "%";
    public static final String SLASH_SEPARATOR = " / ";
    public static final String PROJECT_TAG = "#";
    public static final int YNAB_AMOUNT_CONST = 1000;
    public static final String DOT = ".";
    public static final String RECONCILE_OK = "OK";
    public static final String RECONCILE_NOT_OK = "Not OK!";
    public static final long ZERO_DIIF = 0l;
    public static final String CARDNUM = "cardnum";
    public static final String COUNTRY = "country";
    public static final String UA = "UA";
    public static final long EPOCH_MILLI_FIX = 2146078276;
    public static final String CLEARED = "cleared";
    public static final String YNAB_IGNORE = "#";
    public static final String UNCATEGORIZED = "Uncategorized";
    public static final long INITIAL_TIMESTAMP = 0L;
    public static final String ACCOUNT = "account";
    public static final String INSTRUMENT = "instrument";
    public static final String TAG = "tag";
    public static final String SLASH = "/";
    public static final BigDecimal DEFAULT_ACC_BALANCE = BigDecimal.valueOf(0);
    public static final String SPLITTER = "; ";
    public static final String PR = "[";
    public static final String SUF = "]";
    public static final String SPLITTER_EMPTY = "(?<=.)";
    public static final int START_INCLUSIVE = 0;
    public static final int ZERO = 0;
    public static final int FORMATTER_SPLITTER = 3;
    public static final double EMPTY_BAL = 0d;

    private Constants() {
    }
}
