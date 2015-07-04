/**
 * Twitbot.java
 * 
 * Retrieve Forex signals posted to Twitter, parse them, and pass them to a
 * file in a specified MetaTrader directory to be opened by the MetaTrader
 * platform and parsed for trades.
 * 
 * Currently, signals from the following Twitter accounts are used:
 *   @forex
 *   @fxmgm
 * 
 * @author Dan Shea
 * @version 1.0
 */

import java.io.*;
import java.math.BigInteger;
import java.util.*;
import winterwell.jtwitter.*;

public class Twitbot
{
  static String path = "C:\\Users\\Dan\\AppData\\Roaming\\MetaQuotes\\Terminal\\3212703ED955F10C7534BE8497B221F4\\MQL4\\Files\\";
  static int rr = 1;
  
  static List<Status> list;
  static List<BigInteger> ids;
  static File file;
  static BufferedReader reader;
  static PrintWriter writer;
  static int dir; // none = 0, buy = 1, sell = 2
  static int pos;
  static double price, sl, tp;
  static BigInteger id;
  static String line, text, pair, result;
  
  static Timer timer;
  
  public static void main(String[] args)
  {
    if (args.length > 0) {
      path = args[0];
      System.err.println(path);
    }
    
    timer = new Timer();
    timer.schedule(new TimedTask(), 0, 5000);
  }
  
  public static String getStats()
  {
    // list of accounts from which to grab tweets
    ArrayList<String> users = new ArrayList<String>();
    users.add("forex");
    users.add("fxmgm");
    
    // intitialize some variables
    list = new ArrayList<Status>();
    ids = new ArrayList<BigInteger>();
    result = "";
    
    // log into Twitter
    OAuthSignpostClient oauthClient;
    oauthClient = new OAuthSignpostClient("<CONSUMER_KEY>", 
                                          "<CONSUMER_SECRET>",
                                          "<ACCESS_TOKEN>",
                                          "<ACCESS_TOKEN_SECRET>");
    Twitter twitter = new Twitter(null, oauthClient);
    
    try {
      file = new File("ids.dat");
      reader = new BufferedReader(new FileReader(file));
      while ((line = reader.readLine()) != null) {
        ids.add(new BigInteger(line));
      }
      reader.close();
      
      // get the information we're looking for
      for (int i = 0; i < users.size(); ++i) {
        twitter.setSinceId(ids.get(i));
        list = twitter.getUserTimeline(users.get(i));
        for (int j = list.size() - 1; j >= 0; --j) {
          if (j == 0) {
            ids.set(i, list.get(j).getId());
          }
          text = list.get(j).getDisplayText();
          switch (i) {
            case 0:  getForex();
                     break;
            case 1:  getFXMGM();
                     break;
            default: break;
          }
        }
      }
      
      // write the updated IDs to the file
      writer = new PrintWriter("ids.dat");
      for (int i = 0; i < ids.size(); ++i) {
        writer.println(ids.get(i).toString());
      }
      writer.close();
    }
    catch (Exception ex) {
      // we're done here
    }
    
    // write to the file to be read by MetaTrader
    try {
      PrintWriter out = new PrintWriter
        (new OutputStreamWriter
           (new FileOutputStream(path + "results.txt")));
      out.println(result);
      out.close();
    }
    catch (Exception e) {
      // do nothing
    }
    
    return result;
  }
  
  public static void getForex()
  {
    try {
      if (text.indexOf("Buy $") != -1) {
        dir = 1;
        pair = text.substring(text.indexOf("Buy $") + 5, text.indexOf(" at"));
        price = Double.parseDouble(text.substring(text.indexOf(" at") + 4, text.indexOf(" stop")));
        sl = Double.parseDouble(text.substring(text.indexOf("stop") + 5, text.indexOf(" targets")));
        tp = Double.parseDouble(text.substring(text.indexOf("targets") + 8, text.indexOf(" and")));
        result += dir + ":" + pair + ":" + price + ":" + sl + ":" + tp + "|";
      }
      else if (text.indexOf("Sell $") != -1) {
        dir = 2;
        pair = text.substring(text.indexOf("Sell $") + 6, text.indexOf(" at"));
        price = Double.parseDouble(text.substring(text.indexOf(" at") + 4, text.indexOf(" stop")));
        sl = Double.parseDouble(text.substring(text.indexOf("stop") + 5, text.indexOf(" targets")));
        tp = Double.parseDouble(text.substring(text.indexOf("targets") + 8, text.indexOf(" and")));
        result += dir + ":" + pair + ":" + price + ":" + sl + ":" + tp + "|";
      }
    }
    catch (Exception ex) {
      // we're done here
    }
  }
  
  public static void getFXMGM() {
    try {
      if (text.indexOf("[FXMGM] ") != 0) {
        dir = 0;
      }
      else if (text.indexOf("Buy") != -1) {
        dir = 1;
        pair = text.substring(text.indexOf("Buy $") + 5, text.indexOf(" @"));
        price = Double.parseDouble(text.substring(text.indexOf("@") + 1,
                                                  text.indexOf(" Stop Loss")));
        sl = Double.parseDouble(text.substring(text.indexOf("Loss @ ") + 7,
                                               text.indexOf(" (Period")));
        tp = price + ((price - sl) * rr);
        result += dir + ":" + pair + ":" + price + ":" + sl + ":" + tp + "|";
      }
      else if (text.indexOf("Sell") != -1) {
        dir = 2;
        pair = text.substring(text.indexOf("Sell $") + 6, text.indexOf(" @"));
        price = Double.parseDouble(text.substring(text.indexOf("@") + 1,
                                                  text.indexOf(" Stop Loss")));
        sl = Double.parseDouble(text.substring(text.indexOf("Loss @ ") + 7,
                                               text.indexOf(" (Period")));
        tp = price - ((sl - price) * rr);
        result += dir + ":" + pair + ":" + price + ":" + sl + ":" + tp + "|";
      }
    }
    catch (Exception ex) {
      // we're done here
    }
  }
  
  public static class TimedTask extends TimerTask
  {
    public void run( )
    {
      getStats( );
    }
  }
}