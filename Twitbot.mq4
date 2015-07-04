//+------------------------------------------------------------------+
//|                                                      Twitbot.mq4 |
//|                                                                  |
//| Take the signals from Twitbot and parse them to open trades.     |
//+------------------------------------------------------------------+
#property copyright "Daniel Shea"

extern bool MoneyManagement = true;
extern double Lots = 0.01;
extern int Slippage = 5;

bool runMe;

//+------------------------------------------------------------------+
//| expert initialization function                                   |
//+------------------------------------------------------------------+
int init()
{
  runMe = true;
  spin();
  return(0);
}

//+------------------------------------------------------------------+
//| expert deinitialization function                                 |
//+------------------------------------------------------------------+
int deinit()
{
  runMe = false;
  return(0);
}

//+------------------------------------------------------------------+
//| expert start function                                            |
//+------------------------------------------------------------------+
int start()
{
  return(0);
}

//+------------------------------------------------------------------+
//| where the magic happens                                          |
//+------------------------------------------------------------------+
int spin()
{
  string arr[], pair, str;
  int dir, handle, pos[];
  double StopLevel;
  
  while(runMe) {
    handle = FileOpen("results.txt", FILE_CSV | FILE_READ, '|');
    
    // if the function fails
    if(handle < 1) {
      Sleep(10000);
      continue;
    }
    
    // if the file is empty
    if(FileSize(handle)==0) {
      FileClose(handle);
      Sleep(10000);
      continue;
    }
    
    // calculate the new Lots size if allowed
    if(MoneyManagement) {
      Lots = NormalizeDouble((AccountEquity() * 0.0001), 2);
      if (Lots < 0.01) {
        Lots = 0.01;
      }
    }
    
    while(!FileIsEnding(handle)) {
      str = FileReadString(handle);
      if(str != "") {
        int ticket;
        double price, sl, tp;
        StrToArray(str, ":", arr);
        dir = StrToInteger(arr[0]);
        //pair = StringConcatenate(arr[1],"m");
        pair = arr[1];
        price = StrToDouble(arr[2]);
        sl = StrToDouble(arr[3]);
        tp = StrToDouble(arr[4]);
        StopLevel = MarketInfo(pair, MODE_STOPLEVEL) * Point;
        if(dir == 1) {
          if ((price - sl) < StopLevel) {
            sl = price - StopLevel;
          }
          if ((tp - price) < StopLevel) {
            tp = price + StopLevel;
          }
          // open a buy order
          if (price > Ask) {
            // open a buy stop order
            ticket = OrderSend(pair,OP_BUYSTOP,Lots,price,Slippage,sl,tp,"Panacea BuyStop",16384,0,Green);
          }
          else if (price < Ask) {
            // open a buy limit order
            ticket = OrderSend(pair,OP_BUYLIMIT,Lots,price,Slippage,sl,tp,"Panacea BuyLimit",16384,0,Green);
          }
        }
        else if(dir == 2) {
          if ((sl - price) < StopLevel) {
            sl = price + StopLevel;
          }
          if ((price - tp) < StopLevel) {
            tp = price - StopLevel;
          }
          // open a sell order
          if (price < Bid) {
            // open a sell stop order
            ticket = OrderSend(pair,OP_SELLSTOP,Lots,price,Slippage,sl,tp,"Panacea SellStop",16384,0,Red);
          }
          else if (price > Bid) {
            // open a sell limit order
            ticket = OrderSend(pair,OP_SELLLIMIT,Lots,price,Slippage,sl,tp,"Panacea SellLimit",16384,0,Red);
          }
        }
        if(ticket < 0) {
          Print("Current Ask: ",MarketInfo(pair,MODE_ASK),", Current Bid: ",MarketInfo(pair,MODE_BID));
          Print("Stop Level: ",MarketInfo(pair,MODE_STOPLEVEL));
          Print("Trade information: ",str);
          Print("OrderSend failed with error #",GetLastError());
        }
      }
    }
    
    // clear the file
    FileClose(handle);
    FileDelete("results.txt");
    
    Sleep(10000);
  }
  
  return(0);
}

int StrToArray(string _input, string sep, string& arr[])
{
  string pair, tarr[100];
  int Index = 0, LastIndex = 0, count = 0;
  _input = StringTrimRight(StringTrimLeft(_input));
  if(StringSubstr(_input, StringLen(_input) - 1) != sep) {
    _input = _input + sep;
  }
  for(int cnt = 0; cnt <= 100; cnt++) {
    Index = StringFind(_input, sep, LastIndex);
    if (Index > -1) {
      if(Index == LastIndex) {
        pair = "";
      }
      else {
        pair = StringSubstr(_input, LastIndex, Index - LastIndex);
        pair = StringTrimRight(StringTrimLeft(pair));
      }
      tarr[cnt]= pair;
      LastIndex = Index + 1;
      count++;
    }
    else {
      break;
    }
  }
  if(count > 0) {
    ArrayCopy(arr, tarr, 0, 0, count);
  }
  return(count);
}